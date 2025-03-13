package com.blogspot.cdcsutils.lognote

import java.awt.Color
import java.awt.Dimension
import java.awt.event.*
import javax.swing.*
import javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER


class LogViewDialog (mainUI: MainUI, log:String, caretPos: Int) : JDialog(mainUI, "Log", false) {

    val mEditorPane = JEditorPane()
    private val mScrollPane = JScrollPane(mEditorPane)
    private val mMainUI = mainUI
    private val mPopupMenu: PopUpLogViewDialog
    private val mIncludeAction: Action
    private val mAddIncludeKey = "add_include"

    init {
        isUndecorated = true
        mEditorPane.isEditable = false
        mEditorPane.caret.isVisible = true
        mEditorPane.contentType = "text/html"
        mScrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER

        if (!MainUI.IsFlatLaf || MainUI.IsFlatLightLaf) {
            mEditorPane.background = Color(0xFF, 0xFA, 0xE3)
        }
        mEditorPane.font = mMainUI.mFont

        mEditorPane.addKeyListener(KeyHandler())
        mEditorPane.addMouseListener(MouseHandler())
        mEditorPane.addFocusListener(FocusHandler())
        mEditorPane.text = log
//        mEditorPane.caretPosition = caretPos
        var width = mainUI.width - 100
        if (width < 960) {
            width = 960
        }
        mEditorPane.setSize(width, 100)
        mEditorPane.border = BorderFactory.createEmptyBorder(7, 7, 7, 7)

        var height = mainUI.height - 100
        if (height > mEditorPane.preferredSize.height) {
            height = mEditorPane.preferredSize.height + 2
        }
        mScrollPane.preferredSize = Dimension(width, height)

        contentPane.add(mScrollPane)
        pack()

        mIncludeAction = object : AbstractAction(mAddIncludeKey) {
            override fun actionPerformed(evt: ActionEvent?) {
                if (evt != null) {
                    val textSplit = evt.actionCommand.split(":")
                    var comboText = mMainUI.getTextShowLogCombo()
                    if (comboText.isNotEmpty()) {
                        comboText += "|"
                    }
                    val selectedText = mEditorPane.selectedText.replace("\u00a0"," ")
                    comboText += if (textSplit.size == 2) {
                        "${textSplit[1].trim()}$selectedText"
                    } else {
                        selectedText
                    }
                    mMainUI.setTextShowLogCombo(comboText)
                    mMainUI.applyShowLogCombo(true)
                }
            }
        }

        val key = mAddIncludeKey
        mEditorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK or KeyEvent.SHIFT_MASK), key)
        mEditorPane.actionMap.put(key, mIncludeAction)

        Utils.installKeyStrokeEscClosing(this)

        mPopupMenu = PopUpLogViewDialog()
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
                mEditorPane.copy()
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
        var mIncludeItem = JMenuItem(Strings.ADD_INCLUDE)
        var mExcludeItem = JMenuItem(Strings.ADD_EXCLUDE)
        var mSearchAddItem = JMenuItem(Strings.ADD_SEARCH)
        var mSearchSetItem = JMenuItem(Strings.SET_SEARCH)
        var mCopyItem = JMenuItem(Strings.COPY)
        var mCloseItem = JMenuItem(Strings.CLOSE)
        private val mActionHandler = ActionHandler()

        init {
            mIncludeItem.isOpaque = true
            mIncludeItem.foreground = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredFGs[0])
            mIncludeItem.background = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredBGs[0])
            mIncludeItem.addActionListener(mIncludeAction)
            mIncludeItem.mnemonic = KeyEvent.VK_I
            add(mIncludeItem)
            for (idx in 1..9) {
                val item = JMenuItem("${Strings.ADD_INCLUDE} : #$idx")
                item.isOpaque = true
                item.foreground = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredFGs[idx])
                item.background = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredBGs[idx])
                item.addActionListener(mIncludeAction)
                add(item)
            }
            mExcludeItem.addActionListener(mActionHandler)
            add(mExcludeItem)
            mSearchAddItem.addActionListener(mActionHandler)
            add(mSearchAddItem)
            mSearchSetItem.addActionListener(mActionHandler)
            add(mSearchSetItem)
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mCloseItem.addActionListener(mActionHandler)
            add(mCloseItem)
            addFocusListener(FocusHandler())
        }

        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mExcludeItem -> {
                        if (!mEditorPane.selectedText.isNullOrEmpty()) {
                            var text = mMainUI.getTextShowLogCombo()
                            text += "|-" + mEditorPane.selectedText.replace("\u00a0"," ")
                            mMainUI.setTextShowLogCombo(text)
                            mMainUI.applyShowLogCombo(true)
                        }
                    }
                    mSearchAddItem -> {
                        if (!mEditorPane.selectedText.isNullOrEmpty()) {
                            var text = mMainUI.getTextSearchCombo()
                            text += "|" + mEditorPane.selectedText.replace("\u00a0"," ")
                            mMainUI.setTextSearchCombo(text)
                        }
                    }
                    mSearchSetItem -> {
                        if (!mEditorPane.selectedText.isNullOrEmpty()) {
                            mMainUI.setTextSearchCombo(mEditorPane.selectedText.replace("\u00a0"," "))
                        }
                    }
                    mCopyItem -> {
                        mEditorPane.copy()
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
