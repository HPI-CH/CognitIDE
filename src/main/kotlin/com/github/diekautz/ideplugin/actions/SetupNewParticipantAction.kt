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
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages.CANCEL
import com.intellij.openapi.ui.Messages.YES

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
        when (MessageDialogBuilder
            .yesNoCancel("New Participant Setup", "Clear all recorded data?")
            .asWarning()
            .show(e.project)) {
            YES -> e.project?.service<MyLookRecorderService>()?.clearData()
            CANCEL -> return
        }

        // reset participant questioner, generate new id
        when (MessageDialogBuilder
            .yesNoCancel("New Participant Setup", "Reset participant?")
            .asWarning()
            .show(e.project)) {
            YES -> {
                ApplicationManager.getApplication().getService(ParticipantState::class.java)
                    ?.loadState(ParticipantState())
                ShowSettingsUtil.getInstance().editConfigurable(e.project, ParticipantConfigurable())
            }

            CANCEL -> return
        }

        // calibrate user
        when (MessageDialogBuilder
            .yesNoCancel("New Participant Setup", "Redo calibration?")
            .show(e.project)
        ) {
            YES -> openEyeTrackerManager(e.project!!)
            CANCEL -> return
        }

        // start new recording
        when (MessageDialogBuilder
            .yesNoCancel("New Participant Setup", "Start new recording now?")
            .show(e.project)
        ) {
            YES -> e.project?.service<MyTobiiProService>()?.startRecording()
            CANCEL -> return
        }
    }
}