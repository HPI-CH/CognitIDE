package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.GazeSnapshot
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.services.dto.LookElementGaze
import com.github.diekautz.ideplugin.utils.saveRecordingToDisk
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.time.Instant
import java.util.*

@Service(Service.Level.PROJECT)
class DataCollectingService(val project: Project) {
    private val currentRecordingService: RecordingService? = null

    val isRecording: Boolean
        get() = currentRecordingService?.isRunning ?: false

    fun startRecording() = currentRecordingService?.startRecording()
    fun stopRecording() = currentRecordingService?.stopRecording()

    private val gazeSnapshotList = mutableListOf<GazeSnapshot>()
    private val lookElementGazeMap = mutableMapOf<LookElement, Double>()

    fun addGazeSnapshot(lookElement: LookElement, gazeData: GazeData) {
        gazeSnapshotList.add(GazeSnapshot(System.currentTimeMillis(), lookElement, gazeData))
    }

    fun incrementLookElement(lookElement: LookElement, increment: Double) {
        val value = lookElementGazeMap.getOrDefault(lookElement, 0.0)
        lookElementGazeMap[lookElement] = value + increment
    }

    fun clearData() {
        gazeSnapshotList.clear()
        lookElementGazeMap.clear()
    }

    fun saveToDisk() {
        saveRecordingToDisk(
            project,
            Date.from(Instant.now()),
            lookElementGazeMap.map { LookElementGaze(it.key, it.value) },
            gazeSnapshotList
        )
    }
}