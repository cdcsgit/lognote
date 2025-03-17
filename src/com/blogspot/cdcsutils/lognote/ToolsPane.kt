package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.ui.FlatTabbedPaneUI
import java.awt.Color
import java.awt.Component
import java.awt.event.*
import javax.swing.*
import javax.swing.text.Utilities

class ToolsPane private constructor(): JTabbedPane() {
    private val mToolMap = mutableMapOf<ToolId, Component>()
    val mLogTool = LogTool()
    val mTestTool = TestTool()

    companion object {
        enum class ToolId(val value: Int) {
            TOOL_ID_PANEL(0),
            TOOL_ID_LOG(1),
            TOOL_ID_TEST(2);

            companion object {
                fun fromInt(value: Int) = entries.first { it.value == value }
            }
        }

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
        mToolMap[ToolId.TOOL_ID_LOG] = mLogTool
        mToolMap[ToolId.TOOL_ID_TEST] = mTestTool
        addMouseListener(ToolsMouseHandler())
    }

    internal inner class ToolsPopUp : JPopupMenu() {
        val mMainUI = MainUI.getInstance()
        var mItemPanel = JCheckBoxMenuItem(Strings.PANEL)
        var mItemMoveTo = JMenuItem(Strings.MOVE_TO_TOP)
        var mItemLog = JCheckBoxMenuItem(Strings.LOG)
        var mItemTest = JCheckBoxMenuItem("Test")
        private val mToolsActionHandler = ToolsActionHandler()

        init {
            mItemPanel.state = mMainUI.mItemToolPanel.state
            mItemPanel.addActionListener(mToolsActionHandler)
            add(mItemPanel)
            if (mMainUI.mToolRotationStatus == MainUI.ROTATION_BOTTOM_TOP) {
                mItemMoveTo.text = Strings.MOVE_TO_TOP
            } else {
                mItemMoveTo.text = Strings.MOVE_TO_BOTTOM
            }
            mItemMoveTo.addActionListener(mToolsActionHandler)
            add(mItemMoveTo)
            addSeparator()
            mItemLog.state = mMainUI.mItemToolLog.state
            mItemLog.addActionListener(mToolsActionHandler)
            add(mItemLog)
            mItemTest.state = mMainUI.mItemToolTest.state
            mItemTest.addActionListener(mToolsActionHandler)
            add(mItemTest)
        }

        internal inner class ToolsActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mItemPanel -> {
                        mMainUI.mItemToolPanel.doClick()
                    }
                    mItemMoveTo -> {
                        if (mMainUI.mToolRotationStatus == MainUI.ROTATION_BOTTOM_TOP) {
                            mMainUI.rotateToolSplitPane(MainUI.ROTATION_TOP_BOTTOM)
                        } else {
                            mMainUI.rotateToolSplitPane(MainUI.ROTATION_BOTTOM_TOP)
                        }
                    }
                    mItemLog -> {
                        mMainUI.mItemToolLog.doClick()
                    }
                    mItemTest -> {
                        mMainUI.mItemToolTest.doClick()
                    }
                }
            }
        }
    }

    internal inner class ToolsMouseHandler : MouseAdapter() {
        override fun mousePressed(p0: MouseEvent?) {
            super.mousePressed(p0)
        }

        private var popupMenu: JPopupMenu? = null
        override fun mouseReleased(p0: MouseEvent?) {
            if (p0 == null) {
                super.mouseReleased(p0)
                return
            }

            if (SwingUtilities.isRightMouseButton(p0)) {
                popupMenu = ToolsPopUp()
                popupMenu?.show(p0.component, p0.x, p0.y)
            } else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }
    }

    fun addTab(toolId: ToolId) {
        if (mToolMap[toolId] == null) {
            Utils.printlnLog("$toolId is invalid")
            return
        }
        var isExist = false
        for (idx in 0 until tabCount) {
            val tabComponent = getComponentAt(idx)
            if (tabComponent == mToolMap[toolId]) {
                Utils.printlnLog("tab[${tabComponent.name}] is already shown")
                isExist = true
                break
            }
        }

        if (!isExist) {
            addTab(mToolMap[toolId]!!.name, mToolMap[toolId])
        }
    }

    fun removeTab(toolId: ToolId) {
        if (mToolMap[toolId] == null) {
            Utils.printlnLog("$toolId is invalid")
            return
        }
        for (idx in 0 until tabCount) {
            val tabComponent = getComponentAt(idx)
            if (tabComponent == mToolMap[toolId]) {
                removeTabAt(idx)
                break
            }
        }
    }

    fun showTab(toolId: ToolId) {
        if (toolId != ToolId.TOOL_ID_PANEL) {
            if (mToolMap[toolId] == null) {
                Utils.printlnLog("$toolId is invalid")
                return
            }

            addTab(toolId)

            selectedComponent = mToolMap[toolId]
        }

        if (!isVisible && tabCount > 0) {
            isVisible = true
        }
    }

    fun hideTab(toolId: ToolId) {
        if (toolId != ToolId.TOOL_ID_PANEL) {
            if (mToolMap[toolId] == null) {
                Utils.printlnLog("$toolId is invalid")
                return
            }

            removeTab(toolId)
            if (isVisible && tabCount == 0) {
                isVisible = false
            }
        }
        else {
            isVisible = false
        }
    }

    fun updateVisible(visible: Boolean) {
        if (visible) {
            if (tabCount > 0) {
                isVisible = true
            }
            else {
                isVisible = false
                Utils.printlnLog("cannot change visible status, tabCount = $tabCount")
            }
        }
        else {
            isVisible = false
        }
    }

    fun isExistInTab(toolId: ToolId): Boolean {
        var isExist = false
        for (idx in 0 until tabCount) {
            val tabComponent = getComponentAt(idx)
            if (tabComponent == mToolMap[toolId]) {
                Utils.printlnLog("tab[${tabComponent.name}] is already shown")
                isExist = true
                break
            }
        }

        return isExist
    }

    inner class TestTool: JLabel() {
        init {
            name = "Test Tool"
            text = "This is test tool"
        }
    }

    class LogTool: JScrollPane() {
        private val mEditorPane = JEditorPane()
        private val mPopupMenu: PopUpLogViewDialog
        private val mIncludeAction: Action
        private val mAddIncludeKey = "add_include"

        init {
            name = Strings.LOG
            mEditorPane.isEditable = false
            mEditorPane.caret.isVisible = true
            mEditorPane.contentType = "text/html"

            mEditorPane.addMouseListener(MouseHandler())
            setViewportView(mEditorPane)
            horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER

            mIncludeAction = object : AbstractAction(mAddIncludeKey) {
                override fun actionPerformed(evt: ActionEvent?) {
                    if (evt != null) {
                        val textSplit = evt.actionCommand.split(":")
                        var comboText = MainUI.getInstance().getTextShowLogCombo()
                        if (comboText.isNotEmpty()) {
                            comboText += "|"
                        }

                        if (mEditorPane.selectedText != null) {
                            val selectedText = mEditorPane.selectedText.replace("\u00a0", " ")
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
            mEditorPane.text = "<html>${pair.first}</html>"
            mEditorPane.caretPosition = pair.second
        }

        fun isVisiblePopupMenu(): Boolean {
            return mPopupMenu.isVisible
        }

        fun addFocusListenerToEditor(focusHandler: FocusListener) {
            mEditorPane.addFocusListener(focusHandler)
        }

        fun addFocusListenerToPopup(popupFocusHandler: FocusListener) {
            mPopupMenu.addFocusListener(popupFocusHandler)
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
                    val offset = mEditorPane.viewToModel(p0.point)
                    var needSelect = true
                    if (!mEditorPane.selectedText.isNullOrEmpty()) {
                        if (offset >= mEditorPane.selectionStart && offset <= mEditorPane.selectionEnd) {
                            needSelect = false
                        }
                    }
                    if (needSelect) {
                        val start = Utilities.getWordStart(mEditorPane, offset)
                        val end = Utilities.getWordEnd(mEditorPane, offset)
                        mEditorPane.select(start, end)
                    }
                    mPopupMenu.show(p0.component, p0.x, p0.y)
                } else {
                    mPopupMenu.isVisible = false
                }

                super.mouseReleased(p0)
            }
        }
    }
}