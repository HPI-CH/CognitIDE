package com.github.diekautz.ideplugin.services.recording

import kotlinx.serialization.Serializable

@Serializable
data class UserInterrupt(
    val epochMillisStart: Long,
    val epochMillisEnd: Long,
    val answer: String = ""
)
