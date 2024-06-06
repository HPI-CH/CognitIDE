package com.github.hpich.cognitide.config.questionnaires

import com.github.hpich.cognitide.config.study.StudyState
import com.github.hpich.cognitide.utils.readJson
import java.util.*

class MidStudyQuestionnaireConfigurable(private val suffix: String) : QuestionnaireConfigurable() {
    override val questionnaire =
        readJson(StudyState.instance.midStudyQuestionnaireJsonPath)

    override fun getQuestionnaireName(): String {
        return super.getQuestionnaireName() + "_" + suffix.lowercase(Locale.getDefault())
    }
}
