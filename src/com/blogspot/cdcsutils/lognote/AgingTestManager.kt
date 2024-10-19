package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.event.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor


class AgingTestManager private constructor(fileName: String) : PropertiesBase(fileName) {
    companion object {
        val AGING_TESTS_LIST_FILE = "lognote_agingtests.xml"
        val ITEM_VERSION = "AGING_TEST_VERSION"

        const val ITEM_TRIGGER_NAME = "_TRIGGER_NAME"
        const val ITEM_TRIGGER_FILTER = "_TRIGGER_FILTER"
        const val ITEM_TRIGGER_ACTION = "_TRIGGER_ACTION"
        const val ITEM_TRIGGER_ACTION_PARAMETER = "_TRIGGER_ACTION_PARAMETER"
        const val ITEM_TRIGGER_ONCE = "_TRIGGER_ONCE"

        const val MAX_TRIGGER_COUNT = 30

        const val MAX_RESULT_LOG = 30

        private val mInstance: AgingTestManager = AgingTestManager(AGING_TESTS_LIST_FILE)
        fun getInstance(): AgingTestManager {
            return mInstance
        }

        enum class TriggerStatus(val value: Int) {
            STOPPED(0),
            STARTED(1);

            companion object {
                fun fromInt(value: Int) = entries.first { it.value == value }
            }
        }

        const val TRIGGER_NEW = 1
        const val TRIGGER_COPY = 2
        const val TRIGGER_EDIT = 3

        enum class TriggerAction(val value: Int) {
            SHOW_DIALOG(0),
            RUN_CMD(1),
            RUN_CMD_SHOW_DIALOG(2);

            companion object {
                fun fromInt(value: Int) = entries.first { it.value == value }
            }
        }

        val ACTION_MAP = mapOf(
            TriggerAction.SHOW_DIALOG to Strings.SHOW_DIALOG,
            TriggerAction.RUN_CMD to Strings.RUN_CMD,
            TriggerAction.RUN_CMD_SHOW_DIALOG to "${Strings.RUN_CMD}/${Strings.SHOW_DIALOG}",
        )
    }

    private val mTriggerList = mutableListOf<TriggerItem>()
    private var mTriggerPanelImpl: TriggerPanel? = null
    val mTriggerPanel: TriggerPanel
        get() {
            if (mTriggerPanelImpl == null) {
                mTriggerPanelImpl = TriggerPanel()
            }
            return mTriggerPanelImpl!!
        }

    init {
        manageVersion()
        loadList()
    }

