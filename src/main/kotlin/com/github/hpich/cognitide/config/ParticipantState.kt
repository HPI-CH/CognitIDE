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
    @Suppress("ktlint")
    // absolute paths will be obsolete once study workflow is implemented
    var participantSetupJSONpath = "D:/Programming/Uni/HPI/Masterprojekt/CognitIDE/resources/participantSetup.json"
    var preQuestionnaireJSONpath = "D:/Programming/Uni/HPI/Masterprojekt/CognitIDE/resources/preQuestionnaire.json"
    var midStudyQuestionnaireJSONpath = "D:/Programming/Uni/HPI/Masterprojekt/CognitIDE/resources/midStudyQuestionnaire.json"
    var postQuestionnaireJSONpath = "D:/Programming/Uni/HPI/Masterprojekt/CognitIDE/resources/postQuestionnaire.json"

    var id: Int = (1..10000).random()
    var horizontalSpread = 16
    var verticalSpread = 16

    var propertiesMap: MutableMap<String, String> = mutableMapOf()

    override fun getState(): ParticipantState = this

    override fun loadState(state: ParticipantState) {
        state.propertiesMap.toMap(propertiesMap)
    }

    fun reset() {
        propertiesMap.clear()
    }

    override fun equals(other: Any?): Boolean {
        return other is ParticipantState && propertiesMap == other.propertiesMap
    }

    override fun hashCode(): Int {
        return propertiesMap.hashCode()
    }

    fun accessPropertyString(key: String): Pair<() -> String, (Any?) -> Unit> {
        val getter = { propertiesMap.getOrDefault(key, "") }
        val setter = { value: Any? -> propertiesMap[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun accessPropertyIntOpt(key: String): Pair<() -> Int?, (Int?) -> Unit> {
        val getter = { propertiesMap.getOrDefault(key, "0").toIntOrNull() }
        val setter = { value: Int? -> propertiesMap[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun accessPropertyInt(key: String): Pair<() -> Int, (Int?) -> Unit> {
        val getter = { propertiesMap.getOrDefault(key, "0").toInt() }
        val setter = { value: Int? -> propertiesMap[key] = value.toString() }
        return Pair(getter, setter)
    }

    companion object {
        val instance: ParticipantState
            get() = ApplicationManager.getApplication().getService(ParticipantState::class.java)
    }
}
