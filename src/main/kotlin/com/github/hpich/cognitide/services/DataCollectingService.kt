package com.github.hpich.cognitide.services

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.extensions.removeAllHighlighters
import com.github.hpich.cognitide.services.dto.*
import com.github.hpich.cognitide.services.recording.InterruptService
import com.github.hpich.cognitide.services.recording.UserInterrupt
import com.github.hpich.cognitide.utils.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.rd.util.getOrCreate
import java.io.File
import java.time.Instant
import java.util.*

@Service(Service.Level.PROJECT)
class DataCollectingService(val project: Project) {
    private var currentRecorder: StudyRecorder? = null
    private var interruptService = InterruptService(project, this)

    val isRecording: Boolean
        get() = currentRecorder?.isRunning ?: false

    var wasHighlighted = false
    val isHighlightAvailable: Boolean
        get() = latestRecordingSaveFolder != null

    val isAnyDataAvailable: Boolean
        get() = sensorData.isNotEmpty() || gazeData.isNotEmpty() || fileChangeData.isNotEmpty() || initialFileContents.isNotEmpty()

    val userInterruptCount: Int
        get() = userInterruptList.size

    fun setRecorder(recorder: StudyRecorder) {
        if (isRecording) return
        currentRecorder = recorder
    }

    fun startRecording() {
        if (isRecording) return
        if (currentRecorder == null) {
            thisLogger().error("No recorder selected!")
            return
        }

        currentRecorder!!.startRecording()
        interruptService.startInterrupting()
    }

    fun stopRecording() {
        currentRecorder?.stopRecording()
        interruptService.stopInterrupting()

        sortRecordedData()
    }

    // map(sensor name -> sensor samples).
    private val sensorData = mutableMapOf<String, MutableList<SensorSample>>()

    // map(element id -> gaze samples).
    private val gazeData = mutableMapOf<Int, MutableList<GazeSample>>()

    // map(smart pointer to psi element -> element id stored in serialized data).
    private val psiElementIds = mutableMapOf<SmartPsiElementPointer<PsiElement>, Int>()

    // map(file path -> smart pointers to psi elements in file).
    private val elementsInFile = mutableMapOf<String, MutableSet<SmartPsiElementPointer<PsiElement>>>()

    // map(psi element -> smart pointer to psi element).
    private val elementPointers = mutableMapOf<PsiElement, SmartPsiElementPointer<PsiElement>>()

    // map(file path -> changesets).
    private val fileChangeData = mutableMapOf<String, MutableList<FileChangeset>>()

    // map(file path -> file checkpoint).
    private val initialFileContents = mutableMapOf<String, FileCheckpoint>()

    private val userInterruptList = mutableListOf<UserInterrupt>()

    private var latestRecordingSaveFolder: File? = null

    fun stats() =
        "interrupts: $userInterruptCount/${CognitIDESettingsState.instance.interruptCount}, " +
            "tracked files: ${initialFileContents.size}, elements: ${psiElementIds.size}"

    /**
     * Add new sensor sample to the list of recorded samples.
     * @param sensorName name of the sensor.
     * @param samples List of SensorSamples containing timestamp and data for all channels.
     */
    fun addSensorSamples(
        sensorName: String,
        samples: List<SensorSample>,
    ) {
        sensorData.getOrCreate(sensorName) { mutableListOf() }.addAll(samples)
    }

    /**
     * Add new GazeSample to the list of recorded gaze samples for a PsiElement.
     * Since the PsiElements can become invalidated when the file gets parsed again at some point, we create a
     * SmartPsiElementPointer, that can survive a reparse, for each relevant PsiElement and map the PsiElement to its
     * pointer.
     * To enable serialization, the SmartPsiElementPointer is mapped to an Int id.
     * @param psiElement PsiElement to which the GazeSample belongs.
     * @param samples List of GazeSamples containing timestamp and distance of gaze to PsiElement.
     */
    fun addGazeSamples(
        psiElement: PsiElement,
        samples: List<GazeSample>,
    ) {
        val elementPointer =
            elementPointers.getOrPut(psiElement) {
                SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElement)
            }
        val id = psiElementIds.getOrPut(elementPointer) { psiElementIds.size }

        gazeData.getOrCreate(id) { mutableListOf() }.addAll(samples)

