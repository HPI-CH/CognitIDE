package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.ui.MyColors
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import kotlinx.coroutines.*
import java.awt.MouseInfo
import java.awt.Point
import java.util.concurrent.ConcurrentHashMap
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class MyMousePositionService(val project: Project) {

    private var refreshJob: Job? = null

    fun trackMouse() {
        thisLogger().debug("Track mouse started!")

        ProgressManager.getInstance().run(task)
        task.shouldRun = true
        refreshJob?.start()
    }

    fun stopTrackMouse() {
        task.shouldRun = false
    }

    val seen = ConcurrentHashMap<PsiElement, Int>()

    fun visualizeSeen() {
        invokeLater {
            FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
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
        }
    }

    private val task = object : Task.Backgroundable(project, "Recording Mouse", true) {
        var shouldRun = true

        override fun run(indicator: ProgressIndicator) {
            runBlocking {
                indicator.isIndeterminate = true
                while (shouldRun) {
                    if (indicator.isCanceled) return@runBlocking
                    val mousePoint = MouseInfo.getPointerInfo().location

                    invokeLater {
                        FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->

                            val relativePoint = Point(mousePoint)
                            SwingUtilities.convertPointFromScreen(relativePoint, editor.contentComponent)
                            if (!editor.contentComponent.contains(relativePoint)) return@let
                            val logicalPosition = editor.xyToLogicalPosition(relativePoint)
                            indicator.text = "${logicalPosition.line}:${logicalPosition.column}"

                            val offset = editor.logicalPositionToOffset(logicalPosition)
                            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                            val element = psiFile?.findElementAt(offset)
                            if (element != null && element !is PsiWhiteSpace) {
                                val value = seen.getOrDefault(element, 0)
                                seen[element] = value + 1
                                thisLogger().info("Have seen element ${element.node} ${seen[element]}")
                            }
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    private fun highlightElements(index: Int, elements: List<PsiElement>, editor: Editor, project: Project) {
        HighlightManager.getInstance(project).addOccurrenceHighlights(
            editor,
            elements.toTypedArray(),
            MyColors.LOOKED_ATTRIBUTES[index],
            true,
            null
        )
    }

}