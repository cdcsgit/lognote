package com.blogspot.kotlinstudy.lognote

import com.blogspot.kotlinstudy.lognote.FormatManager.FormatItem.Token
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import kotlin.math.max


class FormatManager private constructor(){
    interface FormatEventListener {
        fun formatChanged(format: FormatItem)
    }

    private val mEventListeners = ArrayList<FormatEventListener>()

    fun addFormatEventListener(listener: FormatEventListener) {
        mEventListeners.add(listener)
    }

    fun removeFormatEventListener(listener: FormatEventListener) {
        mEventListeners.remove(listener)
    }

    private fun notifyFormatChanged() {
        for (listener in mEventListeners) {
            listener.formatChanged(mCurrFormat)
        }
    }

    data class FormatItem(val mName: String, val mSeparator: String, val mLevelNth: Int, val mLevels: Map<String, Int>, val mTokens: Array<Token>, val mPidTokIdx: Int) {
        data class Token(val mToken: String, val mNth: Int, val mIsSaveFilter: Boolean, var mUiWidth: Int)
        val mSortedTokens: Array<out Token> = mTokens.sortedArrayWith { t1: Token, t2: Token -> t1.mNth - t2.mNth }
        val mSortedPidTokIdx: Int

        init {
            if (mPidTokIdx == -1) {
                mSortedPidTokIdx = -1
            }
            else {
                val token = mTokens[mPidTokIdx].mToken
                var tokIdx = -1
                if (token.isNotEmpty()) {
                    for (idx in 0 until MAX_TOKEN_COUNT) {
                        if (token == mSortedTokens[idx].mToken) {
                            tokIdx = idx
                            break
                        }
                    }
                }
                mSortedPidTokIdx = tokIdx
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FormatItem

            return mName == other.mName
        }

        override fun hashCode(): Int {
            return mName.hashCode()
        }
    }

    companion object {
        const val MAX_TOKEN_COUNT = 3
        const val DEFAULT_LOGCAT = "logcat"

        val TEXT_LEVEL = arrayOf("None", "Verbose", "Debug", "Info", "Warning", "Error", "Fatal")
        const val LEVEL_NONE = 0
        const val LEVEL_VERBOSE = 1
        const val LEVEL_DEBUG = 2
        const val LEVEL_INFO = 3
        const val LEVEL_WARNING = 4
        const val LEVEL_ERROR = 5
        const val LEVEL_FATAL = 6

        private val mInstance: FormatManager = FormatManager()
        fun getInstance(): FormatManager {
            return mInstance
        }
    }

    val mFormats = mutableListOf<FormatItem>()
    var mCurrFormat: FormatItem
    init {
        var separator = ":?\\s+"
        var levels = mapOf("V" to LEVEL_VERBOSE
            , "D" to LEVEL_DEBUG
            , "I" to LEVEL_INFO
            , "W" to LEVEL_WARNING
            , "E" to LEVEL_ERROR
            , "F" to LEVEL_FATAL
        )
        var levelNth = 4

        var tokens: Array<Token> = arrayOf(
            Token("Tag", 5, true, 250),
            Token("PID", 2, false, 120),
            Token("TID", 3, false, 0),
        )
        var pidTokIdx = 1
        mFormats.add(0, FormatItem(DEFAULT_LOGCAT, separator, levelNth, levels, tokens, pidTokIdx))

        // case logcat -time
        separator = "/|\\(?\\s+"
        levels = mapOf("V" to LEVEL_VERBOSE
                , "D" to LEVEL_DEBUG
                , "I" to LEVEL_INFO
                , "W" to LEVEL_WARNING
                , "E" to LEVEL_ERROR
                , "F" to LEVEL_FATAL
        )
        levelNth = 2

        tokens = arrayOf(
            Token("", 0, false, 120),
            Token("", 0, false, 120),
            Token("", 0, false, 120),
        )
        pidTokIdx = -1
        mFormats.add(FormatItem("logcat -time", separator, levelNth, levels, tokens, pidTokIdx))

        // case plain text
        separator = ""
        levels = mapOf()
        levelNth = -1

        tokens = arrayOf(
            Token("", 0, false, 0),
            Token("", 0, false, 0),
            Token("", 0, false, 0),
        )
        pidTokIdx = -1
        mFormats.add(FormatItem("plain text", separator, levelNth, levels, tokens, pidTokIdx))

        // case 2
//        val separator = ":| "
//        val levels = mapOf("V" to LEVEL_VERBOSE
//                , "DEBUG" to LEVEL_DEBUG
//                , "INFO" to LEVEL_INFO
//                , "NOTICE" to LEVEL_WARNING
//                , "ERROR" to LEVEL_ERROR
//                , "F" to LEVEL_FATAL
//        )
//        val levelNth = 4
//
//        val tokens: Array<Token> = arrayOf(
//            Token("", 0, false, 120),
//            Token("", 0, false, 120),
//            Token("", 0, false, 120),
//        )
//        val pidTokIdx = 0

        mCurrFormat = mFormats[0]
    }

//    fun updateFormat(name: String, value: FormatItem) {
//        mFormats[name] = value
//    }
//
//    fun removeFormat(name: String) {
//        mFormats.remove(name)
//    }

