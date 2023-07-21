package com.github.diekautz.ideplugin.utils

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages


fun Project.infoMsg(message: String, logger: Logger? = null, title: String = "OpenEye Info") {
    logger?.info(message)
    invokeLater {
        Messages.showInfoMessage(this, message, title)
    }
}

fun Project.errorMsg(
    message: String,
    logger: Logger? = null,
    throwable: Exception? = null,
    title: String = "OpenEye Error"
) {
    logger?.error(message, throwable)
    invokeLater {
        Messages.showInfoMessage(this, message, title)
    }
}