package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.utils.GazeData
import com.github.diekautz.ideplugin.utils.highlightElements
import com.github.diekautz.ideplugin.utils.increment
import com.github.diekautz.ideplugin.utils.serializeAndSaveToDisk
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

@Service(Service.Level.PROJECT)
class MyLookRecorderService(val project: Project) {

    private val gazeSnapshots = mutableListOf<GazeSnapshot>()
    private val elementGazePoints = mutableMapOf<PsiElement, Double>()

    fun saveGazeSnapshots() {
        gazeSnapshots.ifEmpty {
            thisLogger().warn("No gaze snapshots to be saved. Aborting.")
            return
        }
        serializeAndSaveToDisk(project, gazeSnapshots, "Gaze Snapshot Save Location")
    }

    fun saveElementsGazePoints() {
        elementGazePoints.ifEmpty {
            thisLogger().warn("No element gaze points to be saved. Aborting.")
            return
        }
        serializeAndSaveToDisk(project, elementGazePoints, "Element Gaze Points Save Location")
    }

    fun addGazeSnapshot(epochMillis: Long, virtualFile: VirtualFile, psiElement: PsiElement, rawGazeData: GazeData) {
        val editorGazeSnapshot = EditorGazeSnapshot(
            epochMillis,
            virtualFile.path,
            psiElement.startOffset
        )
        val gazeSnapshot = GazeSnapshot(rawGazeData, editorGazeSnapshot)
        gazeSnapshots.add(gazeSnapshot)
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

    fun addAreaGaze(psiFile: PsiFile, editor: Editor, rawGazeData: GazeData) {
        // distribute look onto surrounding elements evenly
        val eyeX = (rawGazeData.leftEye.x + rawGazeData.rightEye.x) / 2
        val eyeY = (rawGazeData.leftEye.y + rawGazeData.rightEye.y) / 2

        val errorPos = Point(eyeX, eyeY)
        errorMatrix.forEachIndexed { i, rows ->
            rows.forEachIndexed { j, error ->
                errorPos.move(
                    eyeX + (i - errorMatrixSize1 / 2) * 2,
                    eyeY + (j - errorMatrixSize2 / 2)
                )

                val logicalPosition = editor.xyToLogicalPosition(errorPos)
                val offset = editor.logicalPositionToOffset(logicalPosition)
                val element = psiFile.findElementAt(offset)
                if (element != null && element !is PsiWhiteSpace) {
                    elementGazePoints.increment(element, error)
                }
            }
        }
    }

    fun highlightElements(editor: Editor) {
        editor.highlightElements(0, elementGazePoints.keys.toList(), project)
    }

}