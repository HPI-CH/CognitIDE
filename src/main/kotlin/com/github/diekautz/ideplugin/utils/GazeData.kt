package com.github.diekautz.ideplugin.utils

import java.awt.Point

data class GazeData(
    val leftEye: Point,
    val rightEye: Point,
    val leftPupil: Double,
    val rightPupil: Double
)
