package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.config.ParticipantConfigurable
import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.services.MyMousePositionService
import com.github.diekautz.ideplugin.services.MyTobiiProService
import com.github.diekautz.ideplugin.services.recording.MyLookRecorderService
import com.github.diekautz.ideplugin.utils.openEyeTrackerManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil

class SetupNewParticipantAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        // stop recordings
        e.project?.service<MyTobiiProService>()?.stopRecording()
        e.project?.service<MyMousePositionService>()?.stopTrackMouse()

        // clear data
        e.project?.service<MyLookRecorderService>()?.clearData()

        // reset participant questioner, generate new id
        ApplicationManager.getApplication().getService(ParticipantState::class.java)?.loadState(ParticipantState())

        ShowSettingsUtil.getInstance().editConfigurable(e.project, ParticipantConfigurable())

        openEyeTrackerManager(e.project!!)
    }
}