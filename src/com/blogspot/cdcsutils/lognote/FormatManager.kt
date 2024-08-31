package com.blogspot.cdcsutils.lognote

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


class FormatManager private constructor(fileName: String) : PropertiesBase(fileName) {
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

    data class FormatItem(
        val mName: String,
        val mSeparator: String,
        val mTokenCount: Int,
        val mLogNth: Int,
        val mColumnNames: String,
        val mLevelNth: Int,
        val mLevels: Map<String, Int>,
        val mTokenFilters: Array<TokenFilterItem>,
        val mPidTokIdx: Int
    ) {
        data class TokenFilterItem(val mToken: String, val mNth: Int, val mIsSaveFilter: Boolean, var mUiWidth: Int)

        val mSortedTokenFilters: Array<out TokenFilterItem> =
            mTokenFilters.sortedArrayWith { t1: TokenFilterItem, t2: TokenFilterItem -> t1.mNth - t2.mNth }
        val mSortedTokensIdxs = Array(MAX_TOKEN_FILTER_COUNT) { -1 }
        val mSortedPidTokIdx: Int

        init {
            if (mPidTokIdx == -1) {
                mSortedPidTokIdx = -1
            } else {
                val tokenFilterNth = mTokenFilters[mPidTokIdx].mNth
                var tokIdx = -1
                if (tokenFilterNth >= 0) {
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        if (tokenFilterNth == mSortedTokenFilters[idx].mNth) {
                            tokIdx = idx
                            break
                        }
                    }
                }
                mSortedPidTokIdx = tokIdx
            }

            for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                for (idxSorted in 0 until MAX_TOKEN_FILTER_COUNT) {
                    if (mTokenFilters[idx].mToken == mSortedTokenFilters[idxSorted].mToken) {
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
        const val ITEM_TOKEN_COUNT = "_TOKEN_COUNT"
        const val ITEM_LOG_NTH = "_LOG_NTH"
        const val ITEM_COLUMN_NAMES = "_COLUMN_NAMES"
        const val ITEM_LEVEL = "_LEVEL_"
        const val ITEM_LEVEL_NTH = "_LEVEL_NTH"
        const val ITEM_TOKEN_FILTER_NAME = "_TOKEN_NAME_"
        const val ITEM_TOKEN_FILTER_NTH = "_TOKEN_NTH_"
        const val ITEM_TOKEN_SAVE_FILTER = "_TOKEN_SAVE_FILTER_"
        const val ITEM_TOKEN_UI_WIDTH = "_TOKEN_UI_WIDTH_"
        const val ITEM_PID_TOK_IDX = "_PID_TOK_IDX"

        const val MAX_FORMAT_COUNT = 50
        const val MAX_TOKEN_FILTER_COUNT = 3

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

        verifyRepairFormats()

        mCurrFormat = mFormatList[0]
        val logFormat = mConfigManager.getItem(ConfigManager.ITEM_LOG_FORMAT)
        if (logFormat != null) {
            setCurrFormat(logFormat.toString().trim())
        } else {
            setCurrFormat("")
        }
    }

    private fun verifyRepairFormats() {
        val formatList = mutableListOf<FormatItem>()
        var maxColumnNth = 0
        var tokenCount = 0
        var logNth = 0
        var levelNth = -1
        var isUpdated = false
        for (format in mFormatList) {
            maxColumnNth = 0
            tokenCount = 0
            logNth = 0
            levelNth = -1
            isUpdated = false

            if (format.mColumnNames.isNotEmpty()) {
                tokenCount = format.mTokenCount
                logNth = format.mLogNth
                levelNth = format.mLevelNth
                val nameArr = format.mColumnNames.split("|")
                if (nameArr.isNotEmpty()) {
                    nameArr.forEach {
                        val nameSplit = it.split(",")
                        if (nameSplit.size == 3) {
                            val nth = nameSplit[1].toInt()
                            if (maxColumnNth < nth) {
                                maxColumnNth = nth
                            }
                        }
                    }
                }

                for (tokenFilter in format.mTokenFilters) {
                    if (maxColumnNth < tokenFilter.mNth) {
                        maxColumnNth = tokenFilter.mNth
                    }
                }

                if (tokenCount <= maxColumnNth) {
                    tokenCount = maxColumnNth + 1
                    isUpdated = true
                }
                if (tokenCount <= logNth) {
                    logNth = maxColumnNth
                    isUpdated = true
                }
                if (tokenCount <= levelNth) {
                    levelNth = -1
                    isUpdated = true
                }
            }
            if (isUpdated) {
                formatList.add(format.copy(mTokenCount = tokenCount, mLogNth = logNth, mLevelNth = levelNth))
            }
        }

        if (formatList.isNotEmpty()) {
            for (newFormat in formatList) {
                for (idx in 0 until mFormatList.size) {
                    if (newFormat.mName == mFormatList[idx].mName) {
                        mFormatList.removeAt(idx)
                        mFormatList.add(idx, newFormat)
                    }
                }
            }
            formatList.clear()
            saveList()
        }
    }

    private fun addDefaultFormats(formatList: MutableList<FormatItem>) {
        var separator = "\\s+:\\s+|:?\\s+"
        var tokenCount = 7
        var logNth = 6
        var columnNames = "Date,0,50|Time,1,100|PID,2,50|TID,3,50|Level,4,15|Tag,5,150|Log,6,-1"
        var levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        var levelNth = 4

        var tokenFilters: Array<FormatItem.TokenFilterItem> = arrayOf(
            FormatItem.TokenFilterItem("Tag", 5, true, 250),
            FormatItem.TokenFilterItem("PID", 2, false, 120),
            FormatItem.TokenFilterItem("TID", 3, false, 120),
        )
        var pidTokIdx = 1
        formatList.add(0, FormatItem("logcat", separator, tokenCount, logNth, columnNames, levelNth, levels, tokenFilters, pidTokIdx))

        // case plain text
        separator = ""
        tokenCount = 1
        logNth = 0
        columnNames = "Log,0,-1"
        levels = mapOf()
        levelNth = -1

        tokenFilters = arrayOf(
            FormatItem.TokenFilterItem("", 0, false, 0),
            FormatItem.TokenFilterItem("", 0, false, 0),
            FormatItem.TokenFilterItem("", 0, false, 0),
        )
        pidTokIdx = -1
        formatList.add(FormatItem("plain text", separator, tokenCount, logNth, columnNames, levelNth, levels, tokenFilters, pidTokIdx))

        // case logcat -v time
        separator = "/|\\s+\\(\\s+|\\s+\\(|\\(\\s+|\\(|\\s+|\\):?\\s+"
        tokenCount = 6
        logNth = 5
        columnNames = "Date,0,50|Time,1,100|Level,2,15|Tag,3,150|PID,4,50|Log,5,-1"
        levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        levelNth = 2

        tokenFilters = arrayOf(
            FormatItem.TokenFilterItem("", 0, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
        )
        pidTokIdx = -1
        formatList.add(FormatItem("logcat -v time", separator, tokenCount, logNth, columnNames, levelNth, levels, tokenFilters, pidTokIdx))
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
            val tokenCount = try {
                ((mProperties["$i$ITEM_TOKEN_COUNT"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                1
            }
            val logNth = try {
                ((mProperties["$i$ITEM_LOG_NTH"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                0
            }
            val columnNames = (mProperties["$i$ITEM_COLUMN_NAMES"] ?: "") as String
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

            var tokenFilters: Array<FormatItem.TokenFilterItem>
            try {
                tokenFilters = Array(MAX_TOKEN_FILTER_COUNT) {
                    val tokenName = ((mProperties["$i$ITEM_TOKEN_FILTER_NAME$it"] ?: "") as String).trim()
                    val nth = try {
                        ((mProperties["$i$ITEM_TOKEN_FILTER_NTH$it"] ?: "") as String).toInt()
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

                    FormatItem.TokenFilterItem(tokenName, nth, isSaveFilter, uiWidth)
                }
            } catch (ex: Exception) {
                Utils.printlnLog("Failed load format($name) tokens")
                ex.printStackTrace()
                tokenFilters = arrayOf(
                    FormatItem.TokenFilterItem("", 0, false, 120),
                    FormatItem.TokenFilterItem("", 0, false, 120),
                    FormatItem.TokenFilterItem("", 0, false, 120),
                )
            }

            val pidTokIdx = try {
                ((mProperties["$i$ITEM_PID_TOK_IDX"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                -1
            }

            mFormatList.add(FormatItem(name, separator, tokenCount, logNth, columnNames, levelNth, levels, tokenFilters, pidTokIdx))
        }
    }

    private fun saveList() {
        mProperties.clear()
        var format: FormatItem
        for (i in 0 until mFormatList.size) {
            format = mFormatList[i]
            mProperties["$i$ITEM_NAME"] = format.mName
            mProperties["$i$ITEM_SEPARATOR"] = format.mSeparator
            mProperties["$i$ITEM_TOKEN_COUNT"] = format.mTokenCount.toString()
            mProperties["$i$ITEM_LOG_NTH"] = format.mLogNth.toString()
            mProperties["$i$ITEM_COLUMN_NAMES"] = format.mColumnNames
            val keyList = format.mLevels.keys.toList()
            for (key in keyList) {
                val level = format.mLevels[key]
                if (level != null) {
                    mProperties["$i$ITEM_LEVEL$level"] = key
                }
            }

            mProperties["$i$ITEM_LEVEL_NTH"] = format.mLevelNth.toString()

            for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                mProperties["$i$ITEM_TOKEN_FILTER_NAME$idx"] = format.mTokenFilters[idx].mToken
                mProperties["$i$ITEM_TOKEN_FILTER_NTH$idx"] = format.mTokenFilters[idx].mNth.toString()
                mProperties["$i$ITEM_TOKEN_SAVE_FILTER$idx"] = format.mTokenFilters[idx].mIsSaveFilter.toString()
                mProperties["$i$ITEM_TOKEN_UI_WIDTH$idx"] = format.mTokenFilters[idx].mUiWidth.toString()
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
        if (mCurrFormat.mName == selectedItem) {
            Utils.printlnLog("current format not changed(${mCurrFormat.mName})")
            return
        }
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
                    foreground = table.selectionForeground
                    background = table.selectionBackground
                } else {
                    foreground = table.foreground
                    background = table.background
                }
                text = value.toString()
                return this
            }
        }

        inner class FormatTableModel() : DefaultTableModel() {
            private val colNames = arrayOf(Strings.NAME, Strings.SEPARATOR, "${Strings.COLUMN} ${Strings.NAME}", Strings.LEVEL_NTH, Strings.LEVELS, Strings.TOKENS, Strings.PID_TOKEN_FILTER)
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
                        var names = "<font color=#0000FF>Token count : ${format.mTokenCount}</font><br>"
                        names += "<font color=#0000FF>${Strings.LOG} ${Strings.NTH}: ${format.mLogNth}</font><br>"
                        if (format.mColumnNames.isNotEmpty()) {
                            val nameArr = format.mColumnNames.split("|")
                            if (nameArr.isNotEmpty()) {
                                nameArr.forEach {
                                    val nameSplit = it.split(",")
                                    names += if (nameSplit.size == 3) {
                                        "${nameSplit[0]}, nth:${nameSplit[1]}, width:${nameSplit[2]}<br>"
                                    } else {
                                        "$it, nth:-1, width:0<br>"
                                    }
                                }
                            }
                        }
                        return "<html>${names}</html>"
                    }
                    3 -> {
                        return "<html><center>${format.mLevelNth}</center></html>"
                    }
                    4 -> {
                        var levels = ""
                        format.mLevels.forEach { levels += "${TEXT_LEVEL[it.value]} : ${it.key}<br>" }
                        return "<html>${levels}</html>"
                    }
                    5 -> {
                        var tokens = ""
                        var idx = 0
                        format.mTokenFilters.forEach {
                            tokens += "idx $idx : ${it.mToken}, ${it.mNth}, ${it.mIsSaveFilter}, ${it.mUiWidth}<br>"
                            idx++
                        }
                        return "<html>${tokens}</html>"
                    }
                    6 -> {
                        return "<html><center>${format.mPidTokIdx}</center></html>"
                    }
                }
                return super.getValueAt(row, column)
            }
        }

        internal inner class ListSelectionHandler : ListSelectionListener {
            private var mSelectedRow = -1
            override fun valueChanged(p0: ListSelectionEvent?) {
                if (mDialogFormatList.size > 0 && mFormatTable.selectedRow >= 0 && mSelectedRow != mFormatTable.selectedRow) {
                    mSelectedRow = mFormatTable.selectedRow
                    mDetailPanel.setFormat(mDialogFormatList[mSelectedRow])
                }

                return
            }
        }

        inner class TokenFilterPanel(val idx: Int) : JPanel() {
            private val mTokenLabel = JLabel()
            private val mTokenTF = JTextField()
            private val mNthLabel = JLabel()
            private val mNthTF = JTextField()
            private val mIsSaveFilterLabel = JLabel()
            private val mIsSaveFilterCheck = JCheckBox()
            private val mUiWidthLabel = JLabel()
            private val mUiWidthTF = JTextField()
            private val mIdx = idx

            fun setIsEditable(enabled: Boolean) {
                mTokenTF.isEditable = enabled
                mNthTF.isEditable = enabled
                mIsSaveFilterCheck.isEnabled = enabled
                mUiWidthTF.isEditable = enabled
            }

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
            
            fun setToken(tokenFilterItem: FormatItem.TokenFilterItem) {
                mTokenTF.text = tokenFilterItem.mToken
                mNthTF.text = tokenFilterItem.mNth.toString()
                mIsSaveFilterCheck.isSelected = tokenFilterItem.mIsSaveFilter
                mUiWidthTF.text = tokenFilterItem.mUiWidth.toString()
            }
            
            fun getToken(): FormatItem.TokenFilterItem {
                var isValid = true
                val name = mTokenTF.text.trim()

                var nth: Int = 0
                try {
                    nth = mNthTF.text.toInt()
                    mNthTF.background = mDetailPanel.mTextFieldBg
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
                    mUiWidthTF.background = mDetailPanel.mTextFieldBg
                    mUiWidthTF.toolTipText = ""
                } catch (ex: NumberFormatException) {
                    mUiWidthTF.background = Color.RED
                    mUiWidthTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
                    isValid = false
                }

                if (!isValid) {
                    throw NumberFormatException()
                }

                return FormatItem.TokenFilterItem(name, nth, isSaveFilter, uiWidth)
            }
        }

        inner class DetailPanel(isEditable: Boolean) : JPanel() {
//            val mRow = row
            private val mNameLabel = JLabel()
            val mNameTF = JTextField()
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
            private val mTokenFilterArr = Array(MAX_TOKEN_FILTER_COUNT) { TokenFilterPanel(it) }
            private val mTokenFiltersPanel = JPanel()
            val mTextFieldBg: Color
            private val mColumnNamesPanel = JPanel()
            private val mTokenCountLabel = JLabel()
            private val mTokenCountTF = JTextField()
            private val mLogNthLabel = JLabel()
            private val mLogNthTF = JTextField()
            private val mColumnNamesLabel = JLabel()
            private val mColumnNamesTF = JTextField()

            init {
                mNamePanel.layout = FlowLayout(FlowLayout.LEFT)
                mNameLabel.text = Strings.NAME
                mNameTF.preferredSize = Dimension(150, mNameTF.preferredSize.height)
                mNameTF.text = ""
                mTextFieldBg = mNameTF.background
                mSeparatorLabel.text = Strings.SEPARATOR
                mSeparatorTF.text = ""
                mSeparatorTF.preferredSize = Dimension(100, mSeparatorTF.preferredSize.height)
                mTokenCountLabel.text = Strings.TOKEN_COUNT
                mTokenCountTF.preferredSize = Dimension(70, mTokenCountTF.preferredSize.height)
                mLevelNthLabel.text = Strings.LEVEL_NTH
                mLevelNthTF.text = "-1"
                mPidTokIdxLabel.text = "${Strings.PID_TOKEN_FILTER}(${Strings.PID_TOKEN_FILTER_OPTIONAL})"
                for (idx in -1 until MAX_TOKEN_FILTER_COUNT) {
                    mPidTokIdxCombo.addItem("$idx")
                }

                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mNameLabel)
                mNamePanel.add(mNameTF)
                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mSeparatorLabel)
                mNamePanel.add(mSeparatorTF)
                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mTokenCountLabel)
                mNamePanel.add(mTokenCountTF)
                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mLevelNthLabel)
                mNamePanel.add(mLevelNthTF)

                mLevelsPanel.layout = FlowLayout(FlowLayout.LEFT)
                mLevelsPanel.add(JLabel("   "))
                for (idx in 1 until TEXT_LEVEL.size) {
                    mLevelsPanel.add(mLevelsLabelArr[idx])
                    mLevelsPanel.add(mLevelsTFArr[idx])
                    mLevelsPanel.add(JLabel("   "))
                }

                mTokenFiltersPanel.layout = BoxLayout(mTokenFiltersPanel, BoxLayout.Y_AXIS)
                for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                    mTokenFiltersPanel.add(mTokenFilterArr[idx])
                }

                val pidTokPanel = JPanel()
                pidTokPanel.layout = FlowLayout(FlowLayout.LEFT)
                pidTokPanel.add(JLabel("   "))
                pidTokPanel.add(mPidTokIdxLabel)
                pidTokPanel.add(mPidTokIdxCombo)
                mTokenFiltersPanel.add(pidTokPanel)

                mColumnNamesPanel.layout = FlowLayout(FlowLayout.LEFT)
                mColumnNamesPanel.add(JLabel("   "))
                mLogNthLabel.text = "${Strings.LOG} ${Strings.NTH}"
                mLogNthTF.preferredSize = Dimension(40, mLogNthTF.preferredSize.height)
                mColumnNamesPanel.add(mLogNthLabel)
                mColumnNamesPanel.add(mLogNthTF)
                mColumnNamesLabel.text = "    ${Strings.COLUMN}"
                mColumnNamesTF.preferredSize = Dimension(600, mColumnNamesTF.preferredSize.height)
                mColumnNamesPanel.add(mColumnNamesLabel)
                mColumnNamesPanel.add(mColumnNamesTF)

                if (!isEditable) {
                    mNameTF.isEditable = false
                    mSeparatorTF.isEditable = false
                    mLevelNthTF.isEditable = false
                    mPidTokIdxCombo.isEnabled = false
                    for (idx in TEXT_LEVEL.indices) {
                        mLevelsTFArr[idx].isEditable = false
                    }
                    mTokenCountTF.isEditable = false
                    mLogNthTF.isEditable = false
                    mColumnNamesTF.isEditable = false
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        mTokenFilterArr[idx].setIsEditable(false)
                    }
                }

                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                Utils.addHSeparator(this, " ${Strings.LOG_FORMAT} ")
                add(mNamePanel)
                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.LEVELS} ")
                add(mLevelsPanel)
                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.COLUMN} ${Strings.NAME} (${Strings.USED_COLUMN_VIEW_MODE})")
                add(mColumnNamesPanel)
                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.TOKENS} ${Strings.FILTERS}")
                add(mTokenFiltersPanel)
            }

