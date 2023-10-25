package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class LookElementGaze(
    val lookElement: LookElement?,
    val gazeWeight: Double,
)
