package com.github.diekautz.ideplugin.services.debug

import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.recording.InterruptService
import com.github.diekautz.ideplugin.services.recording.LookRecorderService
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import kotlinx.coroutines.*
import java.awt.MouseInfo
import java.awt.Point
import java.time.Instant
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class MousePositionService(val project: Project) {
    private val lookRecorderService = project.service<LookRecorderService>()
    private val interruptService = project.service<InterruptService>()
    private var refreshJob: Job? = null

    fun trackMouse() {
        thisLogger().debug("Track mouse started!")

        task.shouldRun = true
        ProgressManager.getInstance().run(task)
        interruptService.startInterrupting()
        refreshJob?.start()
    }

    fun stopTrackMouse() {
        task.shouldRun = false
        interruptService.stopInterrupting()
    }

    private val task = object : Task.Backgroundable(project, "Recording mouse", true) {
        var shouldRun = true

        var gazeSnapshotN = 0
        var elementGazeN = 0

        override fun run(indicator: ProgressIndicator) {
            runBlocking {
                indicator.isIndeterminate = true
                while (shouldRun) {
                    if (indicator.isCanceled) return@runBlocking
                    val mousePoint = MouseInfo.getPointerInfo().location

                    invokeLater {
                        var relativePoint = Point(mousePoint)
                        val editor = EditorFactory.getInstance().allEditors.firstOrNull {
                            relativePoint = Point(mousePoint)
                            SwingUtilities.convertPointFromScreen(relativePoint, it.contentComponent)
                            it.contentComponent.contains(relativePoint)
                        } ?: return@invokeLater

                        val logicalPosition = editor.xyToLogicalPosition(relativePoint)
                        indicator.text2 = "[debug] mouse ${logicalPosition.line}:${logicalPosition.column}"

                        val offset = editor.logicalPositionToOffset(logicalPosition)
                        val psiFile =
                            PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return@invokeLater
                        val element = psiFile.findElementAt(offset)

                        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)

                        val fakeData = GazeData(mousePoint, mousePoint, 1.0, 1.0)
                        if (virtualFile != null && element != null && element !is PsiWhiteSpace) {
                            gazeSnapshotN = lookRecorderService.addGazeSnapshot(
                                Instant.now().toEpochMilli(),
                                virtualFile,
                                element,
                                fakeData
                            )
                        }
                        elementGazeN = lookRecorderService.addAreaGaze(psiFile, editor, fakeData)
                        indicator.text = "rawGaze: $gazeSnapshotN elements: $elementGazeN"
                    }
                    delay(100)
                }
            }
        }
    }

}