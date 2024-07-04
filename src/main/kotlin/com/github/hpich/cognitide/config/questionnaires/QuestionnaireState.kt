package com.github.hpich.cognitide.config.questionnaires

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.serialization.Serializable

@Serializable
@State(
    name = "com.github.hpich.cognitide.config.questionnaires.QuestionnaireState",
    storages = [Storage("CognitIDEPlugin_questionnaire.xml")],
)
class QuestionnaireState : PersistentStateComponent<QuestionnaireState> {
    var propertiesMap: MutableMap<String, MutableMap<String, String>?> = mutableMapOf()

    private fun getQuestionnaire(questionaire: String): MutableMap<String, String> {
        if (!propertiesMap.containsKey(questionaire) || propertiesMap[questionaire] == null) {
            propertiesMap[questionaire] = mutableMapOf()
        }
        return propertiesMap[questionaire]!!
    }

    override fun getState(): QuestionnaireState = this

    override fun loadState(state: QuestionnaireState) {
        propertiesMap.clear()
        state.propertiesMap.toMap(propertiesMap)
    }

    fun reset() {
        loadState(QuestionnaireState())
    }

    fun accessPropertyString(
        questionnaire: String,
        key: String,
    ): Pair<() -> String, (Any?) -> Unit> {
        val getter = { getQuestionnaire(questionnaire).getOrDefault(key, "") }
        val setter = { value: Any? -> getQuestionnaire(questionnaire)[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun accessPropertyInt(
        questionnaire: String,
        key: String,
    ): Pair<() -> Int, (Int?) -> Unit> {
        val getter = { getQuestionnaire(questionnaire).getOrDefault(key, "0").toInt() }
        val setter = { value: Int? -> getQuestionnaire(questionnaire)[key] = value.toString() }
        return Pair(getter, setter)
    }

    fun getQuestionnaireState(questionnaire: String): MutableMap<String, String>? {
        return propertiesMap[questionnaire]
    }

    companion object {
        val instance: QuestionnaireState
            get() = ApplicationManager.getApplication().getService(QuestionnaireState::class.java)
    }
}
