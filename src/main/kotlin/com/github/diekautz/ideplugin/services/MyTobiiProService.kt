package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.recording.GazeData
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit

@Service(Service.Level.PROJECT)
class MyTobiiProService(val project: Project) {

    val gazeData = mutableListOf<GazeData>()

    fun startRecording() {
        task.shouldRun = true
        ProgressManager.getInstance().run(task)
    }

    fun stopRecording() {
        task.shouldRun = false
    }

    private val task = object : Task.Backgroundable(project, "Recording gaze", true) {
        var shouldRun = true

        override fun run(indicator: ProgressIndicator) {
            runBlocking {
                indicator.isIndeterminate = true
                indicator.text = "Resolving Gaze stream"
                try {
                    var streamInlet: LSL.StreamInlet? = null
                    LSL.resolve_stream(
                        "type='Gaze'",
                        1,
                        5.0
                    ).forEach {
                        val inlet = LSL.StreamInlet(it)
                        val info = inlet.info(1.0)
                        thisLogger().debug(
                            "Got stream ${info.name()}: ${
                                info.desc().child("acquisition").child_value("manufacturer")
                            }"
                        )
                        if (info.type() == "Gaze"
                            && info.channel_format() == LSL.ChannelFormat.float32
                            && info.desc().child("acquisition").child_value("manufacturer") == "TobiiPro"
                        ) {
                            streamInlet = inlet
                        }
                    }
                    if (streamInlet == null) {
                        invokeLater {
                            Messages.showInfoMessage(project, "No TobiiPro stream found!", "TobiiService")
                        }
                        thisLogger().info("No TobiiPro stream found!")
                        return@runBlocking
                    }
                    val inlet = streamInlet!!
                    val screenRect = Rectangle(Toolkit.getDefaultToolkit().screenSize)

                    thisLogger().info("Tobii Recording started")
                    indicator.text = "Opening inlet"
                    inlet.open_stream()
                    indicator.text = "Inlet Open. Waiting for data.."
                    val buffer = FloatArray(6)
                    while (shouldRun) {
                        var timestampSeconds = inlet.pull_sample(buffer)
                        if (timestampSeconds == 0.0) continue
                        timestampSeconds += inlet.time_correction()
                        val data = GazeData(
                            Instant.fromEpochSeconds(timestampSeconds.toLong(), 0),
                            Point((buffer[0] * screenRect.width).toInt(), (buffer[1] * screenRect.height).toInt()),
                            Point((buffer[3] * screenRect.width).toInt(), (buffer[4] * screenRect.height).toInt()),
                            buffer[2].toDouble(),
                            buffer[5].toDouble(),
                        )
                        thisLogger().info("New gaze data: $data")
                        gazeData.add(data)
                    }
                } catch (ex: Exception) {
                    invokeLater {
                        Messages.showErrorDialog(project, ex.localizedMessage, "TobiiRecording Exception")
                    }
                    thisLogger().error(ex)
                }
                thisLogger().info("Tobii Recording stopped")
            }
        }
    }


    fun visualizeInEditor() {
        invokeLater {
            FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
                thisLogger().debug(gazeData.joinToString("\n"))
            }
        }
    }

}