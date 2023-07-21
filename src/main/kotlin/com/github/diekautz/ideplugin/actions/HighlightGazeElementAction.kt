package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class HighlightGazeElementAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject?.service<DataCollectingService>()?.isHighlightAvailable ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<DataCollectingService>()?.highlightGazedElements()
    }
}