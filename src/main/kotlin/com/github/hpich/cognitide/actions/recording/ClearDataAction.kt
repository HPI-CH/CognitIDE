package com.github.hpich.cognitide.actions.recording

import com.github.hpich.cognitide.services.DataCollectingService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.MessageDialogBuilder

class ClearDataAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject?.service<DataCollectingService>()?.isAnyDataAvailable ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (MessageDialogBuilder
                .okCancel("Clear Recording", "Do you want to clear all recorded data?")
                .ask(e.project!!)
        ) {
            e.project?.service<DataCollectingService>()?.clearData()
        }
    }
}