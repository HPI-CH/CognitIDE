package com.github.diekautz.ideplugin.extensions

import com.github.diekautz.ideplugin.ui.OpenEyeColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.ui.ImageUtil
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities

fun Editor.xyScreenToLogical(point: Point): LogicalPosition {
    val relativePoint = Point(point)
    SwingUtilities.convertPointFromScreen(relativePoint, contentComponent)
    return xyToLogicalPosition(relativePoint)
}

fun Editor.highlightElements(index: Int, elements: List<PsiElement>, project: Project) {
    val editorPsiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
    val filteredElement = elements.filter {
        it.containingFile == editorPsiFile
    }.toTypedArray()

    HighlightManager.getInstance(project).addOccurrenceHighlights(
        this,
        filteredElement,
        OpenEyeColors.LOOKED_ATTRIBUTES[index],
        true,
        null
    )
}

fun Editor.highlightElementGazePoints(seen: Map<PsiElement, Double>, project: Project) {
    val assignedColors = OpenEyeColors.assignColors(seen)

    assignedColors.forEach { (colorIndex, entries) ->
        highlightElements(
            colorIndex,
            entries,
            project
        )
    }
}

fun FileEditor.screenshot(): BufferedImage {
    val bufferedImage = ImageUtil.createImage(component.height, component.width, BufferedImage.TYPE_INT_RGB)
    component.paintAll(bufferedImage.graphics)
    return bufferedImage
}

fun EditorFactory.removeAllHighlighters() {
    allEditors.forEach {
        it.markupModel.removeAllHighlighters()
    }
}