package com.github.diekautz.ideplugin.config

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class OpenEyeSettingsConfigurable : Configurable {

    private var component: OpenEyeSettingsComponent? = null

    override fun getDisplayName() = "OpenEye Settings"
    override fun createComponent(): JComponent {
        component = OpenEyeSettingsComponent()
        return component!!.panel
    }

    override fun isModified(): Boolean {
        val settings = OpenEyeSettingsState.instance
        return settings.tobiiProConnectorExecutable != component!!.tobiiProConnectorExecutable
                || settings.eyeTrackerManagerExecutable != component!!.eyeTrackerManagerExecutable
    }

    override fun apply() {
        val settings = OpenEyeSettingsState.instance
        settings.tobiiProConnectorExecutable = component!!.tobiiProConnectorExecutable
        settings.eyeTrackerManagerExecutable = component!!.eyeTrackerManagerExecutable
    }

    override fun reset() {
        val settings = OpenEyeSettingsState.instance
        component!!.tobiiProConnectorExecutable = settings.tobiiProConnectorExecutable
        component!!.eyeTrackerManagerExecutable = settings.eyeTrackerManagerExecutable
    }

    override fun disposeUIResources() {
        component = null
    }

    override fun getPreferredFocusedComponent() = component?.getPreferredFocusedComponent()
}
