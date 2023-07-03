package com.github.diekautz.ideplugin.services.recording

import kotlinx.serialization.Serializable

@Serializable
data class EditorGazeSnapshot(
    val epochMillis: Long,
    val filePath: String,
    val elementStartOffset: Int
)
