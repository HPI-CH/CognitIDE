package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.services.dto.FileChangeset
import com.github.hpich.cognitide.services.dto.FileCheckpoint
import com.github.hpich.cognitide.ui.CognitIDEColors
import com.github.hpich.cognitide.utils.reopenFilesFromPaths
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.WindowManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import javax.swing.*

private val logger = Logger.getInstance("com.github.hpich.cognitide.utils.IOUtilities")

/**
 * Class to visualize a recording by highlighting looked at elements based on the recorded data.
 * You can specify a certain timestamp during the recording that will be visualized.
 *
 * CAUTION: Running a highlighter will reset all files contained in the recording to the state they were at the end of
 * the recording.
 * TODO: Consider adding a warning popup.
 *
 * Running a recorder will reconstruct all relevant files at a specified timestamp. It will then run a
 * user provided highlighting script that calculates a highlighting intensity based on the recorded sensor data.
 * Finally, the looked at elements are highlighted based on the calculated intensity.
 * TODO: Discard gaze samples recorded during FileChangesets.
 *
 * Tu use a Highlighter, instantiate a new Highlighter for the saveFolder of the recording you want to visualize, and
 * run it as a backgroundable task:
 * `ProgressManager.getInstance().run(Highlighter(project, saveFolder))`
 *
 * @param project Project for which this Highlighter will run.
 * @param saveFolder Folder where the recording is saved, that will be highlighted.
 * @param time Which timestamp will be reconstructed and visualized. If set to 0.0, the final state of the recording
 *             will be reconstructed and visualized.
 */
class Highlighter(
    project: Project,
    private val saveFolder: File,
    private val time: Double = 0.0,
    loadingText: String = "Highlighting recording",
) : Task.Backgroundable(project, loadingText, true) {
    // map(file path -> changesets).
    private var fileChangeData = mapOf<String, List<FileChangeset>>()

    // map(file path -> file checkpoint).
    private var fileContents = mapOf<String, FileCheckpoint>()

    // map(element id -> highlight intensity).
    private var elementHighlighting = mapOf<Int, Double>()

    // Progress Bar UI
    private val progressFrame = JFrame(loadingText)
    private val progressBar = JProgressBar()
    private val progressLabel = JLabel("Process Highlighting...")
    private val panelWidth = 400
    private val panelHeight = 100

    /**
     * Ensure that the progress bar panel is always IDE-centered and on the same screen as the IDE.
     */
    private fun setScreenPosition() {
        val ideFrame = WindowManager.getInstance().getFrame(project)
        if (ideFrame != null) {
            val ideBounds = ideFrame.bounds
            progressFrame.setLocation(
                ideBounds.x + (ideBounds.width - progressFrame.width) / 2,
                ideBounds.y + (ideBounds.height - progressFrame.height) / 2,
            )
        } else {
            progressFrame.setLocationRelativeTo(null)
        }
    }

    private fun initProgressBar() {
        progressBar.isIndeterminate = true
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(progressLabel)
        panel.add(progressBar)
        progressFrame.add(panel)
        progressFrame.setSize(panelWidth, panelHeight)
        setScreenPosition()
        progressFrame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        progressFrame.isVisible = true
    }

    /**
     * Main run function.
     * This will be executed when calling `ProgressManager.getInstance().run(Highlighter(project, saveFolder))`.
     * @param indicator Progress indicator for the task. Can be used to display progress information.
     */
    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        try {
            SwingUtilities.invokeAndWait {
                initProgressBar()
            }
            loadData()
            SwingUtilities.invokeAndWait {
                val filePaths = fileContents.keys.toList()
                reopenFilesFromPaths(project, filePaths)
            }
            reconstructFiles()
            runHighlightScript()
            displayHighlighting()
        } catch (e: Exception) {
            SwingUtilities.invokeAndWait {
                JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
            }
        } finally {
            SwingUtilities.invokeLater {
                progressFrame.dispose()
            }
        }
    }

    /**
     * Load the recording data required for reconstructing the files.
     */
    private fun loadData() {
        fileContents =
            Json.decodeFromString<Map<String, FileCheckpoint>>(File(saveFolder, "initialFileContents.json").readText())
        fileChangeData =
            Json.decodeFromString<Map<String, List<FileChangeset>>>(File(saveFolder, "fileChangeData.json").readText())
    }

    /*private fun createTempFile(prefix: String, suffix: String): VirtualFile {
        return File.createTempFile(prefix, ".$suffix")
    }*/

    /**
     * Reconstruct all files included in the recording at the timestamp specified in `time`.
     * For each file, iterate over all changesets and apply those that came before `time`.
     * Finally, write the reconstructed content back to the file.
     */
    private fun reconstructFiles() {
        fileContents.forEach { (path, checkpoint) ->
            fileChangeData[path]?.let {
                it.forEach changesetLoop@{ changeset ->
                    if (changeset.endTime > time && time != 0.0) {
                        return@changesetLoop
                    }
                    changeset.changes.forEach { change ->
                        checkpoint.text =
                            checkpoint.text.replaceRange(
                                change.offset,
                                change.offset + change.oldText.length,
                                change.newText,
                            )
                    }
                    checkpoint.elementOffsets = changeset.elementOffsets
                }
            }
            rewriteFile(path, checkpoint.text)
        }
    }

    /**
     * Replace the content of a file.
     * @param path Path to the file.
     * @param newContent Text that will replace the current content of the file.
     */
    private fun rewriteFile(
        path: String,
        newContent: String,
    ) = runWriteActionAndWait {
        LocalFileSystem.getInstance().findFileByPath(path)?.let { file ->
            FileDocumentManager.getInstance().getDocument(file)?.let { document ->
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setReadOnly(false)
                    document.setText(newContent)
                }
            }
        }
    }

    /**
     * Execute the highlighting script or command.
     * This script should read sensorData.json and gazeData.json and calculate a highlighting value for each psiElement
     * based on the samples before the specified timestamp.
     * The results should be written back to highlighting.json.
     *
     * These results are then parsed by this function.
     */
    private fun runHighlightScript() {
        val command = CognitIDESettingsState.instance.highlightingCommand
        val commandWithParameters = "$command \"${saveFolder.absolutePath}\" $time"
        val process = Runtime.getRuntime().exec(commandWithParameters)
        process.waitFor()
        // TODO catch errors.

        elementHighlighting = Json.decodeFromString<Map<Int, Double>>(File(saveFolder, "highlighting.json").readText())
    }

    /**
     * Display the highlighting calculated by the highlighting script.
     */
    private fun displayHighlighting() =
        invokeAndWaitIfNeeded {
            val textAttributes = CognitIDEColors.getTextAttributesForElements(elementHighlighting)
            val highlightManager = HighlightManager.getInstance(project)

            EditorFactory.getInstance().allEditors.forEach { editor ->
                val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
                fileContents[virtualFile?.path]?.let { checkpoint ->
                    checkpoint.elementOffsets.forEach { elementIndex, (start, end) ->

                        highlightManager.addOccurrenceHighlight(
                            editor,
                            start,
                            end,
                            textAttributes[elementIndex],
                            HighlightManager.HIDE_BY_ESCAPE or HighlightManager.HIDE_BY_TEXT_CHANGE,
                            null,
                        )
                    }
                }
            }
        }
}
