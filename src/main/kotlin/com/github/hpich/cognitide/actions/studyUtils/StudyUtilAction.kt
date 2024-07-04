package com.github.hpich.cognitide.actions.studyUtils

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

interface StudyAction

interface SuspendableStudyAction : StudyAction {
    suspend fun actionPerformed(e: AnActionEvent)
}

abstract class StudyUtilAction : AnAction(), StudyAction
