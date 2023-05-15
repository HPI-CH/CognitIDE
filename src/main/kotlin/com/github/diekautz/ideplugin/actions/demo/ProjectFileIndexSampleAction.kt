package com.github.diekautz.ideplugin.actions.demo

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages

class ProjectFileIndexSampleAction : AnAction() {
    override fun update(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(CommonDataKeys.EDITOR)
        val visibility = project != null && editor != null
        event.presentation.isEnabledAndVisible = visibility
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (project == null || editor == null) {
            return
        }
        val document = editor.document
        val fileDocumentManager = FileDocumentManager.getInstance()
        val virtualFile = fileDocumentManager.getFile(document)
        val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
        if (virtualFile != null) {
            val module = projectFileIndex.getModuleForFile(virtualFile)
            val moduleName = module?.name ?: "No module defined for file"
            val moduleContentRoot = projectFileIndex.getContentRootForFile(virtualFile)
            val isLibraryFile = projectFileIndex.isLibraryClassFile(virtualFile)
            val isInLibraryClasses = projectFileIndex.isInLibraryClasses(virtualFile)
            val isInLibrarySource = projectFileIndex.isInLibrarySource(virtualFile)
            Messages.showInfoMessage(
                "Module: " + moduleName + "\n" +
                        "Module content root: " + moduleContentRoot + "\n" +
                        "Is library file: " + isLibraryFile + "\n" +
                        "Is in library classes: " + isInLibraryClasses +
                        ", Is in library source: " + isInLibrarySource,
                "Main File Info for" + virtualFile.name
            )
        }
    }
}