package com.blogspot.cdcsutils.lognote

import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JPanel

class LogTableDialog (mainUI: MainUI, logPanel: LogPanel) : JFrame("FullLog") {
    private val mMainUI = mainUI
    private val mLogPanel = logPanel
    private var mFrameX = 0
    private var mFrameY = 0
    private var mFrameWidth = 1280
    private var mFrameHeight = 720
    private var mFrameExtendedState = java.awt.Frame.MAXIMIZED_BOTH

    init {
        val img = ImageIcon(this.javaClass.getResource("/images/logo.png"))
        iconImage = img.image

        defaultCloseOperation = DISPOSE_ON_CLOSE
        setLocation(mFrameX, mFrameY)
        setSize(mFrameWidth, mFrameHeight)
        val panel = JPanel(BorderLayout())
        panel.add(mLogPanel, BorderLayout.CENTER)
        contentPane.add(panel)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                Utils.printlnLog("exit table dialog")
                mMainUI.attachLogPanel(mLogPanel)
            }
        })

        Utils.installKeyStrokeEscClosing(this)
    }
}