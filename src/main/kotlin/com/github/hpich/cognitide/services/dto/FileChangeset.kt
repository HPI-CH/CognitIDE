package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

/**
 * A file changeset representing a group of changes that happened in quick succession.
 * Each changeset contains a start and end time (times of earliest and latest change in changeset),
 * as well as a list of all contained changes and the offsets for all looked at elements in the file after the changes.
 * All sensor data recorded during the interval of a changeset cannot be mapped to a code element, since we don't know
 * the positions of elements at that time.
 */
@Serializable
data class FileChangeset(
    val startTime: Double,
    val endTime: Double,
    val changes: MutableList<FileChange>,
    val elementOffsets: MutableMap<Int, Pair<Int, Int>>,
)
