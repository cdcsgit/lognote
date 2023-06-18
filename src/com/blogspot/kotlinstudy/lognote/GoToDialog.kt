package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class GoToDialog (parent: JFrame) : JDialog(parent, "GoTo line", true) {

    val mTF = JTextField()
    private val mLabel = JLabel(" GoTo : ")
    var mLine = -1
        private set

    init {
        mTF.addKeyListener(KeyHandler())
        mTF.alignmentX = JTextField.CENTER_ALIGNMENT
        mTF.preferredSize = Dimension(60, 30)
        mLabel.preferredSize = Dimension(70, 30)
        val panel = JPanel(BorderLayout())
        panel.add(mTF, BorderLayout.CENTER)
        panel.add(mLabel, BorderLayout.WEST)
        contentPane.add(panel)
        pack()
    }

    internal inner class KeyHandler: KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ESCAPE) {
                mLine = -1
                dispose()
            } else if (p0?.keyCode == KeyEvent.VK_ENTER && mTF.text.trim().isNotEmpty()) {
                mLine = try {
                    mTF.text.toInt()
                } catch (e:NumberFormatException) {
                    -1
                }
                dispose()
            }
        }
    }
}