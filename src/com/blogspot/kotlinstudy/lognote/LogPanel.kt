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


class LogPanel(tableModel: LogTableModel, basePanel: LogPanel?) :JPanel() {
    private val mBasePanel = basePanel
    private val mCtrlPanel: ButtonPanel
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
    private val mComponentHandler = ComponenetHander()

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
        mCtrlPanel = ButtonPanel()
//        mFirstBtn = ColorButton(Strings.FIRST)
        mFirstBtn = ColorButton("∧") // △ ▲ ▽ ▼ ↑ ↓ ∧ ∨
        mFirstBtn.toolTipText = TooltipStrings.VIEW_FIRST_BTN
        mFirstBtn.margin = Insets(0, 7, 0, 7)

        mFirstBtn.addActionListener(mActionHandler)
//        mLastBtn = ColorButton(Strings.LAST)
        mLastBtn = ColorButton("∨")
        mLastBtn.toolTipText = TooltipStrings.VIEW_LAST_BTN
        mLastBtn.margin = Insets(0, 7, 0, 7)
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

        mTable.columnSelectionAllowed = true
        mTable.selectionModel.addListSelectionListener(mListSelectionHandler)
        mScrollPane = JScrollPane(mTable)

        mVStatusPanel = VStatusPanel(mTable)

        mBookmarkManager.addBookmarkEventListener(mBookmarkHandler)

        mScrollPane.verticalScrollBar.ui = BasicScrollBarUI()
        mScrollPane.horizontalScrollBar.ui = BasicScrollBarUI()
        mScrollPane.verticalScrollBar.unitIncrement = 20

        mScrollPane.verticalScrollBar.addAdjustmentListener(mAdjustmentHandler)
        mScrollPane.horizontalScrollBar.addAdjustmentListener(mAdjustmentHandler)
        mScrollPane.addMouseListener(MouseHandler())

        mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        mScrollPane.isOpaque = false
        mScrollPane.viewport.isOpaque = false

