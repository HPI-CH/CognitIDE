package com.github.diekautz.ideplugin.actions.recording

import com.github.diekautz.ideplugin.services.TobiiProService
import com.github.diekautz.ideplugin.services.debug.MousePositionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class StopRecordingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<TobiiProService>()?.stopRecording()
        e.project?.service<MousePositionService>()?.stopTrackMouse()
    }
}