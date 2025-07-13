package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.ui.FlatTabbedPaneUI
import java.awt.Color
import java.awt.Component
import java.awt.FontMetrics
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.*
import javax.swing.text.JTextComponent
import javax.swing.text.Utilities


class ToolsPane private constructor(): JTabbedPane() {
    private val mToolMap = mutableMapOf<ToolId, Component>()
    val mToolSelection = ToolSelection(true)
    val mToolTest = ToolTest()

    companion object {
        enum class ToolId(val value: Int) {
            TOOL_ID_PANEL(0),
            TOOL_ID_SELECTION(1),
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
        mToolMap[ToolId.TOOL_ID_SELECTION] = mToolSelection
        mToolMap[ToolId.TOOL_ID_TEST] = mToolTest
        addMouseListener(ToolsMouseHandler())
    }

    interface ToolComponent {
        fun getToolIcon(): Icon?
        fun getTooltip(): String
    }

    internal inner class ToolsPopUp : JPopupMenu() {
        val mMainUI = MainUI.getInstance()
        var mItemPanel = JCheckBoxMenuItem(Strings.PANEL)
        var mItemMoveTo = JMenuItem(Strings.MOVE_TO_TOP)
        var mItemSelection = JCheckBoxMenuItem(Strings.TOOL_SELECTION)
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
            mItemSelection.toolTipText = TooltipStrings.TOOL_SELECTION
            mItemSelection.state = mMainUI.mItemToolSelection.state
            mItemSelection.addActionListener(mToolsActionHandler)
            add(mItemSelection)
            mItemTest.state = mMainUI.mItemToolTest.state
            mItemTest.addActionListener(mToolsActionHandler)
            if (mMainUI.mToolTestEnable) {
                add(mItemTest)
            }
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
                    mItemSelection -> {
                        mMainUI.mItemToolSelection.doClick()
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
            if (mToolMap[toolId] is ToolComponent) {
                val toolComponent = mToolMap[toolId] as ToolComponent
                addTab(mToolMap[toolId]!!.name, toolComponent.getToolIcon(), mToolMap[toolId], toolComponent.getTooltip())
            }
            else {
                addTab(mToolMap[toolId]!!.name, mToolMap[toolId])
            }
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

    fun isShowingTool(toolId: ToolId): Boolean {
        if (!isVisible) {
            return false
        }

        return selectedComponent == mToolMap[toolId]
    }

    inner class ToolTest: JLabel(), ToolComponent {
        init {
            name = "Test Tool"
            text = "This is test tool"
        }

        override fun getToolIcon(): Icon? {
            return null
        }

        override fun getTooltip(): String {
            return ""
        }
    }

    class ToolSelection(isPlainText: Boolean): JScrollPane(), ToolComponent {
        val mIsPlainText = isPlainText
        private val mTextComponent:JTextComponent
        private val mPopupMenu: PopUpLogViewDialog
        private val mIncludeAction: Action
        private val mAddIncludeKey = "add_include"
        var mPrevLines = 2
        var mNextLines = 2

        init {
            name = Strings.TOOL_SELECTION
            mTextComponent = if (mIsPlainText) {
                JTextArea()
            } else {
                JEditorPane()
            }
            mTextComponent.isEditable = false
            mTextComponent.caret.isVisible = true
            if (mTextComponent is JEditorPane) {
                mTextComponent.contentType = "text/html"
            }
            else if (mTextComponent is JTextArea) {
                mTextComponent.lineWrap = true
            }

            mTextComponent.addMouseListener(MouseHandler())
            setViewportView(mTextComponent)
            horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER

            mIncludeAction = object : AbstractAction(mAddIncludeKey) {
                override fun actionPerformed(evt: ActionEvent?) {
                    if (evt != null) {
                        val textSplit = evt.actionCommand.split(":")
                        var comboText = MainUI.getInstance().getTextShowLogCombo()
                        if (comboText.isNotEmpty()) {
                            comboText += "|"
                        }

                        if (mTextComponent.selectedText != null) {
                            val selectedText = if (mIsPlainText) {
                                mTextComponent.selectedText
                            } else {
                                mTextComponent.selectedText.replace("\u00a0", " ")
                            }
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
            mTextComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK), key)
            mTextComponent.actionMap.put(key, mIncludeAction)

            mPopupMenu = PopUpLogViewDialog()
        }

        fun setBgColor(logBG: Color) {
            mTextComponent.background = logBG
        }

        fun setSelectionLog(pair: Pair<String, Int>) {
            mTextComponent.font = MainUI.getInstance().mFont
            if (mIsPlainText) {
                mTextComponent.text = pair.first
            }
            else {
                mTextComponent.text = "<html>${pair.first}</html>"
            }
            mTextComponent.caretPosition = pair.second
        }

        fun isVisiblePopupMenu(): Boolean {
            return mPopupMenu.isVisible
        }

        fun addFocusListenerToEditor(focusHandler: FocusListener) {
            mTextComponent.addFocusListener(focusHandler)
        }

        fun addFocusListenerToPopup(popupFocusHandler: FocusListener) {
            mPopupMenu.addFocusListener(popupFocusHandler)
        }

        internal inner class PopUpLogViewDialog : JPopupMenu() {
            var mIncludeItem = JMenuItem(Strings.ADD_INCLUDE)
            var mIncludeSetItem = JMenuItem(Strings.SET_INCLUDE)
            var mIncludeRemoveItem = JMenuItem(Strings.REMOVE_INCLUDE)
            var mExcludeItem = JMenuItem(Strings.ADD_EXCLUDE)
            var mFindAddItem = JMenuItem(Strings.ADD_FIND)
            var mFindSetItem = JMenuItem(Strings.SET_FIND)
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
                mIncludeSetItem.addActionListener(mActionHandler)
                add(mIncludeSetItem)
                mIncludeRemoveItem.addActionListener(mActionHandler)
                add(mIncludeRemoveItem)

                mExcludeItem.addActionListener(mActionHandler)
                add(mExcludeItem)
                mFindAddItem.addActionListener(mActionHandler)
                add(mFindAddItem)
                mFindSetItem.addActionListener(mActionHandler)
                add(mFindSetItem)
                mCopyItem.addActionListener(mActionHandler)
                add(mCopyItem)
                mCloseItem.addActionListener(mActionHandler)
                add(mCloseItem)
            }

            internal inner class ActionHandler : ActionListener {
                override fun actionPerformed(p0: ActionEvent?) {
                    when (p0?.source) {
                        mIncludeSetItem -> {
                            if (!mTextComponent.selectedText.isNullOrEmpty()) {
                                val selectedText = if (mIsPlainText) {
                                    mTextComponent.selectedText
                                } else {
                                    mTextComponent.selectedText.replace("\u00a0", " ")
                                }

                                MainUI.getInstance().setTextShowLogCombo(selectedText)
                                MainUI.getInstance().applyShowLogCombo(true)
                            }
                        }
                        mIncludeRemoveItem -> {
                            if (!mTextComponent.selectedText.isNullOrEmpty()) {
                                val selectedText = if (mIsPlainText) {
                                    mTextComponent.selectedText
                                } else {
                                    mTextComponent.selectedText.replace("\u00a0", " ")
                                }
                                MainUI.getInstance().removeIncludeFilterShowLogCombo(selectedText)
                            }
                        }
                        mExcludeItem -> {
                            if (!mTextComponent.selectedText.isNullOrEmpty()) {
                                var text = MainUI.getInstance().getTextShowLogCombo()
                                val selectedText = if (mIsPlainText) {
                                    mTextComponent.selectedText
                                } else {
                                    mTextComponent.selectedText.replace("\u00a0", " ")
                                }
                                text += "|-$selectedText"
                                MainUI.getInstance().setTextShowLogCombo(text)
                                MainUI.getInstance().applyShowLogCombo(true)
                            }
                        }
                        mFindAddItem -> {
                            if (!mTextComponent.selectedText.isNullOrEmpty()) {
                                var text = MainUI.getInstance().getTextFindCombo()
                                val selectedText = if (mIsPlainText) {
                                    mTextComponent.selectedText
                                } else {
                                    mTextComponent.selectedText.replace("\u00a0", " ")
                                }
                                text += "|$selectedText"
                                MainUI.getInstance().setTextFindCombo(text)
                            }
                        }
                        mFindSetItem -> {
                            if (!mTextComponent.selectedText.isNullOrEmpty()) {
                                val selectedText = if (mIsPlainText) {
                                    mTextComponent.selectedText
                                } else {
                                    mTextComponent.selectedText.replace("\u00a0", " ")
                                }
                                MainUI.getInstance().setTextFindCombo(selectedText)
                            }
                        }
                        mCopyItem -> {
                            mTextComponent.copy()
                            if (!mIsPlainText) {
                                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                var htmlFromClipboard: String? = null
                                var htmlFlavor: DataFlavor? = null
                                for (flavor in clipboard.availableDataFlavors) {
                                    if (flavor.isMimeTypeEqual(DataFlavor.allHtmlFlavor.mimeType) && flavor.representationClass == DataFlavor.allHtmlFlavor.representationClass) {
                                        htmlFlavor = flavor
                                    }
                                }
                                if (htmlFlavor != null) {
                                    htmlFromClipboard = clipboard.getData(htmlFlavor) as String
                                    htmlFromClipboard = htmlFromClipboard.replace("[\\r\\n]+".toRegex(), "");
                                    val text = Utils.convertHtmlToPlainText(htmlFromClipboard)
                                    if (text.isNotEmpty()) {
                                        val sel = StringSelection(text)
                                        Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, null)
                                    }
                                }
                            }
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
                    mTextComponent.requestFocus()
                    val offset = mTextComponent.viewToModel(p0.point)
                    var needSelect = true
                    if (!mTextComponent.selectedText.isNullOrEmpty()) {
                        if (offset >= mTextComponent.selectionStart && offset <= mTextComponent.selectionEnd) {
                            needSelect = false
                        }
                    }
                    if (needSelect) {
                        val start = Utilities.getWordStart(mTextComponent, offset)
                        val end = Utilities.getWordEnd(mTextComponent, offset)
                        mTextComponent.select(start, end)
                    }
                    mPopupMenu.show(p0.component, p0.x, p0.y)
                } else {
                    mPopupMenu.isVisible = false
                }

                super.mouseReleased(p0)
            }
        }

        override fun getToolIcon(): Icon? {
            return null
        }

        override fun getTooltip(): String {
            return TooltipStrings.TOOL_SELECTION
        }

        fun getLogHeight(width: Int): Int {
            if (mIsPlainText) {
                val fm: FontMetrics = mTextComponent.getFontMetrics(mTextComponent.font)
                val lines = mTextComponent.text.split("\n")

                var lineCount = lines.size
                for (line in lines) {
                    lineCount += fm.stringWidth(line) / width
                }

                return if (lineCount > 0) {
                    val borderInsets = mTextComponent.border.getBorderInsets(mTextComponent)
                    val margin = mTextComponent.margin.top + mTextComponent.margin.bottom + borderInsets.top + borderInsets.bottom
                    val height = fm.height * lineCount + margin
                    if (height > MainUI.getInstance().height) {
                        MainUI.getInstance().height
                    }
                    else {
                        height
                    }
                } else {
                    preferredSize.height
                }
            }
            else {
                return preferredSize.height
            }
        }
    }
}