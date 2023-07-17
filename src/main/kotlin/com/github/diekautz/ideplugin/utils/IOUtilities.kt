package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.ParticipantState
import com.github.diekautz.ideplugin.services.recording.GazeSnapshot
import com.github.diekautz.ideplugin.services.recording.SerializableElementGaze
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val json = Json {
    allowSpecialFloatingPointValues = true
}

fun askAndSaveToDisk(
    project: Project,
    date: Date,
    elementGazePoints: Map<PsiElement, Double>,
    gazeSnapshots: List<GazeSnapshot>
) {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val timestamp = timestampFormat.format(date)

    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    val selectedFolder = FileChooser.chooseFile(descriptor, project, null)

    val participantState = ApplicationManager.getApplication().getService(ParticipantState::class.java)
    val participantId = participantState.id

    if (selectedFolder != null && selectedFolder.isDirectory) {
        if (gazeSnapshots.isNotEmpty()) {
            val file = File(selectedFolder.path, "${participantId}_${timestamp}_gaze.json")
            saveToDisk(gazeSnapshots, file)
        }
        if (elementGazePoints.isNotEmpty()) {
            val file = File(selectedFolder.path, "${participantId}_${timestamp}_elements.json")
            saveToDisk(
                elementGazePoints.map { (psiElement, gazeWeight) ->
                    SerializableElementGaze(psiElement, gazeWeight)
                }, file
            )
        }
        val file = File(selectedFolder.path, "${participantId}_participant.json")
        saveToDisk(participantState, file)
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