        ApplicationManager.getApplication().runReadAction {
            val file = psiElement.containingFile.virtualFile ?: return@runReadAction
            // If not present yet, add psi element to list of elements in this file
            elementsInFile.getOrCreate(file.path) { mutableSetOf() }.add(elementPointer)

            // If not present yet, add psi element to position map of latest change or initial checkpoint.
            val positionMap = currentPositionMap(file)
            positionMap.putIfAbsent(psiElementIds[elementPointer]!!, Pair(psiElement.startOffset, psiElement.endOffset))
        }
    }

    /**
     * Add new file changesets to recorded data.
     * Takes a map with a list of FileChanges for each file path. These changes will be grouped into a single ChangeSet.
     * For each changeset determine the offset of all PsiElements still contained in the file afterward and save them
     * with the changeset.
     * @param changeLists map(file path -> list of file changes)
     */
    fun addFileChanges(changeLists: Map<String, MutableList<FileChange>>) {
        changeLists.forEach { (path, changeList) ->
            if (changeList.isEmpty()) return@forEach

            ApplicationManager.getApplication().runReadAction {
                // Get positions of all psi elements still contained in file
                val positionMap =
                    elementsInFile[path]?.mapNotNull { elementPointer ->
                        val element = elementPointer.element ?: return@mapNotNull null

                        if (element.parent == null || !element.isValid || !psiElementIds.containsKey(elementPointer)) {
                            null
                        } else {
                            psiElementIds[elementPointer]!! to Pair(element.startOffset, element.endOffset)
                        }
                    }?.toMap() ?: emptyMap()

                // Create file changeset.
                fileChangeData.getOrCreate(path) { mutableListOf() }.add(
                    FileChangeset(
                        changeList.first().time,
                        changeList.last().time,
                        changeList.toMutableList(),
                        positionMap.toMutableMap(),
                    ),
                )
            }
        }
    }

    /**
     * Update texts of initial file checkpoints.
     * This function can be called with the changes from FileChangeTracker to make sure that reconstruction of the files
     * will work correctly.
     *
     * Why this is necessary:
     * If the user changes something and then looks at an element for the first time before the changeset is complete,
     * it will be created with the text during the first gaze and this will be inconsistent with the changeset.
     * In this case updating the text will make the position map of the initial checkpoint inconsistent. This is not a
     * problem because if this happens, there are no gazes before the first changeset (and the gazes during the changeset
     * should be discarded anyway).
     *
     * @param initialTexts initial texts as tracked by FileChangeTracker.
     */
    fun updateInitialTexts(initialTexts: Map<String, String>) {
        initialTexts.forEach { (path, text) ->
            initialFileContents.getOrCreate(path) { FileCheckpoint("", mutableMapOf()) }.text = text
        }
    }

    /**
     * Get position map to register new psi elements in.
     * This is always the map from the latest changeset or if there is no changeset yet,
     * the initial checkpoint of a file.
     * If there is also no initial checkpoint, it will be created with the current content of the file.
     * @param virtualFile file for which the current position map is returned.
     * @return position map of last changeset or initial checkpoint.
     */
    private fun currentPositionMap(virtualFile: VirtualFile): MutableMap<Int, Pair<Int, Int>> {
        val changesets = fileChangeData.getOrCreate(virtualFile.path) { mutableListOf() }
        if (changesets.isNotEmpty()) {
            return changesets.last().elementOffsets
        }
        val initialContent =
            initialFileContents.getOrCreate(virtualFile.path) {
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)
                FileCheckpoint(document?.text ?: "", mutableMapOf())
            }
        return initialContent.elementOffsets
    }

    /**
     * Sort recorded sensor data and gaze data according to timestamps of samples.
     * This should be done before saving a recording to disc.
     */
    private fun sortRecordedData() {
        sensorData.forEach { (_, samples) ->
            samples.sort()
        }
        gazeData.forEach { (_, samples) ->
            samples.sort()
        }
    }

    fun addUserInterrupt(
        startMillis: Long,
        endMillis: Long,
        answer: String = "",
    ) {
        UserInterrupt(startMillis, endMillis, answer).let {
            userInterruptList.add(it)
            thisLogger().debug("UserInterrupt added: $it")
        }
    }

    fun clearData() {
        EditorFactory.getInstance().removeAllHighlighters()
        wasHighlighted = false

        currentRecorder?.stopRecording()
        currentRecorder = null
        interruptService.stopInterrupting()
        interruptService = InterruptService(project, this)

        userInterruptList.clear()

        sensorData.clear()
        gazeData.clear()
        psiElementIds.clear()
        elementsInFile.clear()
        fileChangeData.clear()
        initialFileContents.clear()
    }

    /**
     * Save the current recording to disk.
     */
    fun saveToDisk() {
        latestRecordingSaveFolder =
            saveRecordingToDisk(
                project,
                Date.from(Instant.now()),
                sensorData,
                gazeData,
                initialFileContents,
                fileChangeData,
                userInterruptList,
            )
    }

    /**
     * Highlight the recorded data of the latest recording saved to disk.
     * If no recording was saved since the last IDE start, does nothing.
     */
    fun highlightLatestRecording() {
        latestRecordingSaveFolder?.let { highlightRecording(it) }
    }

    /**
     * Highlight the recorded data for a specified recording.
     * @param saveFolder The folder containing the recording.
     */
    fun highlightRecording(saveFolder: File) {
        ProgressManager.getInstance().run(Highlighter(project, saveFolder))
    }

    fun getRecordedFiles() = initialFileContents.keys.toList()
}
