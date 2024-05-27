package com.github.hpich.cognitide.config.questionnaires
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.utils.readJSON

class PreQuestionnaireConfigurable : QuestionnaireConfigurable() {
    override val questionnaire = readJSON(ParticipantState.instance.preQuestionnaireJSONpath)
}
