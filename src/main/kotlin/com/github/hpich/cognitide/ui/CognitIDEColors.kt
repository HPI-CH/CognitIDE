package com.github.hpich.cognitide.ui

import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor

/**
 * Helper object to generate text attributes with correct colors for highlighting.
 * You call `getTextAttributesForElements` to generate the correct TextAttributeKeys for you highlighting intensities.
 */
object CognitIDEColors {
    private val baseColor = JBColor(0xFF4000, 0xFF4000)
    private const val MIN_ALPHA = 0.1

    /**
     * Normalize all highlighting intensity to the value range 0-1.
     * @param highlighting map(element id, highlighting intensity)
     * @return map(element id, normalized highlighting intensity)
     */
    private fun normalizeHighlightingIntensity(highlighting: Map<Int, Double>): Map<Int, Double> {
        if (highlighting.isEmpty()) return emptyMap()

        val min = highlighting.minOf { (_, intensity) -> intensity }
        val max = highlighting.maxOf { (_, intensity) -> intensity }
        return highlighting.mapValues { (_, intensity) -> (intensity - min) / (max - min) }
    }

    /**
     * Applies the specified transparency to the baseColor.
     * @param transparency The desired transparency.
     * @return modified baseColor with correct transparency.
     */
    private fun baseColorWithTransparency(transparency: Double): JBColor {
        // Convert transparency to single byte integer and shift it to correct position for color code.
        val alphaByte = (transparency * 0xFF).toInt() shl 24
        // Zero out alpha-byte from baseColor with binary-and and apply new alpha-byte with binary or.
        val color = alphaByte or (baseColor.rgb and 0x00FFFFFF)
        return JBColor(color, color)
    }

    /**
     * Create the TextAttributes for a JBColor.
     * @param color The color for which the attributes will be created.
     * @return The created TextAttributes.
     */
    private fun createColorAttributes(color: JBColor): TextAttributes =
        HighlighterColors.NO_HIGHLIGHTING.defaultAttributes.clone().apply {
            backgroundColor = color
            effectColor = JBColor.WHITE
            effectType = EffectType.BOXED
        }

    /**
     * Generate the TextAttributeKeys for the colors corresponding to the highlighting intensity (that was calculated
     * by the highlighting script).
     * Intensity values are normalized and then mapped to a color. The color is calculated by taking a base color and
     * changing the alpha value based on the normalized intensity.
     * @param highlighting map(element id, highlighting intensity)
     * @return map(element id, text attributes key with colors corresponding to highlighting intensity)
     */
    fun getTextAttributesForElements(highlighting: Map<Int, Double>): Map<Int, TextAttributesKey> {
        val normalizedHighlighting = normalizeHighlightingIntensity(highlighting)
        return normalizedHighlighting.mapValues { (_, intensity) ->
            val transparency = ((intensity * (1 - MIN_ALPHA) + MIN_ALPHA))
            val color = baseColorWithTransparency(transparency)
            /*
            We use a deprecated function here.
            For normal use cases, the intended way for highlighting is to specify a TextAttributesKey with a fallback
            key and rely on different color schemes to define the actual colors of these. Since we want to control
            the highlighting color directly we can't simply use this approach.
            This works perfectly fine right now but could become a problem if the deprecated function gets removed in
            the future.
            An alternative would be, to define 256 colors for all intensities for all color schemes we want to support.
             */
            @Suppress("DEPRECATION")
            TextAttributesKey.createTextAttributesKey("LOOKED_${color.rgb}", createColorAttributes(color))
        }
    }
}
