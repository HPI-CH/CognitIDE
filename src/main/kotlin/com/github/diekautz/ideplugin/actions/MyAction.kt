package com.github.diekautz.ideplugin.actions

import com.intellij.icons.AllIcons.Icons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Messages
import javax.swing.JCheckBox

class MyAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showCheckboxMessageDialog(
            "Message",
            "Title",
            arrayOf("A", "B", "C"),
            "Checkbox text",
            false,
            0,
            0,
            Icons.Ide.MenuArrow
        ) { i: Int, jCheckBox: JCheckBox ->
            thisLogger().warn("$i, $jCheckBox")
            return@showCheckboxMessageDialog 0
        }
    }

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabledAndVisible = currentProject != null
    }
}