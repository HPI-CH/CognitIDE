package com.github.hpich.cognitide.utils

import com.github.hpich.cognitide.config.CognitIDESettingsConfigurable
import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.config.questionnaires.QuestionnaireState
import com.github.hpich.cognitide.extensions.screenshot
import com.github.hpich.cognitide.services.dto.*
import com.github.hpich.cognitide.services.study.AWorkflowItem
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

val json =
    Json {
        allowSpecialFloatingPointValues = true
    }

private val logger = Logger.getInstance("com.github.hpich.cognitide.utils.IOUtilities")

fun createAndGetRecordingsFolder(): File {
    val recordingsSaveLocation = CognitIDESettingsState.instance.recordingsSaveLocation
    val recordingsFolder = File(recordingsSaveLocation)
    if (!recordingsFolder.exists()) {
        // Show Popup
        val result = Messages.showYesNoDialog("Should the recordings folder be created?", "", "Yes", "No", AllIcons.General.Warning)
        if (result == Messages.OK) {
            recordingsFolder.mkdirs()
        } else {
            throw Error("Could not create recordings folder: $recordingsFolder")
        }
    }
    return recordingsFolder
}

fun createAndGetParticipantFolder(): File {
    val participantId = ParticipantState.instance.id
    val recordingsFolder = createAndGetRecordingsFolder()
    val folder = File(recordingsFolder, "participant_$participantId")
    if (!folder.exists()) {
        folder.mkdir()
    }
    return folder
}

fun createTimestampedFile(
    parent: File,
    filename: String,
    date: Date,
): File {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val timestamp = timestampFormat.format(date)
    return File(parent, "${timestamp}_$filename")
}

fun saveRecordingToDisk(
    project: Project,
    date: Date,
    sensorData: MutableMap<String, MutableList<SensorSample>>,
    gazeData: MutableMap<Int, MutableList<GazeSample>>,
    initialFileContents: MutableMap<String, FileCheckpoint>,
    fileChangeData: MutableMap<String, MutableList<FileChangeset>>,
): File? {
    val questionnaireState = QuestionnaireState.instance.propertiesMap
    val saveFolder = createTimestampedFile(createAndGetParticipantFolder(), "recording", date)
    val participantState = ParticipantState.instance

    if (!saveFolder.exists()) {
        saveFolder.mkdirs()
    }
    try {
        if (sensorData.isNotEmpty()) {
            val file = File(saveFolder, "sensorData.json")
            saveToDisk(json.encodeToString(sensorData), file)
            notifyFileSaved(project, file)
        }
        if (gazeData.isNotEmpty()) {
            val file = File(saveFolder, "gazeData.json")
            saveToDisk(json.encodeToString(gazeData), file)
            notifyFileSaved(project, file)
        }
        if (initialFileContents.isNotEmpty()) {
            val file = File(saveFolder, "initialFileContents.json")
            saveToDisk(json.encodeToString(initialFileContents), file)
            notifyFileSaved(project, file)
        }
        if (fileChangeData.isNotEmpty()) {
            val file = File(saveFolder, "fileChangeData.json")
            saveToDisk(json.encodeToString(fileChangeData), file)
            notifyFileSaved(project, file)
        }
        val file = File(saveFolder, "participant.json")
        saveToDisk(json.encodeToString(participantState), file)
        notifyFileSaved(project, file)

        val questionnairesFile = File(saveFolder, "questionnaires.json")
        saveToDisk(json.encodeToString(questionnaireState), questionnairesFile)
        notifyFileSaved(project, questionnairesFile)

        if (initialFileContents.isNotEmpty()) {
            // highlight, open editors and save screenshots
            val images =
                screenshotFilesInEditor(
                    project,
                    initialFileContents.keys.toList(),
                )
            val imageFolder = File(saveFolder, "files")
            imageFolder.mkdirs()
            images.forEach { (fileName, image) ->
                if (image == null) return@forEach
                val imageFile = File(imageFolder, fileName.replace(Regex("[^a-zA-Z0-9\\-]"), "_") + ".png")
                saveToDisk(image, imageFile)
                notifyFileSaved(project, imageFile)
            }
        }
    } catch (ex: Exception) {
        project.errorMsg("An unknown error occurred whilst saving data!", logger, ex)
        return null
    }
    return saveFolder
}

fun saveQuestionnaireToDisk(
    name: String,
    state: MutableMap<String, String>,
    date: Date,
) {
    val questionnaireFile = createTimestampedFile(createAndGetParticipantFolder(), "questionnaire_$name.json", date)
    saveToDisk(json.encodeToString(state), questionnaireFile)
}

