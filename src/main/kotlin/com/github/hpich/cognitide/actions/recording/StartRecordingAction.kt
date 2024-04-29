package com.github.hpich.cognitide.actions.recording

import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.services.DataCollectingService
import com.github.hpich.cognitide.services.LSLRecorder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class StartRecordingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject != null &&
            ParticipantState.instance.id > 0 &&
            currentProject.service<DataCollectingService>().isRecording == false
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<DataCollectingService>()?.apply {
            setRecorder(LSLRecorder(e.project!!))
            startRecording()
        }
    }
}
