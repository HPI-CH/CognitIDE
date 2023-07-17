package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.utils.removeAllHighlighters
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorFactory

class ClearHighlightersAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        EditorFactory.getInstance().removeAllHighlighters()
    }
}