package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.config.ParticipantState
import com.github.hpich.cognitide.extensions.xyScreenToLogical
import com.github.hpich.cognitide.services.dto.GazeData
import com.github.hpich.cognitide.services.dto.GazeSample
import com.github.hpich.cognitide.services.dto.SensorSample
import com.github.hpich.cognitide.services.recording.FileChangeTracker
import com.github.hpich.cognitide.services.recording.LSLSensor
import com.github.hpich.cognitide.services.recording.MouseSensor
import com.github.hpich.cognitide.services.recording.Sensor
import com.github.hpich.cognitide.utils.errorMatrix
import com.github.hpich.cognitide.utils.openConnector
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.rd.util.getOrCreate
import edu.ucsd.sccn.LSL
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import javax.swing.SwingUtilities
import kotlin.math.roundToInt

/**
 * A Study recorder using Sensors connected via LSL.
 * @param project Project for which this recorder will run.
 */
class LSLRecorder(private val project: Project) : StudyRecorder(project, "Recording Data") {
    private val streamDiscoveryWaitTime = 1.0

    private val lslSensorCount = CognitIDESettingsState.instance.devices.size
    private val sensors = mutableMapOf<String, Sensor>()
    private var gazeSensor: Sensor? = null

    private val fileChangesetDelay = 3.0
    private var fileTracker = FileChangeTracker(this, fileChangesetDelay)

    private val screenDimensions: Dimension = Toolkit.getDefaultToolkit().screenSize

    /**
     * Setup recorder.
     * Discover LSL streams for sensors.
     * @return Boolean indicating if setup was successful.
     */
    override fun setup(indicator: ProgressIndicator): Boolean {
        val streamInfos = LSL.resolve_streams(streamDiscoveryWaitTime)
        if (!initLslSensors(streamInfos)) {
            showConnectorDialogues()
            return false
        }

        initMouseSensor()
        gazeSensor = sensors["Mouse"]
        return true
    }

    /**
     * Main loop.
     * Check each sensor for new samples and pass them to the data collecting service.
     */
    override fun loop(indicator: ProgressIndicator) {
        sensors.forEach { (name, sensor) ->
            val newSamples = sensor.getNewSamples()
            dataCollectingService.addSensorSamples(name, newSamples)

            if (sensor != gazeSensor) {
                return@forEach
            }
            invokeLater {
                gazeSamplesFromSensorSamples(newSamples).forEach { (psiElement, samples) ->
                    dataCollectingService.addGazeSamples(psiElement, samples)
                }
            }
        }

        dataCollectingService.addFileChanges(fileTracker.getGroupedChanges())
        indicator.text = dataCollectingService.stats()
    }

    override fun stopRecording() {
        dataCollectingService.addFileChanges(fileTracker.getAllRemainingChanges())
        dataCollectingService.updateInitialTexts(fileTracker.getInitialFileContents())
        super.stopRecording()
    }

    /**
     * Initialize MouseSensor.
     */
    private fun initMouseSensor() {
        val mouseSensor = MouseSensor(this)
        // TODO add settings to select sensor for gaze data.
        sensors["Mouse"] = mouseSensor
    }

    /**
     * Initialize LSLSensors based on StreamInfos.
     * @param streamInfos Array of StreamInfo objects as obtained by LSL.resolve_streams().
     * @return Boolean indicating whether this was successful.
     */
    private fun initLslSensors(streamInfos: Array<LSL.StreamInfo>): Boolean {
        var addedLslSensorCount = 0
        streamInfos.forEach { streamInfo ->
            val sensor = LSLSensor(streamInfo, this)
            if (sensor.shouldBeUsed()) {
                sensors[sensor.name] = sensor
                addedLslSensorCount++
            }
        }
        return addedLslSensorCount == lslSensorCount
    }

    /**
     * Show Dialogues offering to open connector application for each sensor that was not initialized.
     */
    private fun showConnectorDialogues() {
        invokeLater {
            CognitIDESettingsState.instance.devices.forEach { device ->
                if (sensors[device.name] == null &&
                    MessageDialogBuilder.okCancel(
                        "LSL stream for " + device.name + " could not be found.",
                        "Maybe the connector application is not running. " +
                            "By pressing \"Ok\" you can open its connector application.",
                    ).ask(project)
                ) {
                    openConnector(project, device)
                }
            }
        }
    }

