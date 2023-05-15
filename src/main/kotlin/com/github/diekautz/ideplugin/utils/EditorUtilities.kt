package com.github.diekautz.ideplugin.utils

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import java.awt.Point
import javax.swing.SwingUtilities

fun screenToLogicalInEditor(textEditor: Editor, point: Point): LogicalPosition {
    val relativePoint = Point(point)
    SwingUtilities.convertPointFromScreen(relativePoint, textEditor.contentComponent)
    return textEditor.xyToLogicalPosition(relativePoint)
}