package com.github.diekautz.ideplugin.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.serialization.Serializable

@Serializable
@State(
    name = "com.github.diekautz.ideplugin.config.ParticipantState",
    storages = [Storage("OpenEyePlugin_participant.xml")]
)
class ParticipantState : PersistentStateComponent<ParticipantState> {
    var id: Int = (1..10000).random()
    var horizontalSpread = 16
    var verticalSpread = 16

    // traits
    var gender: String? = null
    var profession: String = ""
    var handedness: String? = null

    // programming questionaire
    var experience10: Int? = null
    var compareExpert5: Int? = null
    var compareClassmates5: Int? = null

    var experienceJava5: Int? = null
    var experienceC5: Int? = null
    var experienceHaskell5: Int? = null
    var experienceProlog5: Int? = null
    var additionalLanguages = ""

    var paradigmFunctional5: Int? = null
    var paradigmLogical5: Int? = null
    var paradigmImperative5: Int? = null
    var paradigmOOP5: Int? = null
    var yearsProgramming = 0
    var yearsProgrammingCompany = 0
    var enrollYear = 0
    var coursesCoding = 0
    var age: Int = 0

    override fun getState(): ParticipantState = this

    override fun loadState(state: ParticipantState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: ParticipantState
            get() = ApplicationManager.getApplication().getService(ParticipantState::class.java)
    }
}