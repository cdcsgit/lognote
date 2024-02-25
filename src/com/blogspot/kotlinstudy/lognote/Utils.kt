package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.AbstractBorder

class Utils {
    companion object {
//        var escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
//        var actionMapKey = javaClass.name + ":WINDOW_CLOSING"
//        var closingAction: Action = object : AbstractAction() {
//            override fun actionPerformed(event: ActionEvent) {
//                this@MainUI.dispatchEvent(WindowEvent(this@MainUI, WindowEvent.WINDOW_CLOSING))
//            }
//        }
        fun installKeyStroke(container: RootPaneContainer, stroke: KeyStroke?, actionMapKey: String?, action: Action?) {
            val rootPane = container.rootPane
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, actionMapKey)
            rootPane.actionMap.put(actionMapKey, action)
        }

        fun installKeyStrokeEscClosing(container: RootPaneContainer) {
            if (container !is Window) {
                println("container is not java.awt.Window")
                return
            }

            val escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
            val actionMapKey = container.javaClass.name + ":WINDOW_CLOSING"
            val closingAction: Action = object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    container.dispatchEvent(WindowEvent(container, WindowEvent.WINDOW_CLOSING))
                }
            }

            val rootPane = container.rootPane
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escStroke, actionMapKey)
            rootPane.actionMap.put(actionMapKey, closingAction)
        }
    }

    class CustomLineBorder(private val mColor: Color, private val mThickness: Int, private val mTarget: Int) :
        AbstractBorder() {
        override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
            if (g != null) {
                g.color = mColor
                if (mTarget and TOP != 0) {
                    for (i in 0 until mThickness) {
                        g.drawLine(x, y + i, width, y + i)
                    }
                }
                if (mTarget and LEFT != 0) {
                    for (i in 0 until mThickness) {
                        g.drawLine(x + i, y, x + i, height)
                    }
                }
                if (mTarget and BOTTOM != 0) {
                    for (i in 0 until mThickness) {
                        g.drawLine(x, height - i - 1, width, height - i - 1)
                    }
                }
                if (mTarget and RIGHT != 0) {
                    for (i in 0 until mThickness) {
                        g.drawLine(width - i - 1, y, width - i - 1, height)
                    }
                }
            }
        }

        override fun getBorderInsets(c: Component?): Insets {
            return getBorderInsets(c, Insets(0, 0, 0, 0))
        }

        override fun getBorderInsets(c: Component?, insets: Insets): Insets {
            insets.top = 0
            insets.left = 0
            insets.bottom = 0
            insets.right = 0
            if (mTarget and TOP != 0) {
                insets.top = mThickness
            }
            if (mTarget and LEFT != 0) {
                insets.left = mThickness
            }
            if (mTarget and BOTTOM != 0) {
                insets.bottom = mThickness
            }
            if (mTarget and RIGHT != 0) {
                insets.right = mThickness
            }
            return insets
        }

        override fun isBorderOpaque(): Boolean {
            return true
        }

        companion object {
            const val TOP = 0x1
            const val LEFT = 0x2
            const val BOTTOM = 0x4
            const val RIGHT = 0x8
        }
    }
}