package com.github.hpich.cognitide.services.study
import com.github.hpich.cognitide.actions.studyUtils.SuspendableStudyAction
import com.github.hpich.cognitide.config.study.StudyState
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

// Points to the folder where the default questionnaires are stored
// Only for the demo / while not configurable in the UI!
val QUESTIONNAIRE_FOLDER = "/Users/franz/projects/code-with-the-flow/dev/CognitIDE/resources/"

@Service(Service.Level.APP)
class StudyWorkflowService {
    private var workflowItems = mutableListOf<AWorkflowItem>()
    private lateinit var initialActionEvent: AnActionEvent
    var isRunning = false
    private var currentStep = 0

    fun saveHardcodedWorkflow() {
        val hardcodedWorkflowItems =
            mutableListOf(
                // WorkflowItem(true, "CloseAllEditors"),
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "preQuestionnaire.json", "pre"),
                // TASKS
                ShuffleItemsWorkflowItem(
                    true,
                    mutableListOf(
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                PopupWorkflowItem(
                                    true,
                                    "Begin of Task 1",
                                    "Task 1 will now begin. The task will end automatically after 15 seconds." +
                                        "Please read the instructions carefully.",
                                ),
                                OpenFileWorkflowItem(true, "src/test/java/org/keywind/theme/AuthenticationUtil.java"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                TimerWorkflowItem(true, 5),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(
                                    true,
                                    "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction",
                                ),
                                PopupWorkflowItem(
                                    true,
                                    "End of Task 1",
                                    "The time for task 1 is over. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(
                                    true,
                                    QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json",
                                    "task1-nasa-tlx",
                                ),
                            ),
                        ),
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                // TASK 2
                                PopupWorkflowItem(
                                    true,
                                    "Begin of Task 2",
                                    "Task 2 will now begin. The task will end automatically after 15 seconds.",
                                ),
                                // WorkflowItem(true, "CloseAllEditors"),
                                OpenFileWorkflowItem(true, "src/test/java/org/keywind/theme/AuthenticationUtil.java"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                TimerWorkflowItem(true, 5),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(
                                    true,
                                    "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction",
                                ),
                                PopupWorkflowItem(
                                    true,
                                    "End of Task 2",
                                    "The time for task 2 is over. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(
                                    true,
                                    QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json",
                                    "task2-nasa-tlx",
                                ),
                            ),
                        ),
                    ),
                ),
                // TASKS END
                PopupWorkflowItem(
                    true,
                    "All tasks done!",
                    "Please fill in the next questionnaire to finish the study.",
                ),
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "postQuestionnaire.json", "post"),
                PopupWorkflowItem(true, "Study End", "The study is now finished. Thank you!"),
            )

        saveWorkflowToDisk(hardcodedWorkflowItems, Date.from(Instant.now()))
    }

    fun startWorkflow(e: AnActionEvent) {
        initialActionEvent = e
        isRunning = true
        currentStep = 0
        val parsedWorkflow = parseWorkflowFromDisk(StudyState.instance.workflowJsonPath)
        if (parsedWorkflow.isNullOrEmpty()) {
            Messages.showErrorDialog(
                "Workflow file not found or empty. Please check the workflow path configured in the" +
                    " Study Configuration settings.",
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
