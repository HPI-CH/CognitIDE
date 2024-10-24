package com.github.hpich.cognitide.config

import com.github.hpich.cognitide.utils.readJson
import com.github.hpich.cognitide.utils.saveQuestionnaireToDisk
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import java.time.Instant
import java.util.*
import javax.swing.JLabel

class QuestionnaireConfigurable(questionnairePath: String, val name: String) : BoundConfigurable(
    "Questionnaire",
) {
    private val model = QuestionnaireState.instance

    private val questionnaire = readJson(questionnairePath)

    private fun getQuestionnaireName(): String = name

    override fun apply() {
        super.apply()
        val state = model.getQuestionnaireState(getQuestionnaireName())
        if (state != null) {
            saveQuestionnaireToDisk(getQuestionnaireName(), state, Date.from(Instant.now()))
        }
    }

    // We want to save the questionnaire even if it is not modified
    override fun isModified(): Boolean {
        return true
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
                                    if (getValue().isEmpty()) setValue("")
                                    comboBox(listOf("") + question.answers!!).bindItem(getValue, setValue)
                                }

                                "freetext" -> {
                                    val (getValue, setValue) = model.accessPropertyString(getQuestionnaireName(), question.property)
                                    if (getValue().isEmpty()) setValue("")
                                    textField().bindText(getValue, setValue)
                                }

                                "slider" -> {
                                    val (getValue, setValue) = model.accessPropertyInt(getQuestionnaireName(), question.property)
                                    slider(
                                        question.min!!,
                                        question.max!!,
                                        question.minorTickSpacing!!,
                                        question.majorTickSpacing!!,
                                    ).bindValue(getValue, setValue)
                                        .labelTable(
                                            mapOf(
                                                question.min to JLabel(question.min.toString()),
                                                (question.max + question.min) / 2 to JLabel(((question.max + question.min) / 2).toString()),
                                                question.max to JLabel(question.max.toString()),
                                            ),
                                        )
                                }

                                "number" -> {
                                    val (getValue, setValue) = model.accessPropertyInt(getQuestionnaireName(), question.property)
                                    if (getValue() == 0) setValue(0)
                                    intTextField(question.min!!..question.max!!).bindIntText(getValue, setValue)
                                }
                            }
                        }
                    }
                }
            }
        }
}
