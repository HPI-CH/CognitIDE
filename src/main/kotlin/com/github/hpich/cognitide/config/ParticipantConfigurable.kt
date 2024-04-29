package com.github.hpich.cognitide.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import java.time.Year

class ParticipantConfigurable : BoundConfigurable(
    "ParticipantConfigurable",
) {
    private val model = ParticipantState.instance

    @Suppress("DialogTitleCapitalization")
    override fun createPanel() =
        panel {
            collapsibleGroup("Participant") {
                row("Id:") {
                    spinner(0..Int.MAX_VALUE).bindIntValue(model::id)
                }
                row("Device/Setup Inaccuracy") {
                    spinner(2..1000, 2)
                        .comment("Horizontal inaccuracy margin diameter in px")
                        .bindIntValue(model::horizontalSpread)
                }
                row(EMPTY_LABEL) {
                    spinner(2..1000, 2)
                        .comment("Vertical inaccuracy margin diameter in px")
                        .bindIntValue(model::verticalSpread)
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
                    comboBox((1..10).toList()).bindItemNullable(model::experience10)
                }
                row("Your programming experience compared to experts\nwith 20 years of practical experience?") {
                    comboBox((1..5).toList()).bindItemNullable(model::compareExpert5)
                }
                row("Your programming experience compared to \nyour class mates?") {
                    comboBox((1..5).toList()).bindItemNullable(model::compareClassmates5)
                }
            }

            group("How experienced are you with the following languages:") {
                row("Java") {
                    comboBox((1..5).toList()).bindItemNullable(model::experienceJava5)
                }
                row("C") {
                    comboBox((1..5).toList()).bindItemNullable(model::experienceC5)
                }
                row("Haskell") {
                    comboBox((1..5).toList()).bindItemNullable(model::experienceHaskell5)
                }
                row("Prolog") {
                    comboBox((1..5).toList()).bindItemNullable(model::experienceProlog5)
                }
                row("Other languages") {
                    textField().bindText(model::additionalLanguages)
                }
            }
            group("How experienced are you with the following programming paradigms:") {
                row("Functional programming") {
                    comboBox((1..5).toList()).bindItemNullable(model::paradigmFunctional5)
                }
                row("Imperative programming") {
                    comboBox((1..5).toList()).bindItemNullable(model::paradigmImperative5)
                }
                row("Logical programming") {
                    comboBox((1..5).toList()).bindItemNullable(model::paradigmLogical5)
                }
                row("Object-oriented programming") {
                    comboBox((1..5).toList()).bindItemNullable(model::paradigmOOP5)
                }
            }
            group("For how many years have you been...") {
                row("programming in general?") {
                    spinner(0..100).bindIntValue(model::yearsProgramming)
                }
                row("for larger software projects, e.g.,\nin a company?") {
                    spinner(0..100).bindIntValue(model::yearsProgrammingCompany)
                }
            }
            group("University") {
                row("Enrollment year") {
                    spinner(1950.rangeTo(Year.now().value)).bindIntValue(model::enrollYear)
                }
                row("How many courses did you take in which\nyou had to implement source code?") {
                    spinner(0..100).bindIntValue(model::coursesCoding)
                }
                row("How large were the professional projects typically?") {
                    comboBox(listOf("NA", "<900", "900-40000", ">40000"))
                        .bindItem(model::projectSize.toNullableProperty())
                    rowComment("(measured in lines of code)")
                }
            }
            group {
                row("How old are you?") {
                    spinner(1..100)
                        .bindIntValue(model::age)
                }
            }
        }
}
