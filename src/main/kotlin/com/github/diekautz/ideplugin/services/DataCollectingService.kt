package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.github.diekautz.ideplugin.extensions.removeAllHighlighters
import com.github.diekautz.ideplugin.extensions.xyScreenToLogical
import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.GazeSnapshot
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.services.dto.LookElementGaze
import com.github.diekautz.ideplugin.services.recording.InterruptService
import com.github.diekautz.ideplugin.services.recording.UserInterrupt
import com.github.diekautz.ideplugin.utils.errorMatrix
import com.github.diekautz.ideplugin.utils.highlightLookElements
import com.github.diekautz.ideplugin.utils.saveRecordingToDisk
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import java.awt.Point
import java.time.Instant
import java.util.*

@Service(Service.Level.PROJECT)
class DataCollectingService(val project: Project) {
    private var currentRecorder: StudyRecorder? = null
    private var interruptService = InterruptService(project, this)

    val isRecording: Boolean
        get() = currentRecorder?.isRunning ?: false

    var wasHighlighted = false
    val isHighlightAvailable: Boolean
        get() = lookElementGazeMap.isNotEmpty()

    val isAnyDataAvailable: Boolean
        get() = gazeSnapshotList.isNotEmpty() || lookElementGazeMap.isNotEmpty()

    val userInterruptCount: Int
        get() = userInterruptList.size

    fun setRecorder(recorder: StudyRecorder) {
        if (isRecording) return
        currentRecorder = recorder
    }

    fun startRecording() {
        if (isRecording) return
        if (currentRecorder == null) {
            thisLogger().error("No recorder selected!")
            return
        }

        currentRecorder!!.startRecording()
        interruptService.startInterrupting()
    }

    fun stopRecording() {
        currentRecorder?.stopRecording()
        interruptService.stopInterrupting()
    }

    private val gazeSnapshotList = mutableListOf<GazeSnapshot>()
    private val lookElementGazeMap = mutableMapOf<LookElement, Double>()
    private val userInterruptList = mutableListOf<UserInterrupt>()

    fun stats() = "interrupts: $userInterruptCount/${OpenEyeSettingsState.instance.interruptCount} " +
            "received: ${gazeSnapshotList.size} elements: ${lookElementGazeMap.size}"

    fun addGazeSnapshot(lookElement: LookElement, gazeData: GazeData) {
        GazeSnapshot(System.currentTimeMillis(), lookElement, gazeData).let {
            gazeSnapshotList.add(it)
            thisLogger().debug("GazeSnapshot added: $it")
        }
    }

    private fun incrementLookElement(lookElement: LookElement, increment: Double) {
        val value = lookElementGazeMap.getOrDefault(lookElement, 0.0)
        lookElementGazeMap[lookElement] = value + increment
    }

    fun incrementLookElementsAround(psiFile: PsiFile, editor: Editor, eyeCenter: Point) {
        // distribute look onto surrounding elements evenly
        val errorPos = Point(eyeCenter)
        errorMatrix.forEachIndexed { i, rows ->
            rows.forEachIndexed { j, error ->
                errorPos.move(
                    eyeCenter.x + (i - errorMatrix.size / 2) * 2,
                    eyeCenter.y + (j - errorMatrix.size / 2)
                )

                val logicalPosition = editor.xyScreenToLogical(errorPos)
                val offset = editor.logicalPositionToOffset(logicalPosition)
                val element = psiFile.findElementAt(offset)
                if (element != null && element !is PsiWhiteSpace) {
                    incrementLookElement(
                        LookElement(
                            element.text,
                            element.containingFile.virtualFile.path,
                            element.startOffset
                        ), error
                    )
                }
            }
        }
    }

    fun addUserInterrupt(startMillis: Long, endMillis: Long, answer: String = "") {
        UserInterrupt(startMillis, endMillis, answer).let {
            userInterruptList.add(it)
            thisLogger().debug("UserInterrupt added: $it")
        }
    }

    fun clearData() {
        EditorFactory.getInstance().removeAllHighlighters()
        wasHighlighted = false

        gazeSnapshotList.clear()
        lookElementGazeMap.clear()
        userInterruptList.clear()

        currentRecorder?.stopRecording()
        currentRecorder = null
        interruptService.stopInterrupting()
        interruptService = InterruptService(project, this)
    }

    fun saveToDisk() {
        saveRecordingToDisk(
            project,
            Date.from(Instant.now()),
            lookElementGazeMap.map { LookElementGaze(it.key, it.value) },
            gazeSnapshotList,
            userInterruptList
        )
    }

    fun highlightGazedElements() = invokeLater {
        wasHighlighted = true
        EditorFactory.getInstance().allEditors.forEach {
            highlightLookElements(it, project, lookElementGazeMap)
        }
    }

    fun getRecordedFiles() = lookElementGazeMap.keys.map { it.filePath }
}