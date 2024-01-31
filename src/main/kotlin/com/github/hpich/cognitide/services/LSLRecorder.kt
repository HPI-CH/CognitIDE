package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.extensions.xyScreenToLogical
import com.github.hpich.cognitide.services.dto.GazeData
import com.github.hpich.cognitide.services.dto.LookElement
import com.github.hpich.cognitide.utils.errorMsg
import com.github.hpich.cognitide.utils.openConnector
import com.github.hpich.cognitide.utils.openTobiiProConnector
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
import com.github.hpich.cognitide.config.CognitIDESettingsState.Companion.instance as cognitIDESettings


class LSLRecorder(
    private val project: Project
) : StudyRecorder(project, "Recording Data") {
    private var tobiiInlet: StreamInlet? = null
    private val otherLSLDataInlet = Array<StreamInlet?>(cognitIDESettings.devices.size) { null }
    private var otherLSLData = Array<FloatArray>(cognitIDESettings.devices.size) { floatArrayOf(-999f) }

    private val buffer = FloatArray(6)
    private val otherBuffers = Array(cognitIDESettings.devices.size) { i ->
        FloatArray(
            cognitIDESettings.devices.get(i).channelCount.toInt()
        ) { -999f }
    } // TODO array; ensure no empty entries
    private val screenDimensions = Toolkit.getDefaultToolkit().screenSize

    private var openStreamsCount = 0

    override val delay = 0L

    override fun loop(indicator: ProgressIndicator) {
        var gazeData: GazeData? = null
        if (tobiiInlet != null) {
            var timestampSeconds = tobiiInlet!!.pull_sample(buffer, 0.0)
            if (timestampSeconds != 0.0) {
                timestampSeconds += tobiiInlet!!.time_correction() //todo
                gazeData = GazeData(
                    (buffer[0] * screenDimensions.width).toInt(),
                    (buffer[1] * screenDimensions.height).toInt(),
                    (buffer[3] * screenDimensions.width).toInt(),
                    (buffer[4] * screenDimensions.height).toInt(),
                    buffer[2].toDouble(),
                    buffer[5].toDouble(),
                ).correctMissingEye()
            } else {
                gazeData = null
            }
        }

        val otherTimestampSeconds: MutableList<Double?> = mutableListOf()
        otherLSLDataInlet.forEachIndexed { index, it -> // ordering ensured through name of device
            otherTimestampSeconds.add(it?.pull_sample(otherBuffers.get(index), 0.0))

            if (otherTimestampSeconds.last() != null && otherTimestampSeconds.last() != 0.0) {
                otherTimestampSeconds += it?.time_correction()
                otherLSLData = otherBuffers
            } else otherLSLData = emptyArray()

        }

        var lookElement: LookElement? = null
        invokeLater {
            if (gazeData != null) {
                val editor = EditorFactory.getInstance().allEditors.firstOrNull {
                    val eyeLocal = Point(gazeData.eyeCenter)
                    SwingUtilities.convertPointFromScreen(eyeLocal, it.contentComponent)
                    it.contentComponent.contains(eyeLocal)
                } ?: return@invokeLater
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                    ?: return@invokeLater
                val eyeCenterGlobal = gazeData.eyeCenter

                val logicalPosition = editor.xyScreenToLogical(eyeCenterGlobal)
                val offset = editor.logicalPositionToOffset(logicalPosition)

                val element = psiFile.findElementAt(offset)
                val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)

                lookElement = element?.let {
                    if (virtualFile == null || it is PsiWhiteSpace) return@let null
                    LookElement(it.text, it.containingFile.virtualFile.path, it.startOffset)
                }
                dataCollectingService.incrementLookElementsAround(psiFile, editor, eyeCenterGlobal)

                indicator.text2 = "eye: ${eyeCenterGlobal.x},${eyeCenterGlobal.y} " +
                        "${logicalPosition.line}:${logicalPosition.column} ${element?.text} ${psiFile.name}"
            }
        }
        dataCollectingService.addGazeSnapshot(lookElement, gazeData, otherLSLData)
        indicator.text = dataCollectingService.stats()
    }

    override fun setup(indicator: ProgressIndicator): Boolean {
        indicator.text = "Searching for LSL inlets"
        var tobiiConnected = false

        try {
            LSL.resolve_streams(1.0).forEach {
                val inletCandidate = StreamInlet(it)
                val info = inletCandidate.info(1.0)

                if (cognitIDESettings.includeTobii && info.type() == "Gaze"
                    && info.channel_format() == LSL.ChannelFormat.float32
                    && info.channel_count() == buffer.size
                    && info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                ) {
                    tobiiInlet = inletCandidate
                    tobiiConnected = true

                    tobiiInlet!!.open_stream()
                    indicator.text = "${openStreamsCount + 1} inlets open. Waiting for data"
                } else if (info.channel_format() == LSL.ChannelFormat.float32
                    && info.name() in cognitIDESettings.devices.map { it.name } //TODO ensure order in another way
                ) {
                    //TODO could be useful for the used to have meta info saved in a file
                    val idx = cognitIDESettings.devices.map { it.name }.indexOf(info.name())
                    otherLSLDataInlet[idx] = inletCandidate

                    otherLSLDataInlet[idx]?.open_stream()
                    openStreamsCount++
                    indicator.text = "${openStreamsCount + if (tobiiConnected) 1 else 0} inlets open. Waiting for data"
                }


                if (openStreamsCount == cognitIDESettings.devices.size
                    && tobiiConnected == cognitIDESettings.includeTobii
                ) {
                    return true
                }
            }

        } catch (ex: Exception) {
            project.errorMsg("Error whilst opening LSL inlet: ${ex.localizedMessage}", logger = thisLogger(), ex)
            return false
        }
        invokeLater {
            if (cognitIDESettings.includeTobii) {
                if (MessageDialogBuilder
                        .okCancel("Is TobiiPro connector application running?", "Open TobiiPro Connector?")
                        .ask(project)
                ) {
                    openTobiiProConnector(project)
                }
            }
            cognitIDESettings.devices.forEach {
                if (MessageDialogBuilder
                        .okCancel(
                            "Is " + it.name + " connector application running?",
                            "Open " + it.name + " Connector?"
                        )
                        .ask(project)
                ) {
                    openConnector(project, it)
                }
            }
        }
        return false
    }

    override fun dispose() {
        if (cognitIDESettings.includeTobii) {
            tobiiInlet!!.close_stream()
        }
        otherLSLDataInlet.forEach {
            it?.close_stream()
        }
    }
}

