package com.github.diekautz.ideplugin.services.recording

data class GazeSnapshot(
    val rawGazeData: GazeData,
    val editorGazeSnapshot: EditorGazeSnapshot,
)
