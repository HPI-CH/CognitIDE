package com.github.hpich.cognitide.actions.questionnaires

import com.github.hpich.cognitide.config.questionnaires.PostQuestionnaireConfigurable
import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil

class ViewPostQuestionnaireAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val dataCollectingService = e.project?.service<DataCollectingService>()
        e.presentation.isEnabled = dataCollectingService != null &&
            !dataCollectingService.isRecording
    }

    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().editConfigurable(e.project, PostQuestionnaireConfigurable())
    }
}
