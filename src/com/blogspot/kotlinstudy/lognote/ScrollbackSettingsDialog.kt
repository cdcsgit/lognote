package com.blogspot.kotlinstudy.lognote

import java.awt.Dimension
import java.awt.FileDialog
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*

class ScrollbackSettingsDialog (parent: MainUI) :JDialog(parent, "Scrollback settings", true), ActionListener {
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private var mScrollbackLabel: JLabel

//    private var mScrollbackTextField: JTextField
//    private var mSplitFileCheck: JCheckBox
    private var mParent : MainUI

    init {
        mParent = parent
        mOkBtn = ColorButton("OK")
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton("Cancel")
        mCancelBtn.addActionListener(this)

        mScrollbackLabel = JLabel("Scrollback(lines)")

//        mScrollbackTextField = JTextField(parent.mScrollback.toString())
//        mScrollbackTextField.preferredSize = Dimension(80, 30)
//        mSplitFileCheck = JCheckBox("Split File", parent.mScrollbackSplitFile)

        val scrollbackPanel = JPanel()
        scrollbackPanel.add(mScrollbackLabel)
//        scrollbackPanel.add(mScrollbackTextField)
//        scrollbackPanel.add(mSplitFileCheck)
        scrollbackPanel.add(mOkBtn)
        scrollbackPanel.add(mCancelBtn)

        contentPane.add(scrollbackPanel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
//            mParent.mScrollback = mScrollbackTextField.text.toString().trim().toInt()
//            mParent.mScrollbackSplitFile = mSplitFileCheck.isSelected
            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }
}