package com.github.hpich.cognitide.config.study

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.serialization.Serializable

@Serializable
@State(
    name = "com.github.hpich.cognitide.config.study.StudyState",
    storages = [Storage("CognitIDEPlugin_study.xml")],
)
class StudyState : PersistentStateComponent<StudyState> {
    // The workflow file containing all the workflow items to execute
    var workflowJsonPath: String = ""

    // Questionnaires
    var participantSetupJsonPath: String = ""
    var preStudyQuestionnaireJsonPath = ""
    var midStudyQuestionnaireJsonPath = ""
    var postStudyQuestionnaireJsonPath = ""

    override fun getState(): StudyState = this

    override fun loadState(state: StudyState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: StudyState
            get() = ApplicationManager.getApplication().getService(StudyState::class.java)
    }
}
