package com.github.diekautz.ideplugin.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class LookElement(
    val text: String,
    val filePath: String,
    val startOffset: Int
) {
    val endOffset: Int
        get() = startOffset + text.length
}
