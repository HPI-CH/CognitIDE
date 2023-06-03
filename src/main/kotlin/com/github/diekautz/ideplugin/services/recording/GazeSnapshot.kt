package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.utils.GazeData

data class GazeSnapshot(
    val rawGazeData: GazeData,
    val editorGazeSnapshot: EditorGazeSnapshot,
)
