package com.blogspot.cdcsutils.lognote

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel


class AboutDialog(mainUI: MainUI) :JDialog(mainUI, Strings.ABOUT, true), ActionListener {
    private var mAboutLabel: JLabel
    private var mCloseBtn : ColorButton

    init {
        mCloseBtn = ColorButton(Strings.CLOSE)
        mCloseBtn.addActionListener(this)

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

        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mCloseBtn) {
            dispose()
        }
    }
}