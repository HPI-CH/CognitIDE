package com.github.diekautz.ideplugin.config

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

class HighlightingConfigurable : BoundConfigurable(
    "HighlightingSettingsConfigurable"
) {
    private val model = HighlightingState.instance

    override fun createPanel() = panel {
        group("User Defined Script for Configuring the Highlighting") {
            row("Path:"){
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                    .bindText(model::highlightingScript)
                    .comment(
                        "Further information can be found <a href='https://github.com/HPI-CH/CognitIDE'>here</a>."
                    )
            }
        }
    }
}
