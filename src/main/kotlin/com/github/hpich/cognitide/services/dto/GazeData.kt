package com.github.hpich.cognitide.services.dto

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

    val eyeCenter: Point
        get() = Point(
            (leftEyeX + rightEyeX) / 2,
            (leftEyeY + rightEyeY) / 2,
        )

    fun correctMissingEye(): GazeData? {
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
