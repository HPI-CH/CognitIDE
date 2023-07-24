package com.github.diekautz.ideplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

abstract class StudyRecorder(
    project: Project,
    progressTitle: String
) : Task.Backgroundable(project, progressTitle, true), Disposable {
    protected val dataCollectingService: DataCollectingService = project.service<DataCollectingService>()
    protected open val delay = 100L
    val isRunning: Boolean
        get() = shouldRun

    fun startRecording() {
        shouldRun = true
        ProgressManager.getInstance().run(this)
    }

    fun stopRecording() {
        shouldRun = false
    }

    private var shouldRun = false
    final override fun run(indicator: ProgressIndicator) {
        runBlocking {
            indicator.isIndeterminate = true
            if (!setup(indicator)) return@runBlocking
            while (shouldRun) {
                if (indicator.isCanceled) {
                    dataCollectingService.stopRecording()
                    return@runBlocking
                }
                loop(indicator)
                delay(delay)
            }
            indicator.isIndeterminate = true
            Disposer.dispose(this@StudyRecorder)
        }
    }

    abstract fun loop(indicator: ProgressIndicator)

    open fun setup(indicator: ProgressIndicator): Boolean = true
}