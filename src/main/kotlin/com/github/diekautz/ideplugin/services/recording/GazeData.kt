package com.github.diekautz.ideplugin.services.recording

import kotlinx.serialization.Serializable
import java.awt.Point

@Serializable
data class GazeData(
    val leftEyeX: Int,
    val leftEyeY: Int,
    val rightEyeX: Int,
    val rightEyeY: Int,
    val leftPupil: Double,
    val rightPupil: Double
) {
    constructor(leftEye: Point, rightEye: Point, leftPupil: Double, rightPupil: Double) : this(
        leftEye.x,
        leftEye.y,
        rightEye.x,
        rightEye.y,
        leftPupil,
        rightPupil
    )

    fun correctMissingOne(): GazeData? {
        if (leftPupil.isNaN() && rightPupil.isNaN()) {
            return null
        }
        if (leftPupil.isNaN()) {
            return GazeData(rightEyeX, rightEyeY, rightEyeX, rightEyeY, leftPupil, rightPupil)
        }
        if (rightPupil.isNaN()) {
            return GazeData(leftEyeX, leftEyeY, leftEyeX, leftEyeY, leftPupil, rightPupil)
        }
        return this
    }
}
