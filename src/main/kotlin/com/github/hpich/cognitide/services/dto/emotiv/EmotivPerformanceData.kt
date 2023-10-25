package com.github.hpich.cognitide.services.dto.emotiv

import com.github.hpich.cognitide.services.dto.GazeDataSerializer
import com.github.hpich.cognitide.services.dto.LookElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Point

@Serializable(with = EmotivPerformanceDataSerializer::class)
data class EmotivPerformanceData(
    val value: Double,
    val attention: Double,
    val engagement: Double,
    val excitement: Double,
    val interest: Double,
    val relaxation: Double,
    val stress: Double
)

@Serializer(forClass = EmotivPerformanceData::class)
object EmotivPerformanceDataSerializer : KSerializer<EmotivPerformanceData> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EmotivPerformanceData", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EmotivPerformanceData) {
        val uniqueRepresentation = "${value.value},;|${value.attention},;|${value.engagement},;|${value.excitement},;|${value.interest},;|${value.relaxation},;|${value.stress}"
        encoder.encodeString(uniqueRepresentation)
    }
}