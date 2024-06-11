package com.blogspot.kotlinstudy.lognote

import com.blogspot.kotlinstudy.lognote.FormatManager.FormatItem.Token
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import kotlin.math.max


class FormatManager private constructor(fileName: String) : PropertiesBase(fileName){
    interface FormatEventListener {
        fun formatChanged(format: FormatItem)
        fun formatListChanged()
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

    private fun notifyFormatListChanged() {
        for (listener in mEventListeners) {
            listener.formatListChanged()
        }
    }

    data class FormatItem(val mName: String, val mSeparator: String, val mLevelNth: Int, val mLevels: Map<String, Int>, val mTokens: Array<Token>, val mPidTokIdx: Int) {
        data class Token(val mToken: String, val mNth: Int, val mIsSaveFilter: Boolean, var mUiWidth: Int)
        val mSortedTokens: Array<out Token> = mTokens.sortedArrayWith { t1: Token, t2: Token -> t1.mNth - t2.mNth }
        val mSortedTokensIdxs = Array(MAX_TOKEN_COUNT) { -1 }
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

            for (idx in 0 until MAX_TOKEN_COUNT) {
                for (idxSorted in 0 until MAX_TOKEN_COUNT) {
                    if (mTokens[idx].mToken == mSortedTokens[idxSorted].mToken) {
                        mSortedTokensIdxs[idx] = idxSorted
                        break
                    }
                }
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
        private const val FORMATS_LIST_FILE = "lognote_formats.xml"
        const val ITEM_VERSION = "FORMAT_VERSION"

        const val ITEM_NAME = "_NAME"
        const val ITEM_SEPARATOR = "_SEPARATOR"
        const val ITEM_LEVEL = "_LEVEL_"
        const val ITEM_LEVEL_NTH = "_LEVEL_NTH"
        const val ITEM_TOKEN_NAME = "_TOKEN_NAME_"
        const val ITEM_TOKEN_NTH = "_TOKEN_NTH_"
        const val ITEM_TOKEN_SAVE_FILTER = "_TOKEN_SAVE_FILTER_"
        const val ITEM_TOKEN_UI_WIDTH = "_TOKEN_UI_WIDTH_"
        const val ITEM_PID_TOK_IDX = "_PID_TOK_IDX"

        const val MAX_FORMAT_COUNT = 50
        const val MAX_TOKEN_COUNT = 3

        val TEXT_LEVEL = arrayOf("None", "Verbose", "Debug", "Info", "Warning", "Error", "Fatal")
        const val LEVEL_NONE = 0
        const val LEVEL_VERBOSE = 1
        const val LEVEL_DEBUG = 2
        const val LEVEL_INFO = 3
        const val LEVEL_WARNING = 4
        const val LEVEL_ERROR = 5
        const val LEVEL_FATAL = 6

        private val mInstance: FormatManager = FormatManager(FORMATS_LIST_FILE)
        fun getInstance(): FormatManager {
            return mInstance
        }
    }

    val mFormatList = mutableListOf<FormatItem>()
    var mCurrFormat: FormatItem

    private val mConfigManager = ConfigManager.getInstance()

    init {
        manageVersion()
        loadList()

        if (mFormatList.isEmpty()) {
            addDefaultFormats(mFormatList)
        }

        mCurrFormat = mFormatList[0]
        val logFormat = mConfigManager.getItem(ConfigManager.ITEM_LOG_FORMAT)
        if (logFormat != null) {
            setCurrFormat(logFormat.toString().trim())
        }
        else {
            setCurrFormat("")
        }
    }

    private fun addDefaultFormats(formatList: MutableList<FormatItem>) {
        var separator = ":?\\s+"
        var levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        var levelNth = 4

        var tokens: Array<Token> = arrayOf(
            Token("Tag", 5, true, 250),
            Token("PID", 2, false, 120),
            Token("TID", 3, false, 120),
        )
        var pidTokIdx = 1
        formatList.add(0, FormatItem("logcat", separator, levelNth, levels, tokens, pidTokIdx))

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
        formatList.add(FormatItem("plain text", separator, levelNth, levels, tokens, pidTokIdx))

        // case logcat -time
        separator = "/|\\(?\\s+"
        levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        levelNth = 2

        tokens = arrayOf(
            Token("", 0, false, 120),
            Token("", 0, false, 120),
            Token("", 0, false, 120),
        )
        pidTokIdx = -1
        formatList.add(FormatItem("logcat -time", separator, levelNth, levels, tokens, pidTokIdx))
    }

