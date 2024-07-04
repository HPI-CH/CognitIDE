package com.github.hpich.cognitide.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.File

@State(name = "com.github.hpich.cognitide.config.CognitIDESettingsState", storages = [Storage("CognitIDEPlugin.xml")])
class CognitIDESettingsState : PersistentStateComponent<CognitIDESettingsState> {
    // recording save location
    var recordingsSaveLocation: String = File(System.getProperty("user.home"), "cognitide-recordings").path

    // interrupt
    var interruptUser = false
    var interruptStopRecordingAfterLast = true
    var interruptDelay = 60
    var interruptCount = 10

    // external applications
    var tobiiProConnectorExecutable = ""
    var eyeTrackerManagerExecutable = ""
    var eyeTrackerSerial = ""
    var includeTobii = true

    // devices //TODO refresh panel
    val devices =
        mutableListOf(
            DeviceSpec("EmotivDataStream-EEG", "19", ""),
            DeviceSpec("Shimmer3 GSR+", "18", ""),
            DeviceSpec("Tobii", "6", ""),
        ) // Todo ensure no empty entries

    override fun getState(): CognitIDESettingsState = this

    override fun loadState(state: CognitIDESettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: CognitIDESettingsState
            get() = ApplicationManager.getApplication().getService(CognitIDESettingsState::class.java)
    }
}
