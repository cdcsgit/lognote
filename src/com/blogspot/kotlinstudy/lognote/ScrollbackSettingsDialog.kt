package com.blogspot.kotlinstudy.lognote

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel

class ScrollbackSettingsDialog (parent: MainUI) :JDialog(parent, "Scrollback settings", true), ActionListener {
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private var mScrollbackLabel: JLabel

    private var mParent : MainUI

    init {
        mParent = parent
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        mScrollbackLabel = JLabel("Scrollback(lines)")

        val scrollbackPanel = JPanel()
        scrollbackPanel.add(mScrollbackLabel)
        scrollbackPanel.add(mOkBtn)
        scrollbackPanel.add(mCancelBtn)

        contentPane.add(scrollbackPanel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }
}