    fun clear() {
        mFormats.clear()
    }

//    fun getNames(): List<String> {
//        return mFormats.keys.toList()
//    }

    fun showFormatListDialog(parent: JFrame) {
        val formatListDialog = FormatListDialog(parent)
        formatListDialog.setLocationRelativeTo(parent)
        formatListDialog.isVisible = true
    }

    fun setCurrFormat(selectedItem: String) {
        for (format in mFormats) {
            if (format.mName == selectedItem) {
                mCurrFormat = format
                break
            }
        }

        notifyFormatChanged()
    }

    inner class FormatListDialog(parent: JFrame) : JDialog(parent, Strings.LOGFORMAT, true), ActionListener {
        inner class MultiLineCellRenderer : JTextPane(), TableCellRenderer {
            init {
                contentType = "text/html"
            }

            override fun getTableCellRendererComponent(
                table: JTable, value: Any,
                isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): Component {
                if (isSelected) {
                    setForeground(table.selectionForeground)
                    setBackground(table.selectionBackground)
                } else {
                    setForeground(table.getForeground())
                    setBackground(table.getBackground())
                }
                text = value.toString()
                when (column) {
                    0, 1, 2, 5 -> {
                        text = "<html><center>${value}</center></html>"
                    }
                }
                return this
            }
        }

        inner class FormatTableModel() : DefaultTableModel() {
            private val colNames = arrayOf("Name", "Separator", "Level Nth", "Levels", "Tokens", "PID Token")
            init {
                setColumnIdentifiers(colNames)
            }

            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }

            override fun getRowCount(): Int {
                return mFormats.size
            }

            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }

            override fun getValueAt(row: Int, column: Int): Any {
                val format = mFormats[row]
                when (column) {
                    0 -> {
                        return "<html><center>${format.mName}</center></html>"
                    }
                    1 -> {
                        return "<html><center>${format.mSeparator}</center></html>"
                    }
                    2 -> {
                        return "<html><center>${format.mLevelNth}</center></html>"
                    }
                    3 -> {
                        var levels = ""
                        format.mLevels.forEach { levels += "${TEXT_LEVEL[it.value]} : ${it.key}<br>" }
                        return "<html>${levels}</html>"
                    }
                    4 -> {
                        var tokens = ""
                        var idx = 0
                        format.mTokens.forEach {
                            tokens += "idx $idx : ${it.mToken}, ${it.mNth}, ${it.mIsSaveFilter}, ${it.mUiWidth}<br>"
                            idx++
                        }
                        return "<html>${tokens}</html>"
                    }
                    5 -> {
                        return "<html><center>${format.mPidTokIdx}</center></html>"
                    }
                }
                return super.getValueAt(row, column)
            }
        }

        internal inner class ListSelectionHandler : ListSelectionListener {
            private var mSelectedRow = -1
            override fun valueChanged(p0: ListSelectionEvent?) {
                if (mSelectedRow != mFormatTable.selectedRow) {
                    mSelectedRow = mFormatTable.selectedRow
                    val format = mFormats[mSelectedRow]
                    mNameTF.text = format.mName
                    mSeparatorTF.text = format.mSeparator
                    mLevelNthTF.text = format.mLevelNth.toString()
                    mPidTokIdxCombo.selectedItem = format.mPidTokIdx.toString()
                    for (idx in TEXT_LEVEL.indices) {
                        mLevelsTFArr[idx].text = ""
                    }
                    format.mLevels.forEach { mLevelsTFArr[it.value].text = it.key }
                    for (idx in 0 until MAX_TOKEN_COUNT) {
                        mTokenArr[idx].setToken(format.mTokens[idx])
                    }
                }

                return
            }
        }