fun saveWorkflowToDisk(
    workflowItems: MutableList<AWorkflowItem>,
    date: Date,
) {
    val workflowFile = createTimestampedFile(createAndGetRecordingsFolder(), "workflow.json", date)
    saveToDisk(json.encodeToString(workflowItems), workflowFile)
}

fun parseWorkflowFromDisk(path: String): MutableList<AWorkflowItem>? {
    try {
        val jsonString = File(path).readLines().joinToString("\n")
        val workflowList = json.decodeFromString<MutableList<AWorkflowItem>>(jsonString)
        return workflowList
    } catch (e: Exception) {
        logger.error(e.message, e)
        return null
    }
}

fun reopenFilesFromPaths(
    project: Project,
    filePaths: List<String>,
): Map<String, FileEditor?> =
    runReadAction {
        val fileEditorManager = FileEditorManager.getInstance(project)
        return@runReadAction filePaths.associateWith { filePath ->
            val vFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (vFile == null) {
                logger.error("Could not find recorded file in my $filePath")
                return@associateWith null
            }
            // Check if the file is already open
            val openEditors = fileEditorManager.getEditors(vFile)
            if (openEditors.isNotEmpty()) {
                return@associateWith openEditors.firstOrNull()
            }
            val editor = fileEditorManager.openFile(vFile, false, true).firstOrNull()
            if (editor == null) {
                logger.error("Could not open an editor for $filePath")
                return@associateWith null
            }
            editor
        }
    }

fun reopenFileFromPath(
    project: Project,
    filePath: String,
): FileEditor? =
    runReadAction {
        val fileEditorManager = FileEditorManager.getInstance(project)
        val vFile = LocalFileSystem.getInstance().findFileByPath(filePath)
        if (vFile == null) {
            logger.error("Could not find recorded file in my $filePath")
        }
        val editor = vFile?.let { fileEditorManager.openFile(it, false, true).firstOrNull() }
        if (editor == null) {
            logger.error("Could not open an editor for $filePath")
        }
        editor
    }

fun screenshotFilesInEditor(
    project: Project,
    filePaths: List<String>,
): Map<String, BufferedImage?> {
    return filePaths.associateWith { filePath ->
        val editor = reopenFileFromPath(project, filePath) ?: return@associateWith null
        editor.screenshot()
    }
}

fun saveToDisk(
    encoded: String,
    file: File,
) {
    try {
        runWriteAction {
            logger.info("Saving ${file.path}")
            file.createNewFile()
            file.writeText(encoded)
        }
    } catch (ex: Exception) {
        if (MessageDialogBuilder
                .yesNo("Saving Error!", "The file could not be saved to ${file.path}.\nChoose directory?")
                .guessWindowAndAsk()
        ) {
            backupSaveData(file)?.let { saveToDisk(encoded, file) }
        }
    }
}

fun saveToDisk(
    data: BufferedImage,
    file: File,
) {
    try {
        runWriteAction {
            logger.info("Saving image ${file.path}")
            file.createNewFile()
            ImageIO.write(data, "png", file)
        }
    } catch (ex: Exception) {
        if (MessageDialogBuilder
                .yesNo("Saving Error! Choose new directory?", "\"The file could not be saved to ${file.path}\"")
                .guessWindowAndAsk()
        ) {
            backupSaveData(file)?.let { saveToDisk(data, it) }
        }
    }
}

fun backupSaveData(file: File): File? {
    val saveFileDialog =
        FileChooserFactory.getInstance()
            .createSaveFileDialog(
                FileSaverDescriptor(
                    "Please choose a location (old was: ${file.path}",
                    "",
                    ".${file.extension}",
                ),
                null,
            )

    return saveFileDialog.save("")?.file
}

fun requestSettingsChange(
    project: Project,
    notFoundMessage: String,
) {
    if (MessageDialogBuilder
            .okCancel("Invalid settings", notFoundMessage)
            .ask(project)
    ) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, CognitIDESettingsConfigurable::class.java)
    }
}

fun wrapPath(path: String) = if (path.startsWith('\"')) path else "\"$path\""

private fun openFileAction(file: File) =
    NotificationAction.createSimple("Open") {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }

private fun notifyFileSaved(
    project: Project,
    file: File,
) {
    Logger.getInstance("IOUtilities").info("Successfully saved ${file.path}")
    invokeLater {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Recording File Saved")
            .createNotification("Successfully saved ${file.path}", NotificationType.INFORMATION)
            .addAction(openFileAction(file))
            .notify(project)
    }
}
