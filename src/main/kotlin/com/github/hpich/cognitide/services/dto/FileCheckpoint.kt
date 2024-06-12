package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

/**
 * Checkpoint of a file containing the text as well as a mapping of all looked at PsiElements to their offset
 * inside the file.
 */
@Serializable
data class FileCheckpoint(
    var text: String,
    var elementOffsets: MutableMap<Int, Pair<Int, Int>>,
)
