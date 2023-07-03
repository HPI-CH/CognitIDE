package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.services.recording.GazeData
import com.github.diekautz.ideplugin.services.recording.MyLookRecorderService
import com.github.diekautz.ideplugin.utils.infoMsg
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.runBlocking
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class MyTobiiProService(val project: Project) {


    private val lookRecorderService = project.service<MyLookRecorderService>()

    fun startRecording() {
        task.shouldRun = true
        ProgressManager.getInstance().run(task)
    }

    fun stopRecording() {
        task.shouldRun = false
    }

    private val task = object : Task.Backgroundable(project, "Recording gaze", true) {
        var shouldRun = true
        val logger = this@MyTobiiProService.thisLogger()

        var gazeSnapshotN = 0
        var elementGazeN = 0

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
                        logger.debug(
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
                        project.infoMsg("No TobiiPro stream found!", logger)
                        return@runBlocking
                    }
                    val inlet = streamInlet!!
                    val screenRect = Rectangle(Toolkit.getDefaultToolkit().screenSize)

                    logger.info("Tobii Recording started")
                    indicator.text = "Opening inlet"
                    inlet.open_stream()
                    indicator.text = "Inlet Open. Waiting for data.."
                    val buffer = FloatArray(6)
                    while (shouldRun) {
                        var timestampSeconds = inlet.pull_sample(buffer)
                        if (timestampSeconds == 0.0) continue
                        timestampSeconds += inlet.time_correction()
                        val data = GazeData(
                            Point((buffer[0] * screenRect.width).toInt(), (buffer[1] * screenRect.height).toInt()),
                            Point((buffer[3] * screenRect.width).toInt(), (buffer[4] * screenRect.height).toInt()),
                            buffer[2].toDouble(),
                            buffer[5].toDouble(),
                        )

                        invokeLater {
                            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
                            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                                ?: return@invokeLater

                            val eyeCenter = Point(
                                (data.leftEyeX + data.rightEyeY) / 2,
                                (data.leftEyeY + data.rightEyeY) / 2,
                            )
                            SwingUtilities.convertPointFromScreen(eyeCenter, editor.contentComponent)
                            if (!editor.contentComponent.contains(eyeCenter)) return@invokeLater
                            val logicalPosition = editor.xyToLogicalPosition(eyeCenter)
                            val offset = editor.logicalPositionToOffset(logicalPosition)

                            val element = psiFile.findElementAt(offset)
                            val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
                            if (virtualFile != null && element != null && element !is PsiWhiteSpace) {
                                gazeSnapshotN = lookRecorderService.addGazeSnapshot(
                                    (timestampSeconds * 1_000.0).toLong(),
                                    virtualFile,
                                    element,
                                    data
                                )
                            }
                            elementGazeN = lookRecorderService.addAreaGaze(psiFile, editor, data)
                            indicator.text  = "rawGaze: $gazeSnapshotN elements: $elementGazeN"
                            indicator.text2 = "eye: ${eyeCenter.x},${eyeCenter.y} ${logicalPosition.line}:${logicalPosition.column} ${element?.text}"
                        }
                    }
                } catch (ex: Exception) {
                    invokeLater {
                        Messages.showErrorDialog(project, ex.localizedMessage, "TobiiRecording Exception")
                    }
                    logger.error(ex)
                }
                logger.info("Tobii Recording stopped")
            }
        }
    }


    fun visualizeInEditor() {
        invokeLater {
            FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
                lookRecorderService.highlightElements(editor)
            }
        }
    }

}