package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.CognitIDESettingsState
import com.intellij.openapi.project.Project
import java.io.File

fun openTobiiProConnector(project: Project) {
    execExternalUtility(
        project,
        CognitIDESettingsState.instance.tobiiProConnectorExecutable,
        "Please provide a valid path to the TobiiPro Connector executable."
    )
}

fun openShimmerConnector(project: Project) {
    execExternalUtility(
        project,
        CognitIDESettingsState.instance.shimmerConnectorExecutable,
        "Please provide a valid path to the Shimmer Connector executable."
    )
}

fun openEmotivConnector(project: Project) {
    execExternalUtility(
        project,
        CognitIDESettingsState.instance.emotivConnectorExecutable,
        "Please provide a valid path to the Emotiv Connector executable."
    )
}

fun cognitIDETrackerManager(project: Project) {
    var command = CognitIDESettingsState.instance.eyeTrackerManagerExecutable
    val deviceSerial = CognitIDESettingsState.instance.eyeTrackerSerial
    if (deviceSerial.isNotBlank()) {
        command += " --mode=usercalibration --device-sn=$deviceSerial"
    }
    execExternalUtility(
        project,
        command,
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
