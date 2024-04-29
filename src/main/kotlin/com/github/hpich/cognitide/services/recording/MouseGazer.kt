/*
Class that can be used for debugging purposes instead of eye tracking sensor in LSL recorder.
Function getGaze() can be used to request current screen position of mouse.
This will return a GazeData object if a certain delay has passed since the last call.
Otherwise, it will return null.
The delay mimics the behaviour of a sensor that delivers new measurements periodically.
 */
package com.github.hpich.cognitide.services.recording
import com.github.hpich.cognitide.services.dto.GazeData
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.*

class MouseGazer(private val delayBetweenSamples: Long = 10L) : Disposable {
    private var nextSampleTime = 0L

    init {
        EditorFactory.getInstance().allEditors.forEach {
            it.addEditorMouseMotionListener(Listener)
        }
        EditorFactory.getInstance().addEditorFactoryListener(Listener, this)
    }

    /*
    Get current GazeData or null depending on the time since last request.
    If more than nextSampleTime has passed, return GazeData with current mouse position.
    Otherwise, return null.
    This can be used in debug mode instead of calling pull_sample on an StreamInlet.
     */
    fun getGaze(): GazeData? {
        if (System.currentTimeMillis() > nextSampleTime) {
            nextSampleTime = System.currentTimeMillis() + delayBetweenSamples
            return mouseGazeData
        }
        return null
    }

    /*
    Listener to keep track of mouse movements.
    It also listens to opening and closing of new editors in order to add itself add a listener to all open editors.
     */
    private companion object Listener : EditorFactoryListener, EditorMouseListener, EditorMouseMotionListener {
        protected var mouseGazeData: GazeData? = null
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
                mouseGazeData = null
                return
            }
            val mousePoint = e.mouseEvent.locationOnScreen
            mouseGazeData = GazeData(mousePoint, mousePoint, 1.0, 1.0)
        }
    }

    override fun dispose() {}
}
