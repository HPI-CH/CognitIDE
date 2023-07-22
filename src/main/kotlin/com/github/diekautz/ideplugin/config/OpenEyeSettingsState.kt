package com.github.diekautz.ideplugin.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.File

@State(name = "com.github.diekautz.ideplugin.config.OpenEyeSettingsState", storages = [Storage("OpenEyePlugin.xml")])
class OpenEyeSettingsState : PersistentStateComponent<OpenEyeSettingsState> {
    // recording save location
    var recordingsSaveLocation: String = File(System.getProperty("user.home"), "openeye-recordings").path

    // interrupt
    var interruptUser = false
    var interruptStopRecordingAfterLast = true
    var interruptDelay = 60
    var interruptCount = 10

    // external applications
    var tobiiProConnectorExecutable = ""
    var eyeTrackerManagerExecutable = ""
    var eyeTrackerSerial = ""

    override fun getState(): OpenEyeSettingsState = this

    override fun loadState(state: OpenEyeSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: OpenEyeSettingsState
            get() = ApplicationManager.getApplication().getService(OpenEyeSettingsState::class.java)
    }
}