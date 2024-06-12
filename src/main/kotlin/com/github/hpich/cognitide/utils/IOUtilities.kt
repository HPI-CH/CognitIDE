package com.github.hpich.cognitide.utils

import com.github.hpich.cognitide.config.CognitIDESettingsConfigurable
import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.config.questionnaires.QuestionnaireState
import com.github.hpich.cognitide.extensions.screenshot
import com.github.hpich.cognitide.services.dto.*
import com.github.hpich.cognitide.services.recording.UserInterrupt
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.vfs.LocalFileSystem
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

fun saveRecordingToDisk(
    project: Project,
    date: Date,
    sensorData: MutableMap<String, MutableList<SensorSample>>,
    gazeData: MutableMap<Int, MutableList<GazeSample>>,
    initialFileContents: MutableMap<String, FileCheckpoint>,
    fileChangeData: MutableMap<String, MutableList<FileChangeset>>,
    userInterrupts: List<UserInterrupt>?,
): File? {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val timestamp = timestampFormat.format(date)

    val participantState = ParticipantState.instance
    val participantId = participantState.id

    val questionnaireState = QuestionnaireState.instance.propertiesMap

    val settingsState = CognitIDESettingsState.instance
    val saveFolder = File(settingsState.recordingsSaveLocation, "${participantId}_$timestamp")

    saveFolder.mkdirs()
    try {
        if (!userInterrupts.isNullOrEmpty()) {
            val file = File(saveFolder, "interrupts.json")
            saveToDisk(json.encodeToString(userInterrupts), file)
            notifyFileSaved(project, file)
        }
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

fun screenshotFilesInEditor(
    project: Project,
    filePaths: List<String>,
): Map<String, BufferedImage?> =
    runReadAction {
        val fileEditorManager = FileEditorManager.getInstance(project)
        return@runReadAction filePaths.associateWith { filePath ->
            val vFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (vFile == null) {
                logger.error("Could not find recorded file in my $filePath")
                return@associateWith null
            }
            val editor = fileEditorManager.openFile(vFile, false, true).firstOrNull()
            if (editor == null) {
                logger.error("Could not open an editor for $filePath")
                return@associateWith null
            }
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
