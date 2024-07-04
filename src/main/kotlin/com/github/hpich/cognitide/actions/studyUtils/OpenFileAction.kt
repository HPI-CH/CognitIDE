package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.diff.applications.DiffApplicationBase.findOrCreateFile
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class OpenFileAction(private val filePath: String) : StudyUtilAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project? = ProjectUtil.getActiveProject()

        if (project != null) {
            val file: VirtualFile? = findOrCreateFile(filePath, project.basePath)

            if (file != null) {
                FileEditorManager.getInstance(project).openFile(file, true)
            }
        }
    }
}
