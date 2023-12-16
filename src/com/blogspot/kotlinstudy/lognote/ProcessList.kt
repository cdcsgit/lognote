package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.table.DefaultTableCellRenderer


data class ProcessItem(val mPid: String, val mCmd: String, val mUser: String)

class ProcessList private constructor() {
    private val mProcessMap: MutableMap<String, ProcessItem> = mutableMapOf()
    private var mUpdatedTime: Long = 0

    companion object {
        private val mInstance: ProcessList = ProcessList()

        fun getInstance(): ProcessList {
            return mInstance
        }

        const val MAX_UPDATE_TIME_SEC = 600 // sec
        const val DEFAULT_UPDATE_TIME = 10000 // msec
        var UpdateTime = DEFAULT_UPDATE_TIME
    }

    fun getProcess(pid: String): ProcessItem? {
        if (MainUI.CurrentMethod != MainUI.METHOD_ADB) {
            return null
        }
        val item = mProcessMap[pid]
        if (item != null) {
            return item
        }
        updateProcesses()

        return mProcessMap[pid]
    }

    fun clear() {
        mProcessMap.clear()
    }

    fun add(processItem: ProcessItem) {
        mProcessMap[processItem.mPid] = processItem
    }

    private fun updateProcesses() {
        if (MainUI.CurrentMethod != MainUI.METHOD_ADB) {
            return
        }

        val time = System.currentTimeMillis()
        if (time > mUpdatedTime + UpdateTime){
            LogCmdManager.getInstance().getProcesses()
            mUpdatedTime = System.currentTimeMillis()
            println("Process list updated")
        }
    }

    fun showList() {
        val mainUI = MainUI.getInstance()
        val listDialog = ListDialog(mainUI)
        listDialog.setLocationRelativeTo(mainUI)
        listDialog.isVisible = true
    }

    inner class ListDialog(mainUI: MainUI) : JDialog(mainUI, Strings.PROCESS_LIST, true), ActionListener {
        private val mScrollPane: JScrollPane
        private val mTable: JTable
        private var mCloseBtn : ColorButton
        private val mCellRenderer = ProcessCellRenderer()

        init {
            val columnNames = arrayOf("Num", "PID", "UID", "CMD")
            val data = Array(mProcessMap.size) {
                arrayOfNulls<Any>(
                    4
                )
            }

            var idx = 0
            for (entry in mProcessMap.entries.iterator()) {
                data[idx][0] = (idx + 1).toString()
                data[idx][1] = entry.value.mPid
                data[idx][2] = entry.value.mUser
                data[idx][3] = entry.value.mCmd
                idx++
            }

            mTable = JTable(data, columnNames)
            mTable.autoResizeMode = JTable.AUTO_RESIZE_OFF
            mTable.columnModel.getColumn(0).preferredWidth = 70
            mTable.columnModel.getColumn(0).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(1).preferredWidth = 70
            mTable.columnModel.getColumn(1).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(2).preferredWidth = 100
            mTable.columnModel.getColumn(2).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(3).preferredWidth = 500
            mTable.columnModel.getColumn(3).cellRenderer = mCellRenderer

            mTable.columnSelectionAllowed = true
            mScrollPane = JScrollPane(mTable)
            mScrollPane.preferredSize = Dimension(740, 600)

            mScrollPane.verticalScrollBar.setUI(BasicScrollBarUI())
            mScrollPane.horizontalScrollBar.setUI(BasicScrollBarUI())
            mScrollPane.verticalScrollBar.unitIncrement = 20

            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false

            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)

            val panel = JPanel()
            panel.layout = BorderLayout()
            panel.add(mScrollPane, BorderLayout.CENTER)

            val btnPanel = JPanel()
            btnPanel.add(mCloseBtn)
            panel.add(btnPanel, BorderLayout.SOUTH)

            addComponentListener(ComponentHandler())
            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mCloseBtn) {
                dispose()
            }
        }

        inner class ProcessCellRenderer : DefaultTableCellRenderer() {
            private var mPadding: Border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                border = BorderFactory.createCompoundBorder(border, mPadding)
                horizontalAlignment = if (column == 0 || column == 1) {
                    RIGHT
                } else {
                    LEFT
                }
                return this
            }
        }

        internal inner class ComponentHandler : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                if (e != null) {
                    updateColumnWidth(e.component.width, mScrollPane.verticalScrollBar.width)
                }
                super.componentResized(e)
            }
        }

        private fun updateColumnWidth(width: Int, scrollVBarWidth: Int) {
            if (mTable.rowCount <= 0) {
                return
            }

            val preferredCmdWidth = width - 240 - scrollVBarWidth - 2

            val columnCmd = mTable.columnModel.getColumn(3)
            if (columnCmd.preferredWidth != preferredCmdWidth) {
                columnCmd.preferredWidth = preferredCmdWidth
            }
        }
    }
}
