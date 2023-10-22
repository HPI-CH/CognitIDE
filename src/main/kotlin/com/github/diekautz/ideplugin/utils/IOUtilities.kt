package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.CognitIDESettingsConfigurable
import com.github.diekautz.ideplugin.config.CognitIDESettingsState
import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.extensions.screenshot
import com.github.diekautz.ideplugin.services.dto.GazeSnapshot
import com.github.diekautz.ideplugin.services.dto.LookElementGaze
import com.github.diekautz.ideplugin.services.recording.UserInterrupt
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

val json = Json {
    allowSpecialFloatingPointValues = true
}

private val logger = Logger.getInstance("com.github.diekautz.ideplugin.utils.IOUtilities")

fun saveRecordingToDisk(
    project: Project,
    date: Date,
    lookElementGazeList: List<LookElementGaze>,
    gazeSnapshots: List<GazeSnapshot>?,
    userInterrupts: List<UserInterrupt>?
) {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val timestamp = timestampFormat.format(date)

    val participantState = ParticipantState.instance
    val participantId = participantState.id

    val settingsState = CognitIDESettingsState.instance
    val saveFolder = File(settingsState.recordingsSaveLocation, "${participantId}_${timestamp}")

    saveFolder.mkdirs()
    try {
        if (!gazeSnapshots.isNullOrEmpty()) {
            val file = File(saveFolder, "gaze.json")
            saveToDisk(json.encodeToString(gazeSnapshots), file)
            notifyFileSaved(project, file)
        }
        if (lookElementGazeList.isNotEmpty()) {
            val file = File(saveFolder, "elements.json")
            saveToDisk(json.encodeToString(lookElementGazeList), file)
            notifyFileSaved(project, file)
        }
        if (!userInterrupts.isNullOrEmpty()) {
            val file = File(saveFolder, "interrupts.json")
            saveToDisk(json.encodeToString(userInterrupts), file)
            notifyFileSaved(project, file)
        }
        val file = File(saveFolder, "participant.json")
        saveToDisk(json.encodeToString(participantState), file)
        notifyFileSaved(project, file)

        if (gazeSnapshots != null){
            // highlight, open editors and save screenshots
            val images = screenshotFilesInEditor(
                project,
                gazeSnapshots.map { it.lookElement!!.filePath }.distinct()
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
    }
}

fun screenshotFilesInEditor(project: Project, filePaths: List<String>): Map<String, BufferedImage?> = runReadAction {
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

fun saveToDisk(encoded: String, file: File) {
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

fun saveToDisk(data: BufferedImage, file: File) {
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
    val saveFileDialog = FileChooserFactory.getInstance()
        .createSaveFileDialog(
            FileSaverDescriptor(
                "Please choose a location (old was: ${file.path}",
                "",
                ".${file.extension}"
            ), null
        )

    return saveFileDialog.save("")?.file
}

fun requestSettingsChange(project: Project, notFoundMessage: String) {
    if (MessageDialogBuilder
            .okCancel("Invalid settings", notFoundMessage)
            .ask(project)
    ) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, CognitIDESettingsConfigurable::class.java)
    }
}

fun wrapPath(path: String) = if (path.startsWith('\"')) path else "\"$path\""

private fun openFileAction(file: File) = NotificationAction.createSimple("Open") {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file)
    }
}

private fun notifyFileSaved(project: Project, file: File) {
    Logger.getInstance("IOUtilities").info("Successfully saved ${file.path}")
    invokeLater {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Recording File Saved")
            .createNotification("Successfully saved ${file.path}", NotificationType.INFORMATION)
            .addAction(openFileAction(file))
            .notify(project)
    }
}
