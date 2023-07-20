package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.services.TobiiProService
import com.github.diekautz.ideplugin.services.recording.LookRecorderService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class HighlightGazeElementAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        val lookRecorderService = currentProject?.service<LookRecorderService>()
        e.presentation.isEnabled = lookRecorderService?.couldHighlight() ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<TobiiProService>()?.visualizeInEditor()
    }
}