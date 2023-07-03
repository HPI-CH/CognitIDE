package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.utils.highlightElementGazePoints
import com.github.diekautz.ideplugin.utils.increment
import com.github.diekautz.ideplugin.utils.infoMsg
import com.github.diekautz.ideplugin.utils.serializeAndSaveToDisk
import com.github.diekautz.ideplugin.utils.xyScreenToLogical
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.startOffset
import java.awt.Point
import java.text.SimpleDateFormat
import java.util.*

@Service(Service.Level.PROJECT)
class MyLookRecorderService(val project: Project) {

    private val gazeSnapshots = mutableListOf<GazeSnapshot>()
    private val elementGazePoints = mutableMapOf<PsiElement, Double>()

    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    fun saveGazeSnapshots() {
        gazeSnapshots.ifEmpty {
            project.infoMsg("No gaze snapshots to be saved.", thisLogger())
            return
        }
        val recordingStart = Date(gazeSnapshots.first().editorGazeSnapshot.epochMillis)
        val filename = "gaze-${timestampFormat.format(recordingStart)}"
        serializeAndSaveToDisk(project, gazeSnapshots, "Gaze Snapshot Save Location", filename)
    }

    fun saveElementsGazePoints() {
        elementGazePoints.ifEmpty {
            project.infoMsg("No element gaze points to be saved.", thisLogger())
            return
        }

        serializeAndSaveToDisk(
            project,
            elementGazePoints.mapKeys { (psiElement, _) ->
                SerializableElement(psiElement)
            },
            "Element Gaze Points Save Location",
            "elements"
        )
    }

    fun addGazeSnapshot(
        epochMillis: Long,
        virtualFile: VirtualFile,
        psiElement: PsiElement,
        rawGazeData: GazeData
    ): Int {
        val editorGazeSnapshot = EditorGazeSnapshot(
            epochMillis,
            virtualFile.path,
            psiElement.startOffset
        )
        val gazeSnapshot = GazeSnapshot(rawGazeData, editorGazeSnapshot)
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
    }

}