package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class CloseAllFilesAction : StudyUtilAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project? = ProjectUtil.getActiveProject()

        if (project != null) {
            val files = FileEditorManager.getInstance(project).openFiles
            for (file in files) {
                FileEditorManager.getInstance(project).closeFile(file)
            }
        }
    }
}
