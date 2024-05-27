package com.github.hpich.cognitide.actions.questionnaires

import com.github.hpich.cognitide.config.questionnaires.MidStudyQuestionnaireConfigurable
import com.github.hpich.cognitide.config.questionnaires.QuestionnaireState
import com.github.hpich.cognitide.services.DataCollectingService
import com.github.hpich.cognitide.utils.cognitIDETrackerManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil

class ViewMidStudyQuestionnaireAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val dataCollectingService = e.project?.service<DataCollectingService>()
        e.presentation.isEnabled = dataCollectingService != null &&
            !dataCollectingService.isRecording
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataCollectingService = e.project!!.service<DataCollectingService>()
        val questionnaireSuffix = this.templatePresentation.text.replace("Questionnaire", "").replace(" ", "")

        // stop recordings
        dataCollectingService.stopRecording()
        QuestionnaireState.instance.loadState(QuestionnaireState())
        ShowSettingsUtil.getInstance().editConfigurable(e.project, MidStudyQuestionnaireConfigurable(questionnaireSuffix))
        cognitIDETrackerManager(e.project!!)
    }
}
