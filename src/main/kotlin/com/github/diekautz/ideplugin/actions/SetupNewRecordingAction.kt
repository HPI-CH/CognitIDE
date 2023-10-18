package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.config.ParticipantConfigurable
import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.services.DataCollectingService
import com.github.diekautz.ideplugin.utils.cognitIDETrackerManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Messages.CANCEL
import com.intellij.openapi.ui.Messages.YES

class SetupNewRecordingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val dataCollectingService = e.project?.service<DataCollectingService>()
        e.presentation.isEnabled = dataCollectingService != null
                && !dataCollectingService.isRecording
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataCollectingService = e.project!!.service<DataCollectingService>()
        val dialogTitle = "New Participant Setup"

        // stop recordings
        dataCollectingService.stopRecording()

        // clear data
        when (MessageDialogBuilder
            .yesNoCancel(dialogTitle, "Clear all recorded data?")
            .asWarning()
            .show(e.project)) {
            YES -> dataCollectingService.clearData()
            CANCEL -> return
        }

        // reset participant questioner, generate new id
        when (MessageDialogBuilder
            .yesNoCancel(dialogTitle, "Reset participant?")
            .asWarning()
            .show(e.project)) {
            YES -> {
                ParticipantState.instance.loadState(ParticipantState())
                ShowSettingsUtil.getInstance().editConfigurable(e.project, ParticipantConfigurable())
            }

            CANCEL -> return
        }

        // calibrate user
        when (MessageDialogBuilder
            .yesNoCancel(dialogTitle, "Redo calibration?")
            .show(e.project)
        ) {
            YES -> cognitIDETrackerManager(e.project!!)
            CANCEL -> return
        }

        // confirm finished
        Messages.showInfoMessage("Great! You can now start your recording session!", dialogTitle)
    }
}