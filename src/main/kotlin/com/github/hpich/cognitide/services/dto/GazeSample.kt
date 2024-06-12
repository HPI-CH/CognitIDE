package com.github.hpich.cognitide.services.dto

import kotlinx.serialization.Serializable

/**
 * A gaze sample containing a timestamp, as well as the weight to the PsiElement to which this sample belongs.
 */
@Serializable
data class GazeSample(val time: Double, val weight: Double) : Comparable<GazeSample> {
    /**
     * Comparator to enable easy sorting by timestamp.
     */
    override fun compareTo(other: GazeSample) = compareValuesBy(this, other, GazeSample::time)
}
