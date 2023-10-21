package com.github.diekautz.ideplugin.config

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

class HighlightingConfigurable : BoundConfigurable(
    "HighlightingSettingsConfigurable"
) {
    private val model = HighlightingState.instance

    override fun createPanel() = panel {
        group("Formula for Highlighting") {
            row("Available variables:") {

            }
            row("Path:"){
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                    .bindText(model::highlightingScript)
            }
        }
    }
}
