package com.github.hpich.cognitide.utils

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DoNotAskOption
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages

fun showMessageWithDoNotAskOption(
    project: Project?,
    title: String,
    message: String,
    doNotAskKey: String,
    action: () -> Unit,
) {
    val doNotAskOption =
        object : DoNotAskOption.Adapter() {
            override fun rememberChoice(
                isSelected: Boolean,
                exitCode: Int,
            ) {
                if (isSelected && exitCode == Messages.YES) {
                    PropertiesComponent.getInstance().setValue(doNotAskKey, true)
                }
            }

            override fun getDoNotShowMessage(): String {
                return "Do not ask again"
            }
        }

    val doNotAskAgain = PropertiesComponent.getInstance().getBoolean(doNotAskKey, false)
    if (doNotAskAgain) {
        action()
        return
    }

    when (
        MessageDialogBuilder.yesNo(title, message)
            .asWarning()
            .doNotAsk(doNotAskOption)
            .ask(project)
    ) {
        true -> action()
        false -> return
    }
}

/**
 * Resets all "Do not ask again" messages.
 */
fun resetDoNotAskAgain() {
    PropertiesComponent.getInstance().unsetValue("highlight.doNotAskAgain")
}
