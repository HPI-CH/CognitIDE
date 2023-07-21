package com.github.diekautz.ideplugin.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class LookElementGaze(
    val lookElement: LookElement,
    val gazeWeight: Double,
)
