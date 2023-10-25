package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Point

@Serializable(with = ShimmerDataSerializer::class)
data class ShimmerData(
    val LOW_NOISE_ACCELEROMETER_X: Double,
    val LOW_NOISE_ACCELEROMETER_Y: Double,
    val LOW_NOISE_ACCELEROMETER_Z: Double,
    val WIDE_RANGE_ACCELEROMETER_X: Double,
    val WIDE_RANGE_ACCELEROMETER_Y: Double,
    val WIDE_RANGE_ACCELEROMETER_Z: Double,
    val MAGNETOMETER_X: Double,
    val MAGNETOMETER_Y: Double,
    val MAGNETOMETER_Z: Double,
    val GYROSCOPE_X: Double,
    val GYROSCOPE_Y: Double,
    val GYROSCOPE_Z: Double,
    val GSR: Double,
    val GSR_CONDUCTANCE: Double,
    val INTERNAL_ADC_A13: Double,
    val PRESSURE: Double,
    val TEMPERATURE: Double
)

@Serializer(forClass = ShimmerData::class)
object ShimmerDataSerializer : KSerializer<ShimmerData> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ShimmerData", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ShimmerData) {
        val uniqueRepresentation = "${value.LOW_NOISE_ACCELEROMETER_X},;|" +
                "${value.LOW_NOISE_ACCELEROMETER_Y},;|" +
                "${value.LOW_NOISE_ACCELEROMETER_Z},;|" +
                "${value.WIDE_RANGE_ACCELEROMETER_X},;|" +
                "${value.WIDE_RANGE_ACCELEROMETER_Y},;|" +
                "${value.WIDE_RANGE_ACCELEROMETER_Z},;|" +
                "${value.MAGNETOMETER_X},;|" +
                "${value.MAGNETOMETER_Y},;|" +
                "${value.MAGNETOMETER_Z},;|" +
                "${value.GYROSCOPE_X},;|" +
                "${value.GYROSCOPE_Y},;|" +
                "${value.GYROSCOPE_Z},;|" +
                "${value.GSR},;|" +
                "${value.GSR_CONDUCTANCE},;|" +
                "${value.INTERNAL_ADC_A13},;|" +
                "${value.PRESSURE},;|" +
                "${value.TEMPERATURE}"


        encoder.encodeString(uniqueRepresentation)
    }
}
