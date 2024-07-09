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
import kotlin.time.Duration.Companion.minutes

// Points to the folder where the default questionnaires are stored
// Only for the demo / while not configurable in the UI!
val QUESTIONNAIRE_FOLDER = "C:\\Users\\CogniPro-Student\\Desktop\\Questionaires\\"

val TASK_ROOT_FOLDER = "C:\\Users\\CogniPro-Student\\IdeaProjects\\cognitide-mp-flow-study\\"

val RELAXATION_VIDEO = "C:\\Users\\CogniPro-Student\\Desktop\\BaselineRecording-Videos\\Relaxation.mp4"
val BASELINE_AUDIO_VIDEO =
    "C:\\Users\\CogniPro-Student\\Desktop\\BaselineRecording-Videos\\Baseline_recording_Audio.mp4"
val BASELINE_CROSSHAIR_VIDEO =
    "C:\\Users\\CogniPro-Student\\Desktop\\BaselineRecording-Videos\\Baseline_recording_Video.mp4"
val VLC_COMMAND = fun(videoPath: String) =
    "\"C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe\" --fullscreen --no-video-title-show \"$videoPath\" \"vlc://quit\""

@Service(Service.Level.APP)
class StudyWorkflowService {
    private var workflowItems = mutableListOf<AWorkflowItem>()
    private lateinit var initialActionEvent: AnActionEvent
    var isRunning = false
    private var currentStep = 0

