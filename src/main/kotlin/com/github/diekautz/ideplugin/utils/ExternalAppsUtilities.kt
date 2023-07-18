package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.OpenEyeSettingsConfigurable
import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import java.io.File

fun openTobiiProConnector(project: Project) {
    execExternalUtility(
        project,
        OpenEyeSettingsState.instance.tobiiProConnectorExecutable,
        "Please provide a valid path to the TobiiPro Connector executable."
    )
}

fun openEyeTrackerManager(project: Project) {
    execExternalUtility(
        project,
        OpenEyeSettingsState.instance.eyeTrackerManagerExecutable,
        "Please provide a valid path to the Eye Tracker Manager executable."
    )
}

fun execExternalUtility(project: Project, path: String, notFoundMessage: String) {
    if (path.isBlank() || File(wrapPath(path)).exists()) {
        requestSettingsChange(project, notFoundMessage)
        return
    }
    try {
        Runtime.getRuntime().exec(wrapPath(path))
    } catch (ex: Exception) {
        requestSettingsChange(project, "${ex.localizedMessage} $notFoundMessage")
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

private fun wrapPath(path: String) = if (path.startsWith('\"')) path else "\"$path\""
