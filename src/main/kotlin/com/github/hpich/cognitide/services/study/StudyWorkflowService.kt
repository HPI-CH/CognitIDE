package com.github.hpich.cognitide.services.study
import com.github.hpich.cognitide.actions.studyUtils.SuspendableStudyAction
import com.github.hpich.cognitide.utils.saveWorkflowToDisk
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
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

    fun startWorkflow(e: AnActionEvent) {
        initialActionEvent = e
        isRunning = true
        currentStep = 0
        workflowItems =
            mutableListOf(
                // WorkflowItem(true, "CloseAllEditors"),
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "preQuestionnaire.json", "pre"),
                // TASK 1
                PopupWorkflowItem(
                    true, "Begin of Task 1",
                    "Task 1 will now begin. The task " +
                        "will end automatically after 15 seconds. Please read the instructions carefully.",
                ),
                OpenFileWorkflowItem(true, "src/test/java/org/keywind/theme/AuthenticationUtil.java"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                TimerWorkflowItem(true, 5),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                PopupWorkflowItem(
                    true, "End of Task 1",
                    "The time for task 1 is over. " +
                        "Please fill out the following questionnaire.",
                ),
                // TASK 1 END
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json", "task1-nasa-tlx"),
                // TASK 2
                PopupWorkflowItem(
                    true, "Begin of Task 2",
                    "Task 2 will now begin. The task " +
                        "will end automatically after 15 seconds.",
                ),
                // WorkflowItem(true, "CloseAllEditors"),
                OpenFileWorkflowItem(true, "src/test/java/org/keywind/theme/AuthenticationUtil.java"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                TimerWorkflowItem(true, 5),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                PopupWorkflowItem(
                    true, "End of Task 2",
                    "The time for task 2 is over. " +
                        "Please fill out the following questionnaire.",
                ),
                // TASK 2 END
                QuestionnaireWorkflowItem(
                    true, QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json",
                    "task2-nasa-tlx",
                ),
                // WorkflowItem(true, "CloseAllEditors"),
                PopupWorkflowItem(
                    true, "All tasks done!",
                    "Please fill in the next " +
                        "questionnaire to finish the study.",
                ),
                QuestionnaireWorkflowItem(
                    true, QUESTIONNAIRE_FOLDER + "postQuestionnaire.json",
                    "post",
                ),
                PopupWorkflowItem(true, "Study End", "The study is now finished. Thank you!"),
            )
        saveWorkflowToDisk(workflowItems, Date.from(Instant.now()))
        runNextAction()
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

    private fun runSuspendableAction(action: SuspendableStudyAction) {
        // Store the current step counter
        val suspendableActionStep = currentStep

        CoroutineScope(Dispatchers.Default).launch {
            action.actionPerformed(initialActionEvent)

            with(Dispatchers.Main) {
                // Only trigger the next action, if the scheduled action is still the current one.
                // E.g. if the user pressed "next step" in the meantime, the currentStep will be larger
                // than suspendableActionStep
                println("Suspendable Step: $suspendableActionStep")
                println("Current Step: $currentStep")
                if (suspendableActionStep == currentStep) {
                    runNextAction()
                }
            }
        }
    }
}
