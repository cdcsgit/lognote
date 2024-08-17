package com.blogspot.cdcsutils.lognote

import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.net.URI
import javax.swing.*


class HelpGotoDialog(parent: JFrame) :JDialog(parent, Strings.HELP, true), ActionListener, MouseListener {
    private var mHelpBtn: ColorButton
    private var mCloseBtn : ColorButton
    private val HELP_ADDRESS = "https://github.com/cdcsgit/lognote#readme"

    init {
        mCloseBtn = ColorButton(Strings.CLOSE)
        mCloseBtn.addActionListener(this)

        mHelpBtn = ColorButton(HELP_ADDRESS)
        mHelpBtn.addMouseListener(this)

        val panel = JPanel()
        panel.layout = BorderLayout()

        val helpPanel = JPanel()
        helpPanel.preferredSize = Dimension(350, 40)
        helpPanel.add(mHelpBtn)
        panel.add(helpPanel, BorderLayout.CENTER)

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

    override fun mouseClicked(e: MouseEvent?) {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if ((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI(HELP_ADDRESS))
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}
