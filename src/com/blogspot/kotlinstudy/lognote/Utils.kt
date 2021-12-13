package com.blogspot.kotlinstudy.lognote

import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import javax.swing.*

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
                println("container is not java.awt.Window");
                return;
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
}