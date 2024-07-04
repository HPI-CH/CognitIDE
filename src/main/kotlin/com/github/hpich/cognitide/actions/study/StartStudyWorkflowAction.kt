package com.github.hpich.cognitide.actions.study
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.services.study.StudyWorkflowService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager

class StartStudyWorkflowAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject != null &&
            ParticipantState.instance.id > 0 &&
            ApplicationManager.getApplication().getService(StudyWorkflowService::class.java).isRunning == false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val service = ApplicationManager.getApplication().getService(StudyWorkflowService::class.java)
        service.startWorkflow(e)
    }
}