    fun saveHardcodedWorkflow() {
        val hardcodedWorkflowItems =
            mutableListOf<AWorkflowItem>(
                // Study Workflow Testing
                PopupWorkflowItem(
                    true,
                    "Introduction Study-Workflow",
                    "We will now give you an introduction to the study-workflow.",
                ),
                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "TESTFILE.MD"),
                TimerWorkflowItem(true, 60),
                PopupWorkflowItem(
                    true,
                    "Introduction Study-Workflow",
                    "You should now be familiar with the study-workflow.",
                ),
                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "TESTFILE.MD"),
                WorkflowItem(true, "CloseAllEditors"),
                // Transition
                PopupWorkflowItem(
                    true,
                    "Begin Study",
                    "When you are ready to begin the study, press okay. The automatic study workflow will then start.",
                ),
                // Pre-Questionnaire
                PopupWorkflowItem(true, "Pre-Study Questionnaire", "Please fill out this pre-study questionnaire."),
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "preQuestionnaire.json", "pre"),
                // Relaxation Video
                PopupWorkflowItem(
                    true,
                    "Relaxation Video",
                    "You will now be shown a 2 minute video. Please try to relax.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(RELAXATION_VIDEO), 2.minutes),
                TimerWorkflowItem(true, 120),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Baseline Recording 1 - Video
                PopupWorkflowItem(
                    true,
                    "Baseline Recording 1 - Video",
                    "The first baseline recording will start now. Please look at the crosshairs until it closes after 1 minute.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(BASELINE_CROSSHAIR_VIDEO), 1.minutes),
                TimerWorkflowItem(true, 60),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Baseline Recording 1 - Audio
                PopupWorkflowItem(
                    true,
                    "Baseline Recording 1 - Audio",
                    "The first baseline recording will start now. Please close your eyes until you hear a sound after 1 minute.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(BASELINE_AUDIO_VIDEO), 1.minutes),
                TimerWorkflowItem(true, 60),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Settling In Period
                PopupWorkflowItem(
                    true,
                    "Settling- in period",
                    "You now have 5 minutes to make yourself familiar with the code base.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                OpenFileWorkflowItem(
                    true,
                    TASK_ROOT_FOLDER + "src\\main\\java\\com\\cognitide\\demo\\DemoApplication.java",
                ),
                OpenFileWorkflowItem(
                    true,
                    TASK_ROOT_FOLDER + "src\\main\\java\\com\\cognitide\\demo\\TestResultCleanup.java",
                ),
                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "README.md"),
                TimerWorkflowItem(true, 300),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "README.md"),
                WorkflowItem(true, "CloseAllEditors"),
                ShuffleItemsWorkflowItem(
                    true,
                    mutableListOf(
                        // Task 1 -- CODING
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                PopupWorkflowItem(true, "Coding Task", "The coding task will begin now. You have 10 minutes."),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\main\\java\\com\\cognitide\\demo\\controller\\TestResultController.java",
                                ),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\test\\java\\com\\cognitide\\demo\\TestResultControllerTest.java",
                                ),
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "programming.md"),
                                TimerWorkflowItem(true, 600),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                                PopupWorkflowItem(
                                    true,
                                    "Coding Task",
                                    "The coding task is now finished. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json", "mid-study-coding"),
                                // Open a file to have atleast one file that can be closed. Workaround for the CloseAllEditors action
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "programming.md"),
                                WorkflowItem(true, "CloseAllEditors"),
                            ),
                        ),
                        // Task 2 -- DEBUGGING
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                PopupWorkflowItem(true, "Debugging Task", "The debugging task will begin now. You have 10 minutes."),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\main\\java\\com\\cognitide\\demo\\TestResultCleanup.java",
                                ),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\test\\java\\com\\cognitide\\demo\\TestResultCleanupTest.java",
                                ),
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "debugging.md"),
                                TimerWorkflowItem(true, 600),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                                PopupWorkflowItem(
                                    true,
                                    "Debugging Task",
                                    "The debugging task is now finished. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json", "mid-study-debugging"),
                                // Open a file to have atleast one file that can be closed. Workaround for the CloseAllEditors action
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "debugging.md"),
                                WorkflowItem(true, "CloseAllEditors"),
                            ),
                        ),
                        // Task 3 -- DOCUMENTATION
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                PopupWorkflowItem(
                                    true,
                                    "Documentation Task",
                                    "The documentation task will begin now. You have 10 minutes.",
                                ),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\main\\java\\com\\cognitide\\demo\\service\\TestResultService.java",
                                ),
                                OpenFileWorkflowItem(
                                    true,
                                    TASK_ROOT_FOLDER + "src\\test\\java\\com\\cognitide\\demo\\TestResultServiceTest.java",
                                ),
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "code_documentation.md"),
                                TimerWorkflowItem(true, 600),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                                PopupWorkflowItem(
                                    true,
                                    "Documentation Task",
                                    "The documentation task is now finished. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(
                                    true,
                                    QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json",
                                    "mid-study-documentation",
                                ),
                                // Open a file to have atleast one file that can be closed. Workaround for the CloseAllEditors action
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "code_documentation.md"),
                                WorkflowItem(true, "CloseAllEditors"),
                            ),
                        ),
                        // Task 4 -- EMAIL
                        GroupWorkflowItem(
                            true,
                            mutableListOf(
                                PopupWorkflowItem(true, "E-Mail Task", "The e-mail task will begin now. You have 10 minutes."),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "email.txt"),
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "natural_language_writing.md"),
                                TimerWorkflowItem(true, 600),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                                PopupWorkflowItem(
                                    true,
                                    "E-Mail Task",
                                    "The e-mail task is now finished. Please fill out the following questionnaire.",
                                ),
                                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "midStudyQuestionnaire.json", "mid-study-email"),
                                // Open a file to have atleast one file that can be closed. Workaround for the CloseAllEditors action
                                OpenFileWorkflowItem(true, TASK_ROOT_FOLDER + "natural_language_writing.md"),
                                WorkflowItem(true, "CloseAllEditors"),
                            ),
                        ),
                    ),
                ),
                // Transition
                PopupWorkflowItem(
                    true,
                    "Task End",
                    "You have now completed all tasks. Please remain seated and follow the instructions.",
                ),
                // Relaxation Video
                PopupWorkflowItem(
                    true,
                    "Relaxation Video",
                    "You will now be shown a 2 minute video. Please try to relax.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(RELAXATION_VIDEO), 2.minutes),
                TimerWorkflowItem(true, 120),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Baseline Recording 1 - Video
                PopupWorkflowItem(
                    true,
                    "Baseline Recording 1 - Video",
                    "The first baseline recording will start now. Please look at the crosshairs until it closes after 1 minute.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(BASELINE_CROSSHAIR_VIDEO), 1.minutes),
                TimerWorkflowItem(true, 60),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Baseline Recording 1 - Audio
                PopupWorkflowItem(
                    true,
                    "Baseline Recording 1 - Audio",
                    "The first baseline recording will start now. Please close your eyes until you hear a sound after 1 minute.",
                ),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
                ExecuteShellCommandWorkflowItem(true, VLC_COMMAND(BASELINE_AUDIO_VIDEO), 1.minutes),
                TimerWorkflowItem(true, 60),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StopRecordingAction"),
                WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.SaveAllRecordingDataAction"),
                // Post-Questionnaire
                PopupWorkflowItem(true, "Post-Study Questionnaire", "Please fill out this post-study questionnaire."),
                QuestionnaireWorkflowItem(true, QUESTIONNAIRE_FOLDER + "postQuestionnaire.json", "post"),
                // Transition
                PopupWorkflowItem(true, "Study End", "The study is now finished. Thank you for your participation!"),
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
