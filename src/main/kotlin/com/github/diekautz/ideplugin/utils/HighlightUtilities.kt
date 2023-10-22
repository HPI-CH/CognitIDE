package com.github.diekautz.ideplugin.utils

import com.github.diekautz.ideplugin.config.CognitIDESettingsState
import com.github.diekautz.ideplugin.config.HighlightingState
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.ui.CognitIDEColors
import com.github.diekautz.ideplugin.utils.script.runScript
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import kotlinx.serialization.decodeFromString
import java.io.File

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

fun highlightLookElements(editor: Editor, project: Project, lookElementGazeMap: Map<LookElement, Double>, pluginClassLoader: ClassLoader) {
    val settingsState = CognitIDESettingsState.instance
    val saveFolder = File(settingsState.recordingsSaveLocation, "tmp").path

    val lookElementGazeMapAlteredByUser =
        json.decodeFromString<Map<String, Double>>(File(saveFolder, "lookElementGazeMapAlteredByUser.json").readText(Charsets.UTF_8)).mapKeys{ json.decodeFromString(LookElement.serializer(), it.key) }
    val assignedColors = CognitIDEColors.assignColors(lookElementGazeMapAlteredByUser) //todo replace with main


    assignedColors.forEach { (colorIndex, entries) ->
        highlightElements(
            editor,
            project,
            colorIndex,
            entries
        )
    }
}