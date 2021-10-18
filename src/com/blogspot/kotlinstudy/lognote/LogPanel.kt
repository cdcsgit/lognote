package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.*
import java.io.File
import java.net.URI
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI



class LogPanel(tableModel: LogTableModel, basePanel: LogPanel?) :JPanel() {
    private val mBasePanel = basePanel
    private val mCtrlPanel: JPanel
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
        mCtrlPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
//        mFirstBtn = ColorButton(Strings.FIRST)
        mFirstBtn = ColorButton("∧") // △ ▲ ▽ ▼ ↑ ↓ ∧ ∨

        mFirstBtn.addActionListener(mActionHandler)
//        mLastBtn = ColorButton(Strings.LAST)
        mLastBtn = ColorButton("∨")
        mLastBtn.addActionListener(mActionHandler)
        mTagBtn = ColorToggleButton(Strings.TAG)
        mTagBtn.addActionListener(mActionHandler)
        mPidBtn = ColorToggleButton(Strings.PID)
        mPidBtn.addActionListener(mActionHandler)
        mTidBtn = ColorToggleButton(Strings.TID)
        mTidBtn.addActionListener(mActionHandler)
        mWindowedModeBtn = ColorButton(Strings.WINDOWED_MODE)
        mWindowedModeBtn.addActionListener(mActionHandler)
        mBookmarksBtn = ColorToggleButton(Strings.BOOKMARKS)
        mBookmarksBtn.addActionListener(mActionHandler)
        mFullBtn = ColorToggleButton(Strings.FULL)
        mFullBtn.addActionListener(mActionHandler)

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

        add(mCtrlPanel, BorderLayout.NORTH)
        add(mVStatusPanel, BorderLayout.WEST)
        add(mScrollPane, BorderLayout.CENTER)

        transferHandler = TableTransferHandler()
        addComponentListener(mComponentHandler)

        mIsCreatingUI = false
    }

    var mFont: Font = Font("Dialog", Font.PLAIN, 12)
        set(value) {
            field = value
            mTable.font = value
            mTable.rowHeight = value.size + 4
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

        fun tableChangedInternal(event: LogTableModelEvent?) {
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
                                println("tableChanged Tid = " + Thread.currentThread().getId() + ", num = " + num + ", selectedLine = " + selectedLine)
                                mTable.setRowSelectionInterval(idx, idx)
                                val viewRect: Rectangle = mTable.getCellRect(idx, 0, true)
                                println(
                                    "tableChanged Tid = " + Thread.currentThread()
                                        .getId() + ", viewRect = " + viewRect.toString() + ", rowCount = " + mTable.rowCount + ", idx " + idx
                                )
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

    internal inner class ActionHandler() : ActionListener {
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
            if (!info.isDrop) {
                return false
            }

            var file: File? = null

            if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                val data: String
                try {
                    data = info.transferable.getTransferData(DataFlavor.stringFlavor) as String
                    file = File(URI(data.trim()))
                } catch (e: Exception) {
                    return false
                }
            }

            if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                val listFile: Any
                try {
                    listFile = info.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                    if (listFile is List<*>) {
                        val iterator = listFile.iterator()
                        if (iterator.hasNext()) {
                            file = iterator.next() as File
                        }
                    }
                } catch (e: Exception) {
                    return false
                }
            }

            val frame = SwingUtilities.windowForComponent(this@LogPanel) as MainUI
            if (file != null) {
                frame.openFile(file.absolutePath)
            }
            return true
        }
    }

    internal inner class ComponenetHander() : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            if (e != null) {
                mTable.updateColumnWidth(e.component.width)
            }
            super.componentResized(e)
        }
    }

    internal inner class PopUpLogPanel() : JPopupMenu() {
        var mReconnectItem = JMenuItem("Reconnect adb")
        var mStartItem = JMenuItem("Start")
        var mStopItem = JMenuItem("Stop")
        var mClearItem = JMenuItem("Clear")
        var mClearSaveItem = JMenuItem("Clear/Save")
        val mActionHandler = ActionHandler()

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

        internal inner class ActionHandler() : ActionListener {
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

        var popupMenu: JPopupMenu? = null
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

    }
}
