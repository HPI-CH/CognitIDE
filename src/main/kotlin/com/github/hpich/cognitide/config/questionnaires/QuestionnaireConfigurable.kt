package com.github.hpich.cognitide.config.questionnaires

import com.github.hpich.cognitide.utils.Questionnaire
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

abstract class QuestionnaireConfigurable : BoundConfigurable(
    "QuestionnaireConfigurable",
) {
    private val model = QuestionnaireState.instance

    abstract val questionnaire: Questionnaire

    open fun getQuestionnaireName(): String {
        return questionnaire.questionnaireType
    }

    override fun createPanel() =
        panel {
            questionnaire.sections.forEach {
                    section ->
                group(section.title) {
                    if (section.text != "") {
                        row {
                            label(section.text)
                        }
                    }
                    section.questions.forEach {
                            question ->
                        row(question.title) {
                            when (question.type) {
                                "dropdown" -> {
                                    val (getValue, setValue) = model.accessPropertyString(getQuestionnaireName(), question.property)
                                    comboBox(question.answers!!).bindItem(getValue, setValue)
                                }

                                "freetext" -> {
                                    val (getValue, setValue) = model.accessPropertyString(getQuestionnaireName(), question.property)
                                    textField().bindText(getValue, setValue)
                                }

                                "slider" -> {
                                    val (getValue, setValue) = model.accessPropertyIntOpt(getQuestionnaireName(), question.property)
                                    comboBox((question.min!!..question.max!!).toList()).bindItem(getValue, setValue)
                                }
                            }
                        }
                    }
                }
            }
        }
}
