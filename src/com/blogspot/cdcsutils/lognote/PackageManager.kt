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


data class PackageItem(val mPackageName: String, val mUid: String, var mIsShow: Boolean, var mIsSelected: Boolean)

class PackageManager private constructor() {
    private val mConfigManager = ConfigManager.getInstance()

    private val mPackageMap: MutableMap<String, PackageItem> = mutableMapOf()
    var mPackageArray = Array(0) { arrayOfNulls<Any>(4) }
    val mShowPackageList = mutableListOf<PackageItem>()
    private var mSelectedUids = ""

    private var mUpdatedTime: Long = 0

    companion object {
        private val mInstance: PackageManager = PackageManager()

        fun getInstance(): PackageManager {
            return mInstance
        }

        const val DEFAULT_UPDATE_TIME = 0 // msec
        var UpdateTime = DEFAULT_UPDATE_TIME
        const val MAX_PACKAGE_COUNT = 20
    }

    init {
        loadConfigPackages()
    }

    fun clear() {
        mPackageMap.clear()
    }

    fun add(item: PackageItem) {
        for (showItem in mShowPackageList) {
            if (item.mPackageName == showItem.mPackageName) {
                item.mIsShow = true
                item.mIsSelected = showItem.mIsSelected
                mShowPackageList.remove(showItem)
                mShowPackageList.add(item)
                return
            }
        }

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

        mPackageArray = Array(mShowPackageList.size + mPackageMap.size) {
            arrayOfNulls<Any>(
                5
            )
        }

        var idx = 0
        for (item in mShowPackageList) {
            mPackageArray[idx][0] = (idx + 1).toString()
            mPackageArray[idx][1] = item.mPackageName
            mPackageArray[idx][2] = item.mUid
            mPackageArray[idx][3] = item.mIsShow
            mPackageArray[idx][4] = item.mIsSelected
            idx++
        }

        val keySet: List<String> = ArrayList(mPackageMap.keys)
        sort(keySet)

        for (key in keySet) {
            mPackageArray[idx][0] = (idx + 1).toString()
            mPackageArray[idx][1] = mPackageMap[key]?.mPackageName ?: ""
            mPackageArray[idx][2] = mPackageMap[key]?.mUid ?: ""
            mPackageArray[idx][3] = false
            mPackageArray[idx][4] = true
            idx++
        }

        val mainUI = MainUI.getInstance()
        val packageSelectDialog = PackageSelectDialog(mainUI)
        packageSelectDialog.setLocationRelativeTo(mainUI)
        packageSelectDialog.isVisible = true
    }

    fun updateUids(packageBtns: Array<PackageToggleButton>) {
        updatePackages()
        mSelectedUids = ""
        for (item in packageBtns) {
            if (item.isSelected) {
                var tmpItem: PackageItem? = null
                for (showItem in mShowPackageList) {
                    if (showItem.mPackageName == item.text) {
                        showItem.mIsSelected = true
                        tmpItem = showItem
                        break
                    }
                }
                if (tmpItem != null) {
                    if (mSelectedUids.isEmpty()) {
                        mSelectedUids = tmpItem.mUid
                    }
                    else {
                        mSelectedUids += ",${tmpItem.mUid}"
                    }
                    item.mIsValid = true
                }
                else {
                    item.mIsValid = false
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

    fun loadConfigPackages() {
        val packages = mConfigManager.loadPackages()
        mShowPackageList.clear()
        for (item in packages) {
            val textSplited = item.split("|")
            if (textSplited.size != 2) {
                continue
            }

            mShowPackageList.add(PackageItem(textSplited[0], "", true, textSplited[1].toBoolean()))
        }
    }

    fun saveConfigPackages() {
        val packages = ArrayList<String>()

        var packageItem: String
        var count = 0
        for (item in mShowPackageList) {
            if (count >= MAX_PACKAGE_COUNT) {
                break
            }
            packageItem = "${item.mPackageName}|${item.mIsSelected}"
            packages.add(packageItem)
            count++
        }
        mConfigManager.savePackages(packages)
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
            mTable.addKeyListener(KeyHandler())
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

            mTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "none")

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

            val label = JLabel(" * ${Strings.SELECT_UNSELECT_PACKAGE}")
            label.preferredSize = Dimension(label.preferredSize.width, 40)
            panel.add(label, BorderLayout.NORTH)

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

        internal inner class KeyHandler : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {

                super.keyPressed(e)
            }

            override fun keyReleased(e: KeyEvent?) {
                when (e?.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        val isShow = mTable.getValueAt(mTable.selectedRow, 3) as Boolean
                        mTable.setValueAt(!isShow, mTable.selectedRow, 3)
                    }
                }

                super.keyReleased(e)
            }
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mCloseBtn) {
                dispose()
            }
            else if (e?.source == mOkBtn) {
                mShowPackageList.clear()
                for (item in mPackageArray) {
                    if (item[3] == true) {
                        mShowPackageList.add(
                            PackageItem(item[1].toString(), item[2].toString(), item[3] as Boolean, item[4] as Boolean)
                        )
                    }
                }

                saveConfigPackages()

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
