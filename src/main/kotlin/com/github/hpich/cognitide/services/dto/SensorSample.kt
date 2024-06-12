package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

/**
 * A sensor sample containing a timestamp, as well as a FloatArray with values for all sensor channels.
 */
@Serializable
data class SensorSample(val time: Double, val values: FloatArray) : Comparable<SensorSample> {
    /**
     * Comparator to enable easy sorting by timestamp.
     */
    override fun compareTo(other: SensorSample) = compareValuesBy(this, other, SensorSample::time)
}
