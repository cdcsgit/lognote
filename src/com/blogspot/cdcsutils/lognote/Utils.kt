package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.util.SystemInfo
import java.awt.*
import java.awt.event.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.AbstractBorder
import kotlin.system.exitProcess


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
                printlnLog("container is not java.awt.Window")
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

        fun addVSeparator(panel:JPanel, height: Int) {
            val separator1 = JSeparator(SwingConstants.VERTICAL)
            separator1.preferredSize = Dimension(separator1.preferredSize.width, height)
            if (MainUI.IsFlatLaf && !MainUI.IsFlatLightLaf) {
                separator1.foreground = Color.GRAY
                separator1.background = Color.GRAY
            }
            else {
                separator1.foreground = Color.DARK_GRAY
                separator1.background = Color.DARK_GRAY
            }
            panel.add(separator1)
        }

        fun addHSeparator(target:JPanel, title: String) {
            val titleHtml = title.replace(" ", "&nbsp;")
            val separator = JSeparator(SwingConstants.HORIZONTAL)
            val label = JLabel("<html><b>$titleHtml</b></html>")
            val panel = JPanel(BorderLayout())
            val separPanel = JPanel(BorderLayout())
            separPanel.add(Box.createVerticalStrut(label.font.size / 2), BorderLayout.NORTH)
            separPanel.add(separator, BorderLayout.CENTER)
            panel.add(label, BorderLayout.WEST)
            panel.add(separPanel, BorderLayout.CENTER)
            target.add(panel)
        }

        fun addHEmptySeparator(target:JPanel, height: Int) {
            val panel = JPanel()
            panel.preferredSize = Dimension(1, height)
            target.add(panel)
        }

        fun printlnLog(log: String) {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS")
            val formattedNow = now.format(formatter)

            println("$formattedNow ${Thread.currentThread().id} $log")
        }

        fun replaceCmd(cmd: String): String {
            val newCmd = if (cmd.startsWith("adb ")) {
                cmd.replaceFirst("adb ", "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} ")
            } else if (cmd.startsWith("adb.exe ")) {
                cmd.replaceFirst("adb.exe ", "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} ")
            } else {
                "$cmd ${LogCmdManager.getInstance().mTargetDevice}"
            }

            return newCmd
        }

        private fun getLognoteCmd(): String {
            val pid = ManagementFactory.getRuntimeMXBean().name.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
            var command: String = ""

            try {
                if (SystemInfo.isWindows) {
                    val process = Runtime.getRuntime().exec("wmic process where processid=$pid get commandline")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String
                    while ((reader.readLine().also { line = it }) != null) {
                        if (line.contains(".jar") || line.contains(".class")) {
                            command = line.trim { it <= ' ' }
                            break
                        }
                    }
                } else if (SystemInfo.isLinux || SystemInfo.isMacOS) {
                    val process = Runtime.getRuntime().exec(
                        arrayOf(
                            "/bin/sh", "-c",
                            "ps -p $pid -o command="
                        )
                    )
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    command = reader.readLine().trim { it <= ' ' }
                }

                if (command.isEmpty()) {
                    printlnLog("Failed get command")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return command
        }

        fun restartLognote() {
            val cmd = getLognoteCmd()
            if (cmd.isNotEmpty()) {
                Runtime.getRuntime().exec(cmd)
                exitProcess(0)
            }
            else {
                printlnLog("lognote cmd is empty")
            }
        }

        fun convertHtmlToPlainText(html: String): String {
            var text = html

            text = text.replace("(?i)<br\\s*/?>".toRegex(), "\n")

            text = text.replace("(?i)<p[^>]*>".toRegex(), "\n\n")
            text = text.replace("(?i)<div[^>]*>".toRegex(), "\n\n")
            text = text.replace("(?i)<h[1-6][^>]*>".toRegex(), "\n\n")
            text = text.replace("(?i)<ul[^>]*>".toRegex(), "\n")
            text = text.replace("(?i)<ol[^>]*>".toRegex(), "\n")
            text = text.replace("(?i)<li[^>]*>".toRegex(), "\n* ")

            text = text.replace("<[^>]*>".toRegex(), "")

            text = text.replace("&nbsp;".toRegex(), " ")
            text = text.replace("&amp;".toRegex(), "&")
            text = text.replace("&lt;".toRegex(), "<")
            text = text.replace("&gt;".toRegex(), ">")
            text = text.replace("&quot;".toRegex(), "\"")
            text = text.replace("&#39;".toRegex(), "'")

            text = text.replace("(?m)^[ \t]*\r?\n".toRegex(), "")
            text = text.replace("(?m)(\\n){3,}".toRegex(), "\n\n")
            text = text.trim { it <= ' ' }

            return text
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

    object NumberKeyListener: KeyAdapter() {
        override fun keyTyped(e: KeyEvent?) {
            val char = e?.keyChar
            if (!char?.isDigit()!!) {
                e.consume()
            }
        }
    }
}