package com.github.hpich.cognitide.config.study

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class StudyConfigurable : BoundConfigurable(
    "Study Setup",
) {
    private val model = StudyState.instance

    override fun createPanel() =
        panel {
            group("Questionnaires") {
                row("Participant Setup:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    ).bindText(model::participantSetupJsonPath)
                }.rowComment("Additional questions for initial participant setup")
                row("Pre-Study Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    ).bindText(model::preStudyQuestionnaireJsonPath)
                }.rowComment("Select pre-study questionnaire to be shown before the first baseline recording")
                row("Mid-Study Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    ).bindText(model::midStudyQuestionnaireJsonPath)
                }.rowComment("Select mid-study questionnaire to be shown after participant completes a task")
                row("Post-Study Questionnaire:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                    ).bindText(model::postStudyQuestionnaireJsonPath)
                }.rowComment("Select post-study questionnaire to be shown after the second baseline recording")
            }
        }
}
