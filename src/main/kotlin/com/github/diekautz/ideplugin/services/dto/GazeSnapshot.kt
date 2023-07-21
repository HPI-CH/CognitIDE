package com.github.diekautz.ideplugin.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class GazeSnapshot(
    val epochMillis: Long,
    val lookElement: LookElement,
    val rawGazeData: GazeData
)
