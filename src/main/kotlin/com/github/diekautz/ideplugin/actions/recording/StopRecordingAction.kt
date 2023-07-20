package com.github.diekautz.ideplugin.actions.recording

import com.github.diekautz.ideplugin.services.MyTobiiProService
import com.github.diekautz.ideplugin.services.debug.MyMousePositionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class StopRecordingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<MyTobiiProService>()?.stopRecording()
        e.project?.service<MyMousePositionService>()?.stopTrackMouse()
    }
}