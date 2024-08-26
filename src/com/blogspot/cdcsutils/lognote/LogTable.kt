package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.*
import javax.swing.border.AbstractBorder
import javax.swing.table.DefaultTableCellRenderer


open class LogTable(tableModel:LogTableModel) : JTable(tableModel){
    companion object {
        const val COLUMN_0_WIDTH = 80

        const val MIN_LOG_WIDTH = 720
        const val DEFAULT_LOG_WIDTH = 1920
//        const val DEFAULT_LOG_WIDTH = 3840
        var LogWidth = DEFAULT_LOG_WIDTH
    }

    var mTableModel = tableModel
    private val mTableColor: ColorManager.TableColor
    private val mBookmarkManager = BookmarkManager.getInstance()

    init {
        setShowGrid(false)
        autoResizeMode = AUTO_RESIZE_OFF
        autoscrolls = false
        dragEnabled = true
        dropMode = DropMode.INSERT

        val columnNum = columnModel.getColumn(0)
        columnNum.cellRenderer = NumCellRenderer()

        val columnLog = columnModel.getColumn(1)
        columnLog.cellRenderer = LogCellRenderer()
        intercellSpacing = Dimension(0, 0)

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none")
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK), "none")
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK), "none")

        addMouseListener(MouseHandler())
        addMouseMotionListener(MouseHandler())
        addKeyListener(TableKeyHandler())

        mTableColor = if (mTableModel.isFullDataModel()) {
            ColorManager.getInstance().mFullTableColor
        }
        else {
            ColorManager.getInstance().mFilterTableColor
        }
    }

    open fun updateColumnWidth(width: Int, scrollVBarWidth: Int) {
        if (rowCount <= 0) {
            return
        }

        val fontMetrics = getFontMetrics(font)
        val value = mTableModel.getValueAt(rowCount - 1, 0)
        val column0Width = fontMetrics.stringWidth(value.toString()) + 20
        var newWidth = width
        if (width < LogWidth) {
            newWidth = LogWidth
        }
        val preferredLogWidth = newWidth - column0Width - VStatusPanel.VIEW_RECT_WIDTH - scrollVBarWidth - 2

        val columnNum = columnModel.getColumn(0)
        val columnLog = columnModel.getColumn(1)
        if (columnNum.preferredWidth != column0Width) {
            columnNum.preferredWidth = column0Width
            columnLog.preferredWidth = preferredLogWidth
        }
        else {
            if (columnLog.preferredWidth != preferredLogWidth) {
                columnLog.preferredWidth = preferredLogWidth
            }
        }
    }

    var mScanMode = false
        set(value) {
            field = value
            val columnLog = columnModel.getColumn(1)
                columnLog.cellRenderer = LogCellRenderer()
        }

    internal class LineNumBorder(color: Color, thickness: Int) : AbstractBorder() {
        private val mColor = color
        private val mThickness = thickness
        override fun paintBorder(
            c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int
        ) {
            if (width > 0) {
                g?.color = mColor
                for (i in 1..mThickness) {
                    g?.drawLine(width - i , y, width - i, height)
                }
            }
        }

        override fun getBorderInsets(c: Component): Insets {
            return getBorderInsets(c, Insets(0, 0, 0, mThickness))
        }

        override fun getBorderInsets(c: Component?, insets: Insets): Insets {
            insets.top = 0
            insets.left = 0
            insets.bottom = 0
            insets.right = mThickness
            return insets
        }

        override fun isBorderOpaque(): Boolean {
            return true
        }
    }
    internal inner class NumCellRenderer : DefaultTableCellRenderer() {
        init {
            horizontalAlignment = JLabel.RIGHT
            verticalAlignment = JLabel.CENTER
        }
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            var num = -1
            if (value != null) {
                num = value.toString().trim().toInt()
            }

//            println("NumCellRenderer getTableCellRendererComponent $isSelected, $hasFocus, $row, $col, ${isRowSelected(row)}")
            val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel

            label.border = LineNumBorder(mTableColor.mNumLogSeperatorBG, 1)

            foreground = mTableColor.mLineNumFG
            background = if (mBookmarkManager.mBookmarks.contains(num)) {
                if (isRowSelected(row)) {
                    mTableColor.mNumBookmarkSelectedBG
                }
                else {
                    mTableColor.mNumBookmarkBG
                }
            } else if (isRowSelected(row)) {
                mTableColor.mNumSelectedBG
            } else {
                mTableColor.mLineNumBG
            }

            return label
        }
    }

    internal inner class LogCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            val newValue:String = if (value != null) {
                mTableModel.getPrintValue(value.toString(), row, col, isSelected)
            } else {
                ""
            }
            val label:JLabel
            if (newValue.isEmpty()) {
                label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
                foreground = mTableModel.getFgColor(row)
            }
            else {
                label = super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, col) as JLabel
            }

            label.border = BorderFactory.createEmptyBorder(0, 5, 0, 0)
            val numValue = mTableModel.getValueAt(row, 0)
            val num = numValue.toString().trim().toInt()
            if (mBookmarkManager.mBookmarks.contains(num)) {
                if (isRowSelected(row)) {
                    background = mTableColor.mBookmarkSelectedBG
                }
                else {
                    background = mTableColor.mBookmarkBG
                }
            } else if (isRowSelected(row)) {
                background = mTableColor.mSelectedBG
            } else {
                background = mTableColor.mLogBG
            }

            return label

        }
    }