    private fun loadList() {
        loadXml()
        mTriggerList.clear()
        for (i in 0 until MAX_TRIGGER_COUNT) {
            val name = (mProperties["$i${ITEM_TRIGGER_NAME}"] ?: "") as String
            if (name.trim().isEmpty()) {
                break
            }
            val filter = (mProperties["$i${ITEM_TRIGGER_FILTER}"] ?: "") as String
            val action = try {
                ((mProperties["$i${ITEM_TRIGGER_ACTION}"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                0
            }
            val actionParameter = (mProperties["$i${ITEM_TRIGGER_ACTION_PARAMETER}"] ?: "") as String

            val once = try {
                ((mProperties["$i${ITEM_TRIGGER_ONCE}"] ?: "true") as String).toBoolean()
            } catch (ex: Exception) {
                true
            }

            mTriggerList.add(TriggerItem(name, filter, TriggerAction.fromInt(action), actionParameter, once))
        }
    }

    private fun saveList() {
        mProperties.clear()
        var trigger: TriggerItem
        for (i in 0 until mTriggerList.size) {
            trigger = mTriggerList[i]
            mProperties["$i${ITEM_TRIGGER_NAME}"] = trigger.mName
            mProperties["$i${ITEM_TRIGGER_FILTER}"] = trigger.mFilter
            mProperties["$i${ITEM_TRIGGER_ACTION}"] = trigger.mAction.value.toString()
            mProperties["$i${ITEM_TRIGGER_ACTION_PARAMETER}"] = trigger.mActionParam
            mProperties["$i${ITEM_TRIGGER_ONCE}"] = trigger.mOnce.toString()
        }

        saveXml()
    }
    
    override fun manageVersion() {

    }

    fun pullTheTrigger(log: String) {
        for (item in mTriggerList) {
            if (item.mFilterPattern.matcher(log).find()) {
                if (!item.mOnce) {
                    item.mRunCount++
                }
                item.addLog(log)
                runAction(item)
                if (item.mOnce) {
                    item.mStatus = TriggerStatus.STOPPED
                    mTriggerPanel.updateInUseTrigger()
                    mTriggerPanel.repaint()
                    MainUI.getInstance().mFilteredLogPanel.mTableModel.mFilterTriggerLog = makeFilterTrigger()
                }
                break
            }
        }
    }

    private fun runAction(item: TriggerItem) {
        Thread(ActionRunner(item)).start()
    }

    private fun makeFilterTrigger(): String {
        var filterTrigger = ""
        for (item in mTriggerList) {
            if (item.mStatus == TriggerStatus.STARTED) {
                filterTrigger += if (filterTrigger.isEmpty() || filterTrigger.substring(filterTrigger.length - 1) == "|") {
                    item.mFilter
                } else {
                    "|" + item.mFilter
                }
            }
        }

        return filterTrigger
    }

    private fun runCmd(item: TriggerItem) {
        var cmd = item.mActionParam
        cmd = Utils.replaceCmd(cmd)

        val runtime = Runtime.getRuntime()
        val proc = runtime.exec(cmd)

        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
        val stdError = BufferedReader(InputStreamReader(proc.errorStream))

        var s: String? = null
        while (stdInput.readLine().also { s = it } != null) {
            Utils.printlnLog("${Strings.CMD_RESULT} : $s")
        }

        while (stdError.readLine().also { s = it } != null) {
            Utils.printlnLog("${Strings.CMD_RESULT_ERROR} : $s")
        }
    }

    inner class ActionRunner(val item: TriggerItem): Runnable {
        override fun run() {
            when (item.mAction) {
                TriggerAction.SHOW_DIALOG -> {
                    if (item.mResultDialog.isVisible) {
                        item.mResultDialog.updateData(item)
                    }
                    else {
                        item.mResultDialog.setLocationRelativeTo(MainUI.getInstance())
                        item.mResultDialog.isVisible = true
                    }
                }
                TriggerAction.RUN_CMD -> {
                    runCmd(item)
                }
                TriggerAction.RUN_CMD_SHOW_DIALOG -> {
                    runCmd(item)
                    if (item.mResultDialog.isVisible) {
                        item.mResultDialog.updateData(item)
                    }
                    else {
                        item.mResultDialog.setLocationRelativeTo(MainUI.getInstance())
                        item.mResultDialog.isVisible = true
                    }
                }
            }
        }
    }

    fun startTrigger(selectedRow: Int) {
        if (mTriggerList[selectedRow].mStatus != TriggerStatus.STARTED) {
            mTriggerList[selectedRow].clearLogs()
            mTriggerList[selectedRow].mStatus = TriggerStatus.STARTED
            mTriggerPanel.updateInUseTrigger()
            mTriggerPanel.repaint()
            MainUI.getInstance().mFilteredLogPanel.mTableModel.mFilterTriggerLog = makeFilterTrigger()
        }
    }

    fun stopTrigger(selectedRow: Int) {
        if (mTriggerList[selectedRow].mStatus != TriggerStatus.STOPPED) {
            mTriggerList[selectedRow].mStatus = TriggerStatus.STOPPED
            mTriggerPanel.updateInUseTrigger()
            mTriggerPanel.repaint()
            MainUI.getInstance().mFilteredLogPanel.mTableModel.mFilterTriggerLog = makeFilterTrigger()
        }
    }

    fun stopTrigger(name: String) {
        for (trigger in mTriggerList) {
            if (trigger.mName == name) {
                if (trigger.mStatus != TriggerStatus.STOPPED) {
                    trigger.mStatus = TriggerStatus.STOPPED
                    mTriggerPanel.updateInUseTrigger()
                    mTriggerPanel.repaint()
                    MainUI.getInstance().mFilteredLogPanel.mTableModel.mFilterTriggerLog = makeFilterTrigger()
                }
                break
            }
        }
    }

    inner class TriggerItem(val mName: String, val mFilter: String, val mAction: TriggerAction, val mActionParam: String, val mOnce: Boolean) {
        var mStatus: TriggerStatus = TriggerStatus.STOPPED
        var mRunCount: Int = 0
        val mFilterPattern: Pattern = Pattern.compile(mFilter, Pattern.CASE_INSENSITIVE)
        private var mResultDialogImpl: ResultDialog? = null
        val mResultDialog: ResultDialog
            get() {
                if (mResultDialogImpl == null) {
                    mResultDialogImpl = ResultDialog(MainUI.getInstance(), this)
                }
                return mResultDialogImpl!!
            }

        private val mLogList = mutableListOf<String>()

        fun addLog(log: String) {
            if (mLogList.size > MAX_RESULT_LOG) {
                mLogList.removeAt(0)
            }
            mLogList.add(log)
        }

        fun getLogs(): String {
            val resultLog = StringBuilder("")
            for (result in mLogList) {
                resultLog.append("$result\n")
            }
            return resultLog.toString()
        }

        fun clearLogs() {
            mLogList.clear()
        }
    }

    inner class TriggerPanel: JPanel(), ActionListener {
        val mTriggerTableModel = TriggerTableModel()
        private val mTriggerTable = JTable(mTriggerTableModel)
        private val mScrollPane = JScrollPane(mTriggerTable)
        private val mMouseHandler = MouseHandler()
        private val mInUseLabel: JLabel
        private var mInUseTrigger = ""

        private var mAddBtn: JButton
        private var mHideBtn: JButton
        private var mHideListBtn: JButton


        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            addMouseListener(mMouseHandler)

            val inUsePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            mInUseLabel = JLabel("${Strings.AGING_TEST_TRIGGER} / ${Strings.IN_USE} - [$mInUseTrigger] ")
            inUsePanel.add(mInUseLabel)

            mAddBtn = JButton(Strings.ADD)
            mAddBtn.margin = Insets(0, 3, 0, 3)
            if (mInUseTrigger.isNotEmpty()) {
                mAddBtn.isEnabled = false
            }
            mAddBtn.addActionListener(this)
            
            mHideBtn = JButton(Strings.HIDE)
            mHideBtn.margin = Insets(0, 3, 0, 3)
            if (mInUseTrigger.isNotEmpty()) {
                mHideBtn.isEnabled = false
            }
            mHideBtn.addActionListener(this)
            mHideListBtn = JButton(Strings.HIDE_LIST)
            mHideListBtn.margin = Insets(0, 3, 0, 3)
            mHideListBtn.addActionListener(this)
            inUsePanel.add(mAddBtn)
            inUsePanel.add(mHideListBtn)
            inUsePanel.add(mHideBtn)

            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false
            mScrollPane.addMouseListener(mMouseHandler)
            mScrollPane.preferredSize = Dimension(mScrollPane.preferredSize.width, 120)

            mTriggerTable.rowSelectionAllowed = false
            mTriggerTable.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            mTriggerTable.tableHeader.reorderingAllowed = false
            mTriggerTable.columnModel.getColumn(0).preferredWidth = 40
            mTriggerTable.columnModel.getColumn(1).preferredWidth = 400
            mTriggerTable.columnModel.getColumn(2).preferredWidth = 60
            mTriggerTable.columnModel.getColumn(3).preferredWidth = 450
            mTriggerTable.columnModel.getColumn(4).preferredWidth = 15
            mTriggerTable.columnModel.getColumn(5).preferredWidth = 25
            mTriggerTable.addMouseListener(TableMouseHandler())
            mTriggerTable.setDefaultRenderer(String::class.java, TriggerTextRenderer())

            val triggerCtrlRenderer = TriggerCtrlRenderer()
            mTriggerTable.columnModel.getColumn(5).cellRenderer = TriggerCtrlRenderer()
            mTriggerTable.columnModel.getColumn(5).cellEditor = TriggerCtrlEditor()
            mTriggerTable.rowHeight = triggerCtrlRenderer.getTableCellRendererComponent(mTriggerTable, "",
                isSelected = true,
                hasFocus = true,
                row = 0,
                column = 0
            ).preferredSize.height

            add(inUsePanel)
            add(mScrollPane)
        }

        fun updateInUseTrigger() {
            mTriggerPanel.mInUseTrigger = ""
            for (trigger in mTriggerList) {
                if (trigger.mStatus == TriggerStatus.STARTED) {
                    if (mTriggerPanel.mInUseTrigger.isEmpty()) {
                        mTriggerPanel.mInUseTrigger = trigger.mName
                    }
                    else {
                        mTriggerPanel.mInUseTrigger += ", ${trigger.mName}"
                    }
                }
            }
            mInUseLabel.text = " ${Strings.AGING_TEST_TRIGGER} / ${Strings.IN_USE} - [$mInUseTrigger] "
            mHideBtn.isEnabled = mInUseTrigger.isEmpty()
        }

        fun canHide(): Boolean {
            return mInUseTrigger.isEmpty()
        }

        override fun actionPerformed(e: ActionEvent?) {
            when (e?.source) {
                mAddBtn -> {
                    showAddDialog()
                }
                mHideBtn -> {
                    isVisible = false
                }
                mHideListBtn -> {
                    mScrollPane.isVisible = !mScrollPane.isVisible
                    if (mScrollPane.isVisible) {
                        mHideListBtn.text = Strings.HIDE_LIST
                    }
                    else {
                        mHideListBtn.text = Strings.SHOW_LIST
                    }
                }
            }
        }

        inner class TriggerTableModel() : DefaultTableModel() {
            private val colNames = arrayOf(Strings.NAME, Strings.FILTER, Strings.ACTION, Strings.ACTION_PARAMETER, "${Strings.ONCE}/${Strings.REPEAT}", Strings.SETTING)
            init {
                setColumnIdentifiers(colNames)
            }

            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }

            override fun getRowCount(): Int {
                return mTriggerList.size
            }

            override fun isCellEditable(row: Int, column: Int): Boolean {
                return column == 5
            }

            override fun getValueAt(row: Int, column: Int): Any {
                val trigger = mTriggerList[row]
                when (column) {
                    0 -> {
                        return trigger.mName
                    }
                    1 -> {
                        return trigger.mFilter
                    }
                    2 -> {
                        return ACTION_MAP[trigger.mAction].toString()
                    }
                    3 -> {
                        return trigger.mActionParam
                    }
                    4 -> {
                        return if (trigger.mOnce) Strings.ONCE else Strings.REPEAT
                    }
                    5 -> {
                        return if (trigger.mStatus == TriggerStatus.STOPPED) Strings.STOPPED else Strings.STARTED
                    }
                }
                return super.getValueAt(row, column)
            }

            override fun setValueAt(aValue: Any?, row: Int, column: Int) {
                // do nothing
            }
        }

        inner class TriggerTextRenderer : DefaultTableCellRenderer() {
            init {
                horizontalAlignment = JLabel.CENTER
            }

            override fun getTableCellRendererComponent(
                table: JTable,
                value: Any,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            }
        }

        inner class TriggerCtrlPane(row: Int) : JPanel(), ActionListener {
            private val mBtns: Array<JButton>
            private var mBtnNum = 0
            private val mIdxStartStop = mBtnNum++
            private val mIdxResult = mBtnNum++
            private val mIdxEdit = mBtnNum++
            private val mIdxCopy = mBtnNum++
            private val mIdxRemove = mBtnNum++
            private val mIdxUp = mBtnNum++
            private val mIdxDown = mBtnNum++
            private var mRow = row
            private var mStatus: TriggerStatus = TriggerStatus.STOPPED

            override fun getToolTipText(event: MouseEvent?): String {
                toolTipText = ""
                for (btn in mBtns) {
                    if (event != null && event.x >= btn.x && event.x < (btn.x + btn.width)) {
                        toolTipText = btn.toolTipText
                        break
                    }
                }
                return super.getToolTipText(event)
            }

            init {
                layout = FlowLayout(FlowLayout.LEFT, 0, 0)
                mBtns = Array(mBtnNum) { JButton("") }
                mBtns[mIdxStartStop].actionCommand = "START_STOP"
                mBtns[mIdxStartStop].icon = ImageIcon(this.javaClass.getResource("/images/trigger_start.png"))
                mBtns[mIdxStartStop].toolTipText = "${Strings.START} / ${Strings.STOP}"
                mBtns[mIdxResult].actionCommand = "RESULT"
                mBtns[mIdxResult].icon = ImageIcon(this.javaClass.getResource("/images/trigger_result.png"))
                mBtns[mIdxResult].toolTipText = "${Strings.TRIGGER} ${Strings.RESULT}"

                mBtns[mIdxEdit].actionCommand = "EDIT"
                mBtns[mIdxEdit].icon = ImageIcon(this.javaClass.getResource("/images/trigger_edit.png"))
                mBtns[mIdxEdit].toolTipText = Strings.EDIT
                mBtns[mIdxCopy].actionCommand = "COPY"
                mBtns[mIdxCopy].icon = ImageIcon(this.javaClass.getResource("/images/trigger_copy.png"))
                mBtns[mIdxCopy].toolTipText = Strings.COPY
                mBtns[mIdxRemove].actionCommand = "REMOVE"
                mBtns[mIdxRemove].icon = ImageIcon(this.javaClass.getResource("/images/trigger_remove.png"))
                mBtns[mIdxRemove].toolTipText = Strings.DELETE

                mBtns[mIdxUp].actionCommand = "MOVE_UP"
                mBtns[mIdxUp].icon = ImageIcon(this.javaClass.getResource("/images/trigger_up.png"))
                mBtns[mIdxUp].toolTipText = Strings.MOVE_UP
                mBtns[mIdxDown].actionCommand = "MOVE_DOWN"
                mBtns[mIdxDown].icon = ImageIcon(this.javaClass.getResource("/images/trigger_down.png"))
                mBtns[mIdxDown].toolTipText = Strings.MOVE_DOWN

                // flatLaf workaround : Make the cell editor panel hierarchy three or more levels
                val p2 = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                for (btn in mBtns) {
                    btn.margin = Insets(2, 2, 2, 2)
                    p2.add(btn)
                    btn.addActionListener(this@TriggerCtrlPane)
                }
                val p1 = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                p1.add(p2)
                add(p1)
            }

            fun addActionListener(listener: ActionListener?) {
                for (btn in mBtns) {
                    btn.addActionListener(listener)
                }
            }

            fun updatePane(row: Int) {
                background = mTriggerTable.background
                mBtns[mIdxStartStop].background = mTriggerTable.background
                mBtns[mIdxResult].background = mTriggerTable.background
                mBtns[mIdxEdit].background = mTriggerTable.background
                mBtns[mIdxCopy].background = mTriggerTable.background
                mBtns[mIdxRemove].background = mTriggerTable.background
                mBtns[mIdxUp].background = mTriggerTable.background
                mBtns[mIdxDown].background = mTriggerTable.background
                
                mRow = row
                if (mTriggerList.isNotEmpty() && mTriggerList[mRow].mStatus != mStatus) {
                    if (mTriggerList[row].mStatus == TriggerStatus.STOPPED) {
                        mBtns[mIdxStartStop].icon = ImageIcon(this.javaClass.getResource("/images/trigger_start.png"))
                        mBtns[mIdxEdit].isEnabled = true
                        mBtns[mIdxRemove].isEnabled = true
                    }
                    else {
                        mBtns[mIdxStartStop].icon = ImageIcon(this.javaClass.getResource("/images/trigger_stop.png"))
                        mBtns[mIdxEdit].isEnabled = false
                        mBtns[mIdxRemove].isEnabled = false
                    }
                    mStatus = mTriggerList[mRow].mStatus
                }
            }

            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.actionCommand) {
                    "START_STOP" -> {
                        if (mTriggerList[mRow].mStatus == TriggerStatus.STOPPED) {
                            startTrigger(mRow)
                        } else {
                            stopTrigger(mRow)
                        }
                    }
                    "RESULT" -> {
                        mTriggerList[mRow].mResultDialog.setLocationRelativeTo(MainUI.getInstance())
                        mTriggerList[mRow].mResultDialog.isVisible = true
                    }
                    "EDIT" -> {
                        if (mTriggerList[mRow].mStatus == TriggerStatus.STOPPED) {
                            val editDialog =
                                EditDialog(MainUI.getInstance(), TRIGGER_EDIT, mTriggerList[mRow])
                            editDialog.setLocationRelativeTo(MainUI.getInstance())
                            editDialog.isVisible = true
                        }
                        else {
                            JOptionPane.showMessageDialog(MainUI.getInstance(), String.format(Strings.TRIGGER_CANNOT_EDIT, mTriggerList[mRow].mName), Strings.ERROR, JOptionPane.ERROR_MESSAGE)
                        }
                    }
                    "COPY" -> {
                        if (mTriggerList.size >= MAX_TRIGGER_COUNT) {
                            JOptionPane.showMessageDialog(MainUI.getInstance(), "${Strings.TRIGGER_CANNOT_ADD} : $MAX_TRIGGER_COUNT", Strings.ERROR, JOptionPane.ERROR_MESSAGE)
                            return
                        }

                        val editDialog = EditDialog(MainUI.getInstance(), TRIGGER_COPY, mTriggerList[mRow])
                        editDialog.setLocationRelativeTo(MainUI.getInstance())
                        editDialog.isVisible = true
                    }
                    "REMOVE" -> {
                        if (mTriggerList[mRow].mStatus == TriggerStatus.STOPPED) {
                            val ret = JOptionPane.showConfirmDialog(
                                MainUI.getInstance(),
                                String.format(Strings.CONFIRM_DELETE_TRIGGER, mTriggerList[mRow].mName),
                                Strings.DELETE,
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE
                            )
                            if (ret == JOptionPane.OK_OPTION) {
                                mTriggerList.removeAt(mRow)
                                saveList()
                                mTriggerTableModel.fireTableDataChanged()
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(MainUI.getInstance(), String.format(Strings.TRIGGER_CANNOT_DELETE, mTriggerList[mRow].mName), Strings.ERROR, JOptionPane.ERROR_MESSAGE)
                        }

                    }
                    "MOVE_UP" -> {
                        if (mTriggerList.size > 1 && mTriggerList.size > mRow && mRow > 0) {
                            val trigger = mTriggerList[mRow]
                            mTriggerList.removeAt(mRow)
                            mTriggerList.add(mRow - 1, trigger)
                            saveList()
                            mTriggerTableModel.fireTableDataChanged()
                            mTriggerTable.setRowSelectionInterval(mRow - 1, mRow - 1)
                        }
                    }
                    "MOVE_DOWN" -> {
                        if (mTriggerList.size > 1 && mTriggerList.size > mRow && mRow < (mTriggerList.size - 1)) {
                            val trigger = mTriggerList[mRow]
                            mTriggerList.removeAt(mRow)
                            mTriggerList.add(mRow + 1, trigger)
                            saveList()
                            mTriggerTableModel.fireTableDataChanged()
                            mTriggerTable.setRowSelectionInterval(mRow + 1, mRow + 1)
                        }
                    }
                }
            }
        }

        inner class TriggerCtrlRenderer : DefaultTableCellRenderer() {
            private val mTriggerCtrlPane = TriggerCtrlPane(-1)

            override fun getTableCellRendererComponent(
                table: JTable,
                value: Any,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                mTriggerCtrlPane.updatePane(row)
                return mTriggerCtrlPane
            }
        }

        inner class TriggerCtrlEditor : AbstractCellEditor(), TableCellEditor {
            private val mTriggerCtrlPane = TriggerCtrlPane(-1)

            init {
                mTriggerCtrlPane.addActionListener { SwingUtilities.invokeLater { stopCellEditing() } }
            }

            override fun getCellEditorValue(): Any {
                return ""
            }

            override fun isCellEditable(e: EventObject): Boolean {
                return true
            }

            override fun getTableCellEditorComponent(
                table: JTable,
                value: Any,
                isSelected: Boolean,
                row: Int,
                column: Int
            ): Component {
                mTriggerCtrlPane.updatePane(row)
                return mTriggerCtrlPane
            }
        }


        internal inner class MouseHandler : MouseAdapter() {
            override fun mouseClicked(p0: MouseEvent?) {
                if (SwingUtilities.isLeftMouseButton(p0)) {
                    if (p0?.clickCount == 2) {
                        if (p0.source == mScrollPane) {
                            showAddDialog()
                        }
                    }
                }

                super.mouseClicked(p0)
            }
        }

        fun showAddDialog() {
            if (mTriggerList.size >= MAX_TRIGGER_COUNT) {
                JOptionPane.showMessageDialog(MainUI.getInstance(), "${Strings.TRIGGER_CANNOT_ADD} : $MAX_TRIGGER_COUNT", Strings.ERROR, JOptionPane.ERROR_MESSAGE)
                return
            }
            val editDialog = EditDialog(MainUI.getInstance(), TRIGGER_NEW, TriggerItem("", "", TriggerAction.SHOW_DIALOG, "", true))
            editDialog.setLocationRelativeTo(MainUI.getInstance())
            editDialog.isVisible = true
        }

        internal inner class EditDialog(parent: MainUI, cmd: Int, item: TriggerItem) :JDialog(parent, Strings.EDIT, true), ActionListener {
            private var mOkBtn: JButton
            private var mCancelBtn: JButton

            private var mNameLabel: JLabel
            private var mNameTF: JTextField

            private var mOnceLabel: JLabel
            private var mOnceCombo: ColorComboBox<String>

            private var mFilterLabel: JLabel
            private var mFilterTF: JTextField

            private var mActionLabel: JLabel
            private var mActionCombo: ColorComboBox<String>

            private var mActionParamLabel: JLabel
            private var mActionParamTF: JTextField
            private val mTFBackground: Color

            private var mCmd = cmd

            init {
                val panelWidth = 700
                mOkBtn = JButton(Strings.OK)
                mOkBtn.addActionListener(this)
                mCancelBtn = JButton(Strings.CANCEL)
                mCancelBtn.addActionListener(this)

                mNameLabel = JLabel(" ${Strings.NAME} : ")
                mNameTF = JTextField(item.mName)
                mNameTF.preferredSize = Dimension(150, mNameTF.preferredSize.height)
                if (mCmd == TRIGGER_EDIT) {
                    mNameTF.isEditable = false
                }

                mOnceLabel = JLabel("      ${Strings.ONCE}/${Strings.REPEAT} : ")
                mOnceCombo = ColorComboBox()
                mOnceCombo.isEditable = false
                mOnceCombo.renderer = ColorComboBox.ComboBoxRenderer()
                mOnceCombo.preferredSize = Dimension(100, mOnceCombo.preferredSize.height)
                mOnceCombo.addItem(Strings.ONCE)
                mOnceCombo.addItem(Strings.REPEAT)
                mOnceCombo.selectedIndex = if (item.mOnce) 0 else 1

                mFilterLabel = JLabel(" ${Strings.FILTER} : ")
                mFilterTF = JTextField(item.mFilter)

                mActionLabel = JLabel(" ${Strings.ACTION} : ")
                mActionCombo = ColorComboBox()
                mActionCombo.isEditable = false
                mActionCombo.renderer = ColorComboBox.ComboBoxRenderer()
                mActionCombo.preferredSize = Dimension(270, mNameTF.preferredSize.height)
                for (action in ACTION_MAP) {
                    mActionCombo.addItem(action.value)
                }
                mActionCombo.selectedIndex = item.mAction.value

                mActionParamLabel = JLabel(" ${Strings.ACTION_PARAMETER} : ")
                mActionParamTF = JTextField(item.mActionParam)

                val panel1 = JPanel(FlowLayout(FlowLayout.LEFT))
                val layout1 = FlowLayout(FlowLayout.LEFT)
                layout1.vgap = 0
                layout1.hgap = 0
                panel1.layout = layout1
                panel1.add(mNameLabel)
                panel1.add(mNameTF)
                panel1.add(mOnceLabel)
                panel1.add(mOnceCombo)

                val panel2 = JPanel()
                val layout2 = FlowLayout(FlowLayout.LEFT)
                layout2.vgap = 0
                layout2.hgap = 0
                panel2.layout = layout2
                panel2.add(mFilterLabel)
                panel2.add(mFilterTF)

                val panel3 = JPanel()
                val layout3 = FlowLayout(FlowLayout.LEFT)
                layout3.vgap = 0
                layout3.hgap = 0
                panel3.layout = layout3
                panel3.add(mActionLabel)
                panel3.add(mActionCombo)

                val panel4 = JPanel()
                val layout4 = FlowLayout(FlowLayout.LEFT)
                layout4.vgap = 0
                layout4.hgap = 0
                panel4.layout = layout4
                panel4.add(mActionParamLabel)
                panel4.add(mActionParamTF)

                val itemPanel = JPanel()
                itemPanel.layout = BoxLayout(itemPanel, BoxLayout.Y_AXIS)
                itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
                itemPanel.add(panel1)
                itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
                itemPanel.add(panel2)
                itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
                itemPanel.add(panel3)
                itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
                itemPanel.add(panel4)
                itemPanel.preferredSize = Dimension(panelWidth, itemPanel.preferredSize.height)
                itemPanel.addComponentListener(ComponentHandler())

                val confirmPanel = JPanel()
                confirmPanel.preferredSize = Dimension(panelWidth, 40)
                confirmPanel.add(mOkBtn)
                confirmPanel.add(mCancelBtn)

                val panel = JPanel()
                panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
                panel.add(itemPanel)
                panel.add(confirmPanel)

                contentPane.add(panel)
                pack()
                mFilterTF.preferredSize = Dimension(panelWidth - (mFilterLabel.width + 5), mFilterTF.preferredSize.height)
                mActionParamTF.preferredSize = Dimension(panelWidth - (mActionParamLabel.width + 5), mActionParamTF.preferredSize.height)
                contentPane.remove(panel)
                contentPane.add(panel)
                pack()

                Utils.installKeyStrokeEscClosing(this)
                mTFBackground = mNameTF.background
            }

            inner class ComponentHandler: ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    mFilterTF.preferredSize = Dimension(e.component.width - (mFilterLabel.width + 5), mFilterTF.preferredSize.height)
                    mActionParamTF.preferredSize = Dimension(e.component.width - (mActionParamLabel.width + 5), mActionParamTF.preferredSize.height)
                    super.componentResized(e)
                }
            }

