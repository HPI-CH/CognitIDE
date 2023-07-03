package com.github.diekautz.ideplugin.utils

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json {
    allowStructuredMapKeys = true
}

inline fun <reified T> serializeAndSaveToDisk(project: Project, data: T, dialogTitle: String, filename: String? = null) {
    val descriptor = FileSaverDescriptor(dialogTitle, "", ".json")
    val saveFileDialog = FileChooserFactory.getInstance()
        .createSaveFileDialog(descriptor, project)


    val file = saveFileDialog.save(filename)?.file
    if (file != null) {
        val encoded = json.encodeToString(data)
        runWriteAction {
            file.createNewFile()
            file.writeText(encoded)
        }
    }
}