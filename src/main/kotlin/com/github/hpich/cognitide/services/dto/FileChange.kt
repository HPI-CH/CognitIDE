package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

/**
 * A file change containing a timestamp, as well as a position inside the file, the previous text and the new text.
 */
@Serializable
data class FileChange(val time: Double, val offset: Int, val oldText: String, val newText: String) : Comparable<FileChange> {
    /**
     * Comparator to enable easy sorting by timestamp.
     */
    override fun compareTo(other: FileChange) = compareValuesBy(this, other, FileChange::time)
}