        add(mCtrlPanel, BorderLayout.NORTH)
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
        var isAdded = false
        if (customArray != null) {
            for (item in customArray) {
                if (!item.mTableBar) {
                    continue
                }
                val button = TableBarButton(item.mTitle)
                button.mValue = item.mValue
                button.toolTipText = "${item.mTitle} : ${item.mValue}"
                button.margin = Insets(0, 3, 0, 3)
                button.addActionListener(ActionListener { e: ActionEvent? ->
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.setTextShowLogCombo((e?.source as TableBarButton).mValue)
                    frame.applyShowLogCombo()
                })
                mCtrlPanel.add(button)
                isAdded = true
            }
        }
        if (!isAdded) {
            val button = TableBarButton(Strings.ADD_FILTER)
            button.toolTipText = TooltipStrings.ADD_FILTER_BTN
            button.margin = Insets(0, 3, 0, 3)
            button.addActionListener(ActionListener { e: ActionEvent? ->
                val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                frame.mFiltersBtn.doClick()
            })
            mCtrlPanel.add(button)
        }
    }

    private fun updateTableBarCmds(customArray: ArrayList<CustomListManager.CustomElement>?) {
        var isAdded = false
        if (customArray != null) {
            for (item in customArray) {
                if (!item.mTableBar) {
                    continue
                }
                val button = TableBarButton(item.mTitle)
                button.mValue = item.mValue
                button.toolTipText = "${item.mTitle} : ${item.mValue}"
                button.margin = Insets(0, 3, 0, 3)
                button.addActionListener(ActionListener { e: ActionEvent? ->
                    var cmd = (e?.source as TableBarButton).mValue
                    if (cmd.startsWith("adb ")) {
                        cmd = cmd.replaceFirst("adb ", "${AdbManager.getInstance().mAdbCmd} -s ${AdbManager.getInstance().mTargetDevice} ")
                    } else if (cmd.startsWith("adb.exe ")) {
                        cmd = cmd.replaceFirst("adb.exe ", "${AdbManager.getInstance().mAdbCmd} -s ${AdbManager.getInstance().mTargetDevice} ")
                    }

                    if (cmd.isNotEmpty()) {
                        val runtime = Runtime.getRuntime()
                        runtime.exec(cmd)
                    }
                })
                mCtrlPanel.add(button)
                isAdded = true
            }
        }
        if (!isAdded) {
            val button = TableBarButton(Strings.ADD_CMD)
            button.toolTipText = TooltipStrings.ADD_CMD_BTN
            button.margin = Insets(0, 3, 0, 3)
            button.addActionListener(ActionListener { e: ActionEvent? ->
                val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                frame.mCmdsBtn.doClick()
            })
            mCtrlPanel.add(button)
        }
    }

    fun updateTableBar(customArray: ArrayList<CustomListManager.CustomElement>?) {
        mCtrlPanel.removeAll()
        mCtrlPanel.add(mFirstBtn)
        mCtrlPanel.add(mLastBtn)
        mCtrlPanel.add(mPidBtn)
        mCtrlPanel.add(mTidBtn)
        mCtrlPanel.add(mTagBtn)

        if (mBasePanel != null) {
            mCtrlPanel.add(mFullBtn)
            mCtrlPanel.add(mBookmarksBtn)
        }
        if (mBasePanel == null) {
            mCtrlPanel.add(mWindowedModeBtn)
        }

        addVSeparator(mCtrlPanel)
        if (mBasePanel != null) {
            updateTableBarFilters(customArray)
        }
        else {
            updateTableBarCmds(customArray)
        }
        mCtrlPanel.updateUI()
    }

    var mFont: Font = Font("Dialog", Font.PLAIN, 12)
        set(value) {
            field = value
            mTable.font = value
            mTable.rowHeight = value.size + 4

            var bg = Color.WHITE
            if (mBasePanel != null) {
                bg = ColorManager.getInstance().mFilterTableColor.LogBG
                println("Color filter log bg $bg")
            }
            else {
                bg = ColorManager.getInstance().mFullTableColor.LogBG
                println("Color full log bg $bg")
            }

            if (bg != background) {
                background =  bg
            }

            repaint()
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
            mTable.updateColumnWidth(this@LogPanel.width)
            if (event?.mDataChange == LogTableModelEvent.EVENT_CHANGED) {
                if (getGoToLast() && mTable.rowCount > 0) {
                    val viewRect = mTable.getCellRect(mTable.rowCount - 1, 0, true)
                    viewRect.x = mTable.visibleRect.x
                    mTable.scrollRectToVisible(viewRect)
                }
                else {
                    if (event.mRemovedCount > 0 && mTable.selectedRow > 0) {
                        var idx = mTable.selectedRow - event.mRemovedCount
                        if (idx < 0) {
                            idx = 0
                        }

                        val selectedLine = mTable.getValueAt(idx, 0).toString().trim().toInt()

                        if (selectedLine >= 0) {
                            mTable.setRowSelectionInterval(idx, idx)
                            val viewRect: Rectangle = mTable.getCellRect(idx, 0, true)
                            mTable.scrollRectToVisible(viewRect)
                            mTable.scrollRectToVisible(viewRect) // sometimes not work
                        }
                    }
                }
            } else if (event?.mDataChange == LogTableModelEvent.EVENT_FILTERED) {
                if (mBasePanel != null) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    val selectedLine = frame.getMarkLine()
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
            if (p0?.source == mFirstBtn) {
                goToFirst()
            } else if (p0?.source == mLastBtn) {
                goToLast()
            } else if (p0?.source == mWindowedModeBtn) {
                val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                frame.windowedModeLogPanel(this@LogPanel)
            } else if (p0?.source == mTagBtn) {
                val selected = mTagBtn.model.isSelected
                mTable.mTableModel.mBoldTag = selected
                mTable.repaint()
            } else if (p0?.source == mPidBtn) {
                val selected = mPidBtn.model.isSelected
                mTable.mTableModel.mBoldPid = selected
                mTable.repaint()
            } else if (p0?.source == mTidBtn) {
                val selected = mTidBtn.model.isSelected
                mTable.mTableModel.mBoldTid = selected
                mTable.repaint()
            } else if (p0?.source == mBookmarksBtn) {
                val selected = mBookmarksBtn.model.isSelected
                if (selected) {
                    mFullBtn.model.isSelected = false
                }
                mTable.mTableModel.mBookmarkMode = selected
                mTable.repaint()
            } else if (p0?.source == mFullBtn) {
                val selected = mFullBtn.model.isSelected
                if (selected) {
                    mBookmarksBtn.model.isSelected = false
                }
                mTable.mTableModel.mFullMode = selected
                mTable.repaint()
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

            val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
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
                        frame, Strings.MSG_SELECT_OPEN_MODE,
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
                            frame.openFile(file.absolutePath, true)
                        }
                    }
                    1 -> {
                        var isFirst = true
                        for (file in fileList) {
                            if (isFirst) {
                                frame.openFile(file.absolutePath, false)
                                isFirst = false
                            } else {
                                frame.openFile(file.absolutePath, true)
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

    internal inner class ComponenetHander : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            if (e != null) {
                mTable.updateColumnWidth(e.component.width)
            }
            super.componentResized(e)
        }
    }

    internal inner class PopUpLogPanel : JPopupMenu() {
        var mReconnectItem = JMenuItem("Reconnect adb")
        var mStartItem = JMenuItem("Start")
        var mStopItem = JMenuItem("Stop")
        var mClearItem = JMenuItem("Clear")
        var mClearSaveItem = JMenuItem("Clear/Save")
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
            mClearSaveItem.addActionListener(mActionHandler)
            add(mClearSaveItem)
        }

        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                if (p0?.source == mReconnectItem) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.reconnectAdb()
                } else if (p0?.source == mStartItem) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.startAdbLog()
                } else if (p0?.source == mStopItem) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.stopAdbLog()
                } else if (p0?.source == mClearItem) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.clearAdbLog()
                } else if (p0?.source == mClearSaveItem) {
                    val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
                    frame.clearSaveAdbLog()
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

    internal inner class ButtonPanel : JPanel() {
        internal inner class ButtonFlowLayout(align: Int, hgap: Int, vgap: Int) : FlowLayout(align, hgap, vgap) {
            override fun minimumLayoutSize(target: Container?): Dimension {
                return Dimension(0, 0)
            }
        }
        var mLastComponent: Component? = null
        init {
            layout = ButtonFlowLayout(FlowLayout.LEFT, 0, 0)
            addComponentListener(
                object : ComponentAdapter() {
                    var mPrevPoint: Point? = null
                    override fun componentResized(e: ComponentEvent) {
                        super.componentResized(e)
                        for (item in components) {
                            if (mLastComponent == null) {
                                mLastComponent = item
                            } else {
                                if ((item.location.y + item.height) > (mLastComponent!!.location.y + mLastComponent!!.height)) {
                                    mLastComponent = item
                                }
                            }
                        }
                        if (mPrevPoint == null || mPrevPoint!!.y != mLastComponent!!.location.y) {
                            println("lastComonent moved to ${mLastComponent!!.location}")
                            preferredSize = Dimension(preferredSize.width, mLastComponent!!.location.y + mLastComponent!!.height)
                            updateUI()
                        }
                        mPrevPoint = mLastComponent!!.location
                    }
                })
        }
    }
}
