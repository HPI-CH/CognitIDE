package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.ui.MyColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.awt.Point
import javax.swing.SwingUtilities

fun screenToLogicalInEditor(textEditor: Editor, point: Point): LogicalPosition {
    val relativePoint = Point(point)
    SwingUtilities.convertPointFromScreen(relativePoint, textEditor.contentComponent)
    return textEditor.xyToLogicalPosition(relativePoint)
}

fun highlightElements(index: Int, elements: List<PsiElement>, editor: Editor, project: Project) {
    HighlightManager.getInstance(project).addOccurrenceHighlights(
        editor,
        elements.toTypedArray(),
        MyColors.LOOKED_ATTRIBUTES[index],
        true,
        null
    )
}

fun highlightSeenElements(seen: Map<PsiElement, Int>, editor: Editor, project: Project) {
    val groupedSeen = seen.toList().groupBy { entry ->
        val index = MyColors.boarders.indexOfFirst { entry.second < it }
        if (index >= 0) index else MyColors.boarders.lastIndex
    }
    groupedSeen.forEach { (colorIndex, entries) ->
        highlightElements(
            colorIndex,
            entries.map { it.first },
            editor,
            project
        )
    }
}