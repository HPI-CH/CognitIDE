package com.github.hpich.cognitide.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

class HighlightingConfigurable : BoundConfigurable(
    "HighlightingSettingsConfigurable",
) {
    private val model = HighlightingState.instance

    override fun createPanel() =
        panel {
            group("Command to execute the user-provided Highlighting Script.") {
                @Suppress("ktlint")
                row("Command:") {
                    textField(
                    )
                        .bindText(model::highlightingCommand)
                        .comment(
                            "This command will be appended with parameters providing the folder of the recording " +
                                    "as well as the timestamp that will be highlighted. \n" +
                                    "Example: `python \"path/to/highlighting.py\"` will be executed as " +
                                    "`python \"path/to/highlighting.py\" \"path/to/recording/folder\" timestamp`. " +
                                    "Further information and an example script can be found " +
                                    "<a href='https://github.com/HPI-CH/CognitIDE'>here</a>.",
                        )
                }
            }
        }
}
