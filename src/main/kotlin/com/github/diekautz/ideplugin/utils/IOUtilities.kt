package com.github.diekautz.ideplugin.utils

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun serializeAndSaveToDisk(project: Project, data: Any, dialogTitle: String, filename: String? = null) {
    val descriptor = FileSaverDescriptor(dialogTitle, "", ".json")
    val saveFileDialog = FileChooserFactory.getInstance()
        .createSaveFileDialog(descriptor, project)


    val file = saveFileDialog.save(filename)?.virtualFile
    if (file != null) {
        val encoded = Json.encodeToString(data)
        file.setBinaryContent(encoded.encodeToByteArray())
    }
}