package com.github.diekautz.ideplugin.services.recording

data class EditorGazeSnapshot(
    val epochMillis: Long,
    val filePath: String,
    val elementStartOffset: Int
)
