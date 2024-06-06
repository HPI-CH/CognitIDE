package com.github.hpich.cognitide.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.serialization.Serializable

@Serializable
@State(
    name = "com.github.hpich.cognitide.config.ParticipantState",
    storages = [Storage("CognitIDEPlugin_participant.xml")],
)
class ParticipantState : PersistentStateComponent<ParticipantState> {
    var id: Int = 0
    var horizontalSpread = 16
    var verticalSpread = 16

    var propertiesMap: MutableMap<String, String> = mutableMapOf()

    override fun getState(): ParticipantState = this

    override fun loadState(state: ParticipantState) {
        propertiesMap.clear()
        state.propertiesMap.toMap(propertiesMap)
        id = state.id
        horizontalSpread = state.horizontalSpread
        verticalSpread = state.verticalSpread
    }

    fun accessPropertyString(key: String): Pair<() -> String, (Any?) -> Unit> {
        val getter = { propertiesMap.getOrDefault(key, "") }
        val setter = { value: Any? -> propertiesMap[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun accessPropertyInt(key: String): Pair<() -> Int, (Int?) -> Unit> {
        val getter = { propertiesMap.getOrDefault(key, "0").toInt() }
        val setter = { value: Int? -> propertiesMap[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun reset() {
        loadState(ParticipantState())
    }

    companion object {
        val instance: ParticipantState
            get() = ApplicationManager.getApplication().getService(ParticipantState::class.java)
    }
}
