package com.github.diekautz.ideplugin.actions.recording

import com.github.diekautz.ideplugin.services.recording.LookRecorderService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class SaveAllRunDataAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        val lookRecorderService = currentProject?.service<LookRecorderService>()
        e.presentation.isEnabledAndVisible = lookRecorderService?.couldSave() ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<LookRecorderService>()?.askAndSaveBoth()
    }
}