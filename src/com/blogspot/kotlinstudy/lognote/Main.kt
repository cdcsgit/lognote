package com.blogspot.kotlinstudy.lognote

import javax.swing.SwingUtilities

class Main {
    companion object {
        const val VERSION: String = "0.3.2"
        const val NAME: String = "LogNote"

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("swing.aatext", "true")

            SwingUtilities.invokeLater {
                val mainUI = MainUI.getInstance()

                mainUI.isVisible = true
                mainUI.updateUIAfterVisible(args)
            }
        }
    }
}
