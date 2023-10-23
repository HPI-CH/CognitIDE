package com.github.hpich.cognitide.actions.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage


class CalculateScreenBrightnessAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val rect = Rectangle(Toolkit.getDefaultToolkit().screenSize)
        val capture = Robot().createScreenCapture(rect)

        val lum = lum(capture)
        Messages.showInfoMessage("Average Luminance is $lum", "Luminance")
    }
    private fun lum(img: BufferedImage): Double {
        val width = img.width
        val height = img.height
        var average = 0.0
        var count = 0
        for (i in 0 until height) {
            for (j in 0 until width) {
                val clr: Int = img.getRGB(j, i)
                val r = clr and 0x00ff0000 shr 16
                val g = clr and 0x0000ff00 shr 8
                val b = clr and 0x000000ff
                val luminance = 0.299 * r + 0.587 * g + 0.114 * b
                average += luminance
                count++
            }
        }
        return average / count
    }
}