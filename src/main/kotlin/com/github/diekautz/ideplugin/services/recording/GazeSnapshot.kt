package com.github.diekautz.ideplugin.services.recording

import kotlinx.serialization.Serializable

@Serializable
data class GazeSnapshot(
    val rawGazeData: GazeData,
    val editorGazeSnapshot: EditorGazeSnapshot,
)
