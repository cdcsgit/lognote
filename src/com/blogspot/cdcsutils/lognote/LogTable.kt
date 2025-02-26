package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.*
import javax.swing.border.AbstractBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.MatteBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader


open class LogTable(tableModel:LogTableModel) : JTable(tableModel){
    companion object {
        const val COLUMN_0_WIDTH = 80

        const val MIN_LOG_WIDTH = 720
        const val DEFAULT_LOG_WIDTH = 1920
//        const val DEFAULT_LOG_WIDTH = 3840
        var LogWidth = DEFAULT_LOG_WIDTH
        
        const val PROCESS_COLOR_RANGE = 0x40
    }

    var mTableModel = tableModel
    private val mTableColor: ColorManager.TableColor
    private val mBookmarkManager = BookmarkManager.getInstance()
    val mMultiClickInterval = try {
        Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval").toString().toInt() + 500
    } catch(ex: Exception) {
        Utils.printlnLog("failed get awt.multiClickInterval : ${ex.stackTraceToString()}")
        1000
    }
    var mIsMousePressedTableHeader = false
    var mSkipUpdateColumnWidth = false

    private var mBaseRed: Int = 0
    private var mBaseGreen: Int = 0
    private var mBaseBlue: Int = 0

    init {
        this.setShowGrid(false)
        autoResizeMode = AUTO_RESIZE_OFF
        autoscrolls = false
        dropMode = DropMode.INSERT

        val columnNum = columnModel.getColumn(LogTableModel.COLUMN_NUM)
        columnNum.cellRenderer = NumCellRenderer()

        val cellRenderer = LogCellRenderer()
        val columnProcessName = columnModel.getColumn(LogTableModel.COLUMN_PROCESS_NAME)
        columnProcessName.cellRenderer = ProcessCellRenderer()

        val columnLog = columnModel.getColumn(LogTableModel.COLUMN_LOG_START)
        columnLog.cellRenderer = cellRenderer
        intercellSpacing = Dimension(0, 0)

        val header: JTableHeader = this.getTableHeader()
        val renderer = header.defaultRenderer as DefaultTableCellRenderer
        renderer.horizontalAlignment = SwingConstants.LEFT

        this.updateProcessNameColumnWidth(false)

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none")
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK), "none")
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK), "none")

        this.addMouseListener(MouseHandler())
        this.addMouseMotionListener(MouseHandler())
        this.addKeyListener(TableKeyHandler())

        tableHeader.addMouseListener(TableHeaderMouseHandler())
        val tmpFont = tableHeader.font
        tableHeader.font = Font(tmpFont.fontName, tmpFont.style, tmpFont.size * 90 / 100)
        tableHeader.preferredSize = Dimension(tableHeader.preferredSize.width, tableHeader.font.size + 2)

        mTableColor = if (mTableModel.isFullDataModel()) {
            ColorManager.getInstance().mFullTableColor
        }
        else {
            ColorManager.getInstance().mFilterTableColor
        }

        updateProcessBgColor()

        val colorEventListener = object: ColorManager.ColorEventListener{
            override fun colorChanged(event: ColorManager.ColorEvent?) {
                updateProcessBgColor()
            }
        }

        ColorManager.getInstance().addColorEventListener(colorEventListener)
    }

    private fun updateProcessBgColor() {
        val tmpRed = mTableColor.mLogBG.red - (PROCESS_COLOR_RANGE / 2)
        mBaseRed = if (tmpRed < 0) {
            0
        } else if (tmpRed + PROCESS_COLOR_RANGE > 0xFF) {
            0xFF - PROCESS_COLOR_RANGE
        }
        else {
            tmpRed
        }
        val tmpGreen = mTableColor.mLogBG.green - (PROCESS_COLOR_RANGE / 2)
        mBaseGreen = if (tmpGreen < 0) {
            0
        } else if (tmpGreen + PROCESS_COLOR_RANGE > 0xFF) {
            0xFF - PROCESS_COLOR_RANGE
        }
        else {
            tmpGreen
        }
        val tmpBlue = mTableColor.mLogBG.blue - (PROCESS_COLOR_RANGE / 2)
        mBaseBlue = if (tmpBlue < 0) {
            0
        } else if (tmpBlue + PROCESS_COLOR_RANGE > 0xFF) {
            0xFF - PROCESS_COLOR_RANGE
        }
        else {
            tmpBlue
        }
    }

    open fun updateProcessNameColumnWidth(isShow: Boolean) {
        val columnPackageName = columnModel.getColumn(LogTableModel.COLUMN_PROCESS_NAME)

        if (isShow) {
            val columnLog = columnModel.getColumn(LogTableModel.COLUMN_LOG_START)
            columnPackageName.minWidth = columnLog.minWidth
            columnPackageName.maxWidth = columnLog.maxWidth
            columnPackageName.preferredWidth = 150
        }
        else {
            columnPackageName.minWidth = 0
            columnPackageName.preferredWidth = 0
            columnPackageName.maxWidth = 0
        }
    }

    open fun updateColumnWidth(width: Int, scrollVBarWidth: Int) {
        if (rowCount <= 0 || mIsMousePressedTableHeader || mSkipUpdateColumnWidth) {
            return
        }

        val fontMetrics = getFontMetrics(font)
        val value = mTableModel.getValueAt(rowCount - 1, 0)
        val column0Width = fontMetrics.stringWidth(value.toString()) + 20
        var newWidth = width
        if (width < LogWidth) {
            newWidth = LogWidth
        }

        val columnPackageName = columnModel.getColumn(LogTableModel.COLUMN_PROCESS_NAME)
        val preferredLogWidth = newWidth - column0Width - VStatusPanel.VIEW_RECT_WIDTH - scrollVBarWidth - 2 - columnPackageName.width

        val columnNum = columnModel.getColumn(LogTableModel.COLUMN_NUM)
        val columnLog = columnModel.getColumn(LogTableModel.COLUMN_LOG_START)
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

            val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel

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
            val thickness = 1
            if (isSelected) {
                val top = if (isCellSelected(row - thickness, col)) 0 else thickness
                val left = if (isCellSelected(row, col - thickness)) 0 else thickness
                val bottom = if (isCellSelected(row + thickness, col)) 0 else thickness
                val right = if (isCellSelected(row, col + thickness)) 0 else thickness

                if (top + left + bottom + right > 0) {
                    val lineBorder = MatteBorder(top, left, bottom, right, Color.GRAY)
                    val emptyBorder = MatteBorder(thickness - top, thickness - left, thickness - bottom, thickness - right, background)
                    label.border = CompoundBorder(lineBorder, emptyBorder)
                }
                else {
                    label.border = MatteBorder(thickness, thickness, thickness, thickness, background)
                }
            }
            else {
                label.border = MatteBorder(thickness, thickness, thickness, thickness, background)
            }

            return label
        }
    }

    internal inner class ProcessCellRenderer : DefaultTableCellRenderer() {
        private val mColorRenderer = ProcessCellColorRenderer()
        private val mLogRenderer = ProcessCellLogRenderer()

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            return if (LogTableModel.TypeShowProcessName == LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR) {
                mColorRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
            } else if  (LogTableModel.TypeShowProcessName == LogTableModel.SHOW_PROCESS_SHOW) {
                mLogRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
            } else {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
            }
        }
    }

    internal inner class ProcessCellColorRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            val label:JLabel = super.getTableCellRendererComponent(table, mTableModel.getValueAt(row, col).toString(), isSelected, hasFocus, row, col) as JLabel

            val prevPid = mTableModel.getValuePid(row - 1)
            val pid = mTableModel.getValuePid(row)

            val pidInt = try {
                pid.toInt()
            } catch (ex: NumberFormatException) {
                0
            }

            foreground = mTableModel.getFgColor(row)

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
                background = Color(pidInt % PROCESS_COLOR_RANGE + mBaseRed, (pidInt + (pidInt / 2)) % PROCESS_COLOR_RANGE + mBaseGreen, (pidInt  + (pidInt / 3)) % PROCESS_COLOR_RANGE + mBaseBlue)
            }

            val thickness = 1
            val thicknessLeft = 5
            if (isSelected) {
                val top = if (isCellSelected(row - thickness, col)) 0 else thickness
                val left = if (isCellSelected(row, col - thickness)) 0 else thickness
                val bottom = if (isCellSelected(row + thickness, col)) 0 else thickness
                val right = if (isCellSelected(row, col + thickness)) 0 else thickness

                label.border = BorderFactory.createEmptyBorder(thickness - top, thicknessLeft - left, thickness - bottom, thickness - right)
                if (top + left + bottom + right > 0) {
                    label.border = CompoundBorder(MatteBorder(top, left, bottom, right, Color.GRAY), label.border)
                }
                if (prevPid != pid && top == 0) {
                    label.border = CompoundBorder(MatteBorder(thickness, 0, 0, 0, Color.GRAY), label.border)
                }
            }
            else {
                if (prevPid != pid) {
                    label.border = BorderFactory.createEmptyBorder(0, thicknessLeft, thickness, thickness)
                    label.border = CompoundBorder(MatteBorder(thickness, 0, 0, 0, Color.GRAY), label.border)
                }
                else {
                    label.border = BorderFactory.createEmptyBorder(thickness, thicknessLeft, thickness, thickness)
                }
            }

            return label
        }
    }

    internal inner class ProcessCellLogRenderer : LogCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
            val prevPid = mTableModel.getValuePid(row - 1)
            val pid = mTableModel.getValuePid(row)
            if (prevPid != pid) {
                label.border = CompoundBorder(MatteBorder(1, 0, 0, 0, Color.GRAY), label.border)
            }
            return label
        }
    }

    internal open inner class LogCellRenderer : DefaultTableCellRenderer() {
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

            val thickness = 1
            val thicknessLeft = 5
            if (isSelected) {
                val top = if (isCellSelected(row - thickness, col)) 0 else thickness
                val left = if (isCellSelected(row, col - thickness)) 0 else thickness
                val bottom = if (isCellSelected(row + thickness, col)) 0 else thickness
                val right = if (isCellSelected(row, col + thickness)) 0 else thickness

                label.border = BorderFactory.createEmptyBorder(thickness - top, thicknessLeft - left, thickness - bottom, thickness - right)
                if (top + left + bottom + right > 0) {
                    label.border = CompoundBorder(MatteBorder(top, left, bottom, right, Color.GRAY), label.border)
                }
            }
            else {
                label.border = BorderFactory.createEmptyBorder(thickness, thicknessLeft, thickness, thickness)
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
                value = mTableModel.getValueAt(row, LogTableModel.COLUMN_LOG_START).toString() + "\n"
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
                value = mTableModel.getValueAt(idx, LogTableModel.COLUMN_LOG_START).toString() + "\n"
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
            if (ProcessList.UpdateTime > 0 && MainUI.CurrentMethod == MainUI.METHOD_ADB && column >= 1) { // column >= 1) { // column >= 1, not line number
                val row: Int = rowAtPoint(point)
                val pid = mTableModel.getValuePid(row)
                if (pid.isNotEmpty()) {
                    val processItem = ProcessList.getInstance().getProcess(pid)
                    if (processItem != null) {
                        mProcessItem.text = "${processItem.mPid} : ${processItem.mProcessName} (${processItem.mUser})"
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
        if (LogTableModel.TypeShowProcessName != LogTableModel.SHOW_PROCESS_NONE) {
            return ""
        }

        var processInfo = ""
        val column: Int = columnAtPoint(point)
        if (MainUI.CurrentMethod == MainUI.METHOD_ADB && column >= 1) { // column >= 1, not line number
            val row: Int = rowAtPoint(point)
            val pid = mTableModel.getValuePid(row)
            if (pid.isNotEmpty()) {
                val processItem = ProcessList.getInstance().getProcess(pid)
                if (processItem != null) {
                    processInfo = "${processItem.mPid} : ${processItem.mProcessName} (${processItem.mUser})"
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
            LogTableModel.WaitTimeForDoubleClick  = System.currentTimeMillis() + mMultiClickInterval
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

            repaint()
            super.mouseClicked(p0)
        }
    }

    internal inner class TableHeaderMouseHandler : MouseAdapter() {
        override fun mousePressed(p0: MouseEvent?) {
            mIsMousePressedTableHeader = true
            super.mousePressed(p0)
        }

        override fun mouseReleased(p0: MouseEvent?) {
            mIsMousePressedTableHeader = false

            super.mouseReleased(p0)
        }
    }

    private fun copyToClipboard() {
        val numCols = columnCount
        val numRows = selectedRows.size
        val copyStr = StringBuilder()

        if (numRows >= 1 && numCols > LogTableModel.COLUMN_LOG_START) {
            for (row in 0 until numRows) {
                for (col in LogTableModel.COLUMN_LOG_START until numCols) {
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

    private fun copyToClipboardEx() {
        val numCols = columnCount
        val numRows = selectedRows.size
        val copyStr = StringBuilder()

        if (numRows >= 1 && numCols > LogTableModel.COLUMN_PROCESS_NAME) {
            for (row in 0 until numRows) {
                for (col in LogTableModel.COLUMN_PROCESS_NAME until numCols) {
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

//            if (p0?.keyCode == KeyEvent.VK_C && mPrevKeyEvent?.isControlDown == true && mPrevKeyEvent?.isShiftDown == true && mPrevKeyEvent?.keyCode == KeyEvent.VK_C) {
//                copyToClipboardEx()
//            }
//            else if (p0?.keyCode == KeyEvent.VK_C && mPrevKeyEvent?.isControlDown == true && mPrevKeyEvent?.keyCode == KeyEvent.VK_C) {
//                copyToClipboard()
//            }

            mPrevKeyEvent = null

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
