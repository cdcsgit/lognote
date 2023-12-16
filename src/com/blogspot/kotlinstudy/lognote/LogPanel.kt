package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.*
import java.io.File
import java.net.URI
import java.util.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI


class LogPanel(mainUI: MainUI, tableModel: LogTableModel, basePanel: LogPanel?, focusHandler: MainUI.FocusHandler) :JPanel() {
    private val mMainUI = mainUI
    private val mBasePanel = basePanel
    private val mCtrlMainPanel: ButtonPanel
    private var mFirstBtn: ColorButton
    private var mLastBtn: ColorButton
    private var mTagBtn: ColorToggleButton
    private var mPidBtn: ColorToggleButton
    private var mTidBtn: ColorToggleButton
    private var mWindowedModeBtn: ColorButton
    private var mBookmarksBtn: ColorToggleButton
    private var mFullBtn: ColorToggleButton

    private val mScrollPane: JScrollPane
    private val mVStatusPanel: VStatusPanel
    private val mTable: LogTable
    private var mSelectedRow = -1
    private val mBookmarkManager = BookmarkManager.getInstance()
    private val mAdjustmentHandler = AdjustmentHandler()
    private val mListSelectionHandler = ListSelectionHandler()
    private val mTableModelHandler = TableModelHandler()
    private val mActionHandler = ActionHandler()
    private val mBookmarkHandler = BookmarkHandler()
    private val mComponentHandler = ComponentHandler()
    private val mFocusHandler = focusHandler

    private var mOldLogVPos = -1
    private var mOldLogHPos = -1
    private var mIsCreatingUI = true

    var mIsWindowedMode = false
        set(value) {
            field = value
            mWindowedModeBtn.isEnabled = !value
        }