    /**
     * Map a list of SensorSamples to the PsiElements that were looked at.
     * The gaze will be spread out according to the spread stored in Participant state.
     * Will create a List of GazeSamples for each PsiElement.
     * @param samples List of sensor samples from gaze sensor (eye tracker or mouse)
     * @return Map PsiElement -> List of GazeSamples for that PsiElement
     */
    private fun gazeSamplesFromSensorSamples(samples: List<SensorSample>): Map<PsiElement, List<GazeSample>> {
        // Due to spread can have several weights for the same element at the same time.
        // To add them together we use this structure: map(psi element -> map(time -> weight)).
        val gazeMap = mutableMapOf<PsiElement, MutableMap<Double, Double>>()

        samples.forEach sampleLoop@{ sample ->
            val screenPosition = screenPositionFromSensorSample(sample) ?: return@sampleLoop
            val spreadOutPositions = getSpreadOutPositions(screenPosition)
            spreadOutPositions.forEach positionLoop@{ (position, weight) ->
                val editor = editorFromPosition(position) ?: return@positionLoop
                val psiElement = psiElementFromPosition(position, editor)
                val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
                if (virtualFile != null && psiElement != null && psiElement !is PsiWhiteSpace) {
                    gazeMap.getOrCreate(psiElement) { mutableMapOf() }
                        .merge(sample.time, weight) { prevWeight, newWeight -> prevWeight + newWeight }
                }
            }
        }

        // Convert map(psi element -> map(time -> weight) to map(psi element -> list(gaze sample))
        return gazeMap.mapValues { (_, map) ->
            map.toList().map { (time, weight) -> GazeSample(time, weight) }
        }
    }

    /**
     * Calculate spread out positions around a given screen position together with their weight.
     * Weight is looked up in an error matrix.
     * Spread of the positions depends on ParticipantState.
     * @param position Screen position of eye center.
     */
    private fun getSpreadOutPositions(position: Point): MutableList<Pair<Point, Double>> {
        val positions = mutableListOf<Pair<Point, Double>>()
        val horizontalSpread = ParticipantState.instance.horizontalSpread
        val verticalSpread = ParticipantState.instance.verticalSpread
        errorMatrix.forEachIndexed { x, row ->
            row.forEachIndexed { y, weight ->
                val movedPosition = Point(position)
                movedPosition.translate(
                    ((x.toDouble() / errorMatrix.size - 0.5) * horizontalSpread).roundToInt(),
                    ((y.toDouble() / errorMatrix.size - 0.5) * verticalSpread).roundToInt(),
                )
                positions.add(Pair(movedPosition, weight))
            }
        }
        return positions
    }

    /**
     * Get screen position from sensor sample.
     * @param sample Sensor sample with either 2 or 6 channels.
     * @return Screen position of gaze or null if gaze can't be extracted.
     */
    private fun screenPositionFromSensorSample(sample: SensorSample): Point? {
        if (sample.values.size != 2 && sample.values.size != 6) return null

        val mouse = sample.values.size == 2
        val leftX = (sample.values[0] * screenDimensions.width).toInt()
        val leftY = (sample.values[1] * screenDimensions.height).toInt()
        val leftPupil = if (mouse) 1.0 else sample.values[2].toDouble()
        val rightX = if (mouse) leftX else (sample.values[3] * screenDimensions.width).toInt()
        val rightY = if (mouse) leftY else (sample.values[4] * screenDimensions.height).toInt()
        val rightPupil = if (mouse) 1.0 else sample.values[5].toDouble()

        return GazeData(leftX, leftY, rightX, rightY, leftPupil, rightPupil).correctMissingEye()?.eyeCenter
    }

    /**
     * Find the corresponding PsiElement based on a screen position.
     * @param screenPosition Point describing screen position.
     * @param editor Editor containing this point (can be found with editorFromPoint).
     * @return PsiElement at screen position or null if no PsiElement is found.
     */
    private fun psiElementFromPosition(
        screenPosition: Point,
        editor: Editor,
    ): PsiElement? {
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return null

        val logicalPosition = editor.xyScreenToLogical(screenPosition)
        val offset = editor.logicalPositionToOffset(logicalPosition)
        return psiFile.findElementAt(offset)
    }

    /**
     * Get editor containing a certain screen position.
     * @param screenPosition Point describing screen position.
     * return Editor object or null if no editor contains point.
     */
    private fun editorFromPosition(screenPosition: Point): Editor? {
        // Create local copy so there are no side effects on screenPosition.
        val position = Point(screenPosition)

        val ideFrame = WindowManager.getInstance().getIdeFrame(project) ?: return null
        SwingUtilities.convertPointFromScreen(position, ideFrame.component)
        val deepestComponent = SwingUtilities.getDeepestComponentAt(ideFrame.component, position.x, position.y)

        return EditorFactory.getInstance().allEditors.firstOrNull { editor ->
            editor.contentComponent == deepestComponent || editor.contentComponent.isAncestorOf(deepestComponent)
        }
    }

    override fun dispose() {}
}
