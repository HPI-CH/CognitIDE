package com.github.hpich.cognitide.config

import com.github.hpich.cognitide.config.study.StudyState
import com.github.hpich.cognitide.utils.readJsonOptional
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JLabel

class ParticipantConfigurable : BoundConfigurable(
    "ParticipantConfigurable",
) {
    private val model = ParticipantState.instance
    private lateinit var createdPanel: DialogPanel
    private lateinit var idField: Cell<JBTextField>

    private val questionnaire = readJsonOptional(StudyState.instance.participantSetupJsonPath)

    override fun apply() {
        val validation = createdPanel.validateAll()
        if (validation.isNotEmpty()) {
            throw Exception(validation.joinToString("\n"))
        } else {
            createdPanel.apply()
        }
    }

    override fun isModified(): Boolean {
        // Force validation if the idField has not been changed from the placeholder value "0"
        // if isModified returns false, apply would never execute, so no validation would take place
        return idField.component.text == "0" || super.isModified()
    }

    override fun createPanel(): DialogPanel {
        createdPanel =
            panel {
                createStaticPanel(this)
                questionnaire?.sections?.forEach { section ->
                    group(section.title) {
                        section.questions.forEach { question ->
                            row(question.title) {
                                when (question.type) {
                                    "dropdown" -> {
                                        val (getValue, setValue) = model.accessPropertyString(question.property)
                                        comboBox(listOf("") + question.answers!!).bindItem(getValue, setValue)
                                    }

                                    "freetext" -> {
                                        val (getValue, setValue) = model.accessPropertyString(question.property)
                                        textField().bindText(getValue, setValue)
                                    }

                                    "slider" -> {
                                        val (getValue, setValue) = model.accessPropertyInt(question.property)
                                        slider(
                                            question.min!!,
                                            question.max!!,
                                            question.minorTickSpacing!!,
                                            question.majorTickSpacing!!,
                                        ).bindValue(getValue, setValue)
                                            .labelTable(
                                                mapOf(
                                                    question.min to JLabel(question.min.toString()),
                                                    question.max / 2 to JLabel((question.max / 2).toString()),
                                                    question.max to JLabel(question.max.toString()),
                                                ),
                                            )
                                    }

                                    "number" -> {
                                        val (getValue, setValue) = model.accessPropertyInt(question.property)
                                        intTextField(question.min!!..question.max!!).bindIntText(getValue, setValue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return createdPanel
    }

    private fun createStaticPanel(it: Panel) =
        it.group("Participant") {
            row("ID:") {
                val getValue = { model.id }
                val setValue = { value: Int -> model.id = value }
                idField =
                    intTextField(1..10000).bindIntText(getValue, setValue)
                        .validation {
                            val value = it.text.toIntOrNull()
                            when (value) {
                                null -> error("")
                                !in 1..10000 -> error("ID must be between 1 and 10000")
                                else -> null
                            }
                        }

                button("Generate Random ID") {
                    model.id = (1..10000).random()
                    idField.component.text = model.id.toString()
                }
            }
            row("Horizontal Spread:") {
                intTextField(2..1000).bindIntText(model::horizontalSpread)
            }
                .rowComment("Device/Setup Inaccuracy (Horizontal inaccuracy margin diameter in px)")
            row("Vertical Spread:") {
                intTextField(2..1000).bindIntText(model::verticalSpread)
            }
                .rowComment("Device/Setup Inaccuracy (Vertical inaccuracy margin diameter in px)")
        }
}
