package com.github.hpich.cognitide.ui

import com.github.hpich.cognitide.services.dto.LookElement
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.ui.JBColor
import java.awt.Color



object CognitIDEColors {

    private val baseColor = JBColor(0xFF4000, 0xFF4000)
    private var colors: Array<JBColor>? = null

    var LOOKED_ATTRIBUTES: List<TextAttributesKey> = emptyList()

    fun assignColors(elements: Map<LookElement, Double>): Map<Int, List<LookElement>> {
        colors = Array(elements.size) { baseColor }
        val assignedColors = colors?.indices?.associateWith { mutableListOf<LookElement>() }
        val normalizedElements = elements
            .filterNot { it.value.isNaN() }
            .let { filteredElements ->
                val min = filteredElements.minOf { it.value }
                val max = filteredElements.maxOf { it.value }

                filteredElements.mapValues { (key, value) ->
                    (value - min) / (max - min)
                }
            }
        val minAlpha = 0.1
        val transparencyElements = normalizedElements.mapValues {(key, value) -> ((value * (1 - minAlpha)) + minAlpha) * 0xFF}

        transparencyElements.entries.forEachIndexed { index, entry -> colors?.set(index, JBColor(((entry.value.toInt() shl 24) or (baseColor.rgb and 0x00FFFFFF)),((entry.value.toInt() shl 24) or (baseColor.rgb and 0x00FFFFFF)))) }

        transparencyElements.entries.forEachIndexed { index, (psiElement, _) ->
            assignedColors?.get(index)?.add(psiElement)
        }
        LOOKED_ATTRIBUTES = colors!!.map {
            TextAttributesKey.createTextAttributesKey(
                "LOOKED_${it.rgb}",
                backgroundAttributes(it)
            )
        }

        return assignedColors!!
    }

    private fun backgroundAttributes(color: JBColor) =
        HighlighterColors.NO_HIGHLIGHTING.defaultAttributes.clone().apply {
            backgroundColor = color
            effectColor = Color.WHITE
            effectType = EffectType.BOXED
        }
}