//    internal inner class LogWrapCellRenderer : JTextArea(), TableCellRenderer {
//        init {
//            lineWrap = true
//        }
//
//        override fun getTableCellRendererComponent(
//            table: JTable?,
//            value: Any?,
//            isSelected: Boolean,
//            hasFocus: Boolean,
//            row: Int,
//            col: Int
//        ): Component {
//
//            if (table == null) {
//                return this
//            } else {
//                text = value.toString()
//                val logTableModel = mTableModel as LogTableModel
//                foreground = logTableModel.getFgColor(row)
//                setSize(table.columnModel.getColumn(col).width, preferredSize.height)
//                if (table.getRowHeight(row) != preferredSize.height) {
//                    table.setRowHeight(row, preferredSize.height)
//                }
//
//                var isSelectedChecked = isSelected
//                val dropLocation = table.getDropLocation()
//                if (dropLocation != null && !dropLocation.isInsertRow() && !dropLocation.isInsertColumn() && dropLocation.getRow() == row && dropLocation.getColumn() == col) {
//                    isSelectedChecked = true
//                }
//
//                background = Color(0xFF, 0xFF, 0xFF)
//                if (isSelectedChecked || row == table.selectedRow) {
//                    background = Color(0xC0, 0xC0, 0xC0)
//                }
//
//                return this
//            }
//        }
//    }

    fun downPage() {
        val toRect = visibleRect
        toRect.y = (selectedRow + 3) * rowHeight
        scrollRectToVisible(toRect)

        return
    }

    fun upPage() {
        val toRect = visibleRect
        toRect.y = (selectedRow - 3) * rowHeight - toRect.height
        scrollRectToVisible(toRect)

        return
    }

    fun downLine() {
        val toRect = visibleRect
        val rowY = selectedRow * rowHeight

        if (visibleRect.y + visibleRect.height - 4 * rowHeight < rowY) {
            toRect.y += rowHeight
        }
        scrollRectToVisible(toRect)

        return
    }

    fun upLine() {
        val toRect = visibleRect
        val rowY = selectedRow * rowHeight

        if (visibleRect.y + 3 * rowHeight > rowY) {
            toRect.y -= rowHeight
        }
        scrollRectToVisible(toRect)

        return
    }

    protected open fun showSelected(targetRow:Int) {
        val log = StringBuilder("")
        var caretPos = 0
        var value:String

        if (selectedRowCount > 1) {
            for (row in selectedRows) {
                value = mTableModel.getValueAt(row, 1).toString() + "\n"
                log.append(value)
            }
        }
        else {
            var startIdx = targetRow - 2
            if (startIdx < 0) {
                startIdx = 0
            }
            var endIdx = targetRow + 3
            if (endIdx > rowCount) {
                endIdx = rowCount
            }

            for (idx in startIdx until endIdx) {
                if (idx == targetRow) {
                    caretPos = log.length
                }
                value = mTableModel.getValueAt(idx, 1).toString() + "\n"
                log.append(value)
            }
        }

        val mainUI = MainUI.getInstance()
        val logViewDialog = LogViewDialog(mainUI, log.toString().trim(), caretPos)
        logViewDialog.setLocationRelativeTo(mainUI)
        logViewDialog.isVisible = true
    }

    private fun updateBookmark(targetRow:Int) {
        if (selectedRowCount > 1) {
            var isAdd = false
            for (row in selectedRows) {
                val value = mTableModel.getValueAt(row, 0)
                val bookmark = value.toString().trim().toInt()

                if (!mBookmarkManager.isBookmark(bookmark)) {
                    isAdd = true
                    break
                }
            }

            for (row in selectedRows) {
                val value = mTableModel.getValueAt(row, 0)
                val bookmark = value.toString().trim().toInt()

                if (isAdd) {
                    if (!mBookmarkManager.isBookmark(bookmark)) {
                        mBookmarkManager.addBookmark(bookmark)
                    }
                } else {
                    if (mBookmarkManager.isBookmark(bookmark)) {
                        mBookmarkManager.removeBookmark(bookmark)
                    }
                }
            }
        }
        else {
            val value = mTableModel.getValueAt(targetRow, 0)
            val bookmark = value.toString().trim().toInt()
            mBookmarkManager.updateBookmark(bookmark)
        }
    }

    internal inner class PopUpTable(point: Point) : JPopupMenu() {
        var mProcessItem: JMenuItem = JMenuItem("")
        var mCopyItem: JMenuItem = JMenuItem(Strings.COPY)
        var mShowEntireItem = JMenuItem(Strings.SHOW_ENTIRE_LINE)
        var mBookmarkItem = JMenuItem(Strings.BOOKMARK)
        var mReconnectItem = JMenuItem("${Strings.RECONNECT} - adb")
        var mStartItem = JMenuItem(Strings.START)
        var mStopItem = JMenuItem(Strings.STOP)
        var mClearItem = JMenuItem(Strings.CLEAR_VIEWS)
//        var mClearSaveItem = JMenuItem("Clear/Save")
        private val mActionHandler = ActionHandler()

        init {
            val column: Int = columnAtPoint(point)
            if (ProcessList.UpdateTime > 0 && MainUI.CurrentMethod == MainUI.METHOD_ADB && column == 1) { // column == 1, not line number
                val row: Int = rowAtPoint(point)
                val pid = mTableModel.getValueProcess(row)
                if (pid.isNotEmpty()) {
                    val processItem = ProcessList.getInstance().getProcess(pid)
                    if (processItem != null) {
                        mProcessItem.text = "${processItem.mPid} : ${processItem.mCmd} (${processItem.mUser})"
                    }
                    else {
                        mProcessItem.text = "$pid :"
                    }
                    mProcessItem.addActionListener(mActionHandler)
                    add(mProcessItem)
                }
            }

            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mShowEntireItem.addActionListener(mActionHandler)
            add(mShowEntireItem)
            mBookmarkItem.addActionListener(mActionHandler)
            add(mBookmarkItem)
            addSeparator()
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
                    mCopyItem -> {
                        this@LogTable.processKeyEvent(KeyEvent(this@LogTable, KeyEvent.KEY_PRESSED, p0.`when`, KeyEvent.CTRL_MASK, KeyEvent.VK_C, 'C'))
                    }
                    mShowEntireItem -> {
                        showSelected(selectedRow)
                    }
                    mBookmarkItem -> {
                        updateBookmark(selectedRow)
                    }
                    mReconnectItem -> {
                        val mainUI = MainUI.getInstance()
                        mainUI.reconnectAdb()
                    }
                    mStartItem -> {
                        val mainUI = MainUI.getInstance()
                        mainUI.startAdbLog()
                    }
                    mStopItem -> {
                        val mainUI = MainUI.getInstance()
                        mainUI.stopAdbLog()
                    }
                    mClearItem -> {
                        val mainUI = MainUI.getInstance()
                        mainUI.clearAdbLog()
                    }
                    mProcessItem -> {
                        ProcessList.getInstance().showList()
                    }
                }
            }
        }
    }

    private fun getProcessInfo(point: Point): String {
        var processInfo = ""
        val column: Int = columnAtPoint(point)
        if (MainUI.CurrentMethod == MainUI.METHOD_ADB && column >= 1) { // column >= 1, not line number
            val row: Int = rowAtPoint(point)
            val pid = mTableModel.getValueProcess(row)
            if (pid.isNotEmpty()) {
                val processItem = ProcessList.getInstance().getProcess(pid)
                if (processItem != null) {
                    processInfo = "${processItem.mPid} : ${processItem.mCmd} (${processItem.mUser})"
                }
            }
        }

        return processInfo
    }

    override fun getToolTipText(e: MouseEvent): String? {
        if (ProcessList.UpdateTime == 0) {
            return null
        }

        toolTipText = getProcessInfo(e.point)
        return toolTipText
    }

    internal inner class MouseHandler : MouseAdapter() {
        private var firstClickRow = 0
        private var secondClickRow = 0

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
                popupMenu = PopUpTable(Point(p0.x, p0.y))
                popupMenu?.show(p0.component, p0.x, p0.y)
            }
            else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }

        override fun mouseClicked(p0: MouseEvent?) {
            if (SwingUtilities.isLeftMouseButton(p0)) {
                if (p0?.clickCount == 2) {
                    secondClickRow = selectedRow
                    val targetRow = if (firstClickRow > secondClickRow) {
                        firstClickRow
                    } else {
                        secondClickRow
                    }
                    if (columnAtPoint(p0.point) == 0) {
                        updateBookmark(targetRow)
                    } else {
                        showSelected(targetRow)
                    }
                }
                if (p0?.clickCount == 1) {
                    firstClickRow = selectedRow
                }
            }

            super.mouseClicked(p0)
        }
    }

    private fun copyToClipboard() {
        val numCols = columnCount
        val numRows = selectedRows.size
        val copyStr = StringBuilder()

        if (numRows >= 1 && numCols > 1) {
            for (row in 0 until numRows) {
                for (col in 1 until numCols) {
                    copyStr.append(getValueAt(selectedRows[row], col))
                    if (col < numCols - 1) {
                        copyStr.append(" ")
                    }
                }
                copyStr.append(System.lineSeparator())
            }

            val sel = StringSelection(copyStr.toString())
            Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, null)
        }
    }

    internal inner class TableKeyHandler : KeyAdapter() {
        private var mPrevKeyEvent: KeyEvent? = null

        override fun keyReleased(p0: KeyEvent?) {
            when (p0?.keyCode) {
                KeyEvent.VK_DOWN, KeyEvent.VK_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_PAGE_UP -> {
                    val rect = getCellRect(selectedRow, selectedColumn, false)
                    ToolTipManager.sharedInstance().mouseMoved(MouseEvent(this@LogTable, 0, 0, 0, rect.x, rect.y, 0, false))
                }
            }

            if (p0?.keyCode == KeyEvent.VK_C && mPrevKeyEvent?.isControlDown == true && mPrevKeyEvent?.keyCode == KeyEvent.VK_C) {
                copyToClipboard()
            }

            super.keyReleased(p0)
        }

        override fun keyPressed(p0: KeyEvent?) {
            ToolTipManager.sharedInstance().mouseMoved(MouseEvent(this@LogTable, 0, 0, 0, 0, 0, 0, false))
            if (p0?.isControlDown == true) {
                if (p0.keyCode == KeyEvent.VK_B) {
                    updateBookmark(selectedRow)
                }
            }
            else {
                when (p0?.keyCode) {
                    KeyEvent.VK_PAGE_DOWN -> {
                        downPage()
                    }
                    KeyEvent.VK_PAGE_UP -> {
                        upPage()
                    }
                    KeyEvent.VK_DOWN -> {
                        downLine()
                    }
                    KeyEvent.VK_UP -> {
                        upLine()
                    }
                    KeyEvent.VK_ENTER -> {
                        showSelected(selectedRow)
                    }
                }
            }
            mPrevKeyEvent = p0

            super.keyPressed(p0)
        }
    }
}
