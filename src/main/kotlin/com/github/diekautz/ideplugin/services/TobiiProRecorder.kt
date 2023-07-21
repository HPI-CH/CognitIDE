package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.LookElement
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

class TobiiProRecorder(
    private val project: Project
) : StudyRecorder(project, "Recording Gaze") {
    private lateinit var inlet: StreamInlet

    private val buffer = FloatArray(6)
    private val screenDimensions = Toolkit.getDefaultToolkit().screenSize

    override val delay = 0L
    override fun loop(indicator: ProgressIndicator) {
        var timestampSeconds = inlet.pull_sample(buffer)
        if (timestampSeconds == 0.0) return
        timestampSeconds += inlet.time_correction()
        val data = GazeData(
            (buffer[0] * screenDimensions.width).toInt(),
            (buffer[1] * screenDimensions.height).toInt(),
            (buffer[3] * screenDimensions.width).toInt(),
            (buffer[4] * screenDimensions.height).toInt(),
            buffer[2].toDouble(),
            buffer[5].toDouble(),
        ).correctMissingEye() ?: return

        invokeLater {
            var eyeCenter = Point(0, 0)
            val editor = EditorFactory.getInstance().allEditors.firstOrNull {
                eyeCenter = Point(
                    (data.leftEyeX + data.rightEyeX) / 2,
                    (data.leftEyeY + data.rightEyeY) / 2,
                )
                SwingUtilities.convertPointFromScreen(eyeCenter, it.contentComponent)
                it.contentComponent.contains(eyeCenter)
            } ?: return@invokeLater

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                ?: return@invokeLater

            val logicalPosition = editor.xyToLogicalPosition(eyeCenter)
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
                    data
                )
            }
            dataCollectingService.incrementLookElementsAround(psiFile, editor, eyeCenter)
            indicator.text = dataCollectingService.stats()
            indicator.text2 = "eye: ${eyeCenter.x},${eyeCenter.y} " +
                    "${logicalPosition.line}:${logicalPosition.column} ${element?.text}"

        }
    }

    override fun setup(indicator: ProgressIndicator): Boolean {
        indicator.text = "Searching for Tobii Pro inlet"
        try {
            LSL.resolve_stream(
                "type='Gaze'",
                1,
                5.0
            ).forEach {
                val inletCandidate = StreamInlet(it)
                val info = inletCandidate.info(1.0)
                if (info.type() == "Gaze"
                    && info.channel_format() == LSL.ChannelFormat.float32
                    && info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                ) {
                    inlet = inletCandidate
                    indicator.text = "Opening inlet"
                    inlet.open_stream()
                    indicator.text = "Inlet Open. Waiting for data.."
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
}