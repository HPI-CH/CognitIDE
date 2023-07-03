package com.github.diekautz.ideplugin.services.recording

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable

@Serializable
data class SerializableElement(
    val text: String,
    val path: String,
    val offset: Int
) {
    constructor(psiElement: PsiElement) : this(
        psiElement.text,
        psiElement.containingFile.virtualFile.path,
        psiElement.textOffset
    )
}
