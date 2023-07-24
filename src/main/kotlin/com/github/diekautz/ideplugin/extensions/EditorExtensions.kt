package com.github.diekautz.ideplugin.extensions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.util.ui.ImageUtil
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities

fun Editor.xyScreenToLogical(point: Point): LogicalPosition {
    val relativePoint = Point(point)
    SwingUtilities.convertPointFromScreen(relativePoint, contentComponent)
    return xyToLogicalPosition(relativePoint)
}

fun FileEditor.screenshot(): BufferedImage {
    val bufferedImage = ImageUtil.createImage(component.width, component.height, BufferedImage.TYPE_INT_RGB)
    component.paintAll(bufferedImage.graphics)
    return bufferedImage
}

fun EditorFactory.removeAllHighlighters() {
    allEditors.forEach {
        it.markupModel.removeAllHighlighters()
    }
}