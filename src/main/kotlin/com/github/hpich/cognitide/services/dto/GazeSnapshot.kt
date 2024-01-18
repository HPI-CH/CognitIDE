package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

val json = Json {
    allowSpecialFloatingPointValues = true
}

@Serializable(with = GazeSnapshotSerializer::class)
data class GazeSnapshot(
    val epochMillis: Long,
    val lookElement: LookElement?,
    val rawGazeData: GazeData?,
    var otherLSLData: FloatArray?
)

@Serializer(forClass = GazeSnapshot::class)
object GazeSnapshotSerializer : KSerializer<GazeSnapshot> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GazeSnapshot", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: GazeSnapshot) {
        var uniqueRepresentation = "null"
        if(value != null){
            uniqueRepresentation = "${value.epochMillis}|;," +
                "${json.encodeToString(LookElement.serializer(), value.lookElement ?: LookElement("null", "null", 0))}|;," +
                "${json.encodeToString(GazeData.serializer(), value.rawGazeData ?: GazeData(-999,-999,-999,-999,-999.0,-999.0))}|;," +
                "${json.encodeToString(FloatArraySerializer(), value.otherLSLData ?: FloatArray(12) { 1f })}|;," +
                "${json.encodeToString(FloatArraySerializer(), value.otherLSLData ?: FloatArray(12) { 1f })}"
        }
        encoder.encodeString(uniqueRepresentation)
    }
}