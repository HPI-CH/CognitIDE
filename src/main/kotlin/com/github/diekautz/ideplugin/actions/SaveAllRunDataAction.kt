package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.services.recording.MyLookRecorderService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import kotlin.random.Random

class SaveAllRunDataAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val participantId = Messages.showInputDialog(
            e.project,
            "Participant ID:",
            "Input Participant ID",
            null,
            Random.nextInt().mod(10000).toString(),
            intInputValidator
        ) ?: return
        e.project?.service<MyLookRecorderService>()?.askAndSaveBoth(participantId.toInt())
    }

    private val intInputValidator = object : InputValidator {
        override fun checkInput(inputString: String) = (inputString.toIntOrNull() ?: -1) >= 0

        override fun canClose(inputString: String) = checkInput(inputString)
    }
}