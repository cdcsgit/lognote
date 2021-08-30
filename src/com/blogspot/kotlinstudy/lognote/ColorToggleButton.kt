package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JToggleButton


class ColorToggleButton(title:String) : JToggleButton(title){
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val graphics2D = g as Graphics2D
        graphics2D.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        graphics2D.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

        val thickness = 2
        if (isSelected || model.isPressed) {
            graphics2D.color = Color(0xD5, 0xD5, 0xD5)
            graphics2D.fillRect(0, 0, width, height)

            graphics2D.color = Color(0x0, 0x0, 0x0)
            graphics2D.fillRect(0, 0, thickness, height)
            graphics2D.fillRect(0, 0, width, thickness)

            graphics2D.color = Color(0x5C, 0x94, 0xCB)
            graphics2D.drawString(text,
                (width - graphics2D.fontMetrics.stringWidth(text)) / 2,
                (height + graphics2D.fontMetrics.ascent) / 2 - 2)

        }
        else {
            graphics2D.color = Color(0xF0, 0xF0, 0xF0)
            graphics2D.fillRect(0, 0, width, height)

            graphics2D.color = Color(0xBB, 0x84, 0x4C)
            graphics2D.drawString(text,
                (width - graphics2D.fontMetrics.stringWidth(text)) / 2,
                (height + graphics2D.fontMetrics.ascent) / 2 - 2)
        }
    }
}