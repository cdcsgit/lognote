package com.blogspot.cdcsutils.lognote

import javax.swing.table.TableColumn

class LogColumnTable(tableModel:LogColumnTableModel) : LogTable(tableModel) {
    companion object {
    }

    private var mPreferredLogWidth = 0
    private val mColumnItems = tableModel.mColumnItems

    init {
        var column: TableColumn?
        val cellRenderer = LogCellRenderer()
        for (idx in 1 until columnCount) {
            column = columnModel.getColumn(idx)
            column.cellRenderer = cellRenderer
        }
    }

    override fun updateColumnWidth(width: Int, scrollVBarWidth: Int) {
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
        var column: TableColumn?
        if (columnNum.preferredWidth != column0Width || mPreferredLogWidth != preferredLogWidth) {
            columnNum.preferredWidth = column0Width
            for (idx in 1 until columnCount) {
                column = columnModel.getColumn(idx)
                column.preferredWidth = preferredLogWidth * (mColumnItems[idx]?.mWidth ?: 10) / 100
            }
            mPreferredLogWidth = preferredLogWidth
        }
    }

    override fun showSelected(targetRow: Int) {
        val log = StringBuilder("")
        var caretPos = 0
        var value:String

        if (selectedRowCount > 1) {
            for (row in selectedRows) {
                value = mTableModel.getValueAt(row, FormatManager.getInstance().mCurrFormat.mLogNth + 1).toString() + "\n"
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
                value = mTableModel.getValueAt(idx, FormatManager.getInstance().mCurrFormat.mLogNth + 1).toString() + "\n"
                log.append(value)
            }
        }

        val mainUI = MainUI.getInstance()
        val logViewDialog = LogViewDialog(mainUI, log.toString().trim(), caretPos)
        logViewDialog.setLocationRelativeTo(mainUI)
        logViewDialog.isVisible = true
    }
}
