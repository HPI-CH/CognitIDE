package com.github.diekautz.ideplugin.services

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

abstract class RecordingService(
    private val project: Project,
    protected val dataCollectingService: DataCollectingService,
    val title: String
) {
    protected val delay = 100L
    val isRunning: Boolean
        get() = task.isRunning

    fun startRecording() {
        task.isRunning = true
        ProgressManager.getInstance().run(task)
    }

    fun stopRecording() {
        task.isRunning = false
    }

    private val task = object : Task.Backgroundable(project, title, true) {
        var isRunning = true
        override fun run(indicator: ProgressIndicator) {
            runBlocking {
                indicator.isIndeterminate = true
                while (isRunning) {
                    this@RecordingService.run(indicator)
                    delay(delay)
                }
            }
        }
    }

    abstract fun run(indicator: ProgressIndicator)
}