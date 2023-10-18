package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.ui.CognitIDEColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project

private val logger = Logger.getInstance("com.github.diekautz.ideplugin.utils.HighlightUtilities")

private fun highlightElements(editor: Editor, project: Project, index: Int, elements: List<LookElement>) {
    val editorVirtualFile = FileDocumentManager.getInstance().getFile(editor.document)
    val highlightManager = HighlightManager.getInstance(project)
    elements
        .filter {
            it.filePath == editorVirtualFile?.path
        }.forEach { lookElement ->
            logger.debug("Highlighting $index: $lookElement")
            highlightManager.addOccurrenceHighlight(
                editor,
                lookElement.startOffset,
                lookElement.endOffset,
                CognitIDEColors.LOOKED_ATTRIBUTES[index],
                HighlightManager.HIDE_BY_TEXT_CHANGE or HighlightManager.HIDE_BY_ESCAPE,
                null
            )
        }
}

fun highlightLookElements(editor: Editor, project: Project, lookElementGazeMap: Map<LookElement, Double>) {
    val assignedColors = CognitIDEColors.assignColors(lookElementGazeMap)

    assignedColors.forEach { (colorIndex, entries) ->
        highlightElements(
            editor,
            project,
            colorIndex,
            entries
        )
    }
}