            override fun actionPerformed(e: ActionEvent?) {
                if (e?.source == mOkBtn) {
                    var isValid = true
                    mNameTF.background = mTFBackground
                    val triggerName = mNameTF.text.trim()
                    if (triggerName.isEmpty()) {
                        mNameTF.background = Color.RED
                        isValid = false
                    } else if (mCmd != TRIGGER_EDIT) {
                        for (trigger in mTriggerList) {
                            if (trigger.mName == triggerName) {
                                mNameTF.background = Color.RED
                                isValid = false
                            }
                        }
                    }

                    mFilterTF.background = mTFBackground
                    val triggerFilter = mFilterTF.text
                    if (triggerFilter.isBlank()) {
                        mFilterTF.background = Color.RED
                        isValid = false
                    }

                    mActionParamTF.background = mTFBackground
                    val triggerActionParams = mActionParamTF.text
                    if (mActionCombo.selectedIndex == TriggerAction.RUN_CMD.value ||mActionCombo.selectedIndex == TriggerAction.RUN_CMD_SHOW_DIALOG.value) {
                        if (triggerActionParams.isBlank()) {
                            mActionParamTF.background = Color.RED
                            isValid = false
                        }
                    }

                    if (isValid) {
                        if (mCmd == TRIGGER_EDIT) {
                            mTriggerList.removeAt(mTriggerTable.selectedRow)
                            mTriggerList.add(mTriggerTable.selectedRow, TriggerItem(triggerName, triggerFilter, TriggerAction.fromInt(mActionCombo.selectedIndex),
                                triggerActionParams, mOnceCombo.selectedIndex == 0))

                        }
                        else {
                            mTriggerList.add(
                                TriggerItem(
                                    triggerName, triggerFilter, TriggerAction.fromInt(mActionCombo.selectedIndex),
                                    triggerActionParams, mOnceCombo.selectedIndex == 0))
                        }
                        saveList()
                        mTriggerTableModel.fireTableDataChanged()
                        dispose()
                    }
                } else if (e?.source == mCancelBtn) {
                    dispose()
                }
            }
        }

        internal inner class TableMouseHandler : MouseAdapter() {
            override fun mouseClicked(p0: MouseEvent?) {
                if (SwingUtilities.isLeftMouseButton(p0)) {
                    if (p0?.clickCount == 2) {
                        if (p0.source == mTriggerTable) {
                            val selectedRow = mTriggerTable.selectedRow
                            if (selectedRow < 0) {
                                JOptionPane.showMessageDialog(MainUI.getInstance(), "${Strings.INVALID_INDEX} \"$selectedRow\"", Strings.ERROR, JOptionPane.ERROR_MESSAGE)
                                return
                            }

                            if (mTriggerList[selectedRow].mStatus == TriggerStatus.STOPPED) {
                                val ret = JOptionPane.showConfirmDialog(
                                    MainUI.getInstance(),
                                    String.format(Strings.CONFIRM_START_TRIGGER, mTriggerList[selectedRow].mName),
                                    Strings.START,
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE
                                )
                                if (ret == JOptionPane.OK_OPTION) {
                                    startTrigger(selectedRow)
                                }
                            }
                            else {
                                val ret = JOptionPane.showConfirmDialog(
                                    MainUI.getInstance(),
                                    String.format(Strings.CONFIRM_STOP_TRIGGER, mTriggerList[selectedRow].mName),
                                    Strings.STOP,
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE
                                )
                                if (ret == JOptionPane.OK_OPTION) {
                                    stopTrigger(selectedRow)
                                }
                            }
                        }
                    }
                }

                super.mouseClicked(p0)
            }
        }
    }
    
    inner class ResultDialog(parent: MainUI, item: TriggerItem) :JDialog(parent, Strings.INFO, false), ActionListener {
        private var mName = item.mName
        private var mStopBtn: JButton
        private var mOkBtn: JButton

        private var mNameLabel: JLabel
        private var mFilterLabel: JLabel
        private var mActionLabel: JLabel
        private var mActionParamLabel: JLabel

        private val mResultArea: JTextArea
        private val mScrollPane: JScrollPane

        init {
            val panelWidth = 700
            mStopBtn = JButton(Strings.STOP)
            mStopBtn.addActionListener(this)
            mOkBtn = JButton(Strings.OK)
            mOkBtn.addActionListener(this)

            mNameLabel = JLabel(" ${Strings.NAME} : ${item.mName} (${if (item.mOnce) Strings.ONCE else "${Strings.REPEAT} ${item.mRunCount}"})")
            mFilterLabel = JLabel(" ${Strings.FILTER} : ${item.mFilter}")
            mActionLabel = JLabel(" ${Strings.ACTION} : ${ACTION_MAP[item.mAction]}")
            mActionParamLabel = JLabel(" ${Strings.ACTION_PARAMETER} : ${item.mActionParam}")

            val panel1 = JPanel(FlowLayout(FlowLayout.LEFT))
            val layout1 = FlowLayout(FlowLayout.LEFT)
            layout1.vgap = 0
            layout1.hgap = 0
            panel1.layout = layout1
            panel1.add(mNameLabel)

            val panel2 = JPanel()
            val layout2 = FlowLayout(FlowLayout.LEFT)
            layout2.vgap = 0
            layout2.hgap = 0
            panel2.layout = layout2
            panel2.add(mFilterLabel)

            val panel3 = JPanel()
            val layout3 = FlowLayout(FlowLayout.LEFT)
            layout3.vgap = 0
            layout3.hgap = 0
            panel3.layout = layout3
            panel3.add(mActionLabel)

            val panel4 = JPanel()
            val layout4 = FlowLayout(FlowLayout.LEFT)
            layout4.vgap = 0
            layout4.hgap = 0
            panel4.layout = layout4
            panel4.add(mActionParamLabel)

            val itemPanel = JPanel()
            itemPanel.layout = BoxLayout(itemPanel, BoxLayout.Y_AXIS)
            Utils.addHSeparator(itemPanel, Strings.TRIGGER)
            itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
            itemPanel.add(panel1)
            itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
            itemPanel.add(panel2)
            itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
            itemPanel.add(panel3)
            itemPanel.add(Box.createRigidArea(Dimension(0, 5)))
            itemPanel.add(panel4)
            itemPanel.preferredSize = Dimension(panelWidth, itemPanel.preferredSize.height)

            val resultPanel = JPanel()
            resultPanel.layout = BoxLayout(resultPanel, BoxLayout.Y_AXIS)
            mResultArea = JTextArea()
            mResultArea.text = item.getLogs()
            mResultArea.isEditable = false
            mResultArea.isOpaque = false
            mResultArea.wrapStyleWord = true
            mResultArea.lineWrap = true
            mScrollPane = JScrollPane(mResultArea)
            mScrollPane.preferredSize = Dimension(panelWidth, 500)
            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false

            resultPanel.add(mScrollPane)

            val confirmPanel = JPanel()
            confirmPanel.preferredSize = Dimension(panelWidth, 40)
            confirmPanel.add(mOkBtn)
            confirmPanel.add(mStopBtn)

            val panel = JPanel()
            panel.layout = BorderLayout()
            panel.add(itemPanel, BorderLayout.NORTH)
            panel.add(resultPanel, BorderLayout.CENTER)
            panel.add(confirmPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        fun updateData(item: TriggerItem) {
            mNameLabel.text = " ${Strings.NAME} : ${item.mName} (${if (item.mOnce) Strings.ONCE else "${Strings.REPEAT} ${item.mRunCount}"})"
            mFilterLabel.text = " ${Strings.FILTER} : ${item.mFilter}"
            mActionLabel.text = " ${Strings.ACTION} : ${ACTION_MAP[item.mAction]}"
            mActionParamLabel.text = " ${Strings.ACTION_PARAMETER} : ${item.mActionParam}"
            mResultArea.text = item.getLogs()
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mOkBtn) {
                dispose()
            } else if (e?.source == mStopBtn) {
                stopTrigger(mName)
            }
        }
    }
}