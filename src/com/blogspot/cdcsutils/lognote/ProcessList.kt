package com.blogspot.cdcsutils.lognote

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


data class ProcessItem(val mPid: String, val mProcessName: String, val mUser: String)

class ProcessList private constructor() {
    private val mProcessMap: MutableMap<String, ProcessItem> = mutableMapOf()
    private var mUpdatedTime: Long = 0

    // pid -> process known for the current log source (live session, or opened/followed file).
    // Filled from 'ps' results, "Start proc" / am_proc_start log lines and saved "<log>.pnames" side files.
    // Unlike mProcessMap it is not cleared on every 'ps' refresh, so it can be saved next to the log file.
    private val mKnownProcesses = java.util.concurrent.ConcurrentHashMap<String, ProcessItem>()
    @Volatile private var mKnownDirty = false
    private var mLastSavedPath: String? = null
    private var mLastSavedTime: Long = 0

    companion object {
        private val mInstance: ProcessList = ProcessList()

        fun getInstance(): ProcessList {
            return mInstance
        }

        const val MAX_UPDATE_TIME_SEC = 600 // sec
        const val DEFAULT_UPDATE_TIME = 10000 // msec
        var UpdateTime = DEFAULT_UPDATE_TIME

        const val PNAMES_EXT = ".pnames"
        private const val PNAMES_SAVE_INTERVAL = 5000 // msec
        private val START_PROC_REGEX = Regex("Start proc (\\d+):([^\\s/]+)")
        private val AM_PROC_START_REGEX = Regex("am_proc_start: \\[\\d+,(\\d+),\\d+,([^,\\]]+)")
    }

    fun getProcess(pid: String): ProcessItem? {
        if (MainUI.CurrentMethod != MainUI.METHOD_ADB) {
            return mKnownProcesses[pid]
        }
        val item = mProcessMap[pid] ?: mKnownProcesses[pid]
        if (item != null) {
            return item
        }
        try {
            updateProcesses()
        } catch (ex: InterruptedException) {
            Utils.printlnLog("ProcessList.getProcess failed ")
            ex.printStackTrace()
        }

        return mProcessMap[pid]
    }

    fun getProcessName(pid: String): String? {
        if (MainUI.CurrentMethod != MainUI.METHOD_ADB) {
            return mKnownProcesses[pid]?.mProcessName
        }
        var item = mProcessMap[pid]
        if (item != null) {
            if (!mKnownProcesses.containsKey(pid)) {
                addKnown(item)  // remember every pid seen in the live log, for the .pnames side file
            }
            return item.mProcessName
        }
        item = mKnownProcesses[pid]
        if (item != null) {
            return item.mProcessName
        }

        if (updateProcesses()) {
            item = mProcessMap[pid]
            if (item != null) {
                return item.mProcessName
            }
        }

        return null
    }

    fun clear() {
        mProcessMap.clear()
    }

    fun add(processItem: ProcessItem) {
        mProcessMap[processItem.mPid] = processItem
        addKnown(processItem)
    }

    private fun addKnown(processItem: ProcessItem) {
        val prev = mKnownProcesses.put(processItem.mPid, processItem)
        if (prev == null || prev.mProcessName != processItem.mProcessName) {
            mKnownDirty = true
        }
    }

    fun hasKnownProcesses(): Boolean {
        return mKnownProcesses.isNotEmpty()
    }

    fun resetKnownProcesses() {
        mKnownProcesses.clear()
        mKnownDirty = false
        mLastSavedPath = null
        mLastSavedTime = 0
    }

    // Learn pid -> name from ActivityManager "Start proc 1234:com.foo/u0a12 ..." or events "am_proc_start: [0,1234,10012,com.foo,...]"
    fun learnFromLogLine(line: String) {
        val match: MatchResult? = when {
            line.contains("Start proc ") -> START_PROC_REGEX.find(line)
            line.contains("am_proc_start") -> AM_PROC_START_REGEX.find(line)
            else -> null
        }
        if (match != null) {
            addKnown(ProcessItem(match.groupValues[1], match.groupValues[2], ""))
        }
    }

    // Load "<logPath>.pnames" (written while the log was captured live) so an opened file can show process names
    fun loadKnownProcesses(logPath: String, isAppend: Boolean) {
        if (!isAppend) {
            resetKnownProcesses()
        }
        val file = java.io.File(logPath.trim() + PNAMES_EXT)
        if (!file.isFile) {
            Utils.printlnLog("loadKnownProcesses : no process name file - ${file.absolutePath}")
            return
        }
        try {
            file.forEachLine(Charsets.UTF_8) { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    val items = line.split("\t")
                    if (items.size >= 2 && items[0].isNotBlank() && items[1].isNotBlank()) {
                        mKnownProcesses[items[0]] = ProcessItem(items[0], items[1], items.getOrElse(2) { "" })
                    }
                }
            }
            mKnownDirty = false
            Utils.printlnLog("loadKnownProcesses : ${mKnownProcesses.size} processes from ${file.absolutePath}")
        } catch (e: Exception) {
            Utils.printlnLog("loadKnownProcesses : failed - ${e.message}")
        }
    }

    // Write "<logFile>.pnames" next to a log file being saved; throttled unless force is set
    fun saveKnownProcesses(logFile: java.io.File?, force: Boolean) {
        if (logFile == null || mKnownProcesses.isEmpty()) {
            return
        }
        val path = logFile.absolutePath
        val now = System.currentTimeMillis()
        if (!force && path == mLastSavedPath && (!mKnownDirty || now < mLastSavedTime + PNAMES_SAVE_INTERVAL)) {
            return
        }
        try {
            java.io.File(path + PNAMES_EXT).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("# LogNote process names for ${logFile.name} (pid<TAB>name<TAB>user)\n")
                for (item in mKnownProcesses.values.sortedBy { it.mPid.toIntOrNull() ?: Int.MAX_VALUE }) {
                    writer.write("${item.mPid}\t${item.mProcessName}\t${item.mUser}\n")
                }
            }
            mKnownDirty = false
            mLastSavedPath = path
            mLastSavedTime = now
        } catch (e: Exception) {
            Utils.printlnLog("saveKnownProcesses : failed - ${e.message}")
        }
    }

    private fun updateProcesses(): Boolean {
        val time = System.currentTimeMillis()
        if (time > mUpdatedTime + UpdateTime){
            LogCmdManager.getInstance().getProcesses()
            mUpdatedTime = System.currentTimeMillis()
            return true
        } else {
            return false
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
        private var mCloseBtn : JButton
        private val mCellRenderer = ProcessCellRenderer()

        init {
            val columnNames = arrayOf("Num", "PID", "UID", "NAME")
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
                data[idx][3] = entry.value.mProcessName
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

            mTable.columnSelectionAllowed = false
            mScrollPane = JScrollPane(mTable)
            mScrollPane.preferredSize = Dimension(740, 600)

            mScrollPane.verticalScrollBar.setUI(BasicScrollBarUI())
            mScrollPane.horizontalScrollBar.setUI(BasicScrollBarUI())
            mScrollPane.verticalScrollBar.unitIncrement = 20

            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false

            mCloseBtn = JButton(Strings.CLOSE)
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
