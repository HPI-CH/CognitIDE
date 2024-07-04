package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class StudyPopupAction(private val popupTitle: String, private val popupText: String) : StudyUtilAction() {
    override fun actionPerformed(event: AnActionEvent) {
        Messages.showInfoMessage(popupText, popupTitle)
    }
}
