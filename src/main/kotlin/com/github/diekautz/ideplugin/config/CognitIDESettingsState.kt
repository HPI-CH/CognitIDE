package com.github.diekautz.ideplugin.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.File

@State(name = "com.github.diekautz.ideplugin.config.CognitIDESettingsState", storages = [Storage("CognitIDEPlugin.xml")])
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
    var shimmerConnectorExecutable = ""
    var emotivConnectorExecutable = ""
    var eyeTrackerManagerExecutable = ""
    var eyeTrackerSerial = ""
    var includeTobii = true
    var includeShimmer = false
    var includeEmotiv = false

    override fun getState(): CognitIDESettingsState = this

    override fun loadState(state: CognitIDESettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: CognitIDESettingsState
            get() = ApplicationManager.getApplication().getService(CognitIDESettingsState::class.java)
    }
}