        inner class TokenPanel(val idx: Int) : JPanel() {
            private val mTokenLabel = JLabel()
            private val mTokenTF = JTextField()
            private val mNthLabel = JLabel()
            private val mNthTF = JTextField()
            private val mIsSaveFilterLabel = JLabel()
            private val mIsSaveFilterCheck = JCheckBox()
            private val mUiWidthLabel = JLabel()
            private val mUiWidthTF = JTextField()
            private val mIdx = idx
            
            init {
                mTokenLabel.text = "$mIdx : Name"
                mTokenTF.text = ""
                mTokenTF.preferredSize = Dimension(150, mTokenTF.preferredSize.height)
                mNthLabel.text = "Nth"
                mNthTF.text = "-1"
                mIsSaveFilterLabel.text = "Save Filter"
                mIsSaveFilterCheck.isSelected = false
                mUiWidthLabel.text = "Ui Width"
                mUiWidthTF.text = "0"

                layout = FlowLayout(FlowLayout.LEFT)
                add(JLabel("   "))
                add(mTokenLabel)
                add(mTokenTF)
                add(JLabel("       "))
                add(mNthLabel)
                add(mNthTF)
                add(JLabel("       "))
                add(mIsSaveFilterLabel)
                add(mIsSaveFilterCheck)
                add(JLabel("       "))
                add(mUiWidthLabel)
                add(mUiWidthTF)
            }
            
            fun setToken(token: Token) {
                mTokenTF.text = token.mToken
                mNthTF.text = token.mNth.toString()
                mIsSaveFilterCheck.isSelected = token.mIsSaveFilter
                mUiWidthTF.text = token.mUiWidth.toString()
            }
            
            fun getToken(): Token {
                return Token("1", 0, true, 0)
            }
        }
        
        private val mSaveBtn: ColorButton
        private val mDeleteBtn: ColorButton
        private val mCloseBtn: ColorButton
        private val mFormatPanel = JPanel()
        private val mFormatTableModel = FormatTableModel()
        private val mFormatTable = JTable(mFormatTableModel)
        private val mScrollPane = JScrollPane(mFormatTable)
        private val mNameLabel = JLabel()
        private val mNameTF = JTextField()
        private val mSeparatorLabel = JLabel()
        private val mSeparatorTF = JTextField()
        private val mLevelNthLabel = JLabel()
        private val mLevelNthTF = JTextField()
        private val mPidTokIdxLabel = JLabel()
        private val mPidTokIdxCombo = ColorComboBox<String>()
        private val mNamePanel = JPanel()
        private val mLevelsLabelArr = Array(TEXT_LEVEL.size) { JLabel(TEXT_LEVEL[it]) }
        private val mLevelsTFArr = Array(TEXT_LEVEL.size) { JTextField() }
        private val mLevelsPanel = JPanel()
        private val mTokenArr = Array(MAX_TOKEN_COUNT) { TokenPanel(it) }
        private val mTokensPanel = JPanel()

