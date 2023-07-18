package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.OpenEyeSettingsConfigurable
import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.services.recording.GazeSnapshot
import com.github.diekautz.ideplugin.services.recording.SerializableElementGaze
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.psi.PsiElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    val recordingsSaveLocation = OpenEyeSettingsState.instance.recordingsSaveLocation
    val saveFolder = File(recordingsSaveLocation)

    val participantState = ApplicationManager.getApplication().getService(ParticipantState::class.java)
    val participantId = participantState.id

    saveFolder.mkdirs()
    try {
        if (gazeSnapshots.isNotEmpty()) {
            val file = File(saveFolder, "${participantId}_${timestamp}_gaze.json")
            saveToDisk(gazeSnapshots, file)
            notifyFileSaved(project, file)
        }
        if (elementGazePoints.isNotEmpty()) {
            val file = File(saveFolder, "${participantId}_${timestamp}_elements.json")
            saveToDisk(
                elementGazePoints.map { (psiElement, gazeWeight) ->
                    SerializableElementGaze(psiElement, gazeWeight)
                }, file
            )
            notifyFileSaved(project, file)
        }
        val file = File(saveFolder, "${participantId}_participant.json")
        saveToDisk(participantState, file)
        notifyFileSaved(project, file)
    } catch (ex: Exception) {
        OpenEyeSettingsState.thisLogger().error(ex)
        requestSettingsChange(
            project,
            "${ex.localizedMessage}: Please specify a valid location for recording to be saved!"
        )
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

fun requestSettingsChange(project: Project, notFoundMessage: String) {
    if (MessageDialogBuilder
            .okCancel("Invalid settings", notFoundMessage)
            .ask(project)
    ) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, OpenEyeSettingsConfigurable::class.java)
    }
}

fun wrapPath(path: String) = if (path.startsWith('\"')) path else "\"$path\""

private fun notifyFileSaved(project: Project, file: File) {
    Logger.getInstance("IOUtilities").info("Successfully saved ${file.path}")
    invokeLater {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Recording File Saved")
            .createNotification("Successfully saved ${file.path}", NotificationType.INFORMATION)
            .notify(project);
    }
}