    init {
        layout = BorderLayout()
        mCtrlMainPanel = ButtonPanel()
//        mFirstBtn = ColorButton("∧") // △ ▲ ▽ ▼ ↑ ↓ ∧ ∨
        mFirstBtn = ColorButton("")
        mFirstBtn.icon = ImageIcon(this.javaClass.getResource("/images/top.png"))
        mFirstBtn.toolTipText = TooltipStrings.VIEW_FIRST_BTN
        mFirstBtn.margin = Insets(2, 3, 1, 3)

        mFirstBtn.addActionListener(mActionHandler)
        mLastBtn = ColorButton("")
        mLastBtn.icon = ImageIcon(this.javaClass.getResource("/images/bottom.png"))
        mLastBtn.toolTipText = TooltipStrings.VIEW_LAST_BTN
        mLastBtn.margin = Insets(2, 3, 1, 3)
        mLastBtn.addActionListener(mActionHandler)
        mTagBtn = ColorToggleButton(Strings.TAG)
        mTagBtn.toolTipText = TooltipStrings.VIEW_TAG_TOGGLE
        mTagBtn.margin = Insets(0, 3, 0, 3)
        mTagBtn.addActionListener(mActionHandler)
        mPidBtn = ColorToggleButton(Strings.PID)
        mPidBtn.toolTipText = TooltipStrings.VIEW_PID_TOGGLE
        mPidBtn.margin = Insets(0, 3, 0, 3)
        mPidBtn.addActionListener(mActionHandler)
        mTidBtn = ColorToggleButton(Strings.TID)
        mTidBtn.toolTipText = TooltipStrings.VIEW_TID_TOGGLE
        mTidBtn.margin = Insets(0, 3, 0, 3)
        mTidBtn.addActionListener(mActionHandler)
        mWindowedModeBtn = ColorButton(Strings.WINDOWED_MODE)
        mWindowedModeBtn.toolTipText = TooltipStrings.VIEW__WINDOWED_MODE_BTN
        mWindowedModeBtn.margin = Insets(0, 3, 0, 3)
        mWindowedModeBtn.addActionListener(mActionHandler)
        mBookmarksBtn = ColorToggleButton(Strings.BOOKMARKS)
        mBookmarksBtn.toolTipText = TooltipStrings.VIEW_BOOKMARKS_TOGGLE
        mBookmarksBtn.margin = Insets(0, 3, 0, 3)
        mBookmarksBtn.addActionListener(mActionHandler)
        mFullBtn = ColorToggleButton(Strings.FULL)
        mFullBtn.toolTipText = TooltipStrings.VIEW_FULL_TOGGLE
        mFullBtn.margin = Insets(0, 3, 0, 3)
        mFullBtn.addActionListener(mActionHandler)

        updateTableBar(null)

        tableModel.addLogTableModelListener(mTableModelHandler)
        mTable = LogTable(tableModel)
        mTable.addFocusListener(mFocusHandler)

        mTable.columnSelectionAllowed = true
        mTable.selectionModel.addListSelectionListener(mListSelectionHandler)
        mScrollPane = JScrollPane(mTable)

        mVStatusPanel = VStatusPanel(mTable)

        mBookmarkManager.addBookmarkEventListener(mBookmarkHandler)

        mScrollPane.verticalScrollBar.setUI(BasicScrollBarUI())
        mScrollPane.horizontalScrollBar.setUI(BasicScrollBarUI())
        mScrollPane.verticalScrollBar.unitIncrement = 20

        mScrollPane.verticalScrollBar.addAdjustmentListener(mAdjustmentHandler)
        mScrollPane.horizontalScrollBar.addAdjustmentListener(mAdjustmentHandler)
        mScrollPane.addMouseListener(MouseHandler())

        mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        mScrollPane.isOpaque = false
        mScrollPane.viewport.isOpaque = false

        mScrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK), "none")
        mScrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK), "none")

        val ctrlPanel = JPanel()
        ctrlPanel.layout = BoxLayout(ctrlPanel, BoxLayout.Y_AXIS)
        ctrlPanel.add(mCtrlMainPanel)

        add(ctrlPanel, BorderLayout.NORTH)
        add(mVStatusPanel, BorderLayout.WEST)
        add(mScrollPane, BorderLayout.CENTER)

        transferHandler = TableTransferHandler()
        addComponentListener(mComponentHandler)

        mIsCreatingUI = false
    }

    private fun addVSeparator(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width, 10)
        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
            separator1.foreground = Color.GRAY
            separator1.background = Color.GRAY
        }
        else {
            separator1.foreground = Color.DARK_GRAY
            separator1.background = Color.DARK_GRAY
        }

        panel.add(Box.createHorizontalStrut(2))
        panel.add(separator1)
        panel.add(Box.createHorizontalStrut(2))
    }

    private fun updateTableBarFilters(customArray: ArrayList<CustomListManager.CustomElement>?) {
        val filtersBtn = TableBarButton(Strings.FILTERS)
        filtersBtn.icon = ImageIcon(this.javaClass.getResource("/images/filterscmds.png"))
        filtersBtn.toolTipText = TooltipStrings.ADD_FILTER_BTN
        filtersBtn.margin = Insets(0, 3, 0, 3)
        filtersBtn.addActionListener {
            mMainUI.mFiltersManager.showDialog()
        }
        mCtrlMainPanel.add(filtersBtn)

        val icon = ImageIcon(this.javaClass.getResource("/images/filterscmdsitem.png"))
        if (customArray != null) {
            for (item in customArray) {
                if (!item.mTableBar) {
                    continue
                }
                val button = TableBarButton(item.mTitle)
                button.icon = icon
                button.mValue = item.mValue
                button.toolTipText = "<html>${item.mTitle} : <b>\"${item.mValue}\"</b><br><br>* Append : Ctrl + Click</html>"
                button.margin = Insets(0, 3, 0, 3)
                button.addActionListener { e: ActionEvent? ->
                    if ((ActionEvent.CTRL_MASK and e!!.modifiers) != 0) {
                        val filterText = mMainUI.getTextShowLogCombo()
                        if (filterText.isEmpty()) {
                            mMainUI.setTextShowLogCombo((e.source as TableBarButton).mValue)
                        } else {
                            if (filterText.substring(filterText.length - 1) == "|") {
                                mMainUI.setTextShowLogCombo(filterText + (e.source as TableBarButton).mValue)
                            } else {
                                mMainUI.setTextShowLogCombo(filterText + "|" + (e.source as TableBarButton).mValue)
                            }
                        }
                    } else {
                        mMainUI.setTextShowLogCombo((e.source as TableBarButton).mValue)
                    }
                    mMainUI.applyShowLogCombo(false)
                }
                mCtrlMainPanel.add(button)
            }
        }
    }

    private fun updateTableBarCmds(customArray: ArrayList<CustomListManager.CustomElement>?) {
        val cmdsBtn = TableBarButton(Strings.CMDS)
        cmdsBtn.icon = ImageIcon(this.javaClass.getResource("/images/filterscmds.png"))
        cmdsBtn.toolTipText = TooltipStrings.ADD_CMD_BTN
        cmdsBtn.margin = Insets(0, 3, 0, 3)
        cmdsBtn.addActionListener {
            mMainUI.mCmdManager.showDialog()
        }
        mCtrlMainPanel.add(cmdsBtn)

        val icon = ImageIcon(this.javaClass.getResource("/images/filterscmdsitem.png"))
        if (customArray != null) {
            for (item in customArray) {
                if (!item.mTableBar) {
                    continue
                }
                val button = TableBarButton(item.mTitle)
                button.icon = icon
                button.mValue = item.mValue
                button.toolTipText = "${item.mTitle} : ${item.mValue}"
                button.margin = Insets(0, 3, 0, 3)
                button.addActionListener { e: ActionEvent? ->
                    var cmd = (e?.source as TableBarButton).mValue
                    if (cmd.startsWith("adb ")) {
                        cmd = cmd.replaceFirst(
                            "adb ",
                            "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} "
                        )
                    } else if (cmd.startsWith("adb.exe ")) {
                        cmd = cmd.replaceFirst(
                            "adb.exe ",
                            "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} "
                        )
                    }

                    if (cmd.isNotEmpty()) {
                        val runtime = Runtime.getRuntime()
                        runtime.exec(cmd)
                    }
                }
                mCtrlMainPanel.add(button)
            }
        }
    }

    fun updateTableBar(customArray: ArrayList<CustomListManager.CustomElement>?) {
        mCtrlMainPanel.removeAll()
        mCtrlMainPanel.add(mFirstBtn)
        mCtrlMainPanel.add(mLastBtn)
        mCtrlMainPanel.add(mPidBtn)
        mCtrlMainPanel.add(mTidBtn)
        mCtrlMainPanel.add(mTagBtn)

        if (mBasePanel != null) {
            mCtrlMainPanel.add(mFullBtn)
            mCtrlMainPanel.add(mBookmarksBtn)
        }
        if (mBasePanel == null) {
            mCtrlMainPanel.add(mWindowedModeBtn)
        }

        addVSeparator(mCtrlMainPanel)
        if (mBasePanel != null) {
            updateTableBarFilters(customArray)
        }
        else {
            updateTableBarCmds(customArray)
        }
        mCtrlMainPanel.updateUI()
    }

    var mFont: Font = Font(MainUI.DEFAULT_FONT_NAME, Font.PLAIN, 12)
        set(value) {
            field = value
            mTable.font = value
            mTable.rowHeight = value.size + 4

            repaint()
        }

    override fun repaint() {
        val bg = if (mBasePanel != null) {
            ColorManager.getInstance().mFilterTableColor.mLogBG
        }
        else {
            ColorManager.getInstance().mFullTableColor.mLogBG
        }

        if (bg != background) {
            background =  bg
        }

        super.repaint()
    }

    fun goToRow(idx: Int, column: Int) {
        if (idx < 0 || idx >= mTable.rowCount) {
            println("goToRow : invalid idx")
            return
        }
        mTable.setRowSelectionInterval(idx, idx)
        val viewRect: Rectangle
        if (column < 0) {
            viewRect = mTable.getCellRect(idx, 0, true)
            viewRect.x = mTable.visibleRect.x
        } else {
            viewRect = mTable.getCellRect(idx, column, true)
        }
        mTable.scrollRectToVisible(viewRect)
    }

    fun goToRowByNum(num: Int, column: Int) {
        val firstNum = mTable.getValueAt(0, 0).toString().trim().toInt()
        var idx = num - firstNum
        if (idx < 0) {
            idx = 0
        }

        goToRow(idx, column)
    }

    fun setGoToLast(value: Boolean) {
        mTable.mTableModel.mGoToLast = value
    }

    fun getGoToLast(): Boolean {
        return mTable.mTableModel.mGoToLast
    }

    fun goToFirst() {
        setGoToLast(false)
        goToRow(0, -1)
        updateTableUI()
        return
    }

    fun goToLast() {
        if (mTable.rowCount > 0) {
            goToRow(mTable.rowCount - 1, -1)
            setGoToLast(true)
            updateTableUI()
        }

        return
    }

    fun updateTableUI() {
        mTable.updateUI()
    }

    fun getSelectedLine() : Int {
        return mTable.getValueAt(mTable.selectedRow, 0).toString().trim().toInt()
    }

    fun getSelectedRow() : Int {
        return mTable.selectedRow
    }

    internal inner class AdjustmentHandler : AdjustmentListener {
        override fun adjustmentValueChanged(p0: AdjustmentEvent?) {
            if (p0?.source == mScrollPane.verticalScrollBar) {
                val vPos = mScrollPane.verticalScrollBar.value
                if (vPos != mOldLogVPos) {
                    if (vPos < mOldLogVPos && getGoToLast()) {
                        setGoToLast(false)
                    } else if (vPos > mOldLogVPos
                            && !getGoToLast()
                            && (vPos + mScrollPane.verticalScrollBar.size.height) == mScrollPane.verticalScrollBar.maximum) {
                        setGoToLast(true)
                    }
                    mOldLogVPos = vPos
                    mVStatusPanel.repaint()
                }
            } else if (p0?.source == mScrollPane.horizontalScrollBar) {
                val hPos = mScrollPane.horizontalScrollBar.value
                if (hPos != mOldLogHPos) {
                    mOldLogHPos = hPos
                }
            }

        }
    }

    internal inner class TableModelHandler : LogTableModelListener {
        @Synchronized
        override fun tableChanged(event: LogTableModelEvent?) {
            if (event?.mDataChange == LogTableModelEvent.EVENT_CLEARED) {
                mOldLogVPos = -1
            } else {
                if (SwingUtilities.isEventDispatchThread()) {
                    tableChangedInternal(event)
                } else {
                    SwingUtilities.invokeAndWait {
                        tableChangedInternal(event)
                    }
                }
            }
        }

        private fun tableChangedInternal(event: LogTableModelEvent?) {
            updateTableUI()
            mTable.updateColumnWidth(this@LogPanel.width, mScrollPane.verticalScrollBar.width)
            if (event?.mDataChange == LogTableModelEvent.EVENT_CHANGED) {
                if (getGoToLast() && mTable.rowCount > 0) {
                    val viewRect = mTable.getCellRect(mTable.rowCount - 1, 0, true)
                    viewRect.x = mTable.visibleRect.x
                    mTable.scrollRectToVisible(viewRect)
                }
                else {
                    if (event.mRemovedCount > 0 && mTable.selectedRow > 0) {
                        val viewRow: Int = mTable.rowAtPoint(mScrollPane.viewport.viewPosition)
                        var viewIdx = viewRow - event.mRemovedCount
                        if (viewIdx < 0) {
                            viewIdx = 0
                        }

                        var selectIdx = mTable.selectedRow - event.mRemovedCount
                        if (selectIdx < 0) {
                            selectIdx = 0
                        }

                        val viewLine = mTable.getValueAt(viewIdx, 0).toString().trim().toInt()
                        if (viewLine >= 0) {
                            mTable.setRowSelectionInterval(selectIdx, selectIdx)
                            val viewRect: Rectangle = mTable.getCellRect(viewIdx, 0, true)
                            mTable.scrollRectToVisible(viewRect)
                            mTable.scrollRectToVisible(viewRect) // sometimes not work
                        }
                    }
                }
            } else if (event?.mDataChange == LogTableModelEvent.EVENT_FILTERED) {
                if (mBasePanel != null) {
                    val selectedLine = mMainUI.getMarkLine()
                    if (selectedLine >= 0) {
                        var num = 0
                        for (idx in 0 until mTable.rowCount) {
                            num = mTable.getValueAt(idx, 0).toString().trim().toInt()
                            if (selectedLine <= num) {
                                println("tableChanged Tid = ${Thread.currentThread().id}, num = $num, selectedLine = $selectedLine")
                                mTable.setRowSelectionInterval(idx, idx)
                                val viewRect: Rectangle = mTable.getCellRect(idx, 0, true)
                                println("tableChanged Tid = ${Thread.currentThread().id}, viewRect = $viewRect, rowCount = ${ mTable.rowCount }, idx = $idx")
                                mTable.scrollRectToVisible(viewRect)
                                mTable.scrollRectToVisible(viewRect) // sometimes not work
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    internal inner class ListSelectionHandler : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            if (mBasePanel != null) {
                val value = mTable.mTableModel.getValueAt(mTable.selectedRow, 0)
                val selectedRow = value.toString().trim().toInt()

                val baseValue = mBasePanel.mTable.mTableModel.getValueAt(mBasePanel.mTable.selectedRow, 0)
                val baseSelectedRow = baseValue.toString().trim().toInt()

                if (selectedRow != baseSelectedRow) {
                    setGoToLast(false)
                    mBasePanel.setGoToLast(false)
                    mBasePanel.goToRowByNum(selectedRow, -1)
                    mTable.mTableModel.mSelectionChanged = true

                    if (mTable.selectedRow == mTable.rowCount - 1) {
                        setGoToLast(true)
                    }
                }
            } else {
                if (mTable.selectedRow == mTable.rowCount - 1) {
                    setGoToLast(true)
                }
            }

            return
        }
    }

    internal inner class ActionHandler : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            when (p0?.source) {
                mFirstBtn -> {
                    goToFirst()
                }
                mLastBtn -> {
                    goToLast()
                }
                mWindowedModeBtn -> {
                    mMainUI.windowedModeLogPanel(this@LogPanel)
                }
                mTagBtn -> {
                    val selected = mTagBtn.model.isSelected
                    mTable.mTableModel.mBoldTag = selected
                    mTable.repaint()
                }
                mPidBtn -> {
                    val selected = mPidBtn.model.isSelected
                    mTable.mTableModel.mBoldPid = selected
                    mTable.repaint()
                }
                mTidBtn -> {
                    val selected = mTidBtn.model.isSelected
                    mTable.mTableModel.mBoldTid = selected
                    mTable.repaint()
                }
                mBookmarksBtn -> {
                    val selected = mBookmarksBtn.model.isSelected
                    if (selected) {
                        mFullBtn.model.isSelected = false
                    }
                    mTable.mTableModel.mBookmarkMode = selected
                    mTable.repaint()
                }
                mFullBtn -> {
                    val selected = mFullBtn.model.isSelected
                    if (selected) {
                        mBookmarksBtn.model.isSelected = false
                    }
                    mTable.mTableModel.mFullMode = selected
                    mTable.repaint()
                }
            }
        }
    }

    internal inner class BookmarkHandler : BookmarkEventListener {
        override fun bookmarkChanged(event: BookmarkEvent?) {
            mVStatusPanel.repaint()
            if (mTable.mTableModel.mBookmarkMode) {
                mTable.mTableModel.mBookmarkMode = true
            }
            mTable.repaint()
        }
    }

    internal inner class TableTransferHandler : TransferHandler() {
        override fun canImport(info: TransferSupport): Boolean {
            if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return true
            }

            if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return true
            }

            return false
        }

        override fun importData(info: TransferSupport): Boolean {
            println("importData")
            if (!info.isDrop) {
                return false
            }

            val fileList: MutableList<File> = mutableListOf()

            if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                val data: String
                try {
                    data = info.transferable.getTransferData(DataFlavor.stringFlavor) as String
                    val splitData = data.split("\n")

                    for (item in splitData) {
                        if (item.isNotEmpty()) {
                            println("importData item = $item")
                            fileList.add(File(URI(item.trim())))
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return false
                }
            }

            if (fileList.size == 0 && info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                val listFile: Any
                try {
                    listFile = info.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                    if (listFile is List<*>) {
                        val iterator = listFile.iterator()
                        while (iterator.hasNext()) {
                            fileList.add(iterator.next() as File)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return false
                }
            }

            if (fileList.size > 0) {
                val os = System.getProperty("os.name").lowercase(Locale.getDefault())
                println("os = $os, drop = ${info.dropAction}, source drop = ${info.sourceDropActions}, user drop = ${info.userDropAction}")
                val action = if (os.contains("windows")) {
                    info.dropAction
                } else {
                    info.sourceDropActions
                }

                var value = 1
                if (action == COPY) {
                    val options = arrayOf<Any>(
                        Strings.APPEND,
                        Strings.OPEN,
                        Strings.CANCEL
                    )
                    value = JOptionPane.showOptionDialog(
                        mMainUI, Strings.MSG_SELECT_OPEN_MODE,
                        "",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                    )
                }

                when (value) {
                    0 -> {
                        for (file in fileList) {
                            mMainUI.openFile(file.absolutePath, true)
                        }
                    }
                    1 -> {
                        var isFirst = true
                        for (file in fileList) {
                            if (isFirst) {
                                mMainUI.openFile(file.absolutePath, false)
                                isFirst = false
                            } else {
                                mMainUI.openFile(file.absolutePath, true)
                            }
                        }
                    }
                    else -> {
                        println("select cancel")
                    }
                }
            }
            return true
        }
    }

    internal inner class ComponentHandler : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            if (e != null) {
                mTable.updateColumnWidth(e.component.width, mScrollPane.verticalScrollBar.width)
            }
            super.componentResized(e)
        }
    }

    internal inner class PopUpLogPanel : JPopupMenu() {
        var mReconnectItem = JMenuItem("Reconnect adb")
        var mStartItem = JMenuItem("Start")
        var mStopItem = JMenuItem("Stop")
        var mClearItem = JMenuItem("Clear")
//        var mClearSaveItem = JMenuItem("Clear/Save")
        private val mActionHandler = ActionHandler()

        init {
            mReconnectItem.addActionListener(mActionHandler)
            add(mReconnectItem)
            mStartItem.addActionListener(mActionHandler)
            add(mStartItem)
            mStopItem.addActionListener(mActionHandler)
            add(mStopItem)
            mClearItem.addActionListener(mActionHandler)
            add(mClearItem)
//            mClearSaveItem.addActionListener(mActionHandler)
//            add(mClearSaveItem)
        }

        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mReconnectItem -> {
                        mMainUI.reconnectAdb()
                    }
                    mStartItem -> {
                        mMainUI.startAdbLog()
                    }
                    mStopItem -> {
                        mMainUI.stopAdbLog()
                    }
                    mClearItem -> {
                        mMainUI.clearAdbLog()
                    }
//                    mClearSaveItem -> {
//                        mMainUI.clearSaveAdbLog()
//                    }
                }
            }
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
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
                popupMenu = PopUpLogPanel()
                popupMenu?.show(p0.component, p0.x, p0.y)
            } else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }

        override fun mouseDragged(e: MouseEvent?) {
            println("mouseDragged")
            super.mouseDragged(e)
        }
    }

}
