package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class GoToDialog (parent: JFrame) : JDialog(parent, "GoTo line", true) {

    val textField = JTextField()
    val label = JLabel(" GoTo : ")
    var line = -1
        private set

    init {
        textField.addKeyListener(KeyHandler())
        textField.alignmentX = JTextField.CENTER_ALIGNMENT
        textField.preferredSize = Dimension(60, 30)
        label.preferredSize = Dimension(70, 30)
        val panel = JPanel(BorderLayout())
        panel.add(textField, BorderLayout.CENTER)
        panel.add(label, BorderLayout.WEST)
        contentPane.add(panel)
        pack()
    }

    internal inner class KeyHandler: KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ESCAPE) {
                line = -1
                dispose()
            } else if (p0?.keyCode == KeyEvent.VK_ENTER && textField.text.trim().isNotEmpty()) {
                line = try {
                    textField.text.toInt()
                } catch (e:NumberFormatException) {
                    -1
                }
                dispose()
            }
        }
    }
}