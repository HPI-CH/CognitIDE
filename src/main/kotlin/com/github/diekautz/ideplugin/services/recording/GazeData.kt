package com.github.diekautz.ideplugin.services.recording

import java.awt.Point

data class GazeData(
    val leftEye: Point,
    val rightEye: Point,
    val leftPupil: Double,
    val rightPupil: Double
)
