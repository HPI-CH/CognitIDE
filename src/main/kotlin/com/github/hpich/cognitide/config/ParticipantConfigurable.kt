package com.github.hpich.cognitide.config

import com.github.hpich.cognitide.utils.readJSON
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

class ParticipantConfigurable : BoundConfigurable(
    "ParticipantConfigurable",
) {
    private val model = ParticipantState.instance

    private val questionnaire = readJSON(model.participantSetupJSONpath)

    class Tmp {
        var tmpID = ""
        var tmpHorizontalSpread = "16"
        var tmpVerticalSpread = "16"

        fun tmpToInt(tmpVariable: String): Int {
            return try {
                when (tmpVariable) {
                    "id" -> tmpID.toIntOrNull() ?: -1
                    "horizontalSpread" -> tmpHorizontalSpread.toIntOrNull() ?: -1
                    "verticalSpread" -> tmpVerticalSpread.toIntOrNull() ?: -1
                    else -> -1
                }
            } catch (e: NumberFormatException) {
                -1
            }
        }
    }

    private val tmp = Tmp()

    override fun createPanel() =

        panel {
            group("Import Questionnaires") {
                @Suppress("ktlint")
                row("Participant Setup:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    )
                        .bindText(model::participantSetupJSONpath)
                }
                row("Pre-Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    )
                        .bindText(model::preQuestionnaireJSONpath)
                }
                row("Mid-Study-Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    )
                        .bindText(model::midStudyQuestionnaireJSONpath)
                }
                row("Post-Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    )
                        .bindText(model::postQuestionnaireJSONpath)
                }
            }
            questionnaire.sections.forEach {
                    section ->
                group(section.title) {
                    section.questions.forEach {
                            question ->
                        row(question.title) {
                            when (question.type) {
                                "dropdown" -> {
                                    val (getValue, setValue) = model.accessPropertyString(question.property)
                                    comboBox(question.answers!!).bindItem(getValue, setValue)
                                }

                                "freetext" -> {
                                    val (getValue, setValue) = model.accessPropertyString(question.property)
                                    textField().bindText(getValue, setValue)
                                }

                                "slider" -> {
                                    val (getValue, setValue) = model.accessPropertyIntOpt(question.property)
                                    comboBox((question.min!!..question.max!!).toList()).bindItem(getValue, setValue)
                                }

                                "number" -> {
                                    when (question.property) {
                                        "id" -> {
                                            textField().bindText(tmp::tmpID)
                                            val id = tmp.tmpToInt(question.property)
                                            // if input is not a valid int, a random id will be given
                                            if (question.min!! <= id && question.max!! >= id) {
                                                model.id = id
                                            }
                                        }
                                        "horizontalSpread" -> {
                                            textField().bindText(tmp::tmpHorizontalSpread)
                                            val horizontalSpread = tmp.tmpToInt(question.property)
                                            if (question.min!! <= horizontalSpread && question.max!! >= horizontalSpread) {
                                                model.horizontalSpread = horizontalSpread
                                            }
                                        }
                                        "verticalSpread" -> {
                                            textField().bindText(tmp::tmpVerticalSpread)
                                            val verticalSpread = tmp.tmpToInt(question.property)
                                            if (question.min!! <= verticalSpread && question.max!! >= verticalSpread) {
                                                model.verticalSpread = verticalSpread
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}
