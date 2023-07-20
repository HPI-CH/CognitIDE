package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.OpenEyeSettingsConfigurable
import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.extensions.screenshot
import com.github.diekautz.ideplugin.services.recording.GazeSnapshot
import com.github.diekautz.ideplugin.services.recording.InterruptService
import com.github.diekautz.ideplugin.services.recording.SerializableElementGaze
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
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

fun saveRecordingToDisk(
    project: Project,
    date: Date,
    elementGazePoints: Map<PsiElement, Double>,
    gazeSnapshots: List<GazeSnapshot>
) {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val timestamp = timestampFormat.format(date)

    val interruptService = project.service<InterruptService>()


    val participantState = ParticipantState.instance
    val participantId = participantState.id

    val settingsState = OpenEyeSettingsState.instance
    val saveFolder = File(settingsState.recordingsSaveLocation, "${participantId}_${timestamp}")

    saveFolder.mkdirs()
    try {
        if (gazeSnapshots.isNotEmpty()) {
            val file = File(saveFolder, "gaze.json")
            saveToDisk(gazeSnapshots, file)
            notifyFileSaved(project, file)
        }
        if (elementGazePoints.isNotEmpty()) {
            val file = File(saveFolder, "elements.json")
            saveToDisk(
                elementGazePoints.map { (psiElement, gazeWeight) ->
                    SerializableElementGaze(psiElement, gazeWeight)
                }, file
            )
            notifyFileSaved(project, file)
        }
        if (interruptService.recordedInterrupts.isNotEmpty()) {
            val file = File(saveFolder, "interrupts.json")
            saveToDisk(interruptService.recordedInterrupts, file)
            notifyFileSaved(project, file)
        }
        val file = File(saveFolder, "participant.json")
        saveToDisk(participantState, file)
        notifyFileSaved(project, file)

        // highlight, open editors and save screenshots
        val images = screenshotFilesInEditor(
            project,
            gazeSnapshots.map { it.editorGazeSnapshot.filePath }.distinct()
        )
        val imageFolder = File(saveFolder, "files")
        imageFolder.mkdirs()
        images.forEach { (fileName, image) ->
            if (image == null) return@forEach
            val imageFile = File(imageFolder, fileName.replace(File.separatorChar, '_') + ".png")
            saveToDisk(image, imageFile)
            notifyFileSaved(project, imageFile)
        }
    } catch (ex: Exception) {
        OpenEyeSettingsState.thisLogger().error(ex)
        requestSettingsChange(
            project,
            "${ex.localizedMessage}: Please specify a valid location for recording to be saved!"
        )
    }
}

fun screenshotFilesInEditor(project: Project, filePaths: List<String>): Map<String, BufferedImage?> = runReadAction {
    val logger = Logger.getInstance("EditorScreenshots")
    val fileEditorManager = FileEditorManager.getInstance(project)
    mutableListOf<BufferedImage>()
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

inline fun <reified T> askAndSaveToDisk(project: Project, data: T, dialogTitle: String, filename: String? = null) {
    val descriptor = FileSaverDescriptor(dialogTitle, "", ".json")
    val saveFileDialog = FileChooserFactory.getInstance()
        .createSaveFileDialog(descriptor, project)


    val file = saveFileDialog.save(filename)?.file
    if (file != null) {
        saveToDisk(data, file)
    }
}

inline fun <reified T> saveToDisk(data: T, file: File) {
    val encoded = json.encodeToString(data)
    runWriteAction {
        file.createNewFile()
        file.writeText(encoded)
    }
}

fun saveToDisk(data: BufferedImage, file: File) {
    runWriteAction {
        file.createNewFile()
        ImageIO.write(data, "png", file)
    }
}

fun requestSettingsChange(project: Project, notFoundMessage: String) {
    if (MessageDialogBuilder
            .okCancel("Invalid settings", notFoundMessage)
            .ask(project)
    ) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, OpenEyeSettingsConfigurable::class.java)
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
            .notify(project);
    }
}
