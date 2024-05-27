package com.github.hpich.cognitide.config.questionnaires

import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.utils.readJSON
import java.util.*

class MidStudyQuestionnaireConfigurable(private val suffix: String) : QuestionnaireConfigurable() {
    override val questionnaire = readJSON(ParticipantState.instance.midStudyQuestionnaireJSONpath)

    override fun getQuestionnaireName(): String {
        return super.getQuestionnaireName() + "_" + suffix.lowercase(Locale.getDefault())
    }
}
