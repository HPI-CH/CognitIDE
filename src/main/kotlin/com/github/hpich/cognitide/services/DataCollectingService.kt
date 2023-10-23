package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.config.HighlightingState
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.extensions.removeAllHighlighters
import com.github.hpich.cognitide.extensions.xyScreenToLogical
import com.github.hpich.cognitide.services.dto.*
import com.github.hpich.cognitide.services.dto.emotiv.EmotivPerformanceData
import com.github.hpich.cognitide.services.recording.InterruptService
import com.github.hpich.cognitide.services.recording.UserInterrupt
import com.github.hpich.cognitide.utils.errorMatrix
import com.github.hpich.cognitide.utils.highlightLookElements
import com.github.hpich.cognitide.services.dto.LookElement
import com.github.hpich.cognitide.utils.saveRecordingToDisk
import com.github.hpich.cognitide.utils.saveTmpFiles
import com.github.hpich.cognitide.utils.script.runScript
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Point
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.roundToInt

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
        saveTmpFiles(lookElementGazeMap, gazeSnapshotList)
    }

    private val gazeSnapshotList = mutableListOf<GazeSnapshot>()
    private val lookElementGazeMap = mutableMapOf<LookElement, Double>()
    private val userInterruptList = mutableListOf<UserInterrupt>()

    fun stats() = "interrupts: $userInterruptCount/${CognitIDESettingsState.instance.interruptCount} " +
            "received: ${gazeSnapshotList.size} elements: ${lookElementGazeMap.size}"

    fun addGazeSnapshot(lookElement: LookElement?, gazeData: GazeData?, shimmerData: ShimmerData?, emotivPerformanceData: EmotivPerformanceData?) {
        GazeSnapshot(System.currentTimeMillis(), lookElement, gazeData, shimmerData, emotivPerformanceData).let {
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
        val horizontalSpread = ParticipantState.instance.horizontalSpread
        val verticalSpread = ParticipantState.instance.verticalSpread
        errorMatrix.forEachIndexed { i, rows ->
            rows.forEachIndexed { j, error ->
                val addHorizontalSpread = (((i / errorMatrix.size.toDouble()) - 0.5) * horizontalSpread).roundToInt()
                val addVerticalSpread = (((j / errorMatrix.size.toDouble()) - 0.5) * verticalSpread).roundToInt()
                errorPos.move(
                    eyeCenter.x + addHorizontalSpread,
                    eyeCenter.y + addVerticalSpread
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

        currentRecorder?.stopRecording()
        currentRecorder = null
        interruptService.stopInterrupting()
        interruptService = InterruptService(project, this)

        gazeSnapshotList.clear()
        lookElementGazeMap.clear()
        userInterruptList.clear()
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
        val settingsState = CognitIDESettingsState.instance
        val saveFolder = File(settingsState.recordingsSaveLocation, "tmp")
        val pluginClassLoader = this.javaClass.getClassLoader()
        val saveFolderPath = saveFolder.path
        val highlightingState = HighlightingState.instance

        runScript(arrayOf(highlightingState.highlightingScript, saveFolderPath.toString()), pluginClassLoader)

            EditorFactory.getInstance().allEditors.forEach {
                highlightLookElements(it, project, lookElementGazeMap, pluginClassLoader)
            }

    }

    fun getRecordedFiles() = lookElementGazeMap.keys.map { it.filePath }.distinct()
}

