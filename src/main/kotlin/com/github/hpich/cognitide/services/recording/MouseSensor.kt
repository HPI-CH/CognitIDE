package com.github.hpich.cognitide.services.recording

import com.github.hpich.cognitide.services.StudyRecorder
import com.github.hpich.cognitide.services.dto.SensorSample
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.*
import com.intellij.util.containers.addIfNotNull
import edu.ucsd.sccn.LSL
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit

/**
 * A sensor to collect mouse data.
 * Mouse coordinates will be in range (0.0 - 1.0) like the eye tracker data.
 * @param recorder StudyRecorder using this sensor. This is used as parent in the Disposers hierarchy.
 * @param delayBetweenSamples Control how often the MouseSensor provides new samples
 */
class MouseSensor(recorder: StudyRecorder, private val delayBetweenSamples: Double = 0.2) : Sensor(recorder) {
    private var nextSampleTime = LSL.local_clock() + delayBetweenSamples
    private val screenDimensions: Dimension = Toolkit.getDefaultToolkit().screenSize

    init {
        EditorFactory.getInstance().allEditors.forEach {
            it.addEditorMouseMotionListener(MouseSensor)
        }
        EditorFactory.getInstance().addEditorFactoryListener(MouseSensor, this)
    }

    /**
     * Get all new samples from mouse.
     * @return List of all new samples. Will be empty if time since last method call is shorter
     * than delayBetweenSamples.
     */
    override fun getNewSamples(): List<SensorSample> {
        val samples = mutableListOf<SensorSample>()
        while (LSL.local_clock() > nextSampleTime) {
            samples.addIfNotNull(createNewSensorSample())
            nextSampleTime += delayBetweenSamples
        }
        return samples
    }

    /**
     * Create a new SensorSample for nextSampleTime at the current position.
     * @return The created SensorSample or null if position is null.
     */
    private fun createNewSensorSample(): SensorSample? {
        if (position == null) {
            return null
        }
        return SensorSample(
            nextSampleTime,
            floatArrayOf(
                position!!.x.toFloat() / screenDimensions.width,
                position!!.y.toFloat() / screenDimensions.height,
            ),
        )
    }

    /**
     * Check if sensor should be recorded. Will always return true.
     */
    override fun shouldBeUsed(): Boolean {
        return true
    }

    override fun dispose() {}

    /**
     * Listener to keep track of mouse movements.
     * It also listens to opening and closing of new editors in order to add itself add a listener to all open editors.
     */
    private companion object Listener : EditorFactoryListener, EditorMouseListener, EditorMouseMotionListener {
        protected var position: Point? = null
        var editorMouseEvent: EditorMouseEvent? = null

        override fun editorCreated(event: EditorFactoryEvent) {
            super.editorCreated(event)
            event.editor.addEditorMouseMotionListener(this)
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            super.editorReleased(event)
            event.editor.removeEditorMouseMotionListener(this)
        }

        override fun mouseExited(event: EditorMouseEvent) {
            super.mouseExited(event)
            editorMouseEvent = null
        }

        override fun mouseMoved(e: EditorMouseEvent) {
            super.mouseMoved(e)
            editorMouseEvent = e

            val editor = e.editor
            if (editor.isDisposed) {
                position = null
                return
            }
            position = e.mouseEvent.locationOnScreen
        }

        override fun mouseDragged(e: EditorMouseEvent) {
            super.mouseDragged(e)

            val editor = e.editor
            if (editor.isDisposed) {
                position = null
                return
            }
            position = e.mouseEvent.locationOnScreen
        }
    }
}
