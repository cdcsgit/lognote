package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.util.SystemInfo
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities

class Main {
    companion object {
        const val VERSION: String = "1.2"
        const val NAME: String = "LogNote"

        // Diagnostic (off by default, enable with -Dlognote.lagdetect=true):
        // when the UI thread does not respond for LAG_MS, print what it is doing, to find the cause of UI lag.
        private const val LAG_MS = 200L
        private fun startLagDetector() {
            val watchdog = Thread {
                var edt: Thread? = null
                SwingUtilities.invokeAndWait { edt = Thread.currentThread() }
                var lastReport = 0L
                while (true) {
                    val done = java.util.concurrent.atomic.AtomicBoolean(false)
                    val start = System.currentTimeMillis()
                    SwingUtilities.invokeLater { done.set(true) }
                    Thread.sleep(LAG_MS)
                    if (!done.get()) {
                        val stack = edt?.stackTrace ?: emptyArray()
                        while (!done.get()) {
                            Thread.sleep(20)
                        }
                        val blocked = System.currentTimeMillis() - start
                        val now = System.currentTimeMillis()
                        if (now - lastReport > 1000) {  // at most one report per second
                            lastReport = now
                            val sb = StringBuilder("LAG: UI thread blocked ${blocked} ms, it was doing:\n")
                            for (frame in stack.take(40)) {
                                sb.append("        at ").append(frame).append('\n')
                            }
                            Utils.printlnLog(sb.toString())
                        }
                    } else {
                        Thread.sleep(100)
                    }
                }
            }
            watchdog.isDaemon = true
            watchdog.name = "LagDetector"
            watchdog.start()
            Utils.printlnLog("Lag detector enabled (${LAG_MS} ms)")
        }

        @JvmStatic
        fun main(args: Array<String>) {
            // Windows: Java's Direct3D drawing can stall for seconds while scrolling (seen on Java 8 after some
            // use, until restart). Use the plain software pipeline unless the user set sun.java2d.d3d explicitly.
            // Must run before any AWT/Swing class is initialized.
            if (SystemInfo.isWindows && System.getProperty("sun.java2d.d3d") == null) {
                System.setProperty("sun.java2d.d3d", "false")
            }

            when {
                SystemInfo.isWindows -> Utils.printlnLog("Current OS: Windows (${SystemInfo.osVersion})")
                SystemInfo.isMacOS -> Utils.printlnLog("Current OS: macOS (${SystemInfo.osVersion})")
                SystemInfo.isLinux -> Utils.printlnLog("Current OS: Linux (${SystemInfo.osVersion})")
                else -> Utils.printlnLog("Current OS: Unknown")
            }

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

            if (System.getProperty("lognote.lagdetect") == "true") {
                startLagDetector()
            }

            SwingUtilities.invokeLater {
                val mainUI = MainUI.getInstance()

                mainUI.isVisible = true
                mainUI.updateUIAfterVisible(args)
            }
        }
    }
}
