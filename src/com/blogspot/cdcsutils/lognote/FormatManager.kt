package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


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
        val mLogPosition: Int,
        val mColumnNames: String,
        val mLevelPosition: Int,
        val mLevels: Map<String, Int>,
        val mTokenFilters: Array<TokenFilterItem>,
        val mPidTokIdx: Int,
        val mSampleText: String
    ) {
        data class TokenFilterItem(val mToken: String, val mPosition: Int, val mIsSaveFilter: Boolean, var mUiWidth: Int)

        val mSortedTokenFilters: Array<out TokenFilterItem> =
            mTokenFilters.sortedArrayWith { t1: TokenFilterItem, t2: TokenFilterItem -> t1.mPosition - t2.mPosition }
        val mSortedTokensIdxs = Array(MAX_TOKEN_FILTER_COUNT) { -1 }
        val mSortedPidTokIdx: Int

        init {
            if (mPidTokIdx == -1) {
                mSortedPidTokIdx = -1
            } else {
                val tokenFilterPosition = mTokenFilters[mPidTokIdx].mPosition
                var tokIdx = -1
                if (tokenFilterPosition >= 0) {
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        if (tokenFilterPosition == mSortedTokenFilters[idx].mPosition) {
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
        const val ITEM_LOG_POSITION = "_LOG_NTH"
        const val ITEM_COLUMN_NAMES = "_COLUMN_NAMES"
        const val ITEM_LEVEL = "_LEVEL_"
        const val ITEM_LEVEL_POSITION = "_LEVEL_NTH"
        const val ITEM_TOKEN_FILTER_NAME = "_TOKEN_NAME_"
        const val ITEM_TOKEN_FILTER_POSITION = "_TOKEN_NTH_"
        const val ITEM_TOKEN_SAVE_FILTER = "_TOKEN_SAVE_FILTER_"
        const val ITEM_TOKEN_UI_WIDTH = "_TOKEN_UI_WIDTH_"
        const val ITEM_PID_TOK_IDX = "_PID_TOK_IDX"
        const val ITEM_SAMPLE_TEXT = "_SAMPLE_TEXT"

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

        const val SEPARATOR_DELIMITER = ":::SEPARATOR:::"

        fun splitLog(line: String, tokenCount: Int, separator: String, separatorList: List<String>?): List<String> {
            if (separatorList == null) {
                return line.split(Regex(separator), tokenCount)
            }
            else {
                var remainingLine = line
                val splitedLine: MutableList<String> = mutableListOf()
                var tmpSplited: List<String>
                for (item in separatorList) {
                    tmpSplited = remainingLine.split(Regex(item), 2)
                    splitedLine.add(tmpSplited[0])
                    if (tmpSplited.size > 1) {
                        remainingLine = tmpSplited[1]
                    }
                    else {
                        break
                    }
                }
                splitedLine.add(remainingLine)
                return splitedLine
            }
        }

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
        var maxColumnPosition = 0
        var tokenCount = 0
        var logPosition = 0
        var levelPosition = -1
        var isUpdated = false
        for (format in mFormatList) {
            maxColumnPosition = 0
            tokenCount = 0
            logPosition = 0
            levelPosition = -1
            isUpdated = false

            if (format.mColumnNames.isNotEmpty()) {
                tokenCount = format.mTokenCount
                logPosition = format.mLogPosition
                levelPosition = format.mLevelPosition
                val nameArr = format.mColumnNames.split("|")
                if (nameArr.isNotEmpty()) {
                    nameArr.forEach {
                        val nameSplit = it.split(",")
                        if (nameSplit.size == 3) {
                            val position = nameSplit[1].toInt()
                            if (maxColumnPosition < position) {
                                maxColumnPosition = position
                            }
                        }
                    }
                }

                for (tokenFilter in format.mTokenFilters) {
                    if (maxColumnPosition < tokenFilter.mPosition) {
                        maxColumnPosition = tokenFilter.mPosition
                    }
                }

                if (tokenCount <= maxColumnPosition) {
                    tokenCount = maxColumnPosition + 1
                    isUpdated = true
                }
                if (tokenCount <= logPosition) {
                    logPosition = maxColumnPosition
                    isUpdated = true
                }
                if (tokenCount <= levelPosition) {
                    levelPosition = -1
                    isUpdated = true
                }
            }
            if (isUpdated) {
                formatList.add(format.copy(mTokenCount = tokenCount, mLogPosition = logPosition, mLevelPosition = levelPosition))
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

    private fun getDefaultFormat(name: String): FormatItem {
        val separator = "\\s+:\\s+|:?\\s+"
        val tokenCount = 7
        val logPosition = 6
        val columnNames = "Date,0,50|Time,1,100|PID,2,50|TID,3,50|Level,4,15|Tag,5,150|Log,6,-1"
        val levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        val levelPosition = 4

        val tokenFilters: Array<FormatItem.TokenFilterItem> = arrayOf(
            FormatItem.TokenFilterItem("Tag", 5, true, 250),
            FormatItem.TokenFilterItem("PID", 2, false, 120),
            FormatItem.TokenFilterItem("TID", 3, false, 120),
        )
        val pidTokIdx = 1

        val sampleText = "11-20 23:29:26.908  1376  3136 V Test0  : This line is sample 0\n" +
                "11-20 23:29:26.908  1376  3136 D Test1  : This line is sample 1\n" +
                "11-20 23:29:26.908  1376  3136 I Test2  : This line is sample 2\n" +
                "11-20 23:29:26.908  1376  3136 W Test3  : This line is sample 3\n" +
                "11-20 23:29:26.908  1376  3136 E Test4  : This line is sample 4\n" +
                "11-20 23:29:26.908  1376  3136 F Test5  : This line is sample 5"

        return FormatItem(name, separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText)
    }

    private fun addDefaultFormats(formatList: MutableList<FormatItem>) {
        var separator = "\\s+:\\s+|:?\\s+"
        var tokenCount = 7
        var logPosition = 6
        var columnNames = "Date,0,50|Time,1,100|PID,2,50|TID,3,50|Level,4,15|Tag,5,150|Log,6,-1"
        var levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        var levelPosition = 4

        var tokenFilters: Array<FormatItem.TokenFilterItem> = arrayOf(
            FormatItem.TokenFilterItem("Tag", 5, true, 250),
            FormatItem.TokenFilterItem("PID", 2, false, 120),
            FormatItem.TokenFilterItem("TID", 3, false, 120),
        )
        var pidTokIdx = 1

        var sampleText = "11-20 23:29:26.908  1376  3136 V Test0  : This line is sample 0\n" +
                "11-20 23:29:26.908  1376  3136 D Test1  : This line is sample 1\n" +
                "11-20 23:29:26.908  1376  3136 I Test2  : This line is sample 2\n" +
                "11-20 23:29:26.908  1376  3136 W Test3  : This line is sample 3\n" +
                "11-20 23:29:26.908  1376  3136 E Test4  : This line is sample 4\n" +
                "11-20 23:29:26.908  1376  3136 F Test5  : This line is sample 5"

        formatList.add(0, FormatItem("logcat", separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText))

        // case plain text
        separator = ""
        tokenCount = 1
        logPosition = 0
        columnNames = "Log,0,-1"
        levels = mapOf()
        levelPosition = -1

        tokenFilters = arrayOf(
            FormatItem.TokenFilterItem("", 0, false, 0),
            FormatItem.TokenFilterItem("", 0, false, 0),
            FormatItem.TokenFilterItem("", 0, false, 0),
        )
        pidTokIdx = -1
        sampleText = "This line is sample 0\n" +
                "This line is sample 1\n" +
                "This line is sample 2\n" +
                "This line is sample 3\n" +
                "This line is sample 4\n" +
                "This line is sample 5"
        formatList.add(FormatItem("plain text", separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText))

        // case android studio
        separator = "\\s+"
        tokenCount = 7
        logPosition = 6
        columnNames = "Date,0,100|Time,1,120|PID,2,120|Tag,3,50|Package,4,100|Level,5,15|Log,6,-1"
        levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        levelPosition = 5

        tokenFilters = arrayOf(
            FormatItem.TokenFilterItem("", 0, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
        )
        pidTokIdx = -1

        sampleText = "2024-12-24 11:47:05.005  1351-2455  Test0        TestPackage                          V  This line is sample 0\n" +
                "2024-12-24 11:47:05.005  1351-2455  Test1        TestPackage                          D  This line is sample 1\n" +
                "2024-12-24 11:47:05.005  1351-2455  Test2        TestPackage                          I  This line is sample 2\n" +
                "2024-12-24 11:47:05.005  1351-2455  Test3        TestPackage                          W  This line is sample 3\n" +
                "2024-12-24 11:47:05.005  1351-2455  Test4        TestPackage                          E  This line is sample 4\n" +
                "2024-12-24 11:47:05.005  1351-2455  Test5        TestPackage                          F  This line is sample 5"

        formatList.add(FormatItem("android studio", separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText))

        // case logcat -v time
        separator = "/|\\s+\\(\\s+|\\s+\\(|\\(\\s+|\\(|\\s+|\\):?\\s+"
        tokenCount = 6
        logPosition = 5
        columnNames = "Date,0,50|Time,1,100|Level,2,15|Tag,3,150|PID,4,50|Log,5,-1"
        levels = mapOf(
            "V" to LEVEL_VERBOSE,
            "D" to LEVEL_DEBUG,
            "I" to LEVEL_INFO,
            "W" to LEVEL_WARNING,
            "E" to LEVEL_ERROR,
            "F" to LEVEL_FATAL
        )
        levelPosition = 2

        tokenFilters = arrayOf(
            FormatItem.TokenFilterItem("PID", 4, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
            FormatItem.TokenFilterItem("", 0, false, 120),
        )
        pidTokIdx = 0

        sampleText = "11-20 23:29:26.908  V/Test0(  1376): This line is sample 0\n" +
                "11-20 23:29:26.908  D/Test1(  1376): This line is sample 1\n" +
                "11-20 23:29:26.908  I/Test2(  1376): This line is sample 2\n" +
                "11-20 23:29:26.908  W/Test3(  1376): This line is sample 3\n" +
                "11-20 23:29:26.908  E/Test4(  1376): This line is sample 4\n" +
                "11-20 23:29:26.908  F/Test5(  1376): This line is sample 5"

        formatList.add(FormatItem("logcat -v time", separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText))
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
            val logPosition = try {
                ((mProperties["$i$ITEM_LOG_POSITION"] ?: "") as String).toInt()
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

            val levelPosition = try {
                ((mProperties["$i$ITEM_LEVEL_POSITION"] ?: "") as String).toInt()
            } catch (ex: NumberFormatException) {
                -1
            }

            var tokenFilters: Array<FormatItem.TokenFilterItem>
            try {
                tokenFilters = Array(MAX_TOKEN_FILTER_COUNT) {
                    val tokenName = ((mProperties["$i$ITEM_TOKEN_FILTER_NAME$it"] ?: "") as String).trim()
                    val position = try {
                        ((mProperties["$i$ITEM_TOKEN_FILTER_POSITION$it"] ?: "") as String).toInt()
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

                    FormatItem.TokenFilterItem(tokenName, position, isSaveFilter, uiWidth)
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

            val sampleText = (mProperties["$i$ITEM_SAMPLE_TEXT"] ?: "") as String

            mFormatList.add(FormatItem(name, separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokenFilters, pidTokIdx, sampleText))
        }
    }

    private fun isEqualFormatItem(format1: FormatItem, format2: FormatItem): Boolean {
        if (format1.mName != format2.mName) {
            return false
        }
        if (format1.mSeparator != format2.mSeparator) {
            return false
        }
        if (format1.mTokenCount != format2.mTokenCount) {
            return false
        }
        if (format1.mLogPosition != format2.mLogPosition) {
            return false
        }
        if (format1.mColumnNames != format2.mColumnNames) {
            return false
        }
        if (format1.mLevels != format2.mLevels) {
            return false
        }
        if (format1.mLevelPosition != format2.mLevelPosition) {
            return false
        }
        for (idxTok in 0 until MAX_TOKEN_FILTER_COUNT) {
            if (format1.mTokenFilters[idxTok].mToken != format2.mTokenFilters[idxTok].mToken) {
                return false
            }
            if (format1.mTokenFilters[idxTok].mPosition != format2.mTokenFilters[idxTok].mPosition) {
                return false
            }
            if (format1.mTokenFilters[idxTok].mIsSaveFilter != format2.mTokenFilters[idxTok].mIsSaveFilter) {
                return false
            }
            if (format1.mTokenFilters[idxTok].mUiWidth != format2.mTokenFilters[idxTok].mUiWidth) {
                return false
            }
        }

        if (format1.mPidTokIdx != format2.mPidTokIdx) {
            return false
        }

        if (format1.mSampleText != format2.mSampleText) {
            return false
        }

        return true
    }

    private fun isEqualFormatList(list1: MutableList<FormatItem>, list2: MutableList<FormatItem>): Boolean {
        if (list1.size != list2.size) {
            return false
        }

        for (idx in 0 until list1.size) {
            if (!isEqualFormatItem(list1[idx], list2[idx])) {
                return false
            }
        }

        return true
    }

    private fun saveList() {
        mProperties.clear()
        var format: FormatItem
        for (i in 0 until mFormatList.size) {
            format = mFormatList[i]
            mProperties["$i$ITEM_NAME"] = format.mName
            mProperties["$i$ITEM_SEPARATOR"] = format.mSeparator
            mProperties["$i$ITEM_TOKEN_COUNT"] = format.mTokenCount.toString()
            mProperties["$i$ITEM_LOG_POSITION"] = format.mLogPosition.toString()
            mProperties["$i$ITEM_COLUMN_NAMES"] = format.mColumnNames
            val keyList = format.mLevels.keys.toList()
            for (key in keyList) {
                val level = format.mLevels[key]
                if (level != null) {
                    mProperties["$i$ITEM_LEVEL$level"] = key
                }
            }

            mProperties["$i$ITEM_LEVEL_POSITION"] = format.mLevelPosition.toString()

            for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                mProperties["$i$ITEM_TOKEN_FILTER_NAME$idx"] = format.mTokenFilters[idx].mToken
                mProperties["$i$ITEM_TOKEN_FILTER_POSITION$idx"] = format.mTokenFilters[idx].mPosition.toString()
                mProperties["$i$ITEM_TOKEN_SAVE_FILTER$idx"] = format.mTokenFilters[idx].mIsSaveFilter.toString()
                mProperties["$i$ITEM_TOKEN_UI_WIDTH$idx"] = format.mTokenFilters[idx].mUiWidth.toString()
            }

            mProperties["$i$ITEM_PID_TOK_IDX"] = format.mPidTokIdx.toString()
            mProperties["$i$ITEM_SAMPLE_TEXT"] = format.mSampleText
        }

        saveXml()
        notifyFormatListChanged()
    }

    override fun manageVersion() {
        val isLoaded = loadXml()

        if (isLoaded) {
            var confVer: String = (mProperties[ITEM_VERSION] ?: "") as String
            if (confVer.isEmpty()) {
                updateFromV0ToV1()
                confVer = (mProperties[ITEM_VERSION] ?: "") as String
                Utils.printlnLog("manageVersion : $confVer applied")
            }
        }
        else {
            mProperties[ITEM_VERSION] = "1"
        }

        saveXml()
    }

    private fun updateFromV0ToV1() {
        Utils.printlnLog("FormatManager : updateFromV0ToV1 : add sample text ++")
        val formatList = mutableListOf<FormatItem>()
        addDefaultFormats(formatList)
        for (i in 0 until MAX_FORMAT_COUNT) {
            val name = (mProperties["$i$ITEM_NAME"] ?: "") as String
            if (name.trim().isEmpty()) {
                break
            }

            for (format in formatList) {
                if (format.mName == name) {
                    mProperties["$i$ITEM_SAMPLE_TEXT"] = format.mSampleText
                }
            }
        }
        mProperties[ITEM_VERSION] = "1"
        Utils.printlnLog("FormatManager : updateFromV0ToV1 : --")
    }

    fun clear() {
        mFormatList.clear()
    }

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

        private val mFirstBtn: JButton
        private val mPrevBtn: JButton
        private val mNextBtn: JButton
        private val mLastBtn: JButton

        private val mAddBtn: JButton
        private val mCopyBtn: JButton
        private val mDeleteBtn: JButton
        private val mResetBtn: JButton
        private val mSaveBtn: JButton

        private val mFormatPanel = JPanel()
        private val mDetailPanel = DetailPanel(true)

        private val mFormatJList: JList<String>
        private val mFormatListModel: DefaultListModel<String>
        private val mFormatListScrollPane: JScrollPane
        private val mSplitPane: JSplitPane

        init {
            copyFormatList(mFormatList, mDialogFormatList)
            mFormatPanel.layout = BoxLayout(mFormatPanel, BoxLayout.Y_AXIS)

            val inUsePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val inUseLabel = JLabel(" ${Strings.IN_USE} - [${mCurrFormat.mName}] ")
            inUsePanel.add(inUseLabel)

            mFirstBtn = JButton("↑")
            mFirstBtn.addActionListener(this)
            mPrevBtn = JButton("∧")
            mPrevBtn.addActionListener(this)
            mNextBtn = JButton("∨")
            mNextBtn.addActionListener(this)
            mLastBtn = JButton("↓")
            mLastBtn.addActionListener(this)

            mAddBtn = JButton(Strings.ADD)
            mAddBtn.addActionListener(this)
            mCopyBtn = JButton(Strings.COPY)
            mCopyBtn.addActionListener(this)
            mDeleteBtn = JButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mResetBtn = JButton(Strings.RESET)
            mResetBtn.addActionListener(this)
            mSaveBtn = JButton(Strings.SAVE)
            mSaveBtn.addActionListener(this)
            val buttonPanel = JPanel()
            Utils.addVSeparator(buttonPanel, 20)
            buttonPanel.add(mAddBtn)
            buttonPanel.add(mCopyBtn)
            buttonPanel.add(mDeleteBtn)
            buttonPanel.add(mResetBtn)
            Utils.addVSeparator(buttonPanel, 20)
            buttonPanel.add(mSaveBtn)

            inUsePanel.add(buttonPanel)
            mFormatPanel.add(mDetailPanel)
            Utils.addHEmptySeparator(mFormatPanel, 20)

            mFormatListModel = DefaultListModel()
            for (item in mDialogFormatList) {
                mFormatListModel.addElement(item.mName)
            }
            mFormatJList = JList(mFormatListModel)
            mFormatJList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            mFormatJList.selectionModel.addListSelectionListener(ListSelectionHandler())
            mFormatListScrollPane = JScrollPane(mFormatJList)

            val leftBtnPanel = JPanel()
            leftBtnPanel.layout = GridLayout(1, 4)
            leftBtnPanel.add(mFirstBtn)
            leftBtnPanel.add(mPrevBtn)
            leftBtnPanel.add(mNextBtn)
            leftBtnPanel.add(mLastBtn)
            val leftPanel = JPanel()
            leftPanel.layout = BorderLayout()
            leftPanel.add(leftBtnPanel, BorderLayout.NORTH)
            leftPanel.add(mFormatListScrollPane, BorderLayout.CENTER)
            mSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftPanel, mFormatPanel)

            val panel = JPanel(BorderLayout())
            panel.add(inUsePanel, BorderLayout.NORTH)
            panel.add(mSplitPane, BorderLayout.CENTER)

            contentPane.add(panel)
            pack()

            for ((idx, format) in mDialogFormatList.withIndex()) {
                if (format.mName == mCurrFormat.mName) {
                    mFormatJList.selectedIndex = idx
                }
            }

            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    val selectedIdx = mFormatJList.selectedIndex
                    val editingFormat: FormatItem
                    val orgFormat: FormatItem
                    if (selectedIdx >= 0 && selectedIdx < mDialogFormatList.size) {
                        editingFormat = try {
                            mDetailPanel.makeFormatItem()
                        } catch (ex: Exception) {
                            getDefaultFormat("")
                        }

                        orgFormat = mDialogFormatList[selectedIdx]
                    }
                    else {
                        orgFormat = getDefaultFormat("")
                        editingFormat = orgFormat
                    }

                    if (!isEqualFormatList(mFormatList, mDialogFormatList) || !isEqualFormatItem(orgFormat, editingFormat)) {
                        val dialogResult = JOptionPane.showConfirmDialog(this@FormatListDialog, Strings.VAL_CHANGE_SAVE, "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            if (save(true)) {
                                dispose()
                            }
                        }
                        else {
                                dispose()
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

        internal inner class ListSelectionHandler : ListSelectionListener {
            private var mSelectedIdx = -1
            override fun valueChanged(p0: ListSelectionEvent?) {
                if (mDialogFormatList.size > 0 && mFormatJList.selectedIndex >= 0 && mSelectedIdx != mFormatJList.selectedIndex) {
                    mSelectedIdx = mFormatJList.selectedIndex
                    mDetailPanel.setFormat(mDialogFormatList[mSelectedIdx])
                    mDetailPanel.updateSampleLog()
                }

                mSelectedIdx = mFormatJList.selectedIndex
                return
            }
        }

        inner class TokenFilterPanel(val idx: Int) : JPanel() {
            private val mTokenLabel = JLabel()
            private val mTokenTF = JTextField()
            private val mPositionLabel = JLabel()
            private val mPositionTF = JTextField()
            private val mIsSaveFilterLabel = JLabel()
            private val mIsSaveFilterCheck = JCheckBox()
            private val mUiWidthLabel = JLabel()
            private val mUiWidthTF = JTextField()
            private val mIdx = idx

            fun setIsEditable(enabled: Boolean) {
                mTokenTF.isEditable = enabled
                mPositionTF.isEditable = enabled
                mIsSaveFilterCheck.isEnabled = enabled
                mUiWidthTF.isEditable = enabled
            }

            init {
                mTokenLabel.text = "$mIdx : ${Strings.NAME}"
                mTokenTF.text = ""
                mTokenTF.preferredSize = Dimension(150, mTokenTF.preferredSize.height)
                mPositionLabel.text = Strings.POSITION
                mPositionTF.text = "-1"
                mIsSaveFilterLabel.text = Strings.SAVE_FILTER
                mIsSaveFilterCheck.isSelected = false
                mUiWidthLabel.text = "UI ${Strings.WIDTH}"
                mUiWidthTF.text = "0"

                layout = FlowLayout(FlowLayout.LEFT)
                add(JLabel("   "))
                add(mTokenLabel)
                add(mTokenTF)
                add(JLabel("       "))
                add(mPositionLabel)
                add(mPositionTF)
                add(JLabel("       "))
                add(mIsSaveFilterLabel)
                add(mIsSaveFilterCheck)
                add(JLabel("       "))
                add(mUiWidthLabel)
                add(mUiWidthTF)
            }

            fun setToken(tokenFilterItem: FormatItem.TokenFilterItem) {
                mTokenTF.text = tokenFilterItem.mToken
                mPositionTF.text = tokenFilterItem.mPosition.toString()
                mIsSaveFilterCheck.isSelected = tokenFilterItem.mIsSaveFilter
                mUiWidthTF.text = tokenFilterItem.mUiWidth.toString()
            }

            fun getToken(): FormatItem.TokenFilterItem {
                var isValid = true
                val name = mTokenTF.text.trim()

                var position: Int = 0
                try {
                    position = mPositionTF.text.toInt()
                    mPositionTF.background = mDetailPanel.mTextFieldBg
                    mPositionTF.toolTipText = ""
                } catch (ex: NumberFormatException) {
                    mPositionTF.background = Color.RED
                    mPositionTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
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

                return FormatItem.TokenFilterItem(name, position, isSaveFilter, uiWidth)
            }
        }

        inner class DetailPanel(isEditable: Boolean) : JPanel() {
            private val mSampleDataList: MutableList<List<String>> = ArrayList()
            private val mSampleStep1TableModel = SampleStep1TableModel()
            private val mSampleStep1Table = JTable(mSampleStep1TableModel)
            private val mSampleStep1TablePane = JScrollPane(mSampleStep1Table)

            private val mSampleTextArea = JTextArea()
            private val mSampleTextPane = JScrollPane(mSampleTextArea)

            private val mSampleStep2TableModel = SampleStep2TableModel()
            private val mSampleStep2Table = JTable(mSampleStep2TableModel)
            private val mSampleStep2TablePane = JScrollPane(mSampleStep2Table)

            private val mNameLabel = JLabel()
            private val mNameTF = JTextField()
            private val mSeparatorLabel = JLabel()
            private var mSeparator = ""
            private val mSeparatorTfList: MutableList<JTextField> = ArrayList()
            private val mLevelPositionLabel = JLabel()
            private val mLevelPositionTF = JTextField()
            private val mPidTokIdxLabel = JLabel()
            private val mPidTokIdxCombo = ColorComboBox<String>()
            private val mLogFormatPanel = JPanel()
            private val mNamePanel = JPanel()
            private val mSeparatorPanel = JPanel()
            private val mSeparatorListPanel = JPanel()
            private val mSeparatorSingleRadio = JRadioButton("Single")
            private val mSeparatorMultipleRadio = JRadioButton("Multiple")

            private val mLevelsLabelArr = Array(TEXT_LEVEL.size) { JLabel(TEXT_LEVEL[it]) }
            private val mLevelsTFArr = Array(TEXT_LEVEL.size) { JTextField() }
            private val mLevelsPanel = JPanel()
            private val mTokenFilterArr = Array(MAX_TOKEN_FILTER_COUNT) { TokenFilterPanel(it) }
            private val mTokenFiltersPanel = JPanel()
            val mTextFieldBg: Color
            private val mColumnNamesPanel = JPanel()
            private val mTokenCountLabel = JLabel()
            private val mTokenCountTF = JTextField()
            private val mLogPositionLabel = JLabel()
            private val mLogPositionTF = JTextField()
            private val mColumnNamesLabel = JLabel()
            private val mColumnNamesTF = JTextField()

            private val mTableColor = ColorManager.getInstance().mFullTableColor

            init {
                border = BorderFactory.createEmptyBorder(5, 5, 5, 10)
                mSampleTextPane.preferredSize = Dimension(1200, 130)
                mSampleTextPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

                mSampleTextArea.text = ""

                mSampleStep1TablePane.preferredSize = Dimension(1200, 170)
                mSampleStep1TablePane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
                mSampleStep1TablePane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

                mSampleStep1TablePane.isOpaque = false
                mSampleStep1TablePane.viewport.isOpaque = false

                mSampleStep1Table.setDefaultRenderer(String::class.java, LogCellRenderer())
                mSampleStep1Table.tableHeader.reorderingAllowed = false
                mSampleStep1Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                mSampleStep1Table.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN

                mSampleStep2TablePane.preferredSize = Dimension(1200, 170)
                mSampleStep2TablePane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
                mSampleStep2TablePane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

                mSampleStep2TablePane.isOpaque = false
                mSampleStep2TablePane.viewport.isOpaque = false

                mSampleStep2Table.setDefaultRenderer(String::class.java, LogCellRenderer())
                mSampleStep2Table.tableHeader.reorderingAllowed = false
                mSampleStep2Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                mSampleStep2Table.autoResizeMode = JTable.AUTO_RESIZE_OFF

                mSampleTextPane.border = BorderFactory.createEmptyBorder(3, 20, 3, 0)
                mSampleStep1TablePane.border = BorderFactory.createEmptyBorder(3, 20, 3, 0)
                mSampleStep2TablePane.border = BorderFactory.createEmptyBorder(3, 20, 3, 0)

                mNamePanel.layout = FlowLayout(FlowLayout.LEFT)
                mNameLabel.text = Strings.NAME
                mNameTF.preferredSize = Dimension(150, mNameTF.preferredSize.height)
                mNameTF.text = ""
                mTextFieldBg = mNameTF.background
                mSeparatorLabel.text = Strings.SEPARATOR
                mSeparator = ""
                mTokenCountLabel.text = Strings.TOKEN_COUNT
                mTokenCountTF.preferredSize = Dimension(70, mTokenCountTF.preferredSize.height)
                mLevelPositionLabel.text = "${Strings.LEVEL} ${Strings.POSITION}"
                mLevelPositionTF.text = "-1"
                mPidTokIdxLabel.text = "${Strings.PID_TOKEN_FILTER}(${Strings.PID_TOKEN_FILTER_OPTIONAL})"
                for (idx in -1 until MAX_TOKEN_FILTER_COUNT) {
                    mPidTokIdxCombo.addItem("$idx")
                }

                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mNameLabel)
                mNamePanel.add(mNameTF)
                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mTokenCountLabel)
                mNamePanel.add(mTokenCountTF)
                mNamePanel.add(JLabel("   "))
                mNamePanel.add(mLevelPositionLabel)
                mNamePanel.add(mLevelPositionTF)

                mSeparatorSingleRadio.addActionListener {
                        updateSeparatorList()
                }

                mSeparatorMultipleRadio.addActionListener {
                        updateSeparatorList()
                }

                val buttonGroup = ButtonGroup()
                buttonGroup.add(mSeparatorSingleRadio)
                buttonGroup.add(mSeparatorMultipleRadio)

                val separatorRadioPanel = JPanel()
                separatorRadioPanel.layout = FlowLayout(FlowLayout.LEFT)
                separatorRadioPanel.add(JLabel("   "))
                separatorRadioPanel.add(mSeparatorLabel)
                separatorRadioPanel.add(mSeparatorSingleRadio)
                separatorRadioPanel.add(mSeparatorMultipleRadio)

                val separatorListWrapperPanel = JPanel()
                separatorListWrapperPanel.layout = BorderLayout()
                separatorListWrapperPanel.add(JLabel("     "), BorderLayout.WEST)
                mSeparatorListPanel.layout = GridLayout(1, 1, 0, 0)
                mSeparatorTfList.add(JTextField(""))
                mSeparatorListPanel.add(mSeparatorTfList[0])
                separatorListWrapperPanel.add(mSeparatorListPanel, BorderLayout.CENTER)
                mSeparatorPanel.layout = BorderLayout()
                mSeparatorPanel.add(separatorRadioPanel, BorderLayout.NORTH)
                mSeparatorPanel.add(separatorListWrapperPanel, BorderLayout.CENTER)

                mLogFormatPanel.layout = BorderLayout()
                mLogFormatPanel.add(mNamePanel, BorderLayout.NORTH)
                mLogFormatPanel.add(mSeparatorPanel, BorderLayout.CENTER)

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
                mLogPositionLabel.text = "${Strings.LOG} ${Strings.POSITION}"
                mLogPositionTF.preferredSize = Dimension(40, mLogPositionTF.preferredSize.height)
                mLogPositionLabel.toolTipText = TooltipStrings.FILTER_LOG_POSITION
                mLogPositionTF.toolTipText = TooltipStrings.FILTER_LOG_POSITION
                mColumnNamesPanel.add(mLogPositionLabel)
                mColumnNamesPanel.add(mLogPositionTF)
                mColumnNamesLabel.text = "    ${Strings.COLUMN}"
                mColumnNamesTF.preferredSize = Dimension(600, mColumnNamesTF.preferredSize.height)
                mColumnNamesLabel.toolTipText = TooltipStrings.LOG_COLUMN_VALUES
                mColumnNamesTF.toolTipText = TooltipStrings.LOG_COLUMN_VALUES
                mColumnNamesPanel.add(mColumnNamesLabel)
                mColumnNamesPanel.add(mColumnNamesTF)

                if (!isEditable) {
                    mNameTF.isEditable = false
                    mLevelPositionTF.isEditable = false
                    mPidTokIdxCombo.isEnabled = false
                    for (idx in TEXT_LEVEL.indices) {
                        mLevelsTFArr[idx].isEditable = false
                    }
                    mTokenCountTF.isEditable = false
                    mLogPositionTF.isEditable = false
                    mColumnNamesTF.isEditable = false
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        mTokenFilterArr[idx].setIsEditable(false)
                    }
                }

                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                Utils.addHSeparator(this, " ${Strings.LOG_FORMAT} ")
                add(mLogFormatPanel)
                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.LEVELS} ")
                add(mLevelsPanel)

                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.SAMPLE_TEXT} : ${Strings.APPLY_CTRL_ENTER} ")
                add(mSampleTextPane)
                add(mSampleStep1TablePane)

                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.COLUMN} ${Strings.NAME} (${Strings.USED_COLUMN_VIEW_MODE}) : ${Strings.APPLY_CTRL_ENTER}")
                add(mColumnNamesPanel)
                add(mSampleStep2TablePane)
                Utils.addHEmptySeparator(this, 20)
                Utils.addHSeparator(this, " ${Strings.TOKENS} ${Strings.FILTERS}")
                add(mTokenFiltersPanel)

                registerKeyStroke()
            }

            private fun updateSeparatorList() {
                mSeparatorListPanel.removeAll()
                mSeparatorTfList.clear()

                val separatorCount = if (mSeparatorSingleRadio.isSelected) {
                    1
                } else {
                    try {
                        mTokenCountTF.text.toInt() - 1
                    } catch (ex: NumberFormatException) {
                        1
                    }
                }

                mSeparatorListPanel.layout = GridLayout(1, separatorCount)

                val separatorList = mSeparator.split(SEPARATOR_DELIMITER)
                for (idx in 0 until separatorCount) {
                    val tf = JTextField()
                    if (idx < separatorList.size) {
                        tf.text = separatorList[idx]
                    }
                    else {
                        tf.text = separatorList[0]
                    }
                    mSeparatorTfList.add(tf)
                    mSeparatorListPanel.add(tf)
                }
                revalidate()
//                repaint()


            }

            private fun getSeparator(): String {
                var separator = ""
                for (tf in mSeparatorTfList) {
                    if (separator.isNotEmpty()) {
                        separator += SEPARATOR_DELIMITER
                    }
                    separator += tf.text
                }
                return separator
            }

            fun setFormat(format: FormatItem) {
                if (format.mName.isNotEmpty()) {
                    mNameTF.text = format.mName
                    mTokenCountTF.text = format.mTokenCount.toString()
                    mSeparator = format.mSeparator
                    if (mSeparator.contains(SEPARATOR_DELIMITER)) {
                        mSeparatorMultipleRadio.isSelected = true
                    }
                    else {
                        mSeparatorSingleRadio.isSelected = true
                    }
                    updateSeparatorList()
                    mLogPositionTF.text = format.mLogPosition.toString()
                    mColumnNamesTF.text = format.mColumnNames
                    mLevelPositionTF.text = format.mLevelPosition.toString()
                    mPidTokIdxCombo.selectedItem = format.mPidTokIdx.toString()
                    for (idx in TEXT_LEVEL.indices) {
                        mLevelsTFArr[idx].text = ""
                    }
                    format.mLevels.forEach { mLevelsTFArr[it.value].text = it.key }
                    for (idx in 0 until MAX_TOKEN_FILTER_COUNT) {
                        mTokenFilterArr[idx].setToken(format.mTokenFilters[idx])
                    }
                    mSampleTextArea.text = format.mSampleText
                }
            }

            fun getFormat(): FormatItem {
                return mDialogFormatList[0]
            }

            private fun registerKeyStroke() {
                val stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK)
                val actionMapKey = javaClass.name + ":APPLY_SAMPLE"
                val action: Action = object : AbstractAction() {
                    override fun actionPerformed(event: ActionEvent) {
                        updateSampleLog()
                    }
                }
                this@FormatListDialog.rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, actionMapKey)
                this@FormatListDialog.rootPane.actionMap.put(actionMapKey, action)
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
                    mLevelPositionTF.text.toInt()
                    mLevelPositionTF.background = mTextFieldBg
                    mLevelPositionTF.toolTipText = ""
                } catch (ex: NumberFormatException) {
                    mLevelPositionTF.background = Color.RED
                    mLevelPositionTF.toolTipText = TooltipStrings.INVALID_NUMBER_FORMAT
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
                val separator = getSeparator()
                val tokenCount = mTokenCountTF.text.toInt()
                val logPosition = mLogPositionTF.text.toInt()
                val columnNames = mColumnNamesTF.text
                val levels = emptyMap<String, Int>().toMutableMap()
                for (idx in 1 until TEXT_LEVEL.size) {
                    val level = mLevelsTFArr[idx].text.trim()
                    if (level.isNotEmpty()) {
                        levels[level] = idx
                    }
                }

                val levelPosition = mLevelPositionTF.text.toInt()
                val tokens = Array(MAX_TOKEN_FILTER_COUNT) { mTokenFilterArr[it].getToken() }
                val pidTokIdx: Int = if (mPidTokIdxCombo.selectedItem == null) {
                    -1
                } else {
                    mPidTokIdxCombo.selectedItem!!.toString().toInt()
                }
                val sampleText = mSampleTextArea.text

                return FormatItem(name, separator, tokenCount, logPosition, columnNames, levelPosition, levels, tokens, pidTokIdx, sampleText)
            }

            private fun getFgStrColor(level: Int) : String {
                return when (level) {
                    LEVEL_VERBOSE -> {
                        mTableColor.mStrLogLevelVerbose
                    }
                    LEVEL_DEBUG -> {
                        mTableColor.mStrLogLevelDebug
                    }
                    LEVEL_INFO -> {
                        mTableColor.mStrLogLevelInfo
                    }
                    LEVEL_WARNING -> {
                        mTableColor.mStrLogLevelWarning
                    }
                    LEVEL_ERROR -> {
                        mTableColor.mStrLogLevelError
                    }
                    LEVEL_FATAL -> {
                        mTableColor.mStrLogLevelFatal
                    }
                    else -> mTableColor.mStrLogLevelNone
                }
            }

            fun updateSampleLog() {
                val tokenCount = if (mTokenCountTF.text.isEmpty()) {
                    0
                } else {
                    mTokenCountTF.text.toInt()
                }
                val levelIdx = if (mLevelPositionTF.text.isEmpty()) {
                    -1
                } else {
                    mLevelPositionTF.text.toInt()
                }

                mSampleDataList.clear()
                val lines: List<String> = mSampleTextArea.text.split("\n")
                val separator = getSeparator()
                val separatorList = if (separator.contains(SEPARATOR_DELIMITER)) {
                    separator.split(SEPARATOR_DELIMITER)
                }
                else {
                    null
                }
                for (line in lines) {
                    mSampleDataList.add(splitLog(line, tokenCount, separator, separatorList))
                }

                mSampleStep1TableModel.updateColumns(tokenCount, levelIdx)
                mSampleStep2TableModel.updateColumns(tokenCount, levelIdx)
            }

            inner class SampleStep1TableModel : DefaultTableModel() {
                override fun getColumnClass(columnIndex: Int): Class<*> {
                    return String::class.java
                }

                override fun getRowCount(): Int {
                    return mSampleDataList.size
                }

                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }

                override fun getValueAt(row: Int, column: Int): Any {
                    val lineList = mSampleDataList[row]
                    return if (column >= lineList.size) {
                        ""
                    } else {
                        val levelIdx = if (mLevelPositionTF.text.isEmpty()) {
                            -1
                        } else {
                            mLevelPositionTF.text.toInt()
                        }

                        val levelColor = if (levelIdx > 0 && levelIdx < lineList.size) {
                            var level = LEVEL_NONE
                            for (idx in 1 until TEXT_LEVEL.size) {
                                if (mLevelsTFArr[idx].text.isNotEmpty() && mLevelsTFArr[idx].text == lineList[levelIdx]) {
                                    level = idx
                                }
                            }
                            getFgStrColor(level)
                        }
                        else {
                            getFgStrColor(LEVEL_NONE)
                        }

                        "<html><nobr><font color=$levelColor>${lineList[column]}</font></nobr></html>"
                    }
                }

                fun updateColumns(tokenCount: Int, levelIdx: Int) {
                    val columns: MutableList<String> = ArrayList()
                    for (i in 0 until tokenCount) {
                        if (i == levelIdx) {
                            columns.add("$i (level)")
                        }
                        else {
                            columns.add(i.toString())
                        }
                    }
                    mSampleStep1TableModel.setColumnIdentifiers(columns.toTypedArray())
                }
            }

            inner class SampleStep2TableModel : DefaultTableModel() {
                private val mIdxList: MutableList<Int> = ArrayList()

                override fun getColumnClass(columnIndex: Int): Class<*> {
                    return String::class.java
                }

                override fun getRowCount(): Int {
                    return mSampleDataList.size
                }

                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }

                override fun getValueAt(row: Int, column: Int): Any {
                    val lineList = mSampleDataList[row]
                    return if (mIdxList[column] >= lineList.size) {
                        ""
                    } else {
                        val levelIdx = if (mLevelPositionTF.text.isEmpty()) {
                            -1
                        } else {
                            mLevelPositionTF.text.toInt()
                        }

                        val levelColor = if (levelIdx > 0 && levelIdx < lineList.size) {
                            var level = LEVEL_NONE
                            for (idx in 1 until TEXT_LEVEL.size) {
                                if (mLevelsTFArr[idx].text.isNotEmpty() && mLevelsTFArr[idx].text == lineList[levelIdx]) {
                                    level = idx
                                }
                            }
                            getFgStrColor(level)
                        }
                        else {
                            getFgStrColor(LEVEL_NONE)
                        }

                        "<html><nobr><font color=$levelColor>${lineList[mIdxList[column]]}</font></nobr></html>"
                    }
                }

                fun updateColumns(tokenCount: Int, levelIdx: Int) {
                    val nameList: MutableList<String> = ArrayList()
                    val widthList: MutableList<Int> = ArrayList()
                    val columnInfos = mColumnNamesTF.text
                    var lastWidth = mSampleStep2TablePane.width - 20 // 20 : border left
                    var lastWidthIdx = -1
                    mIdxList.clear()
                    if (columnInfos.isNotEmpty()) {
                        val infos = columnInfos.split("|")
                        for ((idx, info) in infos.withIndex()) {
                            val infoItems = info.split(",")
                            if (infoItems.size == 3) {
                                nameList.add(infoItems[0])
                                mIdxList.add(infoItems[1].toInt())
                                val width = infoItems[2].toInt()
                                if (width == -1) {
                                    lastWidthIdx = idx
                                }
                                else {
                                    lastWidth -= width
                                }
                                widthList.add(width)
                            }
                        }
                        if (lastWidthIdx >= 0) {
                            widthList[lastWidthIdx] = lastWidth
                        }
                    }
                    setColumnIdentifiers(nameList.toTypedArray())

                    for (idx in widthList.indices) {
                        mSampleStep2Table.columnModel.getColumn(idx).preferredWidth = widthList[idx]
                    }
                }
            }

            internal open inner class LogCellRenderer : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable?,
                    value: Any?,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int
                ): Component {
                    background = mTableColor.mLogBG
                    val compo = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel

                    return compo
                }
            }
        }

        private fun isExistFormat(name: String, excludeIdx: Int): Boolean {
            var isExist = false
            for (idx in 0 until mDialogFormatList.size) {
                if (name == mDialogFormatList[idx].mName) {
                    if (excludeIdx != idx) {
                        isExist = true
                        break
                    }
                }
            }

            return isExist
        }

        override fun actionPerformed(e: ActionEvent?) {
            when (e?.source) {
                mFirstBtn -> {
                    val selectedIdx = mFormatJList.selectedIndex
                    if (mDialogFormatList.size > 1 && selectedIdx < mDialogFormatList.size) {
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(0, format)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = 0
                    }
                }
                mPrevBtn -> {
                    val selectedIdx = mFormatJList.selectedIndex
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx && selectedIdx > 0) {
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(selectedIdx - 1, format)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = selectedIdx - 1
                    }
                }
                mNextBtn -> {
                    val selectedIdx = mFormatJList.selectedIndex
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx && selectedIdx < (mDialogFormatList.size - 1)) {
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(selectedIdx + 1, format)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = selectedIdx + 1
                    }
                }
                mLastBtn -> {
                    val selectedIdx = mFormatJList.selectedIndex
                    if (mDialogFormatList.size > 1 && mDialogFormatList.size > selectedIdx) {
                        val format = mDialogFormatList[selectedIdx]
                        mDialogFormatList.removeAt(selectedIdx)
                        mDialogFormatList.add(mDialogFormatList.size, format)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = mDialogFormatList.size - 1
                    }
                }

                mAddBtn -> {
                    val editDialog = FormatEditDialog(this, Strings.ADD, "ADD")
                    editDialog.setLocationRelativeTo(parent)
                    editDialog.isVisible = true

                    if (editDialog.mNewName.isNotEmpty()) {
                        val format = getDefaultFormat(editDialog.mNewName)
                        mDialogFormatList.add(format)
                        mFormatListModel.addElement(format.mName)
                        mFormatJList.selectedIndex = mDialogFormatList.size - 1
                    }
                }

                mCopyBtn -> {
                    val selectedIdx = mFormatJList.selectedIndex
                    if (selectedIdx < 0 || selectedIdx >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$selectedIdx\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    var num = 2
                    var name = mDialogFormatList[selectedIdx].mName + " - $num"

                    while (true) {
                        if (!isExistFormat(name, -1)) {
                            break
                        }
                        num++
                        name = mDialogFormatList[selectedIdx].mName + " - $num"
                    }

                    val format: FormatItem = mDialogFormatList[selectedIdx].copy(mName = name)
                    mDialogFormatList.add(format)
                    mFormatListModel.addElement(format.mName)
                    mFormatJList.selectedIndex = mDialogFormatList.size - 1
                }

                mDeleteBtn -> {
                    var selectedIdx = mFormatJList.selectedIndex
                    if (selectedIdx < 0 || selectedIdx >= mDialogFormatList.size) {
                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_INDEX} \"$selectedIdx\"", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }

                    val dialogResult = JOptionPane.showConfirmDialog(this, String.format(Strings.CONFIRM_DELETE_FORMAT, mDialogFormatList[selectedIdx].mName), "Warning", JOptionPane.YES_NO_OPTION)
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        mDialogFormatList.removeAt(selectedIdx)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        if (selectedIdx > 0) {
                            selectedIdx--
                        }
                        mFormatJList.selectedIndex = selectedIdx
                    }
                }

                mResetBtn -> {
                    val dialogResult = JOptionPane.showConfirmDialog(this, Strings.CONFIRM_RESET_FORMAT_LIST, "Warning", JOptionPane.YES_NO_OPTION)
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        mDialogFormatList.clear()
                        addDefaultFormats(mDialogFormatList)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = -1
                        mFormatJList.selectedIndex = 0
                    }
                }

                mSaveBtn -> {
                    save(false)
                }
            }
        }

        fun save(withExit: Boolean): Boolean {
            var isSaved = false
            try {
                val format = mDetailPanel.makeFormatItem()
                val isExist = isExistFormat(format.mName, mFormatJList.selectedIndex)
                if (isExist) {
                    JOptionPane.showMessageDialog(this, "${Strings.INVALID_NAME_EXIST} \"${format.mName}\"", "Error", JOptionPane.ERROR_MESSAGE)
                }
                else {
                    val selectedIdx = mFormatJList.selectedIndex
                    mDialogFormatList.removeAt(selectedIdx)
                    mDialogFormatList.add(selectedIdx, format)
                    copyFormatList(mDialogFormatList, mFormatList)
                    saveList()

                    if (!withExit) {
                        JOptionPane.showMessageDialog(this, Strings.SAVED_FORMAT_LIST, "Info", JOptionPane.INFORMATION_MESSAGE)
                        mFormatListModel.clear()
                        for (item in mDialogFormatList) {
                            mFormatListModel.addElement(item.mName)
                        }
                        mFormatJList.selectedIndex = selectedIdx
                    }
                    isSaved = true
                }
            } catch (ex: Exception) {
                JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
            }

            return isSaved
        }

        inner class FormatEditDialog(parent: JDialog, title: String, cmd: String) : JDialog(parent, title, true), ActionListener {
            private val mNameTF = JTextField()
            private val mOkBtn: JButton
            private val mCancelBtn: JButton
            private val mFormatPanel = JPanel()
            private val mCmd = cmd
            var mNewName = ""

            init {
                mFormatPanel.layout = BoxLayout(mFormatPanel, BoxLayout.Y_AXIS)

                mNameTF.preferredSize = Dimension(300, mNameTF.preferredSize.height)
                val namePanel = JPanel()
                namePanel.add(mNameTF)

                mFormatPanel.add(namePanel)

                mOkBtn = JButton(Strings.OK)
                mOkBtn.addActionListener(this)
                mCancelBtn = JButton(Strings.CANCEL)
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

            override fun actionPerformed(e: ActionEvent?) {
                when (e?.source) {
                    mOkBtn -> {
                        when (mCmd) {
                            "ADD" -> {
                                try {
                                    val name = mNameTF.text.trim()
                                    val isExist = isExistFormat(name, -1)
                                    if (isExist) {
                                        JOptionPane.showMessageDialog(this, "${Strings.INVALID_NAME_EXIST} \"$name\"", "Error", JOptionPane.ERROR_MESSAGE)
                                    }
                                    else {
                                        mNewName = name
                                    }
                                } catch (ex: Exception) {
                                    JOptionPane.showMessageDialog(this, Strings.INVALID_VALUE, "Error", JOptionPane.ERROR_MESSAGE)
                                }
                            }
                        }

                        if (mNewName.isNotEmpty()) {
                            dispose()
                        }
                    }
                    mCancelBtn -> {
                        mNewName = ""
                        dispose()
                    }
                }
            }

            override fun setVisible(b: Boolean) {
                mNameTF.text = mNewName
                super.setVisible(b)
            }
        }
    }
}