        init {
            mFormatPanel.layout = BoxLayout(mFormatPanel, BoxLayout.Y_AXIS)

            mScrollPane.preferredSize = Dimension(1000, 500)
            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false

            mFormatTable.setDefaultRenderer(String::class.java, MultiLineCellRenderer())
            mFormatTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            mFormatTable.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            mFormatTable.columnModel.getColumn(0).preferredWidth = 60
            mFormatTable.columnModel.getColumn(1).preferredWidth = 50
            mFormatTable.columnModel.getColumn(2).preferredWidth = 50
            mFormatTable.columnModel.getColumn(3).preferredWidth = 200
            mFormatTable.columnModel.getColumn(4).preferredWidth = 250
            mFormatTable.columnModel.getColumn(5).preferredWidth = 50
            mFormatTable.selectionModel.addListSelectionListener(ListSelectionHandler())
            updateRowHeights()

            mNamePanel.layout = FlowLayout(FlowLayout.LEFT)
            mNameLabel.text = "Name"
            mNameTF.preferredSize = Dimension(150, mNameTF.preferredSize.height)
            mNameTF.text = ""
            mSeparatorLabel.text = "Separator"
            mSeparatorTF.text = ""
            mSeparatorTF.preferredSize = Dimension(100, mSeparatorTF.preferredSize.height)
            mLevelNthLabel.text = "Level Nth"
            mLevelNthTF.text = "-1"
            mPidTokIdxLabel.text = "Pid Token(optional)"
            for (idx in -1 until MAX_TOKEN_COUNT) {
                mPidTokIdxCombo.addItem("$idx")
            }

            mNamePanel.add(JLabel("   "))
            mNamePanel.add(mNameLabel)
            mNamePanel.add(mNameTF)
            mNamePanel.add(JLabel("   "))
            mNamePanel.add(mSeparatorLabel)
            mNamePanel.add(mSeparatorTF)
            mNamePanel.add(JLabel("   "))
            mNamePanel.add(mLevelNthLabel)
            mNamePanel.add(mLevelNthTF)
            mNamePanel.add(JLabel("   "))
            mNamePanel.add(mPidTokIdxLabel)
            mNamePanel.add(mPidTokIdxCombo)

            mLevelsPanel.layout = FlowLayout(FlowLayout.LEFT)
            mLevelsPanel.add(JLabel("   "))
            for (idx in 1 until TEXT_LEVEL.size) {
                mLevelsPanel.add(mLevelsLabelArr[idx])
                mLevelsPanel.add(mLevelsTFArr[idx])
                mLevelsPanel.add(JLabel("   "))
            }

            mTokensPanel.layout = BoxLayout(mTokensPanel, BoxLayout.Y_AXIS)
            for (idx in 0 until MAX_TOKEN_COUNT) {
                mTokensPanel.add(mTokenArr[idx])
            }

            mFormatPanel.add(mScrollPane)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " Name ")
            mFormatPanel.add(mNamePanel)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " Levels ")
            mFormatPanel.add(mLevelsPanel)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " Tokens ")
            mFormatPanel.add(mTokensPanel)

            mSaveBtn = ColorButton(Strings.SAVE)
            mSaveBtn.addActionListener(this)
            mDeleteBtn = ColorButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)

            val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
            confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
            confirmPanel.add(mSaveBtn)
            confirmPanel.add(mDeleteBtn)
            confirmPanel.add(mCloseBtn)

            val panel = JPanel(BorderLayout())
            panel.add(mFormatPanel, BorderLayout.CENTER)
            panel.add(confirmPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        private fun updateRowHeights() {
            for (row in 0 until mFormatTable.rowCount) {
                var rowHeight = mFormatTable.rowHeight
                for (column in 0 until mFormatTable.columnCount) {
                    val comp: Component = mFormatTable.prepareRenderer(mFormatTable.getCellRenderer(row, column), row, column)
                    rowHeight = max(rowHeight, comp.preferredSize.height)
                }
                mFormatTable.setRowHeight(row, rowHeight)
            }
        }

        private fun addHSeparator(target:JPanel, title: String) {
            val titleHtml = title.replace(" ", "&nbsp;")
            val separator = JSeparator(SwingConstants.HORIZONTAL)
            val label = JLabel("<html><b>$titleHtml</b></html>")
            val panel = JPanel(BorderLayout())
            val separPanel = JPanel(BorderLayout())
            separPanel.add(Box.createVerticalStrut(label.font.size / 2), BorderLayout.NORTH)
            separPanel.add(separator, BorderLayout.CENTER)
            panel.add(label, BorderLayout.WEST)
            panel.add(separPanel, BorderLayout.CENTER)
            target.add(panel)
        }

        private fun addHEmptySeparator(target:JPanel, height: Int) {
            val panel = JPanel()
            panel.preferredSize = Dimension(1, height)
            target.add(panel)
        }

        override fun actionPerformed(e: ActionEvent?) {
            when (e?.source) {
                mSaveBtn -> {
                    dispose()
                }
                mDeleteBtn -> {
                    dispose()
                }
                mCloseBtn -> {
                    dispose()
                }
            }
        }
    }
}