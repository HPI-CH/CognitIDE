package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.extensions.xyScreenToLogical
import com.github.diekautz.ideplugin.services.dto.ShimmerData
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.utils.errorMsg
import com.github.diekautz.ideplugin.utils.openShimmerConnector
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

class ShimmerRecorder(
    private val project: Project
) : StudyRecorder(project, "Recording Shimmer Data") {
    private lateinit var inlet: StreamInlet

    private val buffer = FloatArray(24)

    override val delay = 0L
    override fun loop(indicator: ProgressIndicator) {
        var timestampSeconds = inlet.pull_sample(buffer)
        if (timestampSeconds == 0.0) return
        timestampSeconds += inlet.time_correction()
        val data = ShimmerData(
            buffer[0].toDouble(), buffer[1].toDouble(), buffer[2].toDouble(), buffer[3].toDouble(),
            buffer[4].toDouble(), buffer[5].toDouble(), buffer[6].toDouble(), buffer[7].toDouble(),
            buffer[8].toDouble(), buffer[9].toDouble(), buffer[10].toDouble(), buffer[11].toDouble(),
            buffer[12].toDouble(), buffer[13].toDouble(), buffer[14].toDouble(), buffer[15].toDouble(),
            buffer[16].toDouble(), buffer[17].toDouble()
        )

        invokeLater {
            dataCollectingService.addGazeSnapshot(null, null, data)

            indicator.text = dataCollectingService.stats()
            indicator.text2 = "???"

        }
    }

    override fun setup(indicator: ProgressIndicator): Boolean {
        indicator.text = "Searching for Shimmer inlet"
        try {
            LSL.resolve_stream(
                "type='Shimmer Data'",
                1,
                5.0
            ).forEach {
                val inletCandidate = StreamInlet(it)
                val info = inletCandidate.info(1.0)
                if (info.type() == "Shimmer Data"
                    && info.channel_format() == LSL.ChannelFormat.float32
                    && info.channel_count() == buffer.size
                    && info.desc().child("acquisition").child_value("manufacturer") == "Shimmer"
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
                    .okCancel("No Shimmer stream found!", "Open Shimmer Connector?")
                    .ask(project)
            ) {
                openShimmerConnector(project)
            }
        }
        return false
    }

    override fun dispose() {
        inlet.close_stream()
    }
}