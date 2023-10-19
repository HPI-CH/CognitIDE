package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.extensions.xyScreenToLogical
import com.github.diekautz.ideplugin.services.dto.emotiv.EmotivPerformanceData
import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.services.dto.ShimmerData
import com.github.diekautz.ideplugin.utils.errorMsg
import com.github.diekautz.ideplugin.utils.openTobiiProConnector
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import edu.ucsd.sccn.LSL
import edu.ucsd.sccn.LSL.StreamInlet
import java.awt.Point
import java.awt.Toolkit
import javax.swing.SwingUtilities

class LSLRecorder(
    private val project: Project
) : StudyRecorder(project, "Recording Gaze") {
    private lateinit var inlet: StreamInlet
    private lateinit var shimmerInlet: StreamInlet
    private lateinit var emotivPerformanceInlet: StreamInlet

    private val buffer = FloatArray(6)
    private val shimmerBuffer = FloatArray(17)
    private val emotivPerformanceBuffer = FloatArray(7)
    private val screenDimensions = Toolkit.getDefaultToolkit().screenSize

    override val delay = 0L
    override fun loop(indicator: ProgressIndicator) {
        var timestampSeconds = inlet.pull_sample(buffer, 0.0)
        val data : GazeData?
        if (timestampSeconds != 0.0) {
            timestampSeconds += inlet.time_correction()
            data = GazeData(
                (buffer[0] * screenDimensions.width).toInt(),
                (buffer[1] * screenDimensions.height).toInt(),
                (buffer[3] * screenDimensions.width).toInt(),
                (buffer[4] * screenDimensions.height).toInt(),
                buffer[2].toDouble(),
                buffer[5].toDouble(),
            ).correctMissingEye() ?: return
        } else data = null

        var shimmerTimestampSeconds = shimmerInlet.pull_sample(shimmerBuffer, 0.0)
        val shimmerData : ShimmerData?
        if (shimmerTimestampSeconds != 0.0) {
            shimmerTimestampSeconds += shimmerInlet.time_correction()
            shimmerData = ShimmerData(
                shimmerBuffer[0].toDouble(),
                shimmerBuffer[1].toDouble(),
                shimmerBuffer[2].toDouble(),
                shimmerBuffer[3].toDouble(),
                shimmerBuffer[4].toDouble(),
                shimmerBuffer[5].toDouble(),
                shimmerBuffer[6].toDouble(),
                shimmerBuffer[7].toDouble(),
                shimmerBuffer[8].toDouble(),
                shimmerBuffer[9].toDouble(),
                shimmerBuffer[10].toDouble(),
                shimmerBuffer[11].toDouble(),
                shimmerBuffer[12].toDouble(),
                shimmerBuffer[13].toDouble(),
                shimmerBuffer[14].toDouble(),
                shimmerBuffer[15].toDouble(),
                shimmerBuffer[16].toDouble()
            )
        } else shimmerData = null

        var emotivPerformanceTimestampSeconds = emotivPerformanceInlet.pull_sample(emotivPerformanceBuffer, 0.0)
        val emotivPerformaceData : EmotivPerformanceData?
        if (emotivPerformanceTimestampSeconds != 0.0) {
            emotivPerformanceTimestampSeconds += emotivPerformanceInlet.time_correction()
            emotivPerformaceData = EmotivPerformanceData(
                emotivPerformanceBuffer[0].toDouble(),
                emotivPerformanceBuffer[1].toDouble(),
                emotivPerformanceBuffer[2].toDouble(),
                emotivPerformanceBuffer[3].toDouble(),
                emotivPerformanceBuffer[4].toDouble(),
                emotivPerformanceBuffer[5].toDouble(),
                emotivPerformanceBuffer[6].toDouble()
            )
        } else emotivPerformaceData = null



        invokeLater {
            if (data != null) {
                val editor = EditorFactory.getInstance().allEditors.firstOrNull {
                    val eyeLocal = Point(data.eyeCenter)
                    SwingUtilities.convertPointFromScreen(eyeLocal, it.contentComponent)
                    it.contentComponent.contains(eyeLocal)
                } ?: return@invokeLater
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                    ?: return@invokeLater
                val eyeCenterGlobal = data.eyeCenter

                val logicalPosition = editor.xyScreenToLogical(eyeCenterGlobal)
                val offset = editor.logicalPositionToOffset(logicalPosition)

                val element = psiFile.findElementAt(offset)
                val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)

                if (virtualFile != null && element != null && element !is PsiWhiteSpace) {

                    dataCollectingService.addGazeSnapshot(
                        LookElement(
                            element.text,
                            element.containingFile.virtualFile.path,
                            element.startOffset
                        ),
                        data, shimmerData, emotivPerformaceData
                    )
                }
                dataCollectingService.incrementLookElementsAround(psiFile, editor, eyeCenterGlobal)
                indicator.text = dataCollectingService.stats()
                indicator.text2 = "eye: ${eyeCenterGlobal.x},${eyeCenterGlobal.y} " +
                        "${logicalPosition.line}:${logicalPosition.column} ${element?.text} ${psiFile.name}"
            } else if (shimmerData != null || emotivPerformaceData != null) {
                dataCollectingService.addGazeSnapshot(
                    null,
                    data, shimmerData, emotivPerformaceData
                )
                indicator.text = dataCollectingService.stats()
            }
        }
    }

    override fun setup(indicator: ProgressIndicator): Boolean {
        indicator.text = "Searching for Tobii Pro inlet"
        var all_connected = 0
        var all = 0

        try {
            LSL.resolve_streams(1.0).forEach {
                all += 1
                val inletCandidate = StreamInlet(it)
                val info = inletCandidate.info(1.0)
                if (info.type() == "Gaze"
                    && info.channel_format() == LSL.ChannelFormat.float32
                    && info.channel_count() == buffer.size
                    && info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                ) {

                    inlet = inletCandidate
                    indicator.text = "Inlet Open. Waiting for data"
                    all_connected += 1


                }
                else if (info.name() == "SendData"
                    && info.channel_format() == LSL.ChannelFormat.float32
                    && info.channel_count() == 17
                    //&& info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                ) {

                    shimmerInlet = inletCandidate

                    indicator.text = "Inlet Open. Waiting for data"
                    all_connected += 1

                }
                else if (info.name() == "EmotivDataStream-Performance-Metrics"){
                    emotivPerformanceInlet = inletCandidate

                    indicator.text = "Inlet Open. Waiting for data"
                    all_connected += 1
                }
                if (all_connected == 3) {
                    indicator.text = "Opening inlets"
                    shimmerInlet.open_stream()
                    indicator.text = "Shimmer open"
                    inlet.open_stream()
                    indicator.text = "Tobii open"
                    emotivPerformanceInlet.open_stream()
                    indicator.text = "emotivPerformance open"
                    return true
                }
            }

        } catch (ex: Exception) {
            project.errorMsg("Error whilst opening LSL inlet: ${ex.localizedMessage}", logger = thisLogger(), ex)
            return false
        }
        invokeLater {
            if (MessageDialogBuilder
                    .okCancel("No TobiiPro stream found!", "Open TobiiPro Connector?")
                    .ask(project)
            ) {
                openTobiiProConnector(project)
            }
        }
        return false
    }

    override fun dispose() {
        inlet.close_stream()
    }
}