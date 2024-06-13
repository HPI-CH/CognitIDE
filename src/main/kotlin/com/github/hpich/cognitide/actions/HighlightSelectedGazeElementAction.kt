package com.github.hpich.cognitide.actions

import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class HighlightSelectedGazeElementAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<DataCollectingService>()?.selectAndHighlightRecording()
    }
}
