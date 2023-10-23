package com.github.hpich.cognitide.actions

import com.github.hpich.cognitide.extensions.removeAllHighlighters
import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory

class ClearHighlightersAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject?.service<DataCollectingService>()?.wasHighlighted ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        EditorFactory.getInstance().removeAllHighlighters()
        e.project?.service<DataCollectingService>()?.wasHighlighted = false
    }
}