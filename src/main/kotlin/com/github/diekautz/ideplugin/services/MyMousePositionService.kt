package com.github.diekautz.ideplugin.services

import com.github.diekautz.ideplugin.utils.screenToLogicalInEditor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import kotlinx.coroutines.*
import java.awt.MouseInfo
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class MyMousePositionService(val project: Project) {

    private var refreshJob: Job? = null

    fun trackMouse() {
        thisLogger().debug("Track mouse started!")

        ProgressManager.getInstance().run(task)
        refreshJob?.start()
    }

    private val task = object : Task.Backgroundable(project, "Recording Mouse", true) {
        override fun run(indicator: ProgressIndicator) {
            runBlocking {
                indicator.isIndeterminate = true
                while (true) {
                    if(indicator.isCanceled) return@runBlocking
                    val mousePoint = MouseInfo.getPointerInfo().location

                    invokeLater {
                        FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->

                            val logicalPoint = screenToLogicalInEditor(editor, mousePoint)
                            indicator.text = "${logicalPoint.line}:${logicalPoint.column}"
                            thisLogger().info(logicalPoint.toString())
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

}