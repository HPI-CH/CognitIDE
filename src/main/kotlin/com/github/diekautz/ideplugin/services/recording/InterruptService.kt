package com.github.diekautz.ideplugin.services.recording

import com.github.diekautz.ideplugin.config.OpenEyeSettingsState
import com.github.diekautz.ideplugin.services.TobiiProService
import com.github.diekautz.ideplugin.utils.infoMsg
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.timerTask

@Service(Service.Level.PROJECT)
class InterruptService(val project: Project) {
    private val tobiiProService = project.service<TobiiProService>()
    private val settings = OpenEyeSettingsState.instance

    var recordedInterrupts = mutableListOf<UserInterrupt>()

    private var timer: Timer = Timer("InterruptUserTimer")

    fun startInterrupting() {
        if (!settings.interruptUser) return
        recordedInterrupts.clear()
        stopInterrupting()

        newTimer()
        thisLogger().info("Started interrupt timer!")
    }

    private fun newTimer() {
        timer.schedule(timerTask { interruptUser() }, settings.interruptDelay * 1_000L)
    }

    fun stopInterrupting() {
        timer.purge()
    }

    private fun interruptUser() = invokeLater {
        val interruptStart = System.currentTimeMillis()
        val response = Messages.showInputDialog(
            project,
            "Please pause your workings and answer the verbal questions.",
            "Interrupt ${recordedInterrupts.size + 1}/${settings.interruptCount}",
            null
        )
        val interruptEnd = System.currentTimeMillis()
        if (response == null) {
            project.infoMsg("Stopping recording, user cancelled interrupt!")
            tobiiProService.stopRecording()
            return@invokeLater
        }
        val userInterrupt = UserInterrupt(
            interruptStart,
            interruptEnd,
            response
        )
        recordedInterrupts += userInterrupt
        thisLogger().info("Interrupt ${recordedInterrupts.size} recorded $userInterrupt")

        if (settings.interruptStopRecordingAfterLast && recordedInterrupts.size >= settings.interruptCount) {
            thisLogger().info("All interrupts recorded. Stopping recording.")
            tobiiProService.stopRecording()
        }
        if (settings.interruptUser && recordedInterrupts.size < settings.interruptCount) {
            newTimer()
        } else {
            thisLogger().info("All interrupts recorded. Purging timer.")
        }
    }
}