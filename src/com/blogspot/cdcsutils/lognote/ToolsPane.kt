package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.ui.FlatTabbedPaneUI
import java.awt.Color
import java.awt.event.*
import javax.swing.*

class ToolsPane private constructor(): JTabbedPane() {
    val mLogView: LogView by lazy { LogView() }

    companion object {
        val TAB_TITLES = arrayOf(Strings.LOG)
        const val TAB_LOG_VIEW = 0

        private val mInstance: ToolsPane = ToolsPane()
        fun getInstance(): ToolsPane {
            return mInstance
        }
    }

    init {
        setUI(object : FlatTabbedPaneUI() {
            val mTabHeight = 20
            override fun calculateTabHeight(
                tabPlacement: Int, tabIndex: Int, fontHeight: Int
            ): Int {
                return mTabHeight
            }
        })
    }

    fun showTab(view: Int) {
        var isExist = false
        for (i in 0 until tabCount) {
            val tabTitle: String = getTitleAt(i)
            if (tabTitle == TAB_TITLES[view]) {
                Utils.printlnLog("tab[${TAB_TITLES[view]}] is already shown")
                isExist = true
                break
            }
        }

        if (!isExist) {
            if (view == TAB_LOG_VIEW) {
                addTab(TAB_TITLES[view], mLogView)
            }
        }
        if (tabCount > 0) {
            isVisible = true
        }
    }

    fun hideTab(view: Int) {
        for (idx in 0 until tabCount) {
            val tabTitle: String = getTitleAt(idx)
            if (tabTitle == TAB_TITLES[view]) {
                removeTabAt(idx)
                break
            }
        }
        if (tabCount == 0) {
            isVisible = false
        }
    }

    inner class LogView: JScrollPane() {
        private val mEditorPane = JEditorPane()
        private val mPopupMenu: PopUpLogViewDialog
        private val mIncludeAction: Action
        private val mAddIncludeKey = "add_include"

        init {
            mEditorPane.isEditable = false
            mEditorPane.caret.isVisible = true
            mEditorPane.contentType = "text/html";

            mEditorPane.addMouseListener(MouseHandler())
            setViewportView(mEditorPane)

            mIncludeAction = object : AbstractAction(mAddIncludeKey) {
                override fun actionPerformed(evt: ActionEvent?) {
                    if (evt != null) {
                        val textSplit = evt.actionCommand.split(":")
                        var comboText = MainUI.getInstance().getTextShowLogCombo()
                        if (comboText.isNotEmpty()) {
                            comboText += "|"
                        }

                        val selectedText = mEditorPane.selectedText.replace("\u00a0"," ")
                        comboText += if (textSplit.size == 2) {
                            "${textSplit[1].trim()}$selectedText"
                        } else {
                            selectedText
                        }
                        MainUI.getInstance().setTextShowLogCombo(comboText)
                        MainUI.getInstance().applyShowLogCombo(true)
                    }
                }
            }

            val key = mAddIncludeKey
            mEditorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK), key)
            mEditorPane.actionMap.put(key, mIncludeAction)

            mPopupMenu = PopUpLogViewDialog()
        }

        fun setBgColor(logBG: Color) {
            mEditorPane.background = logBG
        }

        fun setLog(pair: Pair<String, Int>) {
//            mTextArea.text = pair.first
            mEditorPane.text = "<html><font color=#ff0000>${pair.first}</font></html>"
            mEditorPane.caretPosition = pair.second
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
            }

            internal inner class ActionHandler : ActionListener {
                override fun actionPerformed(p0: ActionEvent?) {
                    when (p0?.source) {
                        mExcludeItem -> {
                            if (!mEditorPane.selectedText.isNullOrEmpty()) {
                                var text = MainUI.getInstance().getTextShowLogCombo()
                                text += "|-" + mEditorPane.selectedText.replace("\u00a0"," ") // remove &nbsp;
                                MainUI.getInstance().setTextShowLogCombo(text)
                                MainUI.getInstance().applyShowLogCombo(true)
                            }
                        }
                        mSearchAddItem -> {
                            if (!mEditorPane.selectedText.isNullOrEmpty()) {
                                var text = MainUI.getInstance().getTextSearchCombo()
                                text += "|" + mEditorPane.selectedText.replace("\u00a0"," ")
                                MainUI.getInstance().setTextSearchCombo(text)
                            }
                        }
                        mSearchSetItem -> {
                            if (!mEditorPane.selectedText.isNullOrEmpty()) {
                                MainUI.getInstance().setTextSearchCombo(mEditorPane.selectedText.replace("\u00a0"," "))
                            }
                        }
                        mCopyItem -> {
                            mEditorPane.copy()
                        }
                        mCloseItem -> {
                        }
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
}