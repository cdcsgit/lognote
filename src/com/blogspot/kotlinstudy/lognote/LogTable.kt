package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.StringBuilder
import javax.swing.*
import javax.swing.table.*
import javax.swing.SwingUtilities
import javax.swing.JFrame

class LogTable(tableModel:LogTableModel) : JTable(tableModel){
    var mTableModel = tableModel
    private val mBookmarkManager = BookmarkManager.getInstance()

    companion object {
        val VIEW_LINE_ONE = 0
        val VIEW_LINE_WRAP = 1

        val COLUMN_0_WIDTH = 80
    }

    init {
        setShowGrid(false)
        tableHeader = null
        autoResizeMode = AUTO_RESIZE_OFF
        autoscrolls = false
        dragEnabled = true
        dropMode = DropMode.INSERT


        val columnNum = columnModel.getColumn(0)
//        columnNum.preferredWidth = COLUMN_0_WIDTH
        columnNum.cellRenderer = NumCellRenderer()

        val columnLog = columnModel.getColumn(1)
//        columnLog.preferredWidth = gd.displayMode.width - COLUMN_0_WIDTH - 25
        columnLog.cellRenderer = LogCellRenderer()
        setRowMargin(0)

        val enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "none")

        addMouseListener(MouseHandler())
        addKeyListener(TableKeyHandler())
    }

    fun updateColumnWidth(width: Int) {
        if (rowCount <= 0) {
            return
        }

        val fontMetrics = getFontMetrics(font)
        val value = mTableModel.getValueAt(rowCount - 1, 0)
        val column0Width = fontMetrics.stringWidth(value.toString()) + 20
        val preferredLogWidth = width - column0Width - 25

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
//            if (value == true) {
                columnLog.cellRenderer = LogCellRenderer()
//            }
//            else {
//                if (mViewMode == VIEW_LINE_ONE) {
//                    columnLog.cellRenderer = LogCellRenderer()
//                }
//                else if (mViewMode == VIEW_LINE_WRAP) {
//                    columnLog.cellRenderer = LogWrapCellRenderer()
//                }
//            }
        }

    var mViewMode = VIEW_LINE_ONE
        set(value) {
            field = value
            val columnLog = columnModel.getColumn(1)
//            if (value == VIEW_LINE_ONE) {
                columnLog.cellRenderer = LogCellRenderer()
//            }
//            else if (value == VIEW_LINE_WRAP) {
//                columnLog.cellRenderer = LogWrapCellRenderer()
//            }
        }

    internal inner class NumCellRenderer() : DefaultTableCellRenderer() {
        init {
            horizontalAlignment = JLabel.RIGHT
            verticalAlignment = JLabel.TOP
        }
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            background = Color(0xD0, 0xD0, 0xD0)
            if (row == table?.selectedRow) {
                background = Color(0xC0, 0xC0, 0xC0)
            }

            var num = -1
            if (value != null) {
                num = value.toString().trim().toInt()
            }
            if (mBookmarkManager.mBookmarks.contains(num)) {
                background = mBookmarkManager.mBackground
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col)
        }
    }

    internal inner class LogCellRenderer() : DefaultTableCellRenderer() {
        init {

        }
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            col: Int
        ): Component {
            val newValue:String
            if (value != null) {
                newValue = mTableModel.getPrintValue(value.toString(), row)
            }
            else {
                newValue = ""
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
                background = mBookmarkManager.mBackground
                val bookmarkColor = mBookmarkManager.mBackground

                if (isSelected || row == table?.selectedRow) {
                    background = Color((0xC0 + bookmarkColor.red) and 0xFF, (0xC0 + bookmarkColor.green) and 0xFF, (0xC0 + bookmarkColor.blue) and 0xFF)
                }
            }
            else if (isSelected || row == table?.selectedRow) {
                background = Color(0xC0, 0xC0, 0xC0)
            } else if (mTableModel.isFullDataModel()) {
                background = Color(0xFA, 0xFA, 0xFF)
            } else {
                background = Color(0xFF, 0xFA, 0xFA)
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

    private fun showSelected() {
        val log = StringBuilder("")
        var value:String
        var startIdx = selectedRow - 2
        if (startIdx < 0) {
            startIdx = 0
        }
        var endIdx = selectedRow + 3
        if (endIdx > rowCount) {
            endIdx = rowCount
        }

        for (idx in startIdx until endIdx) {
            value = mTableModel.getValueAt(idx, 1).toString() + "\n"
            log.append(value)
        }

        val frame = SwingUtilities.windowForComponent(this@LogTable) as JFrame
        val logViewDialog = LogViewDialog(frame, log.toString().trim())
        logViewDialog.setLocationRelativeTo(frame)
        logViewDialog.isVisible = true
    }

    private fun updateBookmark() {
        val value = mTableModel.getValueAt(selectedRow, 0)
        val bookmark = value.toString().trim().toInt()
        mBookmarkManager.updateBookmark(bookmark)
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mousePressed(p0: MouseEvent?) {
            super.mousePressed(p0)
        }

        override fun mouseReleased(p0: MouseEvent?) {
            super.mouseReleased(p0)
        }

        override fun mouseClicked(p0: MouseEvent?) {
            if (p0?.clickCount == 2) {
                if (columnAtPoint(p0.point) == 0) {
                    updateBookmark()
                } else {
                    showSelected()
                }
            }
            super.mouseClicked(p0)
        }
    }
    internal inner class TableKeyHandler : KeyAdapter() {
//        override fun keyReleased(p0: KeyEvent?) {
//            if (KeyEvent.VK_ENTER == p0?.keyCode) {
//            }
//            super.keyReleased(p0)
//        }

        override fun keyPressed(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_B && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                updateBookmark()
            } else if (p0?.keyCode == KeyEvent.VK_PAGE_DOWN) {
                downPage()
            } else if (p0?.keyCode == KeyEvent.VK_PAGE_UP) {
                upPage()
            } else if (p0?.keyCode == KeyEvent.VK_DOWN) {
                downLine()
            } else if (p0?.keyCode == KeyEvent.VK_UP) {
                upLine()
            } else if (p0?.keyCode == KeyEvent.VK_ENTER) {
                showSelected()
            }
            super.keyPressed(p0)
        }
    }
}