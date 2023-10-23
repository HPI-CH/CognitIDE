package com.github.hpich.cognitide.actions.debug

import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.MessageConstants
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.ui.UIUtil

class OpenAllFilesAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject?.service<DataCollectingService>()?.isHighlightAvailable ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val fileEditorManager = FileEditorManager.getInstance(e.project!!)
        val files = e.project!!.service<DataCollectingService>().getRecordedFiles()

        // check if tab limit settings collides
        if (UISettings.getInstance().editorTabLimit < files.size) {
            when (MessageDialogBuilder
                .yesNoCancel(
                    "Conflicting Settings",
                    "The settings editorTabLimit " +
                            "(how many editor tabs can be open at once) is ${UISettings.getInstance().editorTabLimit} " +
                            "but we would open ${files.size} tabs. Increase the limit?"
                )
                .icon(UIUtil.getWarningIcon())
                .show(e.project!!)) {
                MessageConstants.CANCEL -> return
                MessageConstants.YES -> UISettings.getInstance().editorTabLimit = files.size
            }
        }

        files.forEach { filePath ->
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