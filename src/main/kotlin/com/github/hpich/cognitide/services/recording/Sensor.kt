package com.github.hpich.cognitide.services.recording

import com.github.hpich.cognitide.services.StudyRecorder
import com.github.hpich.cognitide.services.dto.SensorSample
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

abstract class Sensor(recorder: StudyRecorder) : Disposable {
    init {
        Disposer.register(recorder, this)
    }

    /**
     * Get all new samples from sensor.
     * @return List of all new samples. Can be empty if no new samples available.
     */
    abstract fun getNewSamples(): List<SensorSample>

    /**
     * Check if sensor should be used based on CognitIDE Settings.
     */
    abstract fun shouldBeUsed(): Boolean
}
