package com.github.hpich.cognitide.config.questionnaires

import com.github.hpich.cognitide.config.study.StudyState
import com.github.hpich.cognitide.utils.readJson

class PreQuestionnaireConfigurable : QuestionnaireConfigurable() {
    override val questionnaire =
        readJson(StudyState.instance.preStudyQuestionnaireJsonPath)
}
