package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource

class Main {
    companion object {
        const val VERSION: String = "0.2.4"

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("swing.aatext", "true")

            SwingUtilities.invokeLater {
                UIManager.put("ScrollBar.thumb", ColorUIResource(Color(0xE0, 0xE0, 0xE0)))
                UIManager.put("ScrollBar.thumbHighlight", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
                UIManager.put("ScrollBar.thumbShadow", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
                UIManager.put("ComboBox.buttonDarkShadow", ColorUIResource(Color.black))

                val mainUI = MainUI("LogNote")
                mainUI.isVisible = true
                mainUI.updateUIAfterVisible()
            }
        }
    }
}
