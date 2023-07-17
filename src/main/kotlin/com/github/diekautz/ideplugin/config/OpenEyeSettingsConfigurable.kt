package com.github.diekautz.ideplugin.config

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class OpenEyeSettingsConfigurable : BoundConfigurable(
    "OpenEyeSettingsConfigurable"
) {
    private val model = OpenEyeSettingsState.instance

    override fun createPanel() = panel {
        collapsibleGroup("External Applications") {
            row("TobiiPro Connector:") {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                    .bindText(model::tobiiProConnectorExecutable)
            }
            row("Eye Tracker Manager:") {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                    .bindText(model::eyeTrackerManagerExecutable)
                checkBox("Was calibrated")
                    .bindSelected(model::wasTrackerCalibrated)
            }
        }
    }
}
