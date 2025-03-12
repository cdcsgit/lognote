package com.blogspot.cdcsutils.lognote

import javax.swing.table.TableColumn

class LogColumnTable(tableModel:LogColumnTableModel) : LogTable(tableModel) {
    companion object {
    }

    private var mPreferredLogWidth = 0
    private val mColumnItems = tableModel.mColumnItems
    private var mIsNeedUpdateColumnWidth = true

    init {
        var column: TableColumn?
        val cellRenderer = LogCellRenderer()
        for (idx in LogTableModel.COLUMN_LOG_START until columnCount) {
            column = columnModel.getColumn(idx)
            column.cellRenderer = cellRenderer
        }
    }

    override fun updateProcessNameColumnWidth(isShow: Boolean) {
        super.updateProcessNameColumnWidth(isShow)
        mIsNeedUpdateColumnWidth = true
    }

    override fun updateColumnWidth(width: Int, scrollVBarWidth: Int) {
        if (mIsMousePressedTableHeader) {
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
        var column: TableColumn?
        if (columnNum.preferredWidth != column0Width || mPreferredLogWidth != preferredLogWidth) {
            columnNum.preferredWidth = column0Width
            if (mIsNeedUpdateColumnWidth) {
                var remainWidth = preferredLogWidth
                var remainWidthIdx = -1
                for (idx in LogTableModel.COLUMN_LOG_START until columnCount) {
                    if (mColumnItems[idx]!!.mWidth == -1) {
                        remainWidthIdx = idx
                    }
                    else {
                        column = columnModel.getColumn(idx)
                        column.preferredWidth = mColumnItems[idx]!!.mWidth
                        remainWidth -= mColumnItems[idx]!!.mWidth
                    }
                }
                if (remainWidthIdx >= 0 && remainWidth > 0) {
                    column = columnModel.getColumn(remainWidthIdx)
                    column.preferredWidth = remainWidth
                }
                mIsNeedUpdateColumnWidth = false
            }
            mPreferredLogWidth = preferredLogWidth
        }
    }

    override fun getLogText(row:Int): String {
        return mTableModel.getValueAt(row, FormatManager.getInstance().mCurrFormat.mLogPosition + LogTableModel.COLUMN_LOG_START).toString()
    }
}
