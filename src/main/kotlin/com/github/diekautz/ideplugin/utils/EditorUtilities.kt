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

fun Editor.highlightSeenElements(seen: Map<PsiElement, Int>, project: Project) {
    val groupedSeen = seen.toList().groupBy { entry ->
        val index = MyColors.boarders.indexOfFirst { entry.second < it }
        if (index >= 0) index else MyColors.boarders.lastIndex
    }
    groupedSeen.forEach { (colorIndex, entries) ->
        highlightElements(
            colorIndex,
            entries.map { it.first },
            project
        )
    }
}