package com.github.diekautz.ideplugin.recording

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class MyRecordingService {

    val samples = mutableListOf<IdeGazeSample>()

    var isRecording = false
    val status: String
        get() = if (isRecording) "recording" else "stopped"

    fun newRecording() {
        samples.clear()
        isRecording = false
    }

}