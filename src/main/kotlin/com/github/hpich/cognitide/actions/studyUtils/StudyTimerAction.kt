package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.*

class StudyTimerAction : SuspendableStudyAction {
    var durationInSeconds: Int = 5

    override suspend fun actionPerformed(e: AnActionEvent) {
        val durationInMillis = (durationInSeconds * 1000).toLong()
        delay(durationInMillis)
    }
}