            fun setFormat(format: FormatItem) {
                if (format.mName.isNotEmpty()) {
                    mNameTF.text = format.mName
                    mSeparatorTF.text = format.mSeparator
                    mTokenCountTF.text = format.mTokenCount.toString()
                    mLogNthTF.text = format.mLogNth.toString()
                    mColumnNamesTF.text = format.mColumnNames
                    mLevelNthTF.text = format.mLevelNth.toString()
                    mPidTokIdxCombo.selectedItem = format.mPidTokIdx.toString()
                    for (idx in TEXT_LEVEL.indices) {
                        mLevelsTFArr[idx].text = ""
                    }
                    format.mLevels.forEach { mLevelsTFArr[it.value].text = it.key }
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        mTokenFilterArr[idx].setToken(format.mTokenFilters[idx])
                    }
                }
            }

            fun getFormat(): FormatItem {
                return mDialogFormatList[0]
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

                for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                    try {
                        mTokenFilterArr[idx].getToken()
                    } catch (ex: Exception) {
                        isValid = false
                    }
                }

                return isValid
            }

            fun makeFormatItem(): FormatItem {
                if (!isValidFormatItem()) {
                    throw Exception()
                }

                val name = mNameTF.text.trim()
                val separator = mSeparatorTF.text
                val tokenCount = mTokenCountTF.text.toInt()
                val logNth = mLogNthTF.text.toInt()
                val columnNames = mColumnNamesTF.text
                val levels = emptyMap<String, Int>().toMutableMap()
                for (idx in 1 until TEXT_LEVEL.size) {
                    val level = mLevelsTFArr[idx].text.trim()
                    if (level.isNotEmpty()) {
                        levels[level] = idx
                    }
                }

                val levelNth = mLevelNthTF.text.toInt()
                val tokens = Array(MAX_TOKEN_FILTER_COUNT) { mTokenFilterArr[it].getToken() }
                val pidTokIdx: Int = if (mPidTokIdxCombo.selectedItem == null) {
                    -1
                } else {
                    mPidTokIdxCombo.selectedItem!!.toString().toInt()
                }

                return FormatItem(name, separator, tokenCount, logNth, columnNames, levelNth, levels, tokens, pidTokIdx)
            }
        }

        private val mFirstBtn: ColorButton
        private val mPrevBtn: ColorButton
        private val mNextBtn: ColorButton
        private val mLastBtn: ColorButton

        private val mAddBtn: ColorButton
        private val mCopyBtn: ColorButton
        private val mEditBtn: ColorButton
        private val mDeleteBtn: ColorButton
        private val mResetBtn: ColorButton
        private val mOkBtn: ColorButton
        private val mCancelBtn: ColorButton
        private val mFormatPanel = JPanel()
        private val mFormatTableModel = FormatTableModel()
        private val mFormatTable = JTable(mFormatTableModel)
        private val mScrollPane = JScrollPane(mFormatTable)
        private val mDetailPanel = DetailPanel(false)
        private var mIsChanged = false

        init {
            mScrollPane.preferredSize = Dimension(1200, 500)
            mScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

            mScrollPane.isOpaque = false
            mScrollPane.viewport.isOpaque = false

            mFormatTable.setDefaultRenderer(String::class.java, MultiLineCellRenderer())
            mFormatTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            mFormatTable.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            mFormatTable.columnModel.getColumn(0).preferredWidth = 60
            mFormatTable.columnModel.getColumn(1).preferredWidth = 50
            mFormatTable.columnModel.getColumn(2).preferredWidth = 200
            mFormatTable.columnModel.getColumn(3).preferredWidth = 120
            mFormatTable.columnModel.getColumn(4).preferredWidth = 160
            mFormatTable.columnModel.getColumn(5).preferredWidth = 220
            mFormatTable.columnModel.getColumn(6).preferredWidth = 50
            mFormatTable.selectionModel.addListSelectionListener(ListSelectionHandler())
            updateRowHeights()

            mFormatPanel.layout = BoxLayout(mFormatPanel, BoxLayout.Y_AXIS)

            val inUsePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val inUseLabel = JLabel(" ${Strings.IN_USE} - [${mCurrFormat.mName}] ")
            inUsePanel.add(inUseLabel)
            mFormatPanel.add(inUsePanel)
            mFormatPanel.add(mScrollPane)

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
            mEditBtn = ColorButton(Strings.EDIT)
            mEditBtn.addActionListener(this)
            mDeleteBtn = ColorButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mResetBtn = ColorButton(Strings.RESET)
            mResetBtn.addActionListener(this)
            val buttonPanel = JPanel()
            buttonPanel.add(mAddBtn)
            buttonPanel.add(mCopyBtn)
            buttonPanel.add(mEditBtn)
            buttonPanel.add(mDeleteBtn)
            buttonPanel.add(mResetBtn)
            Utils.addVSeparator(buttonPanel, 20)
            buttonPanel.add(mFirstBtn)
            buttonPanel.add(mPrevBtn)
            buttonPanel.add(mNextBtn)
            buttonPanel.add(mLastBtn)

            mFormatPanel.add(buttonPanel)
            mFormatPanel.add(mDetailPanel)
            Utils.addHEmptySeparator(mFormatPanel, 20)

            mOkBtn = ColorButton(Strings.OK)
            mOkBtn.addActionListener(this)
            mCancelBtn = ColorButton(Strings.CANCEL)
            mCancelBtn.addActionListener(this)

            val bottomPanel = JPanel()
            bottomPanel.add(mOkBtn)
            bottomPanel.add(mCancelBtn)

            val panel = JPanel(BorderLayout())
            panel.add(mFormatPanel, BorderLayout.CENTER)
            panel.add(bottomPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
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
                    val editDialog = FormatEditDialog(this, Strings.ADD, "ADD")
                    editDialog.setLocationRelativeTo(parent)
                    editDialog.isVisible = true

                    editDialog.mFormat?.let {
                        mIsChanged = true
                        mDialogFormatList.add(it)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                        mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.rowCount - 1, 0, true))
                    }
                }

                mCopyBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row < 0 || row >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$row\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

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
                    val editDialog = FormatEditDialog(this, Strings.COPY, "COPY")
                    editDialog.setFormat(format)
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true

                    editDialog.mFormat?.let {
                        mIsChanged = true
                        mDialogFormatList.add(it)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                        mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.rowCount - 1, 0, true))
                    }
                }


                mEditBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row < 0 || row >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$row\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    val editDialog = FormatEditDialog(this, Strings.EDIT, "EDIT")
                    editDialog.setFormat(mDialogFormatList[row])
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true

                    editDialog.mFormat?.let {
                        mIsChanged = true
                        mDialogFormatList.removeAt(row)
                        mDialogFormatList.add(row, it)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                        mFormatTable.setRowSelectionInterval(mDialogFormatList.size - 1, mDialogFormatList.size - 1)
                        mFormatTable.scrollRectToVisible(mFormatTable.getCellRect(mFormatTable.rowCount - 1, 0, true))
                    }
                }

                mDeleteBtn -> {
                    val row = mFormatTable.selectedRow
                    if (row < 0 || row >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$row\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    val dialogResult = JOptionPane.showConfirmDialog(this, String.format(Strings.CONFIRM_DELETE_FORMAT, mDialogFormatList[row].mName), "Warning", JOptionPane.YES_NO_OPTION)
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        mIsChanged = true
                        mDialogFormatList.removeAt(row)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                    }
                }

                mResetBtn -> {
                    val dialogResult = JOptionPane.showConfirmDialog(this, Strings.CONFIRM_RESET_FORMAT_LIST, "Warning", JOptionPane.YES_NO_OPTION)
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        mIsChanged = true
                        mDialogFormatList.clear()
                        addDefaultFormats(mDialogFormatList)
                        mFormatTableModel.fireTableDataChanged()
                        updateRowHeights()
                    }
                }

                mOkBtn -> {
                    copyFormatList(mDialogFormatList, mFormatList)
                    saveList()
                    mIsChanged = false
                    JOptionPane.showMessageDialog(this, "${Strings.SAVED_FORMAT_LIST} ($FORMATS_LIST_FILE)", "Info", JOptionPane.INFORMATION_MESSAGE)
                    dispose()
                }
                mCancelBtn -> {
                    dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
                }
            }
        }

        inner class FormatEditDialog(parent: JDialog, title: String, cmd: String) : JDialog(parent, title, true), ActionListener {
            private val mDetailPanel = DetailPanel(true)
            private val mOkBtn: ColorButton
            private val mCancelBtn: ColorButton
            private val mFormatPanel = JPanel()
            private val mCmd = cmd
            var mFormat: FormatItem? = null

            init {
                mFormatPanel.layout = BoxLayout(mFormatPanel, BoxLayout.Y_AXIS)

                if (cmd == "EDIT") {
                    mDetailPanel.mNameTF.isEditable = false
                }
                mFormatPanel.add(mDetailPanel)
                Utils.addHEmptySeparator(mFormatPanel, 20)

                mOkBtn = ColorButton(Strings.OK)
                mOkBtn.addActionListener(this)
                mCancelBtn = ColorButton(Strings.CANCEL)
                mCancelBtn.addActionListener(this)

                val bottomPanel = JPanel()
                bottomPanel.add(mOkBtn)
                bottomPanel.add(mCancelBtn)

                val panel = JPanel(BorderLayout())
                panel.add(mFormatPanel, BorderLayout.CENTER)
                panel.add(bottomPanel, BorderLayout.SOUTH)

                contentPane.add(panel)
                pack()
            }

            fun setFormat(format: FormatItem) {
                mDetailPanel.setFormat(format)
            }

            override fun actionPerformed(e: ActionEvent?) {
                when (e?.source) {
                    mOkBtn -> {
                        mFormat = null
                        when (mCmd) {
                            "ADD", "COPY" -> {
                                var format: FormatItem? = null
                                try {
                                    format = mDetailPanel.makeFormatItem()
                                    val isExist = isExistFormat(format.mName)
                                    if (isExist) {
                                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_NAME_EXIST} \"${format.mName}\"", "Error", JOptionPane.ERROR_MESSAGE)
                                    }
                                    else {
                                        mFormat = format
                                    }
                                } catch (ex: Exception) {
                                    JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
                                }
                            }
                            "EDIT" -> {
                                var format: FormatItem? = null
                                try {
                                    format = mDetailPanel.makeFormatItem()
                                    mFormat = format
                                } catch (ex: Exception) {
                                    JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
                                }
                            }
                        }

                        if (mFormat != null) {
                            dispose()
                        }
                    }
                    mCancelBtn -> {
                        mFormat = null
                        dispose()
                    }
                }
            }
        }
    }
}
