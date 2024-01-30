package com.github.hpich.cognitide.utils

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.config.DeviceSpec
import com.intellij.openapi.project.Project
import java.io.File

fun openTobiiProConnector(project: Project) {
    execExternalUtility(
        project,
        CognitIDESettingsState.instance.tobiiProConnectorExecutable,
        "Please provide a valid path to the TobiiPro Connector executable."
    )
}

fun openConnector(project: Project, device: DeviceSpec) {
    execExternalUtility(
        project,
        device.connectorPath,
        "Please provide a valid path to the connector executable."
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
