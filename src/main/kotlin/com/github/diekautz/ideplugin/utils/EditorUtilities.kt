package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.ui.MyColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.awt.Point
import javax.swing.SwingUtilities

fun Editor.xyScreenToLogical(point: Point): LogicalPosition {
    val relativePoint = Point(point)
    SwingUtilities.convertPointFromScreen(relativePoint, contentComponent)
    return xyToLogicalPosition(relativePoint)
}

fun Editor.highlightElements(index: Int, elements: List<PsiElement>, project: Project) {
    HighlightManager.getInstance(project).addOccurrenceHighlights(
        this,
        elements.toTypedArray(),
        MyColors.LOOKED_ATTRIBUTES[index],
        true,
        null
    )
}

fun Editor.highlightElementGazePoints(seen: Map<PsiElement, Double>, project: Project) {
    val assignedColors = MyColors.assignColors(seen)

    assignedColors.forEach { (colorIndex, entries) ->
        highlightElements(
            colorIndex,
            entries,
            project
        )
    }
}