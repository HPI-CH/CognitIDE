package com.github.diekautz.ideplugin.ui

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.HighlighterColors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.psi.PsiElement
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

    private val percentiles = arrayOf(
        0.0,
        0.1,
        0.2,
        0.3,
        0.4
    )

    val LOOKED_ATTRIBUTES = colors.map {
        TextAttributesKey.createTextAttributesKey(
            "LOOKED_${it.rgb}",
            backgroundAttributes(it)
        )
    }

    fun assignColors(elements: Map<PsiElement, Double>): Map<Int, List<PsiElement>> {
        val assignedColors = colors.indices.associateWith { mutableListOf<PsiElement>() }
        val total = elements.values.sum()
        val sortedElements = elements.entries.sortedByDescending { it.value }
        var colorIndex = colors.size - 1
        sortedElements.forEach { (psiElement, value) ->
            val percent = value / total
            while (colorIndex > 0 && percent < percentiles[colorIndex]) {
                colorIndex--
            }
            assignedColors[colorIndex]?.add(psiElement)
        }

        thisLogger().info("Assigned colors! Percentiles were: \n"
                + percentiles.withIndex().joinToString("\n") {
            "(${it.value}) ${it.value * total} -> ${colors[it.index]}"
        })
        return assignedColors
    }

    private fun backgroundAttributes(color: JBColor) =
        HighlighterColors.NO_HIGHLIGHTING.defaultAttributes.clone().apply {
            backgroundColor = color
            effectColor = JBColor.WHITE
            effectType = EffectType.BOXED
        }
}