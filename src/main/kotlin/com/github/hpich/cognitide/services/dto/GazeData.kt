package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import java.awt.Point

@Serializable(with = GazeDataSerializer::class)
data class GazeData(
    val leftEyeX: Int,
    val leftEyeY: Int,
    val rightEyeX: Int,
    val rightEyeY: Int,
    val leftPupil: Double,
    val rightPupil: Double,
) {
    constructor(leftEye: Point, rightEye: Point, leftPupil: Double, rightPupil: Double) : this(
        leftEye.x,
        leftEye.y,
        rightEye.x,
        rightEye.y,
        leftPupil,
        rightPupil,
    )

    val eyeCenter: Point
        get() =
            Point(
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

@Serializer(forClass = GazeData::class)
object GazeDataSerializer : KSerializer<GazeData> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GazeData", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: GazeData,
    ) {
        var uniqueRepresentation = "null"
        if (value != null) {
            uniqueRepresentation =
                "${value.leftEyeX},;|${value.leftEyeY},;|${value.rightEyeX},;|${value.rightEyeY},;|${value.leftPupil},;|${value.rightPupil}"
        } else {
            uniqueRepresentation = "null"
        }
        encoder.encodeString(uniqueRepresentation)
    }
}
