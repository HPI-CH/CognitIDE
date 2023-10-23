package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = LookElementSerializer::class)
data class LookElement(
    val text: String,
    val filePath: String,
    val startOffset: Int
) {
    val endOffset: Int
        get() = startOffset + text.length
}

@Serializer(forClass = LookElement::class)
object LookElementSerializer : KSerializer<LookElement> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LookElement", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LookElement) {
        val uniqueRepresentation = "${value.text}|${value.filePath}|${value.startOffset}"
        encoder.encodeString(uniqueRepresentation)
    }

    override fun deserialize(decoder: Decoder): LookElement {
        val parts = decoder.decodeString().split('|')
        return LookElement(
            text = parts[0],
            filePath = parts[1],
            startOffset = parts[2].toInt()
        )
    }
}