package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class ExecuteShellCommandAction(command: String?, maxDuration: Duration?) : StudyUtilAction() {
    @Suppress("ktlint")
    private val command = command ?: "/Applications/VLC.app/Contents/MacOS/VLC --no-video-title-show --mouse-hide-timeout=1 --fullscreen \"/Users/franz/Downloads/bpm2016-w2-4-pip (720p).mp4\" vlc://quit"
    private val maxDuration = maxDuration ?: 10.minutes

    override fun actionPerformed(event: AnActionEvent) {
        val basePath = event.project?.basePath ?: return
        val commandParts: List<String> =
            command.split("\"").mapIndexed { idx, text ->
                // odd -> the text was written between two quotes (")
                if (idx % 2 == 1) return@mapIndexed listOf(text)
                return@mapIndexed text.split(" ")
            }.flatten().filter { it.isNotBlank() }

        ProcessBuilder(*commandParts.toTypedArray())
            .directory(File(basePath))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(maxDuration.toLong(DurationUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    }
}
