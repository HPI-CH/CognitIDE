package com.github.hpich.cognitide.services.recording

import com.github.hpich.cognitide.config.CognitIDESettingsState
import com.github.hpich.cognitide.services.DataCollectingService
import com.github.hpich.cognitide.utils.infoMsg
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.util.*
import kotlin.concurrent.timerTask

class InterruptService(
    private val project: Project,
    private val dataCollectingService: DataCollectingService
) {
    private val settings = CognitIDESettingsState.instance

    private var timer: Timer = Timer("InterruptUserTimer")
    private val numInterrupted: Int
        get() = dataCollectingService.userInterruptCount

    fun startInterrupting() {
        if (!settings.interruptUser) return

        newTimer()
        thisLogger().info("Started interrupt timer!")
    }

    private fun newTimer() {
        timer.schedule(timerTask { interruptUser() }, settings.interruptDelay * 1_000L)
    }

    fun stopInterrupting() {
        timer.cancel()
        timer.purge()
        timer = Timer("InterruptUserTimer")
        thisLogger().info("Canceled interrupt timer!")
    }

    private fun interruptUser() = invokeLater {
        val interruptStart = System.currentTimeMillis()
        val response = Messages.showInputDialog(
            project,
            "Please pause your workings and answer the verbal questions.",
            "Interrupt ${numInterrupted + 1}/${settings.interruptCount}",
            null
        )
        val interruptEnd = System.currentTimeMillis()
        if (response == null) {
            project.infoMsg("Stopping recording, user cancelled interrupt!")
            dataCollectingService.stopRecording()
            return@invokeLater
        }
        dataCollectingService.addUserInterrupt(interruptStart, interruptEnd, response)

        if (settings.interruptStopRecordingAfterLast && numInterrupted >= settings.interruptCount) {
            thisLogger().info("All interrupts recorded. Stopping recording.")
            dataCollectingService.stopRecording()
        }
        if (settings.interruptUser && numInterrupted < settings.interruptCount) {
            newTimer()
        } else {
            thisLogger().info("All interrupts recorded. Purging timer.")
        }
    }
}