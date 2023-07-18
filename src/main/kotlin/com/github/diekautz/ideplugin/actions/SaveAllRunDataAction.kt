package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.services.recording.MyLookRecorderService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class SaveAllRunDataAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        val lookRecorderService = currentProject?.service<MyLookRecorderService>()
        e.presentation.isEnabledAndVisible = lookRecorderService?.couldSave() ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<MyLookRecorderService>()?.askAndSaveBoth()
    }
}