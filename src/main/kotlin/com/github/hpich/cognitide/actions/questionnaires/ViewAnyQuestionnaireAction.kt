package com.github.hpich.cognitide.actions.questionnaires

import com.github.hpich.cognitide.actions.studyUtils.StudyUtilAction
import com.github.hpich.cognitide.config.questionnaires.AnyQuestionnaireConfigurable
import com.github.hpich.cognitide.config.study.StudyState
import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.json.JsonFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import javax.swing.JOptionPane

class ViewAnyQuestionnaireAction(private val name: String = "", private val jsonPath: String = "") : StudyUtilAction() {
    override fun update(e: AnActionEvent) {
        val dataCollectingService = e.project?.service<DataCollectingService>()
        e.presentation.isEnabled = dataCollectingService != null &&
            !dataCollectingService.isRecording
    }

    override fun actionPerformed(e: AnActionEvent) {
        var questionnairePath: String = jsonPath
        if (questionnairePath == "") {
            val initialFilePath = StudyState.instance.preStudyQuestionnaireJsonPath
            val initialDirectory =
                VirtualFileManager.getInstance().findFileByUrl("file://$initialFilePath")

            val jsonFileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(JsonFileType.INSTANCE)
            val selectedFile = FileChooser.chooseFile(jsonFileDescriptor, null, initialDirectory)

            if (selectedFile == null) {
                JOptionPane.showMessageDialog(null, "No JSON selected", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }

            questionnairePath = selectedFile.path
        }

        val questionnaireSaveName = if (name != "") name else JOptionPane.showInputDialog("Enter name to store questionnaire results")
        ShowSettingsUtil.getInstance().editConfigurable(e.project, AnyQuestionnaireConfigurable(questionnairePath, questionnaireSaveName))
    }
}
