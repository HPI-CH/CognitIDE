package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.extensions.xyScreenToLogical
import com.github.hpich.cognitide.services.dto.GazeData
import com.github.hpich.cognitide.services.dto.LookElement
import com.github.hpich.cognitide.services.recording.MouseGazer
import com.github.hpich.cognitide.utils.*
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
    private val project: Project,
    private val debugMode: Boolean = false,
) : StudyRecorder(project, "Recording Data") {
    private var mouseGazer: MouseGazer? = null
    private var inlet: StreamInlet? = null
    private val otherLSLDataInlet = Array<StreamInlet?>(CognitIDESettingsState.instance.devices.size) { null }
    private var otherLSLData = Array<FloatArray>(CognitIDESettingsState.instance.devices.size) { floatArrayOf(-999f) }

    private val buffer = FloatArray(6)
    private val otherBuffers =
        Array(
            CognitIDESettingsState.instance.devices.size,
        ) { i ->
            FloatArray(CognitIDESettingsState.instance.devices.get(i).channelCount.toInt()) {
                -999f
            }
        } // TODO array; ensure no empty entries
    private val screenDimensions = Toolkit.getDefaultToolkit().screenSize

    private var openStreamsCount = 0

    override val delay = 0L

    fun Boolean.toInt() = if (this) 1 else 0 // TODO

    override fun loop(indicator: ProgressIndicator) {
        val data: GazeData?
        if (debugMode) {
            data = mouseGazer?.getGaze()
        } else {
            var timestampSeconds = inlet?.pull_sample(buffer, 0.0)
            if (timestampSeconds != null && timestampSeconds != 0.0) {
                timestampSeconds += inlet!!.time_correction() // todo
                data = GazeData(
                    (buffer[0] * screenDimensions.width).toInt(),
                    (buffer[1] * screenDimensions.height).toInt(),
                    (buffer[3] * screenDimensions.width).toInt(),
                    (buffer[4] * screenDimensions.height).toInt(),
                    buffer[2].toDouble(),
                    buffer[5].toDouble(),
                ).correctMissingEye() ?: return
            } else {
                data = null
            }
        }

        val otherTimestampSeconds: MutableList<Double?> = mutableListOf()
        otherLSLDataInlet.forEachIndexed { index, it -> // ordering ensured through name of device
            otherTimestampSeconds.add(it?.pull_sample(otherBuffers.get(index), 0.0))

            if (otherTimestampSeconds.last() != null && otherTimestampSeconds.last() != 0.0) {
                otherTimestampSeconds += it?.time_correction()

                otherLSLData = otherBuffers
            } else {
                otherLSLData = emptyArray()
            }
        }

        invokeLater {
            if (data != null) {
                val editor =
                    EditorFactory.getInstance().allEditors.firstOrNull {
                        val eyeLocal = Point(data.eyeCenter)
                        SwingUtilities.convertPointFromScreen(eyeLocal, it.contentComponent)
                        it.contentComponent.contains(eyeLocal)
                    } ?: return@invokeLater
                val psiFile =
                    PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
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
                            element.startOffset,
                        ),
                        data,
                        otherLSLData,
                    )
                } else {
                    dataCollectingService.addGazeSnapshot(
                        null,
                        data,
                        otherLSLData,
                    )
                }
                dataCollectingService.incrementLookElementsAround(psiFile, editor, eyeCenterGlobal)
                indicator.text = dataCollectingService.stats()
                indicator.text2 = "eye: ${eyeCenterGlobal.x},${eyeCenterGlobal.y} " +
                    "${logicalPosition.line}:${logicalPosition.column} ${element?.text} ${psiFile.name}"
            } else if (otherLSLData.isNotEmpty()) {
                dataCollectingService.addGazeSnapshot(
                    null,
                    null,
                    otherLSLData,
                )
                indicator.text = dataCollectingService.stats()
            }
        }
    }

    override fun setup(indicator: ProgressIndicator): Boolean {
        indicator.text = "Searching for LSL inlets"
        @Suppress("ktlint")
        var tobii_connected = false
        var all = 0

        try {
            LSL.resolve_streams(1.0).forEach {
                all += 1
                val inletCandidate = StreamInlet(it)
                val info = inletCandidate.info(1.0)

                if (CognitIDESettingsState.instance.includeTobii && info.type() == "Gaze" &&
                    info.channel_format() == LSL.ChannelFormat.float32 &&
                    info.channel_count() == buffer.size &&
                    info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                ) {
                    inlet = inletCandidate
                    tobii_connected = true

                    inlet!!.open_stream()
                    indicator.text = "${openStreamsCount + tobii_connected.toInt()} inlets open. Waiting for data"
                } else if (info.channel_format() == LSL.ChannelFormat.float32 &&
                    info.name() in CognitIDESettingsState.instance.devices.map { it.name } // TODO ensure order in another way
                ) {
                    // TODO could be useful for the used to have meta info saved in a file
                    val idx = CognitIDESettingsState.instance.devices.map { it.name }.indexOf(info.name())
                    otherLSLDataInlet[idx] = inletCandidate

                    otherLSLDataInlet[idx]?.open_stream()
                    openStreamsCount++
                    indicator.text = "${openStreamsCount + tobii_connected.toInt()} inlets open. Waiting for data"
                }
                if (openStreamsCount == CognitIDESettingsState.instance.devices.size &&
                    tobii_connected == CognitIDESettingsState.instance.includeTobii
                ) {
                    if (debugMode) {
                        mouseGazer = MouseGazer()
                    }
                    return true
                }
            }
        } catch (ex: Exception) {
            project.errorMsg("Error whilst opening LSL inlet: ${ex.localizedMessage}", logger = thisLogger(), ex)
            return false
        }
        invokeLater {
            if (!debugMode && CognitIDESettingsState.instance.includeTobii) {
                if (MessageDialogBuilder
                        .okCancel("Is TobiiPro connector application running?", "Open TobiiPro Connector?")
                        .ask(project)
                ) {
                    openTobiiProConnector(project)
                }
            }
            CognitIDESettingsState.instance.devices.forEach {
                if (MessageDialogBuilder
                        .okCancel("Is " + it.name + " connector application running?", "Open " + it.name + " Connector?")
                        .ask(project)
                ) {
                    openConnector(project, it)
                }
            }
        }
        return false
    }

    override fun dispose() {
        if (!debugMode && CognitIDESettingsState.instance.includeTobii) {
            inlet!!.close_stream()
        }
        otherLSLDataInlet.forEach {
            it?.close_stream()
        }
    }
}
