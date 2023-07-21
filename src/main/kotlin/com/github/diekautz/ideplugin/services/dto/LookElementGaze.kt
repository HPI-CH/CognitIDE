package com.github.diekautz.ideplugin.services.dto

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable

@Serializable
data class LookElementGaze(
    val lookElement: LookElement,
    val gazeWeight: Double,
) {
    constructor(psiElement: PsiElement, gazeWeight: Double) : this(
        LookElement(
            psiElement.text,
            psiElement.containingFile.virtualFile.path,
            psiElement.textOffset
        ),
        gazeWeight
    )
}
