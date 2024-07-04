package com.github.hpich.cognitide.config.study

import com.github.hpich.cognitide.services.study.WorkflowItem
import com.intellij.json.JsonFileType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.NonNls

class StudyConfigurable : BoundConfigurable(
    "Study Setup",
) {
    private val model = StudyState.instance

    private val actionPrefix = "com.github.hpich.cognitide"
    private val actions: List<AnAction>
    private val workflow: List<WorkflowItem> =
        listOf(
            WorkflowItem(true, "com.github.hpich.cognitide.actions.ViewPreQuestionnaireAction"),
            WorkflowItem(true, "com.github.hpich.cognitide.actions.recording.StartRecordingAction"),
        )

    init {
        actions = getAvailableActions()
    }

    private fun getAvailableActions(): List<AnAction> {
        val actionManager: ActionManager = ActionManager.getInstance()
        val actionIds: MutableList<@NonNls String> =
            actionManager.getActionIdList(actionPrefix)
        return actionIds.map { actionId -> actionManager.getAction(actionId) }
    }

    override fun createPanel() =
        panel {
            pathSetupGroup()
            questionnaireGroup()
            workflowGroup()
        }

    private fun Panel.pathSetupGroup() {
        group("Workflow") {
            row("Workflow File:") {
                textFieldWithBrowseButton(
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(JsonFileType.INSTANCE),
                ).bindText(model::workflowJsonPath)
            }.rowComment("The workflow JSON which should be executed")
        }
    }

    private fun Panel.questionnaireGroup() {
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

    private fun Panel.workflowGroup() {
        panel {
            group("Workflow Configurator") {
                row {
                    text("Configure the workflow that should run, after starting the study.")
                    button("Add Step") {
                    }
                }
                workflow.forEach { item ->
                    actionStep(item)
                }
            }
        }
    }

    private fun Panel.actionStep(item: WorkflowItem) {
        group("Step 1", indent = true) {
            row("") {
                checkBox("Enabled")
                comboBox(actions)
            }
            row {
                button("Remove Step") {}
                button("Move Up") {}
                button("Move Down") {}
            }
        }
    }
}
