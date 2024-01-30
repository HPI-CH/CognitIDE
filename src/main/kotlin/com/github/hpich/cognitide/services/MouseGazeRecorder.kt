package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.services.dto.GazeData
import com.github.hpich.cognitide.services.dto.LookElement
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset

class MouseGazeRecorder(project: Project) : StudyRecorder(project, "Recording Mouse") {
    override fun setup(indicator: ProgressIndicator): Boolean {
        EditorFactory.getInstance().allEditors.forEach {
            it.addEditorMouseListener(Listener)
            it.addEditorMouseMotionListener(Listener)
        }
        EditorFactory.getInstance().addEditorFactoryListener(Listener, this)
        return true
    }

    override fun dispose() {}

    override fun loop(indicator: ProgressIndicator) = invokeLater {
        editorMouseEvent?.let {
            val editor = it.editor
            if (editor.isDisposed) {
                editorMouseEvent = null
                return@let
            }
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return@invokeLater
            val offset = it.offset
            val mousePoint = it.mouseEvent.locationOnScreen
            val relativePoint = it.mouseEvent.point
            val logicalPosition = it.logicalPosition

            val element = psiFile.findElementAt(offset)
            val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)

            val fakeData = GazeData(mousePoint, mousePoint, 1.0, 1.0)
            if (virtualFile != null && element != null && element !is PsiWhiteSpace) {
                dataCollectingService.addGazeSnapshot(
                    LookElement(
                        element.text,
                        element.containingFile.virtualFile.path,
                        element.startOffset
                    ),
                    fakeData,
                    arrayOf(floatArrayOf(-999f))
                )
            }
            dataCollectingService.incrementLookElementsAround(psiFile, editor, mousePoint)
            indicator.text = dataCollectingService.stats()
            indicator.text2 =
                "mouse: ${mousePoint.x},${mousePoint.y} relative: ${relativePoint.x},${relativePoint.y} \n" +
                        "${logicalPosition.line}:${logicalPosition.column} ${element?.text} ${element?.containingFile?.virtualFile?.name}"

        }
    }

    private companion object Listener : EditorFactoryListener, EditorMouseListener, EditorMouseMotionListener {
        var editorMouseEvent: EditorMouseEvent? = null

        override fun editorCreated(event: EditorFactoryEvent) {
            super.editorCreated(event)
            event.editor.addEditorMouseMotionListener(this)
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            super.editorReleased(event)
            event.editor.removeEditorMouseMotionListener(this)
        }

        override fun mouseExited(event: EditorMouseEvent) {
            super.mouseExited(event)
            editorMouseEvent = null
        }

        override fun mouseMoved(e: EditorMouseEvent) {
            super.mouseMoved(e)
            editorMouseEvent = e
        }
    }
}