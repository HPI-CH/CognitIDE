package com.github.diekautz.ideplugin.services.recording

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable

@Serializable
data class SerializableElementGaze(
    val text: String,
    val path: String,
    val offset: Int,
    val gazeWeight: Double,
) {
    constructor(psiElement: PsiElement, gazeWeight: Double) : this(
        psiElement.text,
        psiElement.containingFile.virtualFile.path,
        psiElement.textOffset,
        gazeWeight
    )
}
