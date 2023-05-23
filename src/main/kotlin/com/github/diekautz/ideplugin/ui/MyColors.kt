package com.github.diekautz.ideplugin.ui

import com.intellij.openapi.editor.HighlighterColors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.ui.JBColor


object MyColors {
    val boarders = arrayOf(2, 4, 8, 16)

    private val colors = arrayOf(
        JBColor.GREEN,
        JBColor.YELLOW,
        JBColor.ORANGE,
        JBColor.RED,
        JBColor.PINK
    )

    val LOOKED_ATTRIBUTES = colors.map {
        TextAttributesKey.createTextAttributesKey(
            "LOOKED_${it.rgb}",
            backgroundAttributes(it)
        )
    }

    private fun backgroundAttributes(color: JBColor) =
        HighlighterColors.NO_HIGHLIGHTING.defaultAttributes.clone().apply {
            backgroundColor = color
            effectColor = JBColor.WHITE
            effectType = EffectType.BOXED
        }
}