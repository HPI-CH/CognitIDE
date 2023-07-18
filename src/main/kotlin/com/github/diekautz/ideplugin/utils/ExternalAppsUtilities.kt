package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.intellij.openapi.project.Project
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
