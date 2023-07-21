package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import java.awt.MouseInfo
import java.awt.Point
import javax.swing.SwingUtilities

class MouseGazeRecorder(project: Project) : StudyRecorder(project, "Recording Mouse") {
    override fun setup(indicator: ProgressIndicator) = true

    override fun loop(indicator: ProgressIndicator) = invokeLater {
        val mousePoint = MouseInfo.getPointerInfo().location
        var relativePoint = Point(mousePoint)
        val editor = EditorFactory.getInstance().allEditors.firstOrNull {
            relativePoint = Point(mousePoint)
            SwingUtilities.convertPointFromScreen(relativePoint, it.contentComponent)
            it.contentComponent.contains(relativePoint)
        } ?: return@invokeLater

        val logicalPosition = editor.xyToLogicalPosition(relativePoint)
        indicator.text2 = "[debug] mouse ${logicalPosition.line}:${logicalPosition.column}"

        val offset = editor.logicalPositionToOffset(logicalPosition)
        val psiFile =
            PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return@invokeLater
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
                fakeData
            )
        }
        dataCollectingService.incrementLookElementsAround(psiFile, editor, mousePoint)
        indicator.text = dataCollectingService.stats()
        indicator.text2 = "mouse: ${mousePoint.x},${mousePoint.y} " +
                "${logicalPosition.line}:${logicalPosition.column} ${element?.text}"

    }


}