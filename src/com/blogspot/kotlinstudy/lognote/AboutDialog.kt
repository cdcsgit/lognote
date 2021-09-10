package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*

class AboutDialog(parent: JFrame) :JDialog(parent, "About", true), ActionListener {
    private var mAboutLabel: JLabel
    private var mCloseBtn : ColorButton
    private var mMainUI: MainUI

    init {
        mCloseBtn = ColorButton("Close")
        mCloseBtn.addActionListener(this)
        mMainUI = parent as MainUI

        mAboutLabel = JLabel("<html><center><h1>LogNote " + Main.VERSION + "</h1><br>cdcsman@gmail.com</center></html>")

        val aboutPanel = JPanel()
        aboutPanel.add(mAboutLabel)

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(aboutPanel, BorderLayout.CENTER)

        val btnPanel = JPanel()
        btnPanel.add(mCloseBtn)
        panel.add(btnPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mCloseBtn) {
            dispose()
        }
    }
}