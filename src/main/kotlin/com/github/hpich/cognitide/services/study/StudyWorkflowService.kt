package com.github.hpich.cognitide.services.study

import com.github.hpich.cognitide.actions.studyUtils.SuspendableStudyAction
import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.utils.parseWorkflowFromDisk
import com.github.hpich.cognitide.utils.saveWorkflowToDisk
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

@Service(Service.Level.APP)
class StudyWorkflowService {
    private var workflowItems = mutableListOf<AWorkflowItem>()
    private lateinit var initialActionEvent: AnActionEvent
    var isRunning = false
    private var currentStep = 0

    // a JSON with examples for all Workflow Items can be found in "/resources/Workflows".
    fun saveHardcodedWorkflow() {
        val hardcodedWorkflowItems =
            mutableListOf<AWorkflowItem>()

        saveWorkflowToDisk(hardcodedWorkflowItems, Date.from(Instant.now()))
    }

    fun startWorkflow(e: AnActionEvent) {
        initialActionEvent = e
        isRunning = true
        currentStep = 0
        val parsedWorkflow = parseWorkflowFromDisk(CognitIDESettingsState.instance.workflowJsonPath)
        if (parsedWorkflow.isNullOrEmpty()) {
            Messages.showErrorDialog(
                "Workflow file not found or empty. Please check the workflow path configured in the" + " Study Configuration settings.",
                "Study Workflow Loading Error.",
            )
        } else {
            workflowItems = parsedWorkflow
            runNextAction()
        }
    }

    fun nextStep() {
        runNextAction()
    }

    fun stopWorkflow() {
        workflowItems.clear()
        workflowItems.add(WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"))
        workflowItems.add(WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"))
        runNextAction()
    }

    private fun runNextAction() {
        if (workflowItems.isEmpty()) {
            isRunning = false
            return
        }
        val nextItem = workflowItems.removeFirst()
        currentStep++

        when (nextItem) {
            is AMultiWorkflowItem -> {
                workflowItems.addAll(0, nextItem.getWorkflowItems())
                runNextAction()
            }

            is ASingleWorkflowItem -> {
                val action = nextItem.buildAction(this::runNextAction)

                if (action is AnAction) {
                    // Ensures that the actions are executed in a
                    ApplicationManager.getApplication().invokeLater {
                        action.actionPerformed(initialActionEvent)

                        if (!nextItem.isAsyncAction) {
                            runNextAction()
                        }
                    }
                } else if (action is SuspendableStudyAction) {
                    runSuspendableAction(action)
                }
            }

            else -> {
                // Should not happen as items should only inherit from ASingleItem or AMultiItem
                throw Error("Unknown Item type!")
            }
        }
    }

    private fun runSuspendableAction(action: SuspendableStudyAction) {
        // Store the current step counter
        val suspendableActionStep = currentStep

        CoroutineScope(Dispatchers.Default).launch {
            action.actionPerformed(initialActionEvent)

            with(Dispatchers.Main) {
                // Only trigger the next action, if the scheduled action is still the current one.
                // E.g. if the user pressed "next step" in the meantime, the currentStep will be larger
                // than suspendableActionStep
                if (suspendableActionStep == currentStep) {
                    runNextAction()
                }
            }
        }
    }
}
