package com.github.hpich.cognitide.services.recording

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.services.StudyRecorder
import com.github.hpich.cognitide.services.dto.SensorSample
import edu.ucsd.sccn.LSL
import edu.ucsd.sccn.LSL.StreamInfo
import edu.ucsd.sccn.LSL.StreamInlet

/**
 * A Sensor connected via LSL.
 * @param info StreamInfo object for the stream of this sensor.
 * @param recorder StudyRecorder using this sensor. This is used as parent in the Disposers hierarchy.
 */
class LSLSensor(private val info: StreamInfo, private val recorder: StudyRecorder) : Sensor(recorder) {
    private val inlet = StreamInlet(info)
    private val buffer = FloatArray(info.channel_count())

    // ID of sensor, determined by order of LSL streams in CognitIDE settings or -1 if not listed there.
    private val id: Int
        get() = CognitIDESettingsState.instance.devices.map { it.name }.indexOf(info.name())

    val name: String
        get() = info.name()

    /**
     * Get all new samples from sensor.
     * @return List of all new samples. Can be empty if no new samples available.
     */
    override fun getNewSamples(): List<SensorSample> {
        val result = ArrayList<SensorSample>()
        do {
            val time = inlet.pull_sample(buffer, 0.0)
            if (time > 0.0) {
                result.add(SensorSample(time + inlet.time_correction(), buffer.clone()))
            }
        } while (time > 0.0)

        return result
    }

    /**
     * Check if sensor is mentioned in the CognitIDE settings and has the correct format and channel count.
     * @return Boolean marking if sensor is in use.
     */
    override fun shouldBeUsed(): Boolean {
        if (id == -1) return false

        val correctFormat = info.channel_format() == LSL.ChannelFormat.float32
        return correctFormat
    }

    override fun dispose() {
        inlet.close_stream()
    }
}
