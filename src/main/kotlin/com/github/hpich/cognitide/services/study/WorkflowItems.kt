package com.github.hpich.cognitide.services.study

import com.github.hpich.cognitide.actions.questionnaires.ViewAnyQuestionnaireAction
import com.github.hpich.cognitide.actions.studyUtils.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed class AWorkflowItem {
    abstract val enabled: Boolean
    abstract val actionId: String
    abstract val isAsyncAction: Boolean
    abstract val title: String

    abstract fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction
}

@Serializable
data class WorkflowItem(
    override val enabled: Boolean,
    override val actionId: String,
) : AWorkflowItem() {
    override val title: String = "WorkflowItem"
    override val isAsyncAction: Boolean = false

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        return ExecuteAnyActionAction(actionId)
    }
}

@Serializable
data class TimerWorkflowItem(
    override val enabled: Boolean,
    val durationInSeconds: Int,
) : AWorkflowItem() {
    override val title: String = "TimerWorkflowItem"
    override val actionId: String = "com.github.hpich.cognitide.actions.studyUtils.StudyTimerAction"
    override val isAsyncAction: Boolean = true

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        val action = StudyTimerAction()
        action.durationInSeconds = durationInSeconds
        return action
    }
}

@Serializable
data class QuestionnaireWorkflowItem(
    override val enabled: Boolean,
    val questionnaireFilePath: String,
    val questionnaireName: String,
) : AWorkflowItem() {
    override val title: String = "QuestionnaireWorkflowItem"
    override val actionId: String = "com.github.hpich.cognitide.actions.questionnaires.ViewAnyQuestionnaireAction"
    override val isAsyncAction: Boolean = false

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        return ViewAnyQuestionnaireAction(questionnaireName, questionnaireFilePath)
    }
}

@Serializable
data class PopupWorkflowItem(
    override val enabled: Boolean,
    val popupTitle: String,
    val popupText: String,
) : AWorkflowItem() {
    override val title: String = "PopupWorkflowItem"
    override val isAsyncAction: Boolean = false
    override val actionId: String = "com.github.hpich.cognitide.actions.studyUtils.StudyPopupAction"

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        val action = StudyPopupAction(popupTitle, popupText)
        return action
    }
}

@Serializable
data class OpenFileWorkflowItem(
    override val enabled: Boolean,
    val filePath: String,
) : AWorkflowItem() {
    override val title: String = "OpenFileWorkflowItem"
    override val isAsyncAction: Boolean = false
    override val actionId: String = "com.github.hpich.cognitide.actions.studyUtils.OpenFileAction"

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        return OpenFileAction(filePath)
    }
}

@Serializable
data class ExecuteShellCommandWorkflowItem(
    override val enabled: Boolean,
    val command: String? = null,
    val maxDuration: Duration? = null,
) : AWorkflowItem() {
    override val title: String = "ExecuteShellCommandWorkflowItem"
    override val isAsyncAction: Boolean = true
    override val actionId: String = "com.github.hpich.cognitide.actions.studyUtils.ExecuteShellCommandWorkflowItem"

    override fun buildAction(onActionCompleted: (() -> Unit)?): StudyAction {
        return ExecuteShellCommandAction(command, maxDuration)
    }
}
