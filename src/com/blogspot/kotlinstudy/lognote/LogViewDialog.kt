package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.event.*
import javax.swing.*


class LogViewDialog (parent: JFrame, log:String, caretPos: Int) : JDialog(parent, "Log", false) {

    val mTextArea = JTextArea()
    private val mMainUI = parent as MainUI
    private val mPopupMenu = PopUpLogViewDialog()

    init {
        isUndecorated = true
        mTextArea.isEditable = false
        mTextArea.caret.isVisible = true
        mTextArea.lineWrap = true
        mTextArea.background = Color(0xFF, 0xFA, 0xE3)
        mTextArea.font = mMainUI.mFont

        mTextArea.addKeyListener(KeyHandler())
        mTextArea.addMouseListener(MouseHandler())
        mTextArea.addFocusListener(FocusHandler())
        mTextArea.text = log
        mTextArea.caretPosition = caretPos
        var width = parent.width - 100
        if (width < 960) {
            width = 960
        }
        mTextArea.setSize(width, 100)
        mTextArea.border = BorderFactory.createEmptyBorder(7, 7, 7, 7)

        contentPane.add(mTextArea)
        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    internal inner class KeyHandler: KeyAdapter() {
        private var pressedKeyCode: Int = 0
        override fun keyPressed(p0: KeyEvent?) {
            if (p0 != null) {
                pressedKeyCode = p0.keyCode
            }

            super.keyPressed(p0)
        }

        override fun keyReleased(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ENTER && pressedKeyCode == KeyEvent.VK_ENTER) {
                mTextArea.copy()
                dispose()
            }
        }
    }

    internal inner class FocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            if (!mPopupMenu.isVisible) {
                dispose()
            }
        }
    }

    internal inner class PopUpLogViewDialog : JPopupMenu() {
        var mIncludeItem = JMenuItem("Add Include")
        var mExcludeItem = JMenuItem("Add Exclude")
        var mCopyItem = JMenuItem("Copy")
        var mCloseItem = JMenuItem("Close")
        private val mActionHandler = ActionHandler()

        init {
            mIncludeItem.addActionListener(mActionHandler)
            add(mIncludeItem)
            mExcludeItem.addActionListener(mActionHandler)
            add(mExcludeItem)
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mCloseItem.addActionListener(mActionHandler)
            add(mCloseItem)
            addFocusListener(FocusHandler())
        }

        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mIncludeItem -> {
                        val frame = SwingUtilities.windowForComponent(this@LogViewDialog) as MainUI
                        var text = frame.getTextShowLogCombo()
                        text += "|" + mTextArea.selectedText
                        frame.setTextShowLogCombo(text)
                        frame.applyShowLogCombo()
                    }
                    mExcludeItem -> {
                        val frame = SwingUtilities.windowForComponent(this@LogViewDialog) as MainUI
                        var text = frame.getTextShowLogCombo()
                        text += "|-" + mTextArea.selectedText
                        frame.setTextShowLogCombo(text)
                        frame.applyShowLogCombo()
                    }
                    mCopyItem -> {
                        mTextArea.copy()
                    }
                    mCloseItem -> {
                        dispose()
                    }
                }
            }
        }

        internal inner class FocusHandler: FocusAdapter() {
            override fun focusLost(p0: FocusEvent?) {
                super.focusLost(p0)
                if (!this@LogViewDialog.hasFocus()) {
                    dispose()
                }
            }
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mousePressed(p0: MouseEvent?) {
            super.mousePressed(p0)
        }

        override fun mouseReleased(p0: MouseEvent?) {
            if (p0 == null) {
                super.mouseReleased(p0)
                return
            }

            if (SwingUtilities.isRightMouseButton(p0)) {
                mPopupMenu.show(p0.component, p0.x, p0.y)
            } else {
                mPopupMenu.isVisible = false
            }

            super.mouseReleased(p0)
        }

    }
}