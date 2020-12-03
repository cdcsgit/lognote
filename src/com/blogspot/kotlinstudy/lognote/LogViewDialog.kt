package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Font
import java.awt.event.*
import javax.swing.*

class LogViewDialog (parent: JFrame, log:String) : JDialog(parent, "Log", false) {

    val textArea = JTextArea()
    val mMainUI = parent as MainUI
    init {
        isUndecorated = true
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.background = Color(0xFF, 0xFA, 0xE3)
        textArea.font = mMainUI.mFont
        textArea.addKeyListener(KeyHandler())
        textArea.addMouseListener(MouseHandler())
        textArea.addFocusListener(FocusHandler())
        textArea.text = log
        var width = parent.width - 100
        if (width < 960) {
            width = 960
        }
        textArea.setSize(width, 100)
        textArea.border = BorderFactory.createEmptyBorder(7, 7, 7, 7)

        contentPane.add(textArea)
        pack()
    }

    internal inner class KeyHandler: KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ESCAPE) {
                dispose()
            }
            else if (p0?.keyCode == KeyEvent.VK_ENTER) {
                textArea.copy()
                dispose()
            }
        }
    }

    internal inner class MouseHandler: MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            if (p0?.button == MouseEvent.BUTTON3) {
                textArea.copy()
            }
        }
    }

    internal inner class FocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            dispose()
        }
    }
}