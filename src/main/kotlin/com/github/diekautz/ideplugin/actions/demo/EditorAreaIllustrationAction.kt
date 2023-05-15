package com.github.diekautz.ideplugin.actions.demo

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager


class EditorAreaIllustrationAction : AnAction() {
    /**
     * Displays a message with information about the current caret.
     *
     * @param e Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        // Get access to the editor and caret model. update() validated editor's existence.
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel: CaretModel = editor.caretModel

        // Getting the primary caret ensures we get the correct one of a possible many.
        val primaryCaret: Caret = caretModel.primaryCaret
        // Get the caret information
        val logicalPos: LogicalPosition = primaryCaret.logicalPosition
        val visualPos: VisualPosition = primaryCaret.visualPosition
        val caretOffset: Int = primaryCaret.offset


        val psiFile = PsiDocumentManager.getInstance(e.project!!).getPsiFile(editor.document)

        // Build and display the caret report.
        val report = """
             $logicalPos
             $visualPos
             $psiFile
             Offset: $caretOffset
             """.trimIndent()
        Messages.showInfoMessage(report, "Caret Parameters Inside The Editor")
    }

    /**
     * Sets visibility and enables this action menu item if:
     *
     *  * a project is open
     *  * an editor is active
     *
     *
     * @param e Event related to this action
     */
    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project: Project? = e.project
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        //Set visibility only in case of existing project and editor
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}