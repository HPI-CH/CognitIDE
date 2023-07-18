package com.github.diekautz.ideplugin.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.github.diekautz.ideplugin.config.OpenEyeSettingsState", storages = [Storage("OpenEyePlugin.xml")])
class OpenEyeSettingsState : PersistentStateComponent<OpenEyeSettingsState> {
    // external applications
    var tobiiProConnectorExecutable = ""
    var eyeTrackerManagerExecutable = ""

    // recording save location
    var recordingsSaveLocation = ""

    override fun getState(): OpenEyeSettingsState = this

    override fun loadState(state: OpenEyeSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: OpenEyeSettingsState
            get() = ApplicationManager.getApplication().getService(OpenEyeSettingsState::class.java)
    }
}