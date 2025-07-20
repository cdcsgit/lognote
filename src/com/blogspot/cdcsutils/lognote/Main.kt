package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.util.SystemInfo
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities

class Main {
    companion object {
        const val VERSION: String = "1.2"
        const val NAME: String = "LogNote"

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("swing.aatext", "true")

            if (SystemInfo.isMacOS) {
                // enable screen menu bar
                // (moves menu bar from JFrame window to top of screen)
                System.setProperty( "apple.laf.useScreenMenuBar", "true" )

                // application name used in screen menu bar
                // (in first menu after the "apple" menu)
                System.setProperty( "apple.awt.application.name", "Lognote" )

                // appearance of window title bars
                // possible values:
                //   - "system": use current macOS appearance (light or dark)
                //   - "NSAppearanceNameAqua": use light appearance
                //   - "NSAppearanceNameDarkAqua": use dark appearance
                // (must be set on main thread and before AWT/Swing is initialized;
                //  setting it on AWT thread does not work)
                System.setProperty( "apple.awt.application.appearance", "system" )

                System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS")
            }

            if (SystemInfo.isLinux) {
                 JFrame.setDefaultLookAndFeelDecorated(true)
                 JDialog.setDefaultLookAndFeelDecorated(true)
             }

            SwingUtilities.invokeLater {
                val mainUI = MainUI.getInstance()

                mainUI.isVisible = true
                mainUI.updateUIAfterVisible(args)
            }
        }
    }
}
