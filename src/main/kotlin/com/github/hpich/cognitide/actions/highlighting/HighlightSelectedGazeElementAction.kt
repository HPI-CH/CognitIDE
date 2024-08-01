package com.github.hpich.cognitide.actions.highlighting

import com.github.hpich.cognitide.services.DataCollectingService
import com.github.hpich.cognitide.utils.showMessageWithDoNotAskOption
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class HighlightSelectedGazeElementAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        showMessageWithDoNotAskOption(
            e.project,
            "Highlight Recording?",
            "Highlighting the recording will reset all files contained in the recording to the state they were " +
                "at the end of the recording. Do you wish to proceed?",
            "highlight.doNotAskAgain",
        ) {
            e.project?.service<DataCollectingService>()?.selectAndHighlightRecording()
        }
    }
}
