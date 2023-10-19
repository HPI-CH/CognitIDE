package com.github.diekautz.ideplugin.services.dto.emotiv

import kotlinx.serialization.Serializable
import java.awt.Point

@Serializable
data class EmotivPerformanceData(
    val idk: Double,
    val attention: Double,
    val engagement: Double,
    val excitement: Double,
    val interest: Double,
    val relaxation: Double,
    val stress: Double
) {
    /*constructor(
        LOW_NOISE_ACCELEROMETER_X: Double,
        LOW_NOISE_ACCELEROMETER_Y: Double,
        LOW_NOISE_ACCELEROMETER_Z: Double,
        WIDE_RANGE_ACCELEROMETER_X: Double,
        WIDE_RANGE_ACCELEROMETER_Y: Double,
        WIDE_RANGE_ACCELEROMETER_Z: Double,
        MAGNETOMETER_X: Double,
        MAGNETOMETER_Y: Double,
        MAGNETOMETER_Z: Double,
        GYROSCOPE_X: Double,
        GYROSCOPE_Y: Double,
        GYROSCOPE_Z: Double,
        GSR: Double,
        GSR_CONDUCTANCE: Double,
        INTERNAL_ADC_A13: Double,
        PRESSURE: Double,
        TEMPERATURE: Double,
        V_SENSE_BATT: Double
    ) : this(
        LOW_NOISE_ACCELEROMETER_X,
        LOW_NOISE_ACCELEROMETER_Y,
        LOW_NOISE_ACCELEROMETER_Z,
        WIDE_RANGE_ACCELEROMETER_X,
        WIDE_RANGE_ACCELEROMETER_Y,
        WIDE_RANGE_ACCELEROMETER_Z,
        MAGNETOMETER_X,
        MAGNETOMETER_Y,
        MAGNETOMETER_Z,
        GYROSCOPE_X,
        GYROSCOPE_Y,
        GYROSCOPE_Z,
        GSR,
        GSR_CONDUCTANCE,
        INTERNAL_ADC_A13,
        PRESSURE,
        TEMPERATURE,
        V_SENSE_BATT
    )*/
}
