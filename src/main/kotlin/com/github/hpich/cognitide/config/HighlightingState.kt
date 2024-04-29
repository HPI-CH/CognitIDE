package com.github.hpich.cognitide.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.github.hpich.cognitide.config.HighlightingSettingsState", storages = [Storage("CognitIDEPlugin_highlighting.xml")])
class HighlightingState : PersistentStateComponent<HighlightingState> {
    //
    var highlightingScript = ""

    override fun getState(): HighlightingState = this

    override fun loadState(state: HighlightingState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: HighlightingState
            get() = ApplicationManager.getApplication().getService(HighlightingState::class.java)
    }
}
