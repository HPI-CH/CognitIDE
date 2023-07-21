package com.github.diekautz.ideplugin.services.recording

import kotlinx.serialization.Serializable

@Serializable
data class GazeSnapshot(
    val epochMillis: Long,
    val filePath: String,
    val elementStartOffset: Int,
    val rawGazeData: GazeData
)
