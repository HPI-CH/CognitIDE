package com.github.diekautz.ideplugin.services.recording

data class EditorGazeSnapshot(
    val timeUTCSeconds: Double,
    val filePath: String,
    val elementStartOffset: Int
)
