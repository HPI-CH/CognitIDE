package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.ui.OpenEyeColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project

private fun highlightElements(editor: Editor, project: Project, index: Int, elements: List<LookElement>) {
    val editorVirtualFile = FileDocumentManager.getInstance().getFile(editor.document)
    val highlightManager = HighlightManager.getInstance(project)
    elements
        .filter {
            it.filePath == editorVirtualFile?.path
        }.forEach { lookElement ->
            highlightManager.addOccurrenceHighlight(
                editor,
                lookElement.startOffset,
                lookElement.endOffset,
                OpenEyeColors.LOOKED_ATTRIBUTES[index],
                HighlightManager.HIDE_BY_TEXT_CHANGE or HighlightManager.HIDE_BY_ESCAPE,
                null
            )
        }
}

fun highlightLookElements(editor: Editor, project: Project, lookElementGazeMap: Map<LookElement, Double>) {
    val assignedColors = OpenEyeColors.assignColors(lookElementGazeMap)

    assignedColors.forEach { (colorIndex, entries) ->
        highlightElements(
            editor,
            project,
            colorIndex,
            entries
        )
    }
}