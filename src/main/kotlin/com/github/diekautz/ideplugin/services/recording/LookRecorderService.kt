package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.extensions.*
import com.github.diekautz.ideplugin.services.dto.GazeData
import com.github.diekautz.ideplugin.services.dto.GazeSnapshot
import com.github.diekautz.ideplugin.services.dto.LookElement
import com.github.diekautz.ideplugin.services.dto.LookElementGaze
import com.github.diekautz.ideplugin.utils.*
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import java.awt.Point
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


@Service(Service.Level.PROJECT)
class LookRecorderService(val project: Project) {

    private val gazeSnapshots = mutableListOf<GazeSnapshot>()
    private val elementGazePoints = mutableMapOf<PsiElement, Double>()

    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    fun askAndSaveGazeSnapshots() {
        gazeSnapshots.ifEmpty {
            project.infoMsg("No gaze snapshots to be saved.", thisLogger())
            return
        }
        val recordingStart = Date(gazeSnapshots.first().epochMillis)
        val filename = "gaze-${timestampFormat.format(recordingStart)}"
        askAndSaveToDisk(project, gazeSnapshots, "Gaze Snapshot Save Location", filename)
    }

    fun askAndSaveElementsGazePoints() {
        elementGazePoints.ifEmpty {
            project.infoMsg("No element gaze points to be saved.", thisLogger())
            return
        }

        askAndSaveToDisk(
            project,
            elementGazePoints.map { (psiElement, gazeWeight) ->
                LookElementGaze(psiElement, gazeWeight)
            },
            "Element Gaze Points Save Location",
            "element-gaze"
        )
    }

    fun askAndSaveBoth() {
        if (couldSave()) {
            val date = Date.from(Instant.now())
//            saveRecordingToDisk(
//                project, date,
//                elementGazePoints.map { (psiElement, gazeWeight) ->
//                    LookElementGaze(psiElement, gazeWeight)
//                }, gazeSnapshots, project.service<InterruptService>()
//            )
        } else {
            project.infoMsg("No data to be saved!")
        }
    }

    fun couldSave() = elementGazePoints.isNotEmpty() || gazeSnapshots.isNotEmpty()

    fun addGazeSnapshot(
        epochMillis: Long,
        virtualFile: VirtualFile,
        psiElement: PsiElement,
        rawGazeData: GazeData
    ): Int {
        val gazeSnapshot = GazeSnapshot(
            epochMillis,
            LookElement(psiElement.text, virtualFile.path, psiElement.startOffset),
            rawGazeData
        )
        gazeSnapshots.add(gazeSnapshot)
        return gazeSnapshots.size
    }


    /**
     * Normalized 2d-bell-curve with std dev 2
     */
    private val errorMatrix = arrayOf(
        arrayOf(0.0008, 0.0018, 0.0034, 0.0050, 0.0056, 0.0050, 0.0034, 0.0018, 0.0008),
        arrayOf(0.0018, 0.0044, 0.0082, 0.0119, 0.0135, 0.0119, 0.0082, 0.0044, 0.0018),
        arrayOf(0.0034, 0.0082, 0.0153, 0.0223, 0.0253, 0.0223, 0.0153, 0.0082, 0.0034),
        arrayOf(0.0050, 0.0119, 0.0223, 0.0325, 0.0368, 0.0325, 0.0223, 0.0119, 0.0050),
        arrayOf(0.0056, 0.0135, 0.0253, 0.0368, 0.0424, 0.0368, 0.0253, 0.0135, 0.0056),
        arrayOf(0.0050, 0.0119, 0.0223, 0.0325, 0.0368, 0.0325, 0.0223, 0.0119, 0.0050),
        arrayOf(0.0034, 0.0082, 0.0153, 0.0223, 0.0253, 0.0223, 0.0153, 0.0082, 0.0034),
        arrayOf(0.0018, 0.0044, 0.0082, 0.0119, 0.0135, 0.0119, 0.0082, 0.0044, 0.0018),
        arrayOf(0.0008, 0.0018, 0.0034, 0.0050, 0.0056, 0.0050, 0.0034, 0.0018, 0.0008),
    )
    private val errorMatrixSize1 = errorMatrix.size
    private val errorMatrixSize2 = errorMatrix.first().size

    fun addAreaGaze(psiFile: PsiFile, editor: Editor, rawGazeData: GazeData): Int {
        // distribute look onto surrounding elements evenly
        val eyeX = (rawGazeData.leftEyeX + rawGazeData.rightEyeX) / 2
        val eyeY = (rawGazeData.leftEyeY + rawGazeData.rightEyeY) / 2

        val errorPos = Point(eyeX, eyeY)
        errorMatrix.forEachIndexed { i, rows ->
            rows.forEachIndexed { j, error ->
                errorPos.move(
                    eyeX + (i - errorMatrixSize1 / 2) * 2,
                    eyeY + (j - errorMatrixSize2 / 2)
                )

                val logicalPosition = editor.xyScreenToLogical(errorPos)
                val offset = editor.logicalPositionToOffset(logicalPosition)
                val element = psiFile.findElementAt(offset)
                if (element != null && element !is PsiWhiteSpace) {
                    elementGazePoints.increment(element, error)
                }
            }
        }
        return elementGazePoints.size
    }

    fun highlightElements(editor: Editor) {
        editor.highlightElementGazePoints(elementGazePoints, project)
    }

    fun clearData() {
        gazeSnapshots.clear()
        elementGazePoints.clear()
        EditorFactory.getInstance().removeAllHighlighters()
    }

    fun couldHighlight() = elementGazePoints.isNotEmpty()

    fun openAllFiles() = runReadAction {
        val fileEditorManager = FileEditorManager.getInstance(project)
        mutableListOf<BufferedImage>()
        gazeSnapshots.map { it.lookElement.filePath }.forEach { filePath ->
            val vFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (vFile == null) {
                thisLogger().error("Could not find recorded file in my $filePath")
                return@forEach
            }
            val editor = fileEditorManager.openFile(vFile, false, true).firstOrNull()
            if (editor == null) {
                thisLogger().error("Could not open an editor for $filePath")
                return@forEach
            }
        }
    }

}