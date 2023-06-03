package com.github.diekautz.ideplugin.actions

import com.github.diekautz.ideplugin.services.MyLSLService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ScanLSLStreamsAction: AnAction() {

    override fun update(e: AnActionEvent) {
        val currentProject = e.project
        e.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project!!.service<MyLSLService>().resolveStreams()
    }
}