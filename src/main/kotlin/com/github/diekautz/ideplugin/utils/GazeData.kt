package com.github.diekautz.ideplugin.utils

import kotlinx.datetime.Instant
import java.awt.Point

data class GazeData(
    val timestamp: Instant,
    val leftEye: Point,
    val rightEye: Point,
    val leftPupil: Double,
    val rightPupil: Double
)
