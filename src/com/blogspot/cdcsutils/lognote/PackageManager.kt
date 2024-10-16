package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.Collections.sort
import javax.swing.*
import javax.swing.border.Border
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


data class PackageItem(val mPackageName: String, val mUid: String, var mIsShow: Boolean)

class PackageManager private constructor() {
    private val mPackageMap: MutableMap<String, PackageItem> = mutableMapOf()
    var mPackageArray = Array(0) {
        arrayOfNulls<Any>(
            4
        )
    }

    val mSelectedPackageList = mutableListOf<PackageItem>()
    private var mSelectedUids = ""

    private var mUpdatedTime: Long = 0

    companion object {
        private val mInstance: PackageManager = PackageManager()

        fun getInstance(): PackageManager {
            return mInstance
        }

        const val MAX_UPDATE_TIME_SEC = 600 // sec
        const val DEFAULT_UPDATE_TIME = 10000 // msec
        var UpdateTime = DEFAULT_UPDATE_TIME
    }

    fun getPackageItem(packageName: String): PackageItem? {
        if (MainUI.CurrentMethod != MainUI.METHOD_ADB) {
            return null
        }
        val item = mPackageMap[packageName]
        if (item != null) {
            return item
        }
        try {
            updatePackages()
        } catch (ex: InterruptedException) {
            Utils.printlnLog("PackageList.getPackage failed ")
            ex.printStackTrace()
        }

        return mPackageMap[packageName]
    }

    fun clear() {
        mPackageMap.clear()
    }

    fun add(item: PackageItem) {
        mPackageMap[item.mPackageName] = item
    }

    private fun updatePackages(): Boolean {
        val time = System.currentTimeMillis()
        if (time > mUpdatedTime + UpdateTime){
            LogCmdManager.getInstance().getPackages()
            mUpdatedTime = System.currentTimeMillis()
            return true
        } else {
            return false
        }
    }

    fun showPackageDialog() {
        updatePackages()

        val keySet: List<String> = ArrayList(mPackageMap.keys)
        sort(keySet)

        mPackageArray = Array(mPackageMap.size) {
            arrayOfNulls<Any>(
                4
            )
        }

        for ((idx, key) in keySet.withIndex()) {
            mPackageArray[idx][0] = (idx + 1).toString()
            mPackageArray[idx][1] = mPackageMap[key]?.mPackageName ?: ""
            mPackageArray[idx][2] = mPackageMap[key]?.mUid ?: ""
            mPackageArray[idx][3] = mPackageMap[key]?.mIsShow ?: false
        }

        val mainUI = MainUI.getInstance()
        val packageSelectDialog = PackageSelectDialog(mainUI)
        packageSelectDialog.setLocationRelativeTo(mainUI)
        packageSelectDialog.isVisible = true
    }

    fun updateUid(mPackageBtns: Array<PackageToggleButton>) {
        updatePackages()
        mSelectedUids = ""
        for (item in mPackageBtns) {
            if (item.isSelected) {
                val tmpItem = mPackageMap[item.text]
                if (tmpItem != null) {
                    if (mSelectedUids.isEmpty()) {
                        mSelectedUids = tmpItem.mUid
                    }
                    else {
                        mSelectedUids += ",${tmpItem.mUid}"
                    }
                }
            }
        }
        mPackageMap.clear()
    }

    fun getUids(): String {
        return if (mSelectedUids.trim().isNotEmpty()) {
            "--uid=$mSelectedUids"
        } else {
            ""
        }
    }

    inner class PackageSelectDialog(mainUI: MainUI) : JDialog(mainUI, Strings.SELECT_PACKAGE, true), ActionListener {
        private val mScrollPane: JScrollPane
        private val mTable: JTable
        private var mOkBtn : ColorButton
        private var mCloseBtn : ColorButton
        private val mCellRenderer = PackageCellRenderer()

        init {
            val columnNames = arrayOf("Num", "Package", "UID", "Show")

            val model = object: DefaultTableModel(mPackageArray, columnNames) {
                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }

                override fun getColumnClass(columnIndex: Int): Class<*> {
                    if (columnIndex == 3) {
                        return Boolean::class.java
                    }

                    return super.getColumnClass(columnIndex)
                }

                override fun setValueAt(aValue: Any?, row: Int, column: Int) {
                    if (column == 3) {
                        mPackageArray[row][3] = aValue
                    }
                    super.setValueAt(aValue, row, column)
                }
            }

            mTable = JTable(model)
            mTable.addMouseListener(MouseHandler())
            mTable.setShowGrid(true)
            mTable.columnModel.getColumn(0).preferredWidth = 70
            mTable.columnModel.getColumn(0).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(1).preferredWidth = 190
            mTable.columnModel.getColumn(1).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(2).preferredWidth = 100
            mTable.columnModel.getColumn(2).cellRenderer = mCellRenderer
            mTable.columnModel.getColumn(3).preferredWidth = 70
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

            mOkBtn = ColorButton(Strings.OK)
            mOkBtn.addActionListener(this)

            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)

            val panel = JPanel()
            panel.layout = BorderLayout()
            panel.add(mScrollPane, BorderLayout.CENTER)

            val btnPanel = JPanel()
            btnPanel.add(mOkBtn)
            btnPanel.add(mCloseBtn)
            panel.add(btnPanel, BorderLayout.SOUTH)

            addComponentListener(ComponentHandler())
            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        internal inner class MouseHandler : MouseAdapter() {
            override fun mouseClicked(p0: MouseEvent?) {
                if (SwingUtilities.isLeftMouseButton(p0)) {
                    if (p0?.clickCount == 2) {
                        val isShow = mTable.getValueAt(mTable.selectedRow, 3) as Boolean
                        mTable.setValueAt(!isShow, mTable.selectedRow, 3)
                    }
                }

                super.mouseClicked(p0)
            }
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mCloseBtn) {
                dispose()
            }
            else if (e?.source == mOkBtn) {
                mSelectedPackageList.clear()
                for (item in mPackageArray) {
                    if (item[3] == true) {
                        mSelectedPackageList.add(mPackageMap[item[1].toString()]!!.copy(mIsShow = true))
                    }
                }
                mPackageMap.clear()
                mPackageArray = emptyArray()
                dispose()
            }
        }

        inner class PackageCellRenderer : DefaultTableCellRenderer() {
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
                horizontalAlignment = when (column) {
                    0 -> {
                        RIGHT
                    }
                    3 -> {
                        CENTER
                    }
                    else -> {
                        LEFT
                    }
                }

                if (column == 3) {
                    val isShow = value as Boolean
                    val panel = JPanel()
                    val layout = FlowLayout(FlowLayout.CENTER)
                    layout.vgap = 0
                    panel.layout = layout
                    val checkbox = JCheckBox()
                    checkbox.isSelected = isShow
                    panel.add(checkbox)
                    if (isSelected) {
                        panel.background = mTable.selectionBackground
                    }
                    else {
                        panel.background = mTable.background
                        panel.border = BorderFactory.createMatteBorder(0, 0, 1, 0, mTable.gridColor)
                    }

                    return panel
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

            val columnCmd = mTable.columnModel.getColumn(1)
            if (columnCmd.preferredWidth != preferredCmdWidth) {
                columnCmd.preferredWidth = preferredCmdWidth
            }
        }
    }
}