    private fun loadList() {
        loadXml()
        mFormatList.clear()

        for (i in 0 until MAX_FORMAT_COUNT) {
            val name = (mProperties["$i$ITEM_NAME"] ?: "") as String
            if (name.trim().isEmpty()) {
                break
            }
            val separator = (mProperties["$i$ITEM_SEPARATOR"] ?: "") as String
            val levels = emptyMap<String, Int>().toMutableMap()
            for (idx in 1 until TEXT_LEVEL.size) {
                val level = ((mProperties["$i$ITEM_LEVEL$idx"] ?: "") as String).trim()
                if (level.isNotEmpty()) {
                    levels[level] = idx
                }
            }

            val levelNth = try {
                ((mProperties["$i$ITEM_LEVEL_NTH"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                -1
            }

            var tokens: Array<Token>
            try {
                tokens = Array(MAX_TOKEN_COUNT) {
                    val tokenName = ((mProperties["$i$ITEM_TOKEN_NAME$it"] ?: "") as String).trim()
                    val nth = try {
                        ((mProperties["$i$ITEM_TOKEN_NTH$it"] ?: "") as String).toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                    val check = (mProperties["$i$ITEM_TOKEN_SAVE_FILTER$it"] ?: "") as String
                    val isSaveFilter = if (check.isNotEmpty()) {
                        check.toBoolean()
                    } else {
                        false
                    }
                    val uiWidth = try {
                        ((mProperties["$i$ITEM_TOKEN_UI_WIDTH$it"] ?: "") as String).toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }

                    Token(tokenName, nth, isSaveFilter, uiWidth)
                }
            } catch (ex: Exception) {
                println("Failed load format($name) tokens")
                ex.printStackTrace()
                tokens = arrayOf(
                    Token("", 0, false, 120),
                    Token("", 0, false, 120),
                    Token("", 0, false, 120),
                )
            }

            val pidTokIdx = try {
                ((mProperties["$i$ITEM_PID_TOK_IDX"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                -1
            }

            mFormatList.add(FormatItem(name, separator, levelNth, levels, tokens, pidTokIdx))
        }
    }

    private fun saveList() {
        mProperties.clear()
        var format: FormatItem
        for (i in 0 until mFormatList.size) {
            format = mFormatList[i]
            mProperties["$i$ITEM_NAME"] = format.mName
            mProperties["$i$ITEM_SEPARATOR"] = format.mSeparator
            val keyList = format.mLevels.keys.toList()
            for (key in keyList) {
                val level = format.mLevels[key]
                if (level != null) {
                    mProperties["$i$ITEM_LEVEL$level"] = key
                }
            }

            mProperties["$i$ITEM_LEVEL_NTH"] = format.mLevelNth.toString()

            for (idx in 0 until MAX_TOKEN_COUNT) {
                mProperties["$i$ITEM_TOKEN_NAME$idx"] = format.mTokens[idx].mToken
                mProperties["$i$ITEM_TOKEN_NTH$idx"] = format.mTokens[idx].mNth.toString()
                mProperties["$i$ITEM_TOKEN_SAVE_FILTER$idx"] = format.mTokens[idx].mIsSaveFilter.toString()
                mProperties["$i$ITEM_TOKEN_UI_WIDTH$idx"] = format.mTokens[idx].mUiWidth.toString()
            }

            mProperties["$i$ITEM_PID_TOK_IDX"] = format.mPidTokIdx.toString()
        }

        saveXml()
        notifyFormatListChanged()
    }

    override fun manageVersion() {
        // do nothing
    }

//    fun updateFormat(name: String, value: FormatItem) {
//        mFormatList[name] = value
//    }
//
//    fun removeFormat(name: String) {
//        mFormatList.remove(name)
//    }

    fun clear() {
        mFormatList.clear()
    }

//    fun getNames(): List<String> {
//        return mFormatList.keys.toList()
//    }

    fun showFormatListDialog(parent: JFrame) {
        val formatListDialog = FormatListDialog(parent)
        formatListDialog.setLocationRelativeTo(parent)
        formatListDialog.isVisible = true
    }

    fun setCurrFormat(selectedItem: String) {
        var isUpdated = false
        for (format in mFormatList) {
            if (format.mName == selectedItem) {
                mCurrFormat = format
                isUpdated = true
                break
            }
        }

        if (!isUpdated && mFormatList.isNotEmpty()) {
            mCurrFormat = mFormatList[0]
        }

        notifyFormatChanged()
    }

    private fun copyFormatList(src: MutableList<FormatItem>, dest: MutableList<FormatItem>) {
        dest.clear()
        for (item in src) {
            dest.add(item.copy())
        }
        return
    }
    
    inner class FormatListDialog(parent: JFrame) : JDialog(parent, "${Strings.LOG_FORMAT} ${Strings.SETTING}", true), ActionListener {
        private val mDialogFormatList = mutableListOf<FormatItem>()

        init {
            copyFormatList(mFormatList, mDialogFormatList)
        }
        
        inner class MultiLineCellRenderer : JTextPane(), TableCellRenderer {
            init {
                contentType = "text/html"
                border = Utils.CustomLineBorder(mFormatTable.gridColor, 1, Utils.CustomLineBorder.BOTTOM)
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
            private val colNames = arrayOf(Strings.NAME, Strings.SEPARATOR, Strings.LEVEL_NTH, Strings.LEVELS, Strings.TOKENS, Strings.PID_TOKEN)
            init {
                setColumnIdentifiers(colNames)
            }

            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }

            override fun getRowCount(): Int {
                return mDialogFormatList.size
            }

            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }

            override fun getValueAt(row: Int, column: Int): Any {
                val format = mDialogFormatList[row]
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
                if (mFormatTable.selectedRow >= 0 && mSelectedRow != mFormatTable.selectedRow) {
                    mSelectedRow = mFormatTable.selectedRow
                    val format = mDialogFormatList[mSelectedRow]
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
                mTokenLabel.text = "$mIdx : ${Strings.NAME}"
                mTokenTF.text = ""
                mTokenTF.preferredSize = Dimension(150, mTokenTF.preferredSize.height)
                mNthLabel.text = Strings.NTH
                mNthTF.text = "-1"
                mIsSaveFilterLabel.text = Strings.SAVE_FILTER
                mIsSaveFilterCheck.isSelected = false
                mUiWidthLabel.text = "UI ${Strings.WIDTH}"
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
                var isValid = true
                val name = mTokenTF.text.trim()

                var nth: Int = 0
                try {
                    nth = mNthTF.text.toInt()
                    mNthTF.background = mTextFieldBg
                    mNthTF.toolTipText = ""
                } catch (ex: NumberFormatException) {
                    mNthTF.background = Color.RED
                    mNthTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
                    isValid = false
                }

                val isSaveFilter = mIsSaveFilterCheck.isSelected

                var uiWidth: Int = 0
                try {
                    uiWidth = mUiWidthTF.text.toInt()
                    mUiWidthTF.background = mTextFieldBg
                    mUiWidthTF.toolTipText = ""
                } catch (ex: NumberFormatException) {
                    mUiWidthTF.background = Color.RED
                    mUiWidthTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
                    isValid = false
                }

                if (!isValid) {
                    throw NumberFormatException()
                }

                return Token(name, nth, isSaveFilter, uiWidth)
            }
        }

        private val mFirstBtn: ColorButton
        private val mPrevBtn: ColorButton
        private val mNextBtn: ColorButton
        private val mLastBtn: ColorButton

        private val mAddBtn: ColorButton
        private val mCopyBtn: ColorButton
        private val mReplaceBtn: ColorButton
        private val mDeleteBtn: ColorButton
        private val mResetBtn: ColorButton
        private val mSaveBtn: ColorButton
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
        private val mTextFieldBg: Color
        private var mIsChanged = false

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
            mFormatTable.columnModel.getColumn(2).preferredWidth = 120
            mFormatTable.columnModel.getColumn(3).preferredWidth = 160
            mFormatTable.columnModel.getColumn(4).preferredWidth = 220
            mFormatTable.columnModel.getColumn(5).preferredWidth = 50
            mFormatTable.selectionModel.addListSelectionListener(ListSelectionHandler())
            updateRowHeights()

            mNamePanel.layout = FlowLayout(FlowLayout.LEFT)
            mNameLabel.text = Strings.NAME
            mNameTF.preferredSize = Dimension(150, mNameTF.preferredSize.height)
            mNameTF.text = ""
            mTextFieldBg = mNameTF.background
            mSeparatorLabel.text = Strings.SEPARATOR
            mSeparatorTF.text = ""
            mSeparatorTF.preferredSize = Dimension(100, mSeparatorTF.preferredSize.height)
            mLevelNthLabel.text = Strings.LEVEL_NTH
            mLevelNthTF.text = "-1"
            mPidTokIdxLabel.text = "${Strings.PID_TOKEN}(${Strings.OPTIONAL})"
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

            val inUsePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val inUseLabel = JLabel(" ${Strings.IN_USE} - [${mCurrFormat.mName}] ")
            inUsePanel.add(inUseLabel)
            mFormatPanel.add(inUsePanel)
            mFormatPanel.add(mScrollPane)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " ${Strings.LOG_FORMAT} ")
            mFormatPanel.add(mNamePanel)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " ${Strings.LEVELS} ")
            mFormatPanel.add(mLevelsPanel)
            addHEmptySeparator(mFormatPanel, 20)
            addHSeparator(mFormatPanel, " ${Strings.TOKENS} ")
            mFormatPanel.add(mTokensPanel)

            mFirstBtn = ColorButton("↑")
            mFirstBtn.addActionListener(this)
            mPrevBtn = ColorButton("∧")
            mPrevBtn.addActionListener(this)
            mNextBtn = ColorButton("∨")
            mNextBtn.addActionListener(this)
            mLastBtn = ColorButton("↓")
            mLastBtn.addActionListener(this)

            mAddBtn = ColorButton(Strings.ADD)
            mAddBtn.addActionListener(this)
            mCopyBtn = ColorButton(Strings.COPY)
            mCopyBtn.addActionListener(this)
            mReplaceBtn = ColorButton(Strings.REPLACE)
            mReplaceBtn.addActionListener(this)
            mDeleteBtn = ColorButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mResetBtn = ColorButton(Strings.RESET)
            mResetBtn.addActionListener(this)
            mSaveBtn = ColorButton(Strings.SAVE)
            mSaveBtn.addActionListener(this)
            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)

            val bottomPanel = JPanel()
            bottomPanel.add(mFirstBtn)
            bottomPanel.add(mPrevBtn)
            bottomPanel.add(mNextBtn)
            bottomPanel.add(mLastBtn)
            Utils.addVSeparator(bottomPanel, 20)
            bottomPanel.add(mAddBtn)
            bottomPanel.add(mCopyBtn)
            bottomPanel.add(mReplaceBtn)
            bottomPanel.add(mDeleteBtn)
            bottomPanel.add(mResetBtn)
            Utils.addVSeparator(bottomPanel, 20)
            bottomPanel.add(mSaveBtn)
            bottomPanel.add(mCloseBtn)

            val panel = JPanel(BorderLayout())
            panel.add(mFormatPanel, BorderLayout.CENTER)
            panel.add(bottomPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    if (mIsChanged) {
                        val dialogResult = JOptionPane.showConfirmDialog(this@FormatListDialog, Strings.VAL_CHANGE_SAVE, "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
                        when (dialogResult) {
                            JOptionPane.YES_OPTION -> {
                                copyFormatList(mDialogFormatList, mFormatList)
                                saveList()
                                mIsChanged = false
                                dispose()
                            }
                            JOptionPane.NO_OPTION -> {
                                dispose()
                            }
                            else -> {
                                // do nothing
                            }
                        }
                    }
                    else {
                        dispose()
                    }
                }

                override fun windowClosed(e: WindowEvent?) {
                    if (mFormatList.isEmpty()) {
                        addDefaultFormats(mFormatList)
                    }
                    setCurrFormat(mCurrFormat.mName)

                    super.windowClosed(e)
                }
            })
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

        private fun isValidFormatItem(): Boolean {
            var isValid = true
            val name = mNameTF.text.trim()
            if (name.isEmpty()) {
                mNameTF.background = Color.RED
                mNameTF.toolTipText = TooltipStrings.INVALID_NAME
                isValid = false
            }
            else {
                mNameTF.background = mTextFieldBg
                mNameTF.toolTipText = ""
            }

            try {
                mLevelNthTF.text.toInt()
                mLevelNthTF.background = mTextFieldBg
                mLevelNthTF.toolTipText = ""
            } catch (ex: NumberFormatException) {
                mLevelNthTF.background = Color.RED
                mLevelNthTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
                isValid = false
            }

            for (idx in 0 until MAX_TOKEN_COUNT) {
                try {
                    mTokenArr[idx].getToken()
                } catch (ex: Exception) {
                    isValid = false
                }
            }

            return isValid
        }

        private fun makeFormatItem(): FormatItem {
            if (!isValidFormatItem()) {
                throw Exception()
            }

            val name = mNameTF.text.trim()
            val separator = mSeparatorTF.text
            val levels = emptyMap<String, Int>().toMutableMap()
            for (idx in 1 until TEXT_LEVEL.size) {
                val level = mLevelsTFArr[idx].text.trim()
                if (level.isNotEmpty()) {
                    levels[level] = idx
                }
            }

            val levelNth = mLevelNthTF.text.toInt()
            val tokens = Array(MAX_TOKEN_COUNT) { mTokenArr[it].getToken() }
            val pidTokIdx: Int = if (mPidTokIdxCombo.selectedItem == null) {
                -1
            } else {
                mPidTokIdxCombo.selectedItem!!.toString().toInt()
            }

            return FormatItem(name, separator, levelNth, levels, tokens, pidTokIdx)
        }

        private fun isExistFormat(name: String): Boolean {
            var isExist = false
            for (idx in 0 until mDialogFormatList.size) {
                if (name == mDialogFormatList[idx].mName) {
                    isExist = true
                    break
                }
            }

            return isExist
        }

        override fun actionPerformed(e: ActionEvent?) {
            when (e?.source) {
                mFirstBtn -> {
                    val selectedIdx = mFormatTable.selectedRow
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx) {
                        mIsChanged = true
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(0, format)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(0, 0)
                    }
                }
                mPrevBtn -> {
                    val selectedIdx = mFormatTable.selectedRow
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx && selectedIdx > 0) {
                        mIsChanged = true
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(selectedIdx - 1, format)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(selectedIdx - 1, selectedIdx - 1)
                    }
                }
                mNextBtn -> {
                    val selectedIdx = mFormatTable.selectedRow
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx && selectedIdx < (mDialogFormatList.size - 1)) {
                        mIsChanged = true
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(selectedIdx + 1, format)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(selectedIdx + 1, selectedIdx + 1)
                    }
                }
                mLastBtn -> {
                    val selectedIdx = mFormatTable.selectedRow
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx) {
                        mIsChanged = true
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(mDialogFormatList.size, format)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                    }
                }

                mAddBtn -> {
                    val format: FormatItem
                    try {
                        format = makeFormatItem()
                        val isExist = isExistFormat(format.mName)
                        if (isExist) {
                            JOptionPane.showMessageDialog(this, "${Strings.INVALID_NAME_EXIST} \"${format.mName}\"", "Error", JOptionPane.ERROR_MESSAGE)
                        }
                        else {
                            mIsChanged = true
                            mDialogFormatList.add(format)
                            mFormatTableModel.fireTableDataChanged()
                            updateRowHeights()
                            mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                            mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.getRowCount() - 1, 0, true))
                        }
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }

                mCopyBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row < 0 || row >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$row\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    mIsChanged = true
                    var num = 2
                    var name = mDialogFormatList[row].mName + " - $num"

                    while (true) {
                        if (!isExistFormat(name)) {
                            break
                        }
                        num++
                        name = mDialogFormatList[row].mName + " - $num"
                    }

                    val format: FormatItem = mDialogFormatList[row].copy(mName = name)
                    mDialogFormatList.add(format)
                    mFormatTableModel.fireTableDataChanged()
                    updateRowHeights()
                    mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                    mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.getRowCount() - 1, 0, true))
                }

                mReplaceBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row < 0 || row >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$row\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    val format: FormatItem
                    try {
                        format = makeFormatItem()
                        val isExist = isExistFormat(format.mName)
                        if (isExist && mDialogFormatList[row].mName != format.mName) {
                            JOptionPane.showMessageDialog(this, "${Strings.INVALID_NAME_EXIST} \"${format.mName}\"", "Error", JOptionPane.ERROR_MESSAGE)
                        }
                        else {
                            mIsChanged = true
                            mDialogFormatList.removeAt(row)
                            mDialogFormatList.add(row, format)
                            mFormatTableModel.fireTableDataChanged()
                            updateRowHeights()
                            mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                            mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.getRowCount() - 1, 0, true))
                        }
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }

                mDeleteBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row >= 0 && row < mDialogFormatList.size) {
                        mIsChanged = true
                        mDialogFormatList.removeAt(row)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                    }
                }

                mResetBtn -> {
                    mIsChanged = true
                    mDialogFormatList.clear()
                    addDefaultFormats(mDialogFormatList)
                    mFormatTableModel.fireTableDataChanged()
                    updateRowHeights()
                }

                mSaveBtn -> {
                    copyFormatList(mDialogFormatList, mFormatList)
                    saveList()
                    mIsChanged = false
                    JOptionPane.showMessageDialog(this, "${Strings.SAVED_FORMAT_LIST} ($FORMATS_LIST_FILE)", "Info", JOptionPane.INFORMATION_MESSAGE)
                }
                mCloseBtn -> {
                    dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
                }
            }
        }
    }
}
