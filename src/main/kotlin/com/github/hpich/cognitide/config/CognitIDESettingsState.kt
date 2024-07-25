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

    // highlighting
    var highlightingCommand = ""

    // The workflow file containing all the workflow items to execute
    var workflowJsonPath: String = ""

    // Questionnaires
    var participantSetupJsonPath: String = ""

    var gazeSource: String = "Tobii"

    // devices
    var devices: MutableList<DeviceSpec> =
        mutableListOf()

    override fun getState(): CognitIDESettingsState = this

    override fun loadState(state: CognitIDESettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: CognitIDESettingsState
            get() = ApplicationManager.getApplication().getService(CognitIDESettingsState::class.java)
    }
}
