package com.github.diekautz.ideplugin.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import java.time.Year

class ParticipantConfigurable : BoundConfigurable(
    "ParticipantConfigurable"
) {
    private val model = ParticipantState.instance

    override fun createPanel() = panel {
        collapsibleGroup("Participant") {
            row("Id:") {
                intTextField().bindIntText(model::id)
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
        group("How do you estimate") {
            row {
                comboBox((1..10).toList())
                    .label("your programming experience?")
                    .bindItem(model::experience10)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("your programming experience compared to \nexperts with 20 years of practical experience?")
                    .bindItem(model::compareExpert5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("your programming experience compared to \nyour class mates?")
                    .bindItem(model::compareClassmates5)
            }.layout(RowLayout.PARENT_GRID)
        }

        group("How experienced are you with the following languages:") {
            row {
                comboBox((1..5).toList())
                    .label("Java")
                    .bindItem(model::experienceJava5)
            }.layout(RowLayout.PARENT_GRID)
            row {

                comboBox((1..5).toList())
                    .label("C")
                    .bindItem(model::experienceC5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Haskell")
                    .bindItem(model::experienceHaskell5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Prolog")
                    .bindItem(model::experienceProlog5)
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
                    .bindItem(model::paradigmFunctional5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Imperative programming")
                    .bindItem(model::paradigmImperative5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Logical programming")
                    .bindItem(model::paradigmLogical5)
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox((1..5).toList())
                    .label("Object-oriented programming")
                    .bindItem(model::paradigmOOP5)
            }.layout(RowLayout.PARENT_GRID)
        }
        group("For how many years have you been...") {
            row {
                intTextField(0..100)
                    .label("programming in general?")
                    .bindIntText(model::yearsProgramming)
            }.layout(RowLayout.PARENT_GRID)
            row {
                intTextField(0..100)
                    .label("for larger software projects, e.g., in a company?")
                    .bindIntText(model::yearsProgrammingCompany)
            }.layout(RowLayout.PARENT_GRID)
        }
        group("University") {
            row {
                intTextField(1950.rangeTo(Year.now().value))
                    .label("Enrollment year")
                    .bindIntText(model::enrollYear)
            }
            row {
                intTextField(0..100)
                    .label("How many courses did you take in which you had to implement source code?")
                    .bindIntText(model::coursesCoding)
            }
        }
        group("How old are you?") {
            row {
                intTextField(1..100)
                    .bindIntText(model::age)
            }
        }

    }
}
