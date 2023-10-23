package com.github.diekautz.ideplugin.services.dto.emotiv

import kotlinx.serialization.Serializable
import java.awt.Point

@Serializable
data class EmotivPerformanceData(
    val value: Double,
    val attention: Double,
    val engagement: Double,
    val excitement: Double,
    val interest: Double,
    val relaxation: Double,
    val stress: Double
)
