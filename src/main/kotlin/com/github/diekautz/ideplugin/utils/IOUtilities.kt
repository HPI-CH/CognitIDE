package com.github.diekautz.ideplugin.utils

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun serializeAndSaveToDisk(project: Project, data: Any, dialogTitle: String) {
    val descriptor = FileChooserDescriptorFactory
        .createSingleFileDescriptor(".json")
        .withTitle(dialogTitle)
    val file = FileChooser.chooseFile(descriptor, project, null)
    if (file != null) {
        val encoded = Json.encodeToString(data)
        file.setBinaryContent(encoded.encodeToByteArray())
    }
}