package com.github.hpich.cognitide.actions.study

import com.github.hpich.cognitide.config.ParticipantConfigurable
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.config.questionnaires.QuestionnaireState
import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages.CANCEL
import com.intellij.openapi.ui.Messages.YES

class SetupNewParticipantAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val dataCollectingService = e.project?.service<DataCollectingService>()
        e.presentation.isEnabled = dataCollectingService != null &&
            !dataCollectingService.isRecording
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataCollectingService = e.project!!.service<DataCollectingService>()
        val dialogTitle = "New Participant Setup"

        // stop recordings
        dataCollectingService.stopRecording()

        // clear data
        when (
            MessageDialogBuilder
                .yesNoCancel(
                    dialogTitle,
                    "Setting up a new participant clears all recorded data and resets all participant data. Do you wish to proceed?",
                )
                .asWarning()
                .show(e.project)
        ) {
            YES -> {
                dataCollectingService.clearData()
                ParticipantState.instance.reset()
                QuestionnaireState.instance.reset()
                ShowSettingsUtil.getInstance().editConfigurable(e.project, ParticipantConfigurable())
            }
            CANCEL -> return
        }
    }
}
