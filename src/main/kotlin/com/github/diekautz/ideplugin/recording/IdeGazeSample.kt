package com.github.diekautz.ideplugin.recording

import kotlinx.datetime.Instant

data class IdeGazeSample(
    val rawGaze: GazeData,
    val viewedFileHash: Long,
    val viewedOffset: Long,
) {
    val timestamp: Instant
        get() = rawGaze.timestamp
}