package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.utils.GazeData
import com.intellij.openapi.components.Service
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

    fun addElementGaze(timeSeconds: Double, virtualFile: VirtualFile, psiElement: PsiElement, rawGazeData: GazeData) {
        val editorGazeSnapshot = EditorGazeSnapshot(
            timeSeconds,
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
        arrayOf(0.0008F, 0.0018F, 0.0034F, 0.0050F, 0.0056F, 0.0050F, 0.0034F, 0.0018F, 0.0008F),
        arrayOf(0.0018F, 0.0044F, 0.0082F, 0.0119F, 0.0135F, 0.0119F, 0.0082F, 0.0044F, 0.0018F),
        arrayOf(0.0034F, 0.0082F, 0.0153F, 0.0223F, 0.0253F, 0.0223F, 0.0153F, 0.0082F, 0.0034F),
        arrayOf(0.0050F, 0.0119F, 0.0223F, 0.0325F, 0.0368F, 0.0325F, 0.0223F, 0.0119F, 0.0050F),
        arrayOf(0.0056F, 0.0135F, 0.0253F, 0.0368F, 0.0424F, 0.0368F, 0.0253F, 0.0135F, 0.0056F),
        arrayOf(0.0050F, 0.0119F, 0.0223F, 0.0325F, 0.0368F, 0.0325F, 0.0223F, 0.0119F, 0.0050F),
        arrayOf(0.0034F, 0.0082F, 0.0153F, 0.0223F, 0.0253F, 0.0223F, 0.0153F, 0.0082F, 0.0034F),
        arrayOf(0.0018F, 0.0044F, 0.0082F, 0.0119F, 0.0135F, 0.0119F, 0.0082F, 0.0044F, 0.0018F),
        arrayOf(0.0008F, 0.0018F, 0.0034F, 0.0050F, 0.0056F, 0.0050F, 0.0034F, 0.0018F, 0.0008F),
    )
    val errorMatrixSize1 = errorMatrix.size
    val errorMatrixSize2 = errorMatrix.first().size

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
                    val value = elementGazePoints.getOrDefault(element, 0.0)
                    elementGazePoints[element] = value + error
                }
            }
        }
    }

}