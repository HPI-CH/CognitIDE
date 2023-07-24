package com.github.diekautz.ideplugin.actions.debug

import com.github.diekautz.ideplugin.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem

class OpenAllFilesAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject?.service<DataCollectingService>()?.isHighlightAvailable ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val fileEditorManager = FileEditorManager.getInstance(e.project!!)
        e.project?.service<DataCollectingService>()?.getRecordedFiles()?.forEach { filePath ->
            val vFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            thisLogger().debug("Trying to open file: $filePath")
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