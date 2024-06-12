package com.github.hpich.cognitide.services.recording

import com.github.hpich.cognitide.services.StudyRecorder
import com.github.hpich.cognitide.services.dto.FileChange
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import edu.ucsd.sccn.LSL
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Class to keep track of file changes.
 * Listens for changes on documents and stores all relevant changes to these documents in a local buffer.
 * These changes can be requested with getGroupedChanges(), which groups together changes that happen in
 * short succession.
 * At the end of recording getAllRemainingChanges() returns all buffered changes that are still buffered regardless
 * of their timing.
 * Also stores an initial text for all files with changes, that can be requested with getInitialFileContents().
 * This text can be used to reconstruct all later versions of the file using the tracked changes.
 * @param recorder StudyRecorder using this tracker.
 * @param changesetDelay All events with less than this time between will be grouped together.
 */
class FileChangeTracker(recorder: StudyRecorder, private val changesetDelay: Double) :
    DocumentListener {
    // keys are file paths
    private val initialTexts = mutableMapOf<String, String>()
    private val changes = mutableMapOf<String, MutableList<FileChange>>()
    private val nextChangesetTime = mutableMapOf<String, Double>()

    // to avoid processing duplicate events twice, we store identifiers of recently processed events.
    private val recentChangeEvents = mutableSetOf<String>()

    init {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(this, recorder)
    }

    /**
     * For each file get all buffered changes, if the time since the last change is larger than changesetDelay.
     * Clear the buffer for each file where changes are returned.
     * @param ignoreTime: if this is set to `true`, all changes will be returned, even if the time passed since then is
     *        smaller than changesetDelay.
     * @return map(filepath -> list of changes)
     */
    fun getGroupedChanges(ignoreTime: Boolean = false): MutableMap<String, MutableList<FileChange>> {
        val changeLists = mutableMapOf<String, MutableList<FileChange>>()
        changes.forEach { (path, changeList) ->
            if (ignoreTime || LSL.local_clock() >= nextChangesetTime.getOrDefault(path, 0.0)) {
                changeLists[path] = changeList.toMutableList()
                changeList.clear()
            }
        }
        return changeLists
    }

    /**
     * For each file get all new changes. (Even if the time since the last change is smaller than changesetDelay).
     * This should be called at the end of a recording to make sure we don't discard changes done shortly before
     * stopping the recording.
     * Clear the buffer for each file.
     * @return map(filepath -> list of changes)
     */
    fun getAllRemainingChanges(): MutableMap<String, MutableList<FileChange>> {
        return getGroupedChanges(true)
    }

    /**
     * Get the original file content for all files with changes.
     * @return map(filepath -> file content)
     */
    fun getInitialFileContents(): Map<String, String> {
        return initialTexts
    }

    /**
     * Before a document is changed, collect the original content of the file, if this is the first change.
     */
    override fun beforeDocumentChange(event: DocumentEvent) {
        val document = event.document
        val virtualFile = FileDocumentManager.getInstance().getFile(document) ?: return
        // Changes other than whitespaces will trigger two events, one for a VirtualFileImpl with
        // path = [absolute/path/to/file] and one for a LightVirtualFile with path = [filename].
        // To avoid duplications we only want to track the VirtualFilImpl files which represent files in our
        // local file system.
        if (!virtualFile.isInLocalFileSystem) {
            return
        }
        initialTexts.putIfAbsent(virtualFile.path, document.text)
    }

    /**
     * When a document is changed, collect the file change in a buffer.
     * The buffered changes can be requested with getGroupedChanges() or getAllRemainingChanges().
     */
    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        val virtualFile = FileDocumentManager.getInstance().getFile(document) ?: return
        // Changes other than whitespaces will trigger two events, one for a VirtualFileImpl with
        // path = [absolute/path/to/file] and one for a LightVirtualFile with path = [filename].
        // To avoid duplications we only want to track the VirtualFilImpl files which represent files in our
        // local file system.
        // Also, it can happen that we receive the event for the same file multiple times. We detect this by generating
        // an identifier string and checking if an identical event was processed recently.

        val eventIdentifier =
            event.oldTimeStamp.toString() + ":" +
                virtualFile.path + ":" +
                event.offset + ":" +
                event.oldFragment + ":" +
                event.newFragment
        if (!virtualFile.isInLocalFileSystem || !recentChangeEvents.add(eventIdentifier)) {
            return
        }
        Timer().schedule(timerTask { recentChangeEvents.remove(eventIdentifier) }, 1000)

        changes.getOrPut(virtualFile.path) { mutableListOf() }.add(
            FileChange(
                LSL.local_clock(),
                event.offset,
                event.oldFragment.toString(),
                event.newFragment.toString(),
            ),
        )

        nextChangesetTime[virtualFile.path] = LSL.local_clock() + changesetDelay
    }
}
