package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent

class ExecuteAnyActionAction(private val actionId: String) : StudyUtilAction() {
    override fun actionPerformed(event: AnActionEvent) {
        ActionManager.getInstance().getAction(actionId)?.actionPerformed(event)
    }
}
