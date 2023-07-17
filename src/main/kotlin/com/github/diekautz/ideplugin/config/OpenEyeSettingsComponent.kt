package com.github.diekautz.ideplugin.config

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


class OpenEyeSettingsComponent() {

    private val tobiiProConnectorExecutableField = JBTextField()
    var tobiiProConnectorExecutable: String
        get() = tobiiProConnectorExecutableField.text
        set(value) {
            tobiiProConnectorExecutableField.text = value
        }

    private val eyeTrackerManagerExecutableField = JBTextField()
    var eyeTrackerManagerExecutable: String
        get() = eyeTrackerManagerExecutableField.text
        set(value) {
            eyeTrackerManagerExecutableField.text = value
        }

    val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("TobiiPro Connector path: ", tobiiProConnectorExecutableField, 1, false)
        .addLabeledComponent("Eye Tracker Manager path: ", eyeTrackerManagerExecutableField, 1, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel


    fun getPreferredFocusedComponent(): JComponent {
        return tobiiProConnectorExecutableField
    }
}