package com.github.diekautz.ideplugin.services.dto

import com.github.diekautz.ideplugin.services.dto.emotiv.EmotivPerformanceData
import kotlinx.serialization.Serializable

@Serializable
data class GazeSnapshot(
    val epochMillis: Long,
    val lookElement: LookElement?,
    val rawGazeData: GazeData?,
    val rawShimmerData: ShimmerData?,
    val emotivPerformanceData: EmotivPerformanceData?
)
