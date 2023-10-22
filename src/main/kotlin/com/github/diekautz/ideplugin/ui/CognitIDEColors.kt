package com.github.diekautz.ideplugin.ui

import com.github.diekautz.ideplugin.services.dto.LookElement
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.ui.JBColor
import java.awt.Color



object CognitIDEColors {

    private val percentiles = arrayOf(
        1.0,
        0.4,
        0.15,
        0.08,
        0.05,
        0.02,
    )

    private val baseColor = JBColor(0xFF4000, 0xFF4000)
    private val colors = Array(percentiles.size + 1) { baseColor }

    init {
        val alphaStep = 0xFF / (percentiles.size + 1)
        percentiles.forEachIndexed { index, _ ->
            val rgb = baseColor.rgb + (alphaStep * (index + 1)).shl(24)
            colors[index] = JBColor(rgb, rgb)
        }
    }

    val LOOKED_ATTRIBUTES = colors.map {
        TextAttributesKey.createTextAttributesKey(
            "LOOKED_${it.rgb}",
            backgroundAttributes(it)
        )
    }

    fun assignColors(elements: Map<LookElement, Double>): Map<Int, List<LookElement>> {
        val assignedColors = colors.indices.associateWith { mutableListOf<LookElement>() }
        val sortedElements = elements.entries.sortedByDescending { it.value }
        val size = elements.size
        var percentileIndex = percentiles.lastIndex
        sortedElements.forEachIndexed { index, (psiElement, _) ->
            while (percentileIndex > 0 && index + 1 > percentiles[percentileIndex] * size) {
                percentileIndex--
            }
            assignedColors[percentileIndex]?.add(psiElement)
        }
        return assignedColors
    }

    private fun backgroundAttributes(color: JBColor) =
        HighlighterColors.NO_HIGHLIGHTING.defaultAttributes.clone().apply {
            backgroundColor = color
            effectColor = Color.WHITE
            effectType = EffectType.BOXED
        }
}