package com.github.diekautz.ideplugin.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import java.time.Year

class ParticipantConfigurable : BoundConfigurable(
    "ParticipantConfigurable"
) {
    private val model = ParticipantState.instance

    @Suppress("DialogTitleCapitalization")
    override fun createPanel() = panel {
        collapsibleGroup("Participant") {
            row("Id:") {
                spinner(0..Int.MAX_VALUE).bindIntValue(model::id)
            }
        }
        group("Traits") {
            row("Gender:") {
                comboBox(listOf("male", "female", "non-binary", "prefer not to disclose"))
                    .bindItem(model::gender.toNullableProperty())
            }
            row("Profession:") {
                textField().bindText(model::profession)
            }
            row("Handedness:") {
                comboBox(listOf("left", "right", "mixed", "ambidextrous"))
                    .bindItem(model::handedness.toNullableProperty())
            }
        }
        group("Please estimate (higher is better)") {
            row("Your programming experience:") {
                comboBox((1..10).toList())
                    .bindItemNullable(model::experience10)
            }
            row("Your programming experience compared to experts\nwith 20 years of practical experience?") {
                comboBox((1..5).toList())
                    .bindItemNullable(model::compareExpert5)
            }
            row("Your programming experience compared to \nyour class mates?") {
                comboBox((1..5).toList())
                    .bindItemNullable(model::compareClassmates5)
            }
        }

        group("How experienced are you with the following languages:") {
            row {
                comboBox((1..5).toList())
                    .label("Java")
                    .bindItemNullable(model::experienceJava5)
            }.layout(RowLayout.PARENT_GRID)
            row {

                comboBox((1..5).toList())
                    .label("C")
                    .bindItemNullable(model::experienceC5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Haskell")
                    .bindItemNullable(model::experienceHaskell5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Prolog")
                    .bindItemNullable(model::experienceProlog5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                textField()
                    .label("Other laguages")
                    .bindText(model::additionalLanguages)
            }.layout(RowLayout.PARENT_GRID)
        }
        group("How experienced are you with the following programming paradigms:") {
            row {
                comboBox((1..5).toList())
                    .label("Functional programming")
                    .bindItemNullable(model::paradigmFunctional5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Imperative programming")
                    .bindItemNullable(model::paradigmImperative5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Logical programming")
                    .bindItemNullable(model::paradigmLogical5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Object-oriented programming")
                    .bindItemNullable(model::paradigmOOP5)
            }.layout(RowLayout.PARENT_GRID)
        }
        group("For how many years have you been...") {
            row {
                spinner(0..100)
                    .label("programming in general?")
                    .bindIntValue(model::yearsProgramming)
            }.layout(RowLayout.PARENT_GRID)
            row {
                spinner(0..100)
                    .label("for larger software projects, e.g., in a company?")
                    .bindIntValue(model::yearsProgrammingCompany)
            }.layout(RowLayout.PARENT_GRID)
        }
        group("University") {
            row {
                spinner(1950.rangeTo(Year.now().value))
                    .label("Enrollment year")
                    .bindIntValue(model::enrollYear)
            }
            row {
                spinner(0..100)
                    .label("How many courses did you take in which you had to implement source code?")
                    .bindIntValue(model::coursesCoding)
            }
        }
        group("How old are you?") {
            row {
                spinner(1..100)
                    .bindIntValue(model::age)
            }
        }

    }
}
