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
        JBColor.LIGHT_GRAY,
        JBColor.YELLOW,
        JBColor.ORANGE,
        JBColor.RED,
    )

    private val percentiles = arrayOf(
        1.0,
        0.2,
        0.1,
        0.05,
    )

    val LOOKED_ATTRIBUTES = colors.map {
        TextAttributesKey.createTextAttributesKey(
            "LOOKED_${it.rgb}",
            backgroundAttributes(it)
        )
    }

    fun assignColors(elements: Map<PsiElement, Double>): Map<Int, List<PsiElement>> {
        val assignedColors = colors.indices.associateWith { mutableListOf<PsiElement>() }
        val sortedElements = elements.entries.sortedByDescending { it.value }
        val size = elements.size
        var percentileIndex = percentiles.lastIndex
        sortedElements.forEachIndexed { index, (psiElement, value) ->
            while (percentileIndex > 0 && index + 1 > percentiles[percentileIndex] * size) {
                percentileIndex--
            }
            assignedColors[percentileIndex]?.add(psiElement)
        }

        thisLogger().info("Assigned colors! Percentiles were: \n"
                + percentiles.withIndex().joinToString("\n") {
            "(${it.value}) ${it.value * size} -> ${colors[it.index]}"
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