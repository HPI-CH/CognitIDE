package com.github.hpich.cognitide.config.questionnaires

import com.github.hpich.cognitide.utils.readJson

class AnyQuestionnaireConfigurable(questionnairePath: String, val name: String) : QuestionnaireConfigurable() {
    override val questionnaire = readJson(questionnairePath)

    override fun getQuestionnaireName(): String = name

    // We want to save the questionnaire even if it is not modified
    override fun isModified(): Boolean {
        return true
    }
}
