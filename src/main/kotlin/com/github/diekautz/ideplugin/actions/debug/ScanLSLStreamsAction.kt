package com.github.diekautz.ideplugin.actions.debug

import com.github.diekautz.ideplugin.services.debug.LSLService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ScanLSLStreamsAction: AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabled = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project!!.service<LSLService>().resolveStreams()
    }
}