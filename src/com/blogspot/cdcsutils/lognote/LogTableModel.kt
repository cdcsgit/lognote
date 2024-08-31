package com.blogspot.cdcsutils.lognote

import com.blogspot.cdcsutils.lognote.MainUI.Companion.CurrentMethod
import com.blogspot.cdcsutils.lognote.MainUI.Companion.METHOD_ADB
import java.awt.Color
import java.io.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel


class LogTableModelEvent(source:LogTableModel, change:Int, removedCount:Int) {
    val mSource = source
    val mDataChange = change
//    val mFlags = flag
    val mRemovedCount = removedCount
    companion object {
        const val EVENT_ADDED = 0
        const val EVENT_REMOVED = 1
        const val EVENT_FILTERED = 2
        const val EVENT_CHANGED = 3
        const val EVENT_CLEARED = 4

        const val FLAG_FIRST_REMOVED = 1
    }
}

interface LogTableModelListener {
    fun tableChanged(event:LogTableModelEvent?)
}

open class LogTableModel(mainUI: MainUI, baseModel: LogTableModel?) : AbstractTableModel() {
    companion object {
        var IsColorTagRegex = false
        var IsShowProcessName = true
        var WaitTimeForDoubleClick = System.currentTimeMillis()
        internal const val COLUMN_NUM = 0
        internal const val COLUMN_PROCESS_NAME = 1
        internal const val COLUMN_LOG_START = 2
        private const val COLUMN_COUNT = 3
        const val LEVEL_NONE = FormatManager.LEVEL_NONE
        const val LEVEL_VERBOSE = FormatManager.LEVEL_VERBOSE
        const val LEVEL_DEBUG = FormatManager.LEVEL_DEBUG
        const val LEVEL_INFO = FormatManager.LEVEL_INFO
        const val LEVEL_WARNING = FormatManager.LEVEL_WARNING
        const val LEVEL_ERROR = FormatManager.LEVEL_ERROR
        const val LEVEL_FATAL = FormatManager.LEVEL_FATAL
    }

    private val mAgingTestManager = AgingTestManager.getInstance()

    data class FilteredColor(val mColor: String, val mPattern: Pattern?)

    inner class FilterTokenManager() {
        fun set(idx: Int, value: String) {
            if (mFilterTokens[idx] != value) {
                mIsFilterUpdated = true
                mFilterTokens[idx] = value
            }
            mMainUI.mTokenCombo[idx].mErrorMsg = ""
            val patterns = parsePattern(value, false)
            mFilterShowTokens[idx] = patterns[0]
            mFilterHideTokens[idx] = patterns[1]
            mPatternShowTokens[idx] = compilePattern(mFilterShowTokens[idx], mPatternCase, mPatternShowTokens[idx], mMainUI.mTokenCombo[idx])
            mBaseModel?.mPatternShowTokens?.set(idx, mPatternShowTokens[idx])
            mPatternHideTokens[idx] = compilePattern(mFilterHideTokens[idx], mPatternCase, mPatternHideTokens[idx], mMainUI.mTokenCombo[idx])
        }
    }

    private var mPatternSearchLog: Pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE)
    private var mMatcherSearchLog: Matcher = mPatternSearchLog.matcher("")
    private var mNormalSearchLogSplit: List<String>? = null

    private var mTableColor: ColorManager.TableColor
    private val mColumnNames = arrayOf("Line", "Process", "Log")
    protected var mLogItems:MutableList<LogItem> = mutableListOf()
    private var mBaseModel:LogTableModel? = baseModel
    var mLogFile:File? = null
    private val mLogCmdManager = LogCmdManager.getInstance()
    private val mBookmarkManager = BookmarkManager.getInstance()
    private val mFormatManager = FormatManager.getInstance()
    protected var mTokenFilters = mFormatManager.mCurrFormat.mTokenFilters
    protected var mSortedTokenFilters = mFormatManager.mCurrFormat.mSortedTokenFilters
        set(value) {
            if (!field.contentEquals(value)) {
                field = value
            }

            mSortedTokensIdxs = mFormatManager.mCurrFormat.mSortedTokensIdxs

            mTokenNthMax = mLevelIdx
            for (token in value) {
                if (token.mNth > mTokenNthMax) {
                    mTokenNthMax = token.mNth
                }
            }
            mEmptyTokenFilters = Array(value.size) { "" }
        }

    private var mSortedTokensIdxs = mFormatManager.mCurrFormat.mSortedTokensIdxs
    private var mTokenNthMax = 0
    protected var mEmptyTokenFilters = arrayOf("")
    protected var mLevelIdx = mFormatManager.mCurrFormat.mLevelNth
    protected var mSeparator = mFormatManager.mCurrFormat.mSeparator
    protected var mTokenCount = mFormatManager.mCurrFormat.mTokenCount

    private val mEventListeners = ArrayList<LogTableModelListener>()
    protected val mFilteredFGMap = mutableMapOf<String, FilteredColor>()
    protected val mFilteredBGMap = mutableMapOf<String, FilteredColor>()

    private var mIsFilterUpdated = true

    var mSelectionChanged = false

    private val mMainUI = mainUI

    var mFilterLevel = LEVEL_VERBOSE
        set(value) {
            if (field != value) {
                mIsFilterUpdated = true
            }
            field = if (mSeparator.isEmpty()) {
                LEVEL_NONE
            } else {
                value
            }
        }

    var mFilterLog: String = ""
        set(value) {
            if (field != value) {
                mIsFilterUpdated = true
                field = value
            }
            mMainUI.mShowLogCombo.mErrorMsg = ""
            val patterns = parsePattern(value, true)
            mFilterShowLog = patterns[0]
            mFilterHideLog = patterns[1]

            if (mBaseModel != null) {
                mBaseModel!!.mFilterLog = value
            }
        }

    private var mFilterShowLog: String = ""
        set(value) {
            field = value
            mPatternShowLog = compilePattern(value, mPatternCase, mPatternShowLog, mMainUI.mShowLogCombo)
        }

    private var mFilterHideLog: String = ""
        set(value) {
            field = value
            mPatternHideLog = compilePattern(value, mPatternCase, mPatternHideLog, mMainUI.mShowLogCombo)
        }

    var mFilterHighlightLog: String = ""
        set(value) {
            val patterns = parsePattern(value, false)
            if (field != patterns[0]) {
                mIsFilterUpdated = true
                field = patterns[0]
            }
        }

    private fun updateFilterSearchLog(field: String) {
        var normalSearchLog = ""
        val searchLogSplit = field.split("|")
        mRegexSearchLog = ""

        for (logUnit in searchLogSplit) {
            val hasIt: Boolean = logUnit.chars().anyMatch { c -> "\\.[]{}()*+?^$|".indexOf(c.toChar()) >= 0 }
            if (hasIt) {
                if (mRegexSearchLog.isEmpty()) {
                    mRegexSearchLog = logUnit
                }
                else {
                    mRegexSearchLog += "|$logUnit"
                }
            }
            else {
                if (normalSearchLog.isEmpty()) {
                    normalSearchLog = logUnit
                }
                else {
                    normalSearchLog += "|$logUnit"
                }

                if (mSearchPatternCase == Pattern.CASE_INSENSITIVE) {
                    normalSearchLog = normalSearchLog.uppercase()
                }
            }
        }

        mMainUI.mSearchPanel.mSearchCombo.mErrorMsg = ""
        mPatternSearchLog = compilePattern(mRegexSearchLog, mSearchPatternCase, mPatternSearchLog, mMainUI.mSearchPanel.mSearchCombo)
        mMatcherSearchLog = mPatternSearchLog.matcher("")

        mNormalSearchLogSplit = normalSearchLog.split("|")
    }

    var mFilterSearchLog: String = ""
        set(value) {
            val patterns = parsePattern(value, false)
            if (field != patterns[0]) {
                mIsFilterUpdated = true
                field = patterns[0]

                updateFilterSearchLog(field)
            }

            if (mBaseModel != null) {
                mBaseModel!!.mFilterSearchLog = value
            }
        }

    private var mPatternTriggerLog: Pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE)
    private var mTriggerPatternCase = Pattern.CASE_INSENSITIVE
    var mFilterTriggerLog: String = ""
        set(value) {
            field = value
            mPatternTriggerLog = compilePattern(value, mTriggerPatternCase, mPatternTriggerLog, null)
        }

    var mFilterTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { "" }
    var mFilterShowTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { "" }
    var mFilterHideTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { "" }
    var mPatternShowTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { Pattern.compile("", Pattern.CASE_INSENSITIVE) }
    var mPatternHideTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { Pattern.compile("", Pattern.CASE_INSENSITIVE) }
    var mBoldTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { false }
    var mBoldTokenEndIdx = -1
        set(value) {
            field = -1
            for(idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                if (mBoldTokens[idx]) {
                    field = idx
                }
            }
        }
    internal var mSortedPidTokIdx = mFormatManager.mCurrFormat.mSortedPidTokIdx
    val mFilterTokenMgr = FilterTokenManager()

    private var mPatternCase = Pattern.CASE_INSENSITIVE
    var mMatchCase: Boolean = false
        set(value) {
            if (field != value) {
                mPatternCase = if (!value) {
                    Pattern.CASE_INSENSITIVE
                } else {
                    0
                }

                mMainUI.mShowLogCombo.mErrorMsg = ""
                mPatternShowLog = compilePattern(mFilterShowLog, mPatternCase, mPatternShowLog, mMainUI.mShowLogCombo)
                mPatternHideLog = compilePattern(mFilterHideLog, mPatternCase, mPatternHideLog, mMainUI.mShowLogCombo)
                for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                    mMainUI.mTokenCombo[idx].mErrorMsg = ""
                    mPatternShowTokens[idx] = compilePattern(mFilterShowTokens[idx], mPatternCase, mPatternShowTokens[idx], mMainUI.mTokenCombo[idx])
                    mBaseModel?.mPatternShowTokens?.set(idx, mPatternShowTokens[idx])
                    mPatternHideTokens[idx] = compilePattern(mFilterHideTokens[idx], mPatternCase, mPatternHideTokens[idx], mMainUI.mTokenCombo[idx])
                }

                mIsFilterUpdated = true

                field = value
            }
        }

    private var mRegexSearchLog = ""
    private var mSearchPatternCase = Pattern.CASE_INSENSITIVE
    var mSearchMatchCase: Boolean = false
        set(value) {
            if (field != value) {
                mSearchPatternCase = if (!value) {
                    Pattern.CASE_INSENSITIVE
                } else {
                    0
                }

                mIsFilterUpdated = true

                field = value

                updateFilterSearchLog(mFilterSearchLog)

                if (mBaseModel != null) {
                    mBaseModel!!.mSearchMatchCase = value
                }
            }
        }

    var mGoToLast = true
//        set(value) {
//            field = value
//            Exception().printStackTrace()
//            Utils.printlnLog("tid = " + Thread.currentThread().id)
//        }
    var mBookmarkMode = false
        set(value) {
            field = value
            if (value) {
                mFullMode = false
            }
            mIsFilterUpdated = true
        }

    var mFullMode = false
        set(value) {
            field = value
            if (value) {
                mBookmarkMode = false
            }
            mIsFilterUpdated = true
        }

    var mScrollback = 0
        set(value) {
            field = value
            if (SwingUtilities.isEventDispatchThread()) {
                mLogItems.clear()
                mLogItems = mutableListOf()
                mBaseModel!!.mLogItems.clear()
                mBaseModel!!.mLogItems = mutableListOf()
                mBaseModel!!.mBookmarkManager.clear()
                fireLogTableDataCleared()
                mBaseModel!!.fireLogTableDataCleared()
            } else {
                SwingUtilities.invokeAndWait {
                    mLogItems.clear()
                    mLogItems = mutableListOf()
                    mBaseModel!!.mLogItems.clear()
                    mBaseModel!!.mLogItems = mutableListOf()
                    mBaseModel!!.mBookmarkManager.clear()
                    fireLogTableDataCleared()
                    mBaseModel!!.fireLogTableDataCleared()
                }
            }
        }

    var mScrollbackSplitFile = false

    var mScrollbackKeep = false

    private var mPatternShowLog: Pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE)
    private var mPatternHideLog: Pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE)

    protected var mLevelMap: Map<String, Int>
    init {
        mLevelMap = mFormatManager.mCurrFormat.mLevels
        mTokenNthMax = mLevelIdx
        for (token in mSortedTokenFilters) {
            if (token.mNth > mTokenNthMax) {
                mTokenNthMax = token.mNth
            }
        }
        mEmptyTokenFilters = Array(mSortedTokenFilters.size) { "" }

        if (mSeparator.isEmpty()) {
            mFilterLevel = LEVEL_NONE
        }

        mBaseModel = baseModel
        loadItems(false)

        mTableColor = if (isFullDataModel()) {
            ColorManager.getInstance().mFullTableColor
        }
        else {
            ColorManager.getInstance().mFilterTableColor
        }

        val colorEventListener = object: ColorManager.ColorEventListener{
            override fun colorChanged(event: ColorManager.ColorEvent?) {
                parsePattern(mFilterLog, true) // update color
                mIsFilterUpdated = true
            }
        }

        ColorManager.getInstance().addColorEventListener(colorEventListener)
    }

//    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
//        return true
//    }

    fun isFullDataModel(): Boolean {
        if (mBaseModel == null) {
            return true
        }

        return false
    }

    private fun parsePattern(pattern: String, isUpdateColor: Boolean) : Array<String> {
        val patterns: Array<String> = Array(2) { "" }

        val strs = pattern.split("|")
        var prevPatternIdx = -1
        if (isUpdateColor) {
            mFilteredFGMap.clear()
            mFilteredBGMap.clear()
        }

        for (item in strs) {
            if (prevPatternIdx != -1) {
                patterns[prevPatternIdx] += "|"
                patterns[prevPatternIdx] += item
                if (item.substring(item.length - 1) != "\\") {
                    prevPatternIdx = -1
                }
                continue
            }

            if (item.isNotEmpty()) {
                if (item[0] != '-') {
                    if (patterns[0].isNotEmpty()) {
                        patterns[0] += "|"
                    }

                    if (2 < item.length && item[0] == '#' && item[1].isDigit()) {
                        val key = item.substring(2)
                        patterns[0] += key
                        if (isUpdateColor) {
                            var patt: Pattern? = null
                            val hasIt: Boolean = key.uppercase().chars().anyMatch { c -> "\\.[]{}()*+?^$|".indexOf(c.toChar()) >= 0 }
                            if (hasIt) {
                                patt = Pattern.compile(key.uppercase(), Pattern.CASE_INSENSITIVE)
                            }

                            mFilteredFGMap[key.uppercase()] = FilteredColor(mTableColor.mStrFilteredFGs[item[1].digitToInt()], patt)
                            mFilteredBGMap[key.uppercase()] = FilteredColor(mTableColor.mStrFilteredBGs[item[1].digitToInt()], patt)
                        }
                    }
                    else {
                        patterns[0] += item
                    }

                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 0
                    }
                } else {
                    if (patterns[1].isNotEmpty()) {
                        patterns[1] += "|"
                    }

                    if (3 < item.length && item[1] == '#' && item[2].isDigit()) {
                        patterns[1] += item.substring(3)
                    }
                    else {
                        patterns[1] += item.substring(1)
                    }

                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 1
                    }
                }
            }
        }

        return patterns
    }

    private fun compilePattern(regex: String, flags: Int, prevPattern: Pattern, comboBox: FilterComboBox?): Pattern {
        var pattern = prevPattern
        try {
            pattern = Pattern.compile(regex, flags)
        } catch(ex: java.util.regex.PatternSyntaxException) {
            ex.printStackTrace()
            comboBox?.mErrorMsg = ex.message.toString()
        }

        return pattern
    }

    private var mFilteredItemsThread:Thread? = null
    fun loadItems(isAppend: Boolean) {
        if (mBaseModel == null) {
            if (SwingUtilities.isEventDispatchThread()) {
                loadFile(isAppend)
            } else {
                SwingUtilities.invokeAndWait {
                    loadFile(isAppend)
                }
            }
        }
        else {
            mIsFilterUpdated = true

            if (mFilteredItemsThread == null) {
                mFilteredItemsThread = Thread {
                    run {
                        while (true) {
                            try {
                                if (mIsFilterUpdated) {
                                    mMainUI.markLine()
                                    makeFilteredItems(true)
                                }
                                Thread.sleep(100)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                mFilteredItemsThread?.start()
            }
        }
    }

    fun clearItems() {
        Utils.printlnLog("isEventDispatchThread = ${SwingUtilities.isEventDispatchThread()}")

        if (mBaseModel != null) {
            mBaseModel!!.mGoToLast = true
            mGoToLast = true
            mBaseModel!!.mLogItems.clear()
            mBaseModel!!.mLogItems = mutableListOf()
            mBaseModel!!.mBookmarkManager.clear()
            mLogItems.clear()
            mLogItems = mutableListOf()

            fireLogTableDataCleared()
            mBaseModel!!.fireLogTableDataCleared()

            mIsFilterUpdated = true
            System.gc()
        }
    }

    fun setLogFile(path: String) {
        mLogFile = File(path)
    }

    private fun loadFile(isAppend: Boolean) {
        if (mLogFile == null) {
            return
        }

        var num = 0
        if (isAppend) {
            if (mLogItems.size > 0) {
                val item = mLogItems.last()
                num = item.mNum.toInt()
                num++
                mLogItems.add(LogItem(num.toString(), "LogNote - APPEND LOG : $mLogFile", LEVEL_ERROR, mEmptyTokenFilters, null, null))
                num++
            }
        } else {
            mLogItems.clear()
            mLogItems = mutableListOf()
            mBookmarkManager.clear()
        }

        val bufferedReader = BufferedReader(FileReader(mLogFile!!))
        var line: String?
        var level:Int
        var tokens: Array<String>

        line = bufferedReader.readLine()
        while (line != null) {
            mLogItems.add(makeLogItem(num, line))
            num++
            line = bufferedReader.readLine()
        }

        fireLogTableDataChanged()
    }

    private fun fireLogTableDataChanged(flags: Int) {
        fireLogTableDataChanged(LogTableModelEvent(this, LogTableModelEvent.EVENT_CHANGED, flags))
    }

    private fun fireLogTableDataChanged() {
        fireLogTableDataChanged(LogTableModelEvent(this, LogTableModelEvent.EVENT_CHANGED, 0))
    }

    private fun fireLogTableDataFiltered() {
        fireLogTableDataChanged(LogTableModelEvent(this, LogTableModelEvent.EVENT_FILTERED, 0))
    }

    private fun fireLogTableDataCleared() {
        fireLogTableDataChanged(LogTableModelEvent(this, LogTableModelEvent.EVENT_CLEARED, 0))
    }

    private fun fireLogTableDataChanged(event:LogTableModelEvent) {
        for (listener in mEventListeners) {
            listener.tableChanged(event)
        }
    }

    fun addLogTableModelListener(eventListener:LogTableModelListener) {
        mEventListeners.add(eventListener)
    }

    override fun getRowCount(): Int {
        return mLogItems.size
    }

    override fun getColumnCount(): Int {
        return COLUMN_COUNT
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        try {
            if (rowIndex >= 0 && mLogItems.size > rowIndex) {
                val logItem = mLogItems[rowIndex]
                when (columnIndex) {
                    COLUMN_NUM -> {
                        return logItem.mNum + " "
                    }
                    COLUMN_PROCESS_NAME -> {
                        if (IsShowProcessName) {
                            if (logItem.mProcessName == null) {
                                if ((mSortedPidTokIdx >= 0) && (logItem.mTokenFilterLogs.size > mSortedPidTokIdx)) {
                                    return if (logItem.mTokenFilterLogs[mSortedPidTokIdx] == "0") {
                                        "0"
                                    } else {
                                        ProcessList.getInstance()
                                            .getProcessName(logItem.mTokenFilterLogs[mSortedPidTokIdx]) ?: ""
                                    }
                                }
                            } else {
                                return logItem.mProcessName
                            }
                        }
                        else {
                            return ""
                        }

                    }
                    COLUMN_LOG_START -> {
                        return logItem.mLogLine
                    }
                }
            }
        } catch (e:ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return -1
    }

    override fun getColumnName(column: Int): String {
        return mColumnNames[column]
    }

    fun getFgColor(row: Int) : Color {
        return when (mLogItems[row].mLevel) {
            LEVEL_VERBOSE -> {
                mTableColor.mLogLevelVerbose
            }
            LEVEL_DEBUG -> {
                mTableColor.mLogLevelDebug
            }
            LEVEL_INFO -> {
                mTableColor.mLogLevelInfo
            }
            LEVEL_WARNING -> {
                mTableColor.mLogLevelWarning
            }
            LEVEL_ERROR -> {
                mTableColor.mLogLevelError
            }
            LEVEL_FATAL -> {
                mTableColor.mLogLevelFatal
            }
            else -> {
                mTableColor.mLogLevelNone
            }
        }
    }

    private fun getFgStrColor(row: Int) : String {
        return when (mLogItems[row].mLevel) {
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

    private var mPatternPrintSearch:Pattern? = null
    private var mPatternPrintHighlight:Pattern? = null
    protected var mPatternPrintFilter:Pattern? = null

    open fun getPatternPrintFilter(col: Int): Pattern? {
        if (col >= COLUMN_LOG_START) {
            return mPatternPrintFilter
        }
        return null
    }

    open fun getPrintValue(value: String, row: Int, col: Int, isSelected: Boolean) : String {
        var newValue = value
        if (newValue.indexOf("<") >= 0) {
            newValue = newValue.replace("<", "&lt;")
        }
        if (newValue.indexOf(">") >= 0) {
            newValue = newValue.replace(">", "&gt;")
        }

        val stringBuilder = StringBuilder(newValue)

        val searchStarts: Queue<Int> = LinkedList()
        val searchEnds: Queue<Int> = LinkedList()
        if (mPatternPrintSearch != null) {
            val matcher = mPatternPrintSearch!!.matcher(stringBuilder.toString())
            while (matcher.find()) {
                searchStarts.add(matcher.start(0))
                searchEnds.add(matcher.end(0))
            }
        }
        
        val highlightStarts: Queue<Int> = LinkedList()
        val highlightEnds: Queue<Int> = LinkedList()
        if (mPatternPrintHighlight != null) {
            val matcher = mPatternPrintHighlight!!.matcher(stringBuilder.toString())
            while (matcher.find()) {
                highlightStarts.add(matcher.start(0))
                highlightEnds.add(matcher.end(0))
            }
        }

        val filterStarts: Queue<Int> = LinkedList()
        val filterEnds: Queue<Int> = LinkedList()
        val patternPrintFilter = getPatternPrintFilter(col)
        if (patternPrintFilter != null) {
            val matcher = patternPrintFilter.matcher(stringBuilder.toString())
            while (matcher.find()) {
                filterStarts.add(matcher.start(0))
                filterEnds.add(matcher.end(0))
            }
        }

        val boldStarts: Queue<Int> = LinkedList()
        val boldEnds: Queue<Int> = LinkedList()
        val boldStartTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { -1 }
        val boldEndTokens = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { -1 }

        if (mBoldTokenEndIdx >= 0) {
            var currPos = 0
            val item = mLogItems[row]
            for (idx in 0 .. mBoldTokenEndIdx) {
                if (item.mTokenFilterLogs[idx].isNotEmpty()) {
                    currPos = newValue.indexOf(item.mTokenFilterLogs[idx], currPos)
                    if (mBoldTokens[idx]) {
                        boldStartTokens[idx] = currPos
                        boldEndTokens[idx] = boldStartTokens[idx] + item.mTokenFilterLogs[idx].length
                        boldStarts.add(boldStartTokens[idx])
                        boldEnds.add(boldEndTokens[idx])
                    }
                    currPos++
                }
            }
        }

        val starts = Stack<Int>()
        val ends = Stack<Int>()
        val fgColors = Stack<String>()
        val bgColors = Stack<String>()

        var searchS = -1
        var searchE = -1
        var highlightS = -1
        var highlightE = -1
        var highlightSNext = -1
        var highlightENext = -1
        var filterS = -1
        var filterSNext = -1
        var filterENext = -1
        var filterE = -1
        var boldS = -1
        var boldE = -1
        var boldSNext = -1
        var boldENext = -1
        var boldSCheck = -1
        var boldECheck = -1

        for (idx in newValue.indices) {
            while (searchE <= idx) {
                if (searchStarts.size > 0) {
                    searchS = searchStarts.poll()
                    searchE = searchEnds.poll()

                    if (idx in (searchS + 1) until searchE) {
                        searchS = idx
                    }
                }
                else {
                    searchS = -1
                    searchE = -1
                    break
                }
            }
            while (highlightE <= idx) {
                if (highlightStarts.size > 0) {
                    highlightS = highlightStarts.poll()
                    highlightE = highlightEnds.poll()

                    if (idx in (highlightS + 1) until highlightE) {
                        highlightS = idx
                    }
                }
                else {
                    highlightS = -1
                    highlightE = -1
                    break
                }
            }
            while (filterE <= idx) {
                if (filterStarts.size > 0) {
                    filterS = filterStarts.poll()
                    filterE = filterEnds.poll()

                    if (idx in (filterS + 1) until filterE) {
                        filterS = idx
                    }
                }
                else {
                    filterS = -1
                    filterE = -1
                    break
                }
            }
            while (boldE <= idx) {
                if (boldStarts.size > 0) {
                    boldS = boldStarts.poll()
                    boldE = boldEnds.poll()

                    if (idx in (boldS + 1) until boldE) {
                        boldS = idx
                    }
                }
                else {
                    boldS = -1
                    boldE = -1
                    break
                }
            }

            if (idx == searchS) {
                if (searchE in (highlightS + 1) until highlightE) {
                    highlightS = searchE
                }
                
                if (searchE in (filterS + 1) until filterE) {
                    filterS = searchE
                }

                if (searchE in (boldS + 1) until boldE) {
                    boldS = searchE
                }
                starts.push(searchS)
                ends.push(searchE)
                fgColors.push(mTableColor.mStrSearchFG)
                bgColors.push(mTableColor.mStrSearchBG)
            }

            if (idx in searchS until searchE) {
                continue
            }
            
            if (idx == highlightS) {
                if (highlightE in (filterS + 1) until filterE) {
                    filterS = highlightE
                }

                if (highlightE in (boldS + 1) until boldE) {
                    boldS = highlightE
                }

                if (searchS in 1 until highlightE) {
                    if (highlightE > searchE) {
                        highlightSNext = searchE
                        highlightENext = highlightE
                    }
                    highlightE = searchS
                }
                
                starts.push(highlightS)
                ends.push(highlightE)
                fgColors.push(mTableColor.mStrHighlightFG)
                bgColors.push(mTableColor.mStrHighlightBG)

                if (highlightS < highlightSNext) {
                    highlightS = highlightSNext
                }
                if (highlightE < highlightENext) {
                    highlightE = highlightENext
                }
            }

            if (idx in highlightS until highlightE) {
                continue
            }

            if (idx == filterS) {
                if (filterE in (boldS + 1) until boldE) {
                    boldS = filterE
                }

                if (searchS > filterS && highlightS > filterS) {
                    if (searchS < highlightS) {
                        if (searchS in filterS until filterE) {
                            if (filterE > searchE) {
                                filterSNext = searchE
                                filterENext = filterE
                            }
                            filterE = searchS
                        }
                    }
                    else {
                        if (highlightS in filterS until filterE) {
                            if (filterE > highlightE) {
                                filterSNext = highlightE
                                filterENext = filterE
                            }
                            filterE = highlightS
                        }
                    }
                }
                else if (searchS > filterS) {
                    if (searchS in filterS until filterE) {
                        if (filterE > searchE) {
                            filterSNext = searchE
                            filterENext = filterE
                        }
                        filterE = searchS
                    }
                }
                else if (highlightS > filterS){
                    if (highlightS in filterS until filterE) {
                        if (filterE > highlightE) {
                            filterSNext = highlightE
                            filterENext = filterE
                        }
                        filterE = highlightS
                    }
                }

                starts.push(filterS)
                ends.push(filterE)
                val key = newValue.substring(filterS, filterE).uppercase()

                if (mFilteredFGMap[key] != null) {
                    fgColors.push(mFilteredFGMap[key]!!.mColor)
                    bgColors.push(mFilteredBGMap[key]!!.mColor)
                }
                else if (IsColorTagRegex) {
                    var isFind = false
                    for (item in mFilteredFGMap.keys) {
                        val pattern = mFilteredFGMap[item]?.mPattern
                        if ((pattern != null) && pattern.matcher(key).find()) {
                            fgColors.push(mFilteredFGMap[item]!!.mColor)
                            bgColors.push(mFilteredBGMap[item]!!.mColor)
                            isFind = true
                            break
                        }
                    }
                    if (!isFind) {
                        fgColors.push(mTableColor.mStrFilteredFGs[0])
                        bgColors.push(mTableColor.mStrFilteredBGs[0])
                    }
                }
                else {
                    fgColors.push(mTableColor.mStrFilteredFGs[0])
                    bgColors.push(mTableColor.mStrFilteredBGs[0])
                }

                if (filterS < filterSNext) {
                    filterS = filterSNext
                }
                if (filterE < filterENext) {
                    filterE = filterENext
                }
            }

            if (idx in filterS until filterE) {
                continue
            }

            if (idx == boldS) {
                boldSCheck = -1
                boldECheck = -1
                if (highlightS in (boldS + 1) until boldE) {
                    boldSCheck = highlightS
                    boldECheck = highlightE
                }

                if (filterS in (boldS + 1) until boldE && filterS < highlightS) {
                    boldSCheck = filterS
                    boldECheck = filterE
                }

                if (boldSCheck in 1 until boldE) {
                    if (boldE > boldECheck) {
                        boldSNext = boldECheck
                        boldENext = boldE
                    }
                    boldE = boldSCheck
                }

                starts.push(boldS)
                ends.push(boldE)

                when (boldS) {
                    in boldStartTokens[0] until boldEndTokens[0] -> {
                        fgColors.push(mTableColor.mStrToken0FG)
                        bgColors.push(mTableColor.mStrLogBG)
                    }
                    in boldStartTokens[1] until boldEndTokens[1] -> {
                        fgColors.push(mTableColor.mStrToken1FG)
                        bgColors.push(mTableColor.mStrLogBG)
                    }
                    in boldStartTokens[2] until boldEndTokens[2] -> {
                        fgColors.push(mTableColor.mStrToken2FG)
                        bgColors.push(mTableColor.mStrLogBG)
                    }
                }

                if (boldS < boldSNext) {
                    boldS = boldSNext
                }
                if (boldE < boldENext) {
                    boldE = boldENext
                }
            }
        }

        if (starts.size == 0) {
            if (newValue == value) {
                return ""
            }
            stringBuilder.replace(0, newValue.length, newValue.replace(" ", "&nbsp;"))
        }
        else {
            var beforeStart = 0
            var isFirst = true
            while (!starts.isEmpty()) {
                val start = starts.pop()
                val end = ends.pop()

                val fgColor = fgColors.pop()
                var bgColor = bgColors.pop()

                if (isFirst) {
                    if (end < newValue.length) {
                        stringBuilder.replace(
                                end,
                                newValue.length,
                                newValue.substring(end, newValue.length).replace(" ", "&nbsp;")
                        )
                    }
                    isFirst = false
                }
                if (beforeStart > end) {
                    stringBuilder.replace(
                            end,
                            beforeStart,
                            newValue.substring(end, beforeStart).replace(" ", "&nbsp;")
                    )
                }
                if (start >= 0 && end >= 0) {
                    if (isSelected) {
                        val tmpColor = Color.decode(bgColor)
                        Color(tmpColor.red / 2 + mTableColor.mSelectedBG.red / 2, tmpColor.green / 2 + mTableColor.mSelectedBG.green / 2, tmpColor.blue / 2 + mTableColor.mSelectedBG.blue / 2)
                        bgColor = "#" + Integer.toHexString(Color(
                                tmpColor.red / 2 + mTableColor.mSelectedBG.red / 2,
                                tmpColor.green / 2 + mTableColor.mSelectedBG.green / 2,
                                tmpColor.blue / 2 + mTableColor.mSelectedBG.blue / 2).rgb).substring(2).uppercase()
                    }

                    stringBuilder.replace(
                            end,
                            end,
                            newValue.substring(end, end) + "</font></b>"
                    )
                    stringBuilder.replace(
                            start,
                            end,
                            newValue.substring(start, end).replace(" ", "&nbsp;")
                    )
                    stringBuilder.replace(
                            start,
                            start,
                            "<b><font style=\"color: $fgColor; background-color: $bgColor\">" + newValue.substring(start, start)
                    )
                }
                beforeStart = start
            }
            if (beforeStart > 0) {
                stringBuilder.replace(0, beforeStart, newValue.substring(0, beforeStart).replace(" ", "&nbsp;"))
            }
        }

        val color = getFgStrColor(row)
        stringBuilder.replace(0, 0, "<html><p><nobr><font color=$color>")
        stringBuilder.append("</font></nobr></p></html>")
        return stringBuilder.toString()
    }

    inner class LogItem(val mNum: String, val mLogLine: String, val mLevel: Int, val mTokenFilterLogs: Array<String>, val mTokenLogs: List<String>?, val mProcessName: String?) {
    }

    open fun makeLogItem(num: Int, logLine: String): LogItem {
        val level: Int
        val tokenFilterLogs: Array<String>

        if (mFilterLevel == LEVEL_NONE) {
            level = LEVEL_NONE
            tokenFilterLogs = mEmptyTokenFilters
        }
        else {
            val textSplited = logLine.trim().split(Regex(mSeparator), mTokenCount)
            if (textSplited.size > mTokenNthMax) {
                level = mLevelMap[textSplited[mLevelIdx]] ?: LEVEL_NONE
                tokenFilterLogs = Array(mSortedTokenFilters.size) {
                    if (mSortedTokenFilters[it].mNth >= 0) {
                        textSplited[mSortedTokenFilters[it].mNth]
                    }
                    else {
                        ""
                    }
                }
            } else {
                level = LEVEL_NONE
                tokenFilterLogs = mEmptyTokenFilters
            }
        }

        val processName = if (IsShowProcessName && mSortedPidTokIdx >= 0 && tokenFilterLogs.size > mSortedPidTokIdx) {
            ProcessList.getInstance().getProcessName(tokenFilterLogs[mSortedPidTokIdx])
        } else {
            null
        }

        return LogItem(num.toString(), logLine, level, tokenFilterLogs, null, processName)
    }

    private fun makePattenPrintValue() {
        if (mBaseModel == null) {
            return
        }

        mBaseModel?.mFilterSearchLog = mFilterSearchLog
        if (mFilterSearchLog.isEmpty()) {
            mPatternPrintSearch = null
            mBaseModel?.mPatternPrintSearch = null
        } else {
            var start = 0
            var index = 0
            var skip = false

            while (index != -1) {
                index = mFilterSearchLog.indexOf('|', start)
                start = index + 1
                if (index == 0 || index == mFilterSearchLog.lastIndex || mFilterSearchLog[index + 1] == '|') {
                    skip = true
                    break
                }
            }

            if (!skip) {
                mPatternPrintSearch = Pattern.compile(mFilterSearchLog, mSearchPatternCase)
                mBaseModel?.mPatternPrintSearch = mPatternPrintSearch
            }
        }

        mBaseModel?.mFilterHighlightLog = mFilterHighlightLog
        if (mFilterHighlightLog.isEmpty()) {
            mPatternPrintHighlight = null
            mBaseModel?.mPatternPrintHighlight = null
        } else {
            var start = 0
            var index = 0
            var skip = false

            while (index != -1) {
                index = mFilterHighlightLog.indexOf('|', start)
                start = index + 1
                if (index == 0 || index == mFilterHighlightLog.lastIndex || mFilterHighlightLog[index + 1] == '|') {
                    skip = true
                    break
                }
            }

            if (!skip) {
                mPatternPrintHighlight = Pattern.compile(mFilterHighlightLog, mPatternCase)
                mBaseModel?.mPatternPrintHighlight = mPatternPrintHighlight
            }
        }

        if (mFilterShowLog.isEmpty()) {
            mPatternPrintFilter = null
            mBaseModel?.mPatternPrintFilter = null
        } else {
            var start = 0
            var index = 0
            var skip = false

            while (index != -1) {
                index = mFilterShowLog.indexOf('|', start)
                start = index + 1
                if (index == 0 || index == mFilterShowLog.lastIndex || mFilterShowLog[index + 1] == '|') {
                    skip = true
                    break
                }
            }

            if (!skip) {
                mPatternPrintFilter = Pattern.compile(mFilterShowLog, mPatternCase)
                mBaseModel?.mPatternPrintFilter = mPatternPrintFilter
            }
        }

        return
    }

    private fun isMatchHideToken(item: LogItem): Boolean {
        var isMatch = false
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mFilterHideTokens[idx].isNotEmpty() && mPatternHideTokens[idx].matcher(item.mTokenFilterLogs[mSortedTokensIdxs[idx]]).find()) {
                isMatch = true
                break
            }
        }
        return isMatch
    }

    private fun isNotMatchShowToken(item: LogItem): Boolean {
        var isNotMatch = false
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mFilterShowTokens[idx].isNotEmpty() && mSortedTokensIdxs[idx] >= 0 && !mPatternShowTokens[idx].matcher(item.mTokenFilterLogs[mSortedTokensIdxs[idx]]).find()) {
                isNotMatch = true
                break
            }
        }
        return isNotMatch
    }

    private fun makeFilteredItems(isRedraw: Boolean) {
        if (mBaseModel == null || !mIsFilterUpdated) {
            Utils.printlnLog("skip makeFilteredItems $mBaseModel, $mIsFilterUpdated")
            return
        }
        else {
            mIsFilterUpdated = false
        }

        SwingUtilities.invokeAndWait {
            mLogItems.clear()
            mLogItems = mutableListOf()

            val logItems:MutableList<LogItem> = mutableListOf()
            if (mBookmarkMode) {
                for (item in mBaseModel!!.mLogItems) {
                    if (mBookmarkManager.mBookmarks.contains(item.mNum.toInt())) {
                        logItems.add(item)
                    }
                }
            } else {
                makePattenPrintValue()
                var isShow: Boolean

                var regexShowLog = ""
                var normalShowLog = ""
                val showLogSplit = mFilterShowLog.split("|")

                for (logUnit in showLogSplit) {
                    val hasIt: Boolean = logUnit.chars().anyMatch { c -> "\\.[]{}()*+?^$|".indexOf(c.toChar()) >= 0 }
                    if (hasIt) {
                        if (regexShowLog.isEmpty()) {
                            regexShowLog = logUnit
                        }
                        else {
                            regexShowLog += "|$logUnit"
                        }
                    }
                    else {
                        if (normalShowLog.isEmpty()) {
                            normalShowLog = logUnit
                        }
                        else {
                            normalShowLog += "|$logUnit"
                        }

                        if (mPatternCase == Pattern.CASE_INSENSITIVE) {
                            normalShowLog = normalShowLog.uppercase()
                        }
                    }
                }

                val patternShowLog = Pattern.compile(regexShowLog, mPatternCase)
                val matcherShowLog = patternShowLog.matcher("")
                val normalShowLogSplit = normalShowLog.split("|")

                Utils.printlnLog("Show Log $normalShowLog, $regexShowLog")
                for (item in mBaseModel!!.mLogItems) {
                    if (mIsFilterUpdated) {
                        break
                    }

                    isShow = true

                    if (!mFullMode) {
                        if (item.mLevel != LEVEL_NONE && item.mLevel < mFilterLevel) {
                            isShow = false
                        }
                        else if ((mFilterHideLog.isNotEmpty() && mPatternHideLog.matcher(item.mLogLine).find())
                            || isMatchHideToken(item)) {
                            isShow = false
                        }
                        else if (mFilterShowLog.isNotEmpty()) {
                            var isFound = false
                            if (normalShowLog.isNotEmpty()) {
                                val logLine = if (mPatternCase == Pattern.CASE_INSENSITIVE) {
                                    item.mLogLine.uppercase()
                                } else {
                                    item.mLogLine
                                }
                                for (sp in normalShowLogSplit) {
                                    if (logLine.contains(sp)) {
                                        isFound = true
                                        break
                                    }
                                }
                            }

                            if (!isFound) {
                                if (regexShowLog.isEmpty()) {
                                    isShow = false
                                }
                                else {
                                    matcherShowLog.reset(item.mLogLine)
                                    if (!matcherShowLog.find()) {
                                        isShow = false
                                    }
                                }
                            }
                        }

                        if (isShow) {
                            if (isNotMatchShowToken(item)) {
                                isShow = false
                            }
                        }
                    }

                    if (isShow || mBookmarkManager.mBookmarks.contains(item.mNum.toInt())) {
                        logItems.add(item)
                    }
                }
            }

            mLogItems = logItems
        }

        if (!mIsFilterUpdated && isRedraw) {
            fireLogTableDataFiltered()
            mBaseModel?.fireLogTableDataFiltered()
        }
    }

    internal inner class LogFilterItem(item: LogItem, isShow: Boolean) {
        val mItem = item
        val mIsShow = isShow
    }

    private var mScanThread:Thread? = null
    private var mFileWriter:FileWriter? = null
    private var mIsPause = false

    fun isScanning(): Boolean {
        return mScanThread != null
    }

    private fun updateLogItems(logLines: MutableList<String>, startNum: Int): Int {
        var removedCount = 0
        var baseRemovedCount = 0
        var num = startNum
        var isShow: Boolean
        var item: LogItem
        val logFilterItems: MutableList<LogFilterItem> = mutableListOf()

        synchronized(this) {
            for (tempLine in logLines) {
                item = makeLogItem(num, tempLine)
                isShow = true

                if (mBookmarkMode) {
                    isShow = false
                }

                if (!mFullMode) {
                    if (isShow && item.mLevel != LEVEL_NONE && item.mLevel < mFilterLevel) {
                        isShow = false
                    }
                    if (isShow
                        && ((mFilterHideLog.isNotEmpty() && mPatternHideLog.matcher(item.mLogLine)
                            .find())
                                || (mFilterShowLog.isNotEmpty() && !mPatternShowLog.matcher(item.mLogLine)
                            .find()))
                    ) {
                        isShow = false
                    }
                    if (isShow
                        && (isMatchHideToken(item) || isNotMatchShowToken(item))
                    ) {
                        isShow = false
                    }
                }
                logFilterItems.add(LogFilterItem(item, isShow))
                num++
            }
        }

        SwingUtilities.invokeAndWait {
            if (mScanThread == null) {
                return@invokeAndWait
            }
            val isRemoveItem = !mScrollbackKeep && mScrollback > 0 && WaitTimeForDoubleClick < System.currentTimeMillis()

            for (filterItem in logFilterItems) {
                if (mSelectionChanged) {
                    baseRemovedCount = 0
                    removedCount = 0
                    mSelectionChanged = false
                }

                if (mFilterTriggerLog.isNotEmpty() && mPatternTriggerLog.matcher(filterItem.mItem.mLogLine).find()) {
                    mAgingTestManager.pullTheTrigger(filterItem.mItem.mLogLine)
                }
                mBaseModel!!.mLogItems.add(filterItem.mItem)
                if (filterItem.mIsShow || mBookmarkManager.mBookmarks.contains(filterItem.mItem.mNum.toInt())) {
                    mLogItems.add(filterItem.mItem)
                }
            }
            if (isRemoveItem) {
                while (mBaseModel!!.mLogItems.count() > mScrollback) {
                    mBaseModel!!.mLogItems.removeAt(0)
                    baseRemovedCount++
                }

                while (mLogItems.count() > mScrollback) {
                    mLogItems.removeAt(0)
                    removedCount++
                }
                fireLogTableDataChanged(removedCount)
                removedCount = 0

                mBaseModel!!.fireLogTableDataChanged(baseRemovedCount)
                baseRemovedCount = 0
            }
        }

        return num
    }
    fun startScan() {
        if (mLogFile == null) {
            return
        }

        if (SwingUtilities.isEventDispatchThread()) {
            mScanThread?.interrupt()
        }
        else {
            SwingUtilities.invokeAndWait {
                mScanThread?.interrupt()
            }
        }

        mGoToLast = true
        mBaseModel?.mGoToLast = true

        mScanThread = Thread {
            run {
                Utils.printlnLog("startScan thread started")
                SwingUtilities.invokeAndWait {
                    mLogItems.clear()
                    mLogItems = mutableListOf()
                    mBaseModel!!.mLogItems.clear()
                    mBaseModel!!.mLogItems = mutableListOf()
                    mBaseModel!!.mBookmarkManager.clear()
                    fireLogTableDataCleared()
                    mBaseModel!!.fireLogTableDataCleared()
                }
                fireLogTableDataChanged()
                mBaseModel!!.fireLogTableDataChanged()
                makePattenPrintValue()

                if (CurrentMethod == METHOD_ADB && IsShowProcessName) {
                    ProcessList.getInstance().getProcess("0")
                }
                var currLogFile: File? = mLogFile
                var bufferedReader = BufferedReader(InputStreamReader(mLogCmdManager.mProcessLogcat!!.inputStream))
                var line: String?
                var saveNum = 0
                var startNum = 0

                var nextUpdateTime: Long = 0

                val logLines: MutableList<String> = mutableListOf()

                line = bufferedReader.readLine()
                while (line != null || mMainUI.isRestartAdbLogcat()) {
                    try {
                        nextUpdateTime = System.currentTimeMillis() + 100
                        logLines.clear()

                        if (line == null && mMainUI.isRestartAdbLogcat()) {
                            Utils.printlnLog("line is Null : $line")
                            if (mLogCmdManager.mProcessLogcat == null || !mLogCmdManager.mProcessLogcat!!.isAlive) {
                                if (mMainUI.isRestartAdbLogcat()) {
                                    Thread.sleep(5000)
                                    mMainUI.restartAdbLogcat()
                                    if (mLogCmdManager.mProcessLogcat?.inputStream != null) {
                                        bufferedReader =
                                            BufferedReader(InputStreamReader(mLogCmdManager.mProcessLogcat?.inputStream!!))
                                    } else {
                                        Utils.printlnLog("startScan : inputStream is Null")
                                    }
                                    line = "LogNote - RESTART LOGCAT"
                                }
                            }
                        }

                        if (!mIsPause) {
                            while (line != null) {
                                if (currLogFile != mLogFile) {
                                    try {
                                        mFileWriter?.flush()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    mFileWriter?.close()
                                    mFileWriter = null
                                    currLogFile = mLogFile
                                    saveNum = 0
                                }

                                if (mFileWriter == null) {
                                    mFileWriter = FileWriter(mLogFile)
                                }
                                mFileWriter?.write(line + "\n")
                                saveNum++

                                if (mScrollbackSplitFile && mScrollback > 0 && saveNum >= mScrollback) {
                                    mMainUI.setSaveLogFile()
                                    Utils.printlnLog("Change save file : ${mLogFile?.absolutePath}")
                                }

                                logLines.add(line)
                                line = bufferedReader.readLine()
                                if (System.currentTimeMillis() > nextUpdateTime) {
                                    break
                                }
                            }
                        } else {
                            Thread.sleep(1000)
                        }

                        startNum = updateLogItems(logLines, startNum)
                    } catch (e: Exception) {
                        Utils.printlnLog("startScan thread stop")
                        Utils.printlnLog("stack trace : ${e.stackTraceToString()}")

                        if (e !is InterruptedException) {
                            JOptionPane.showMessageDialog(mMainUI, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                        }

                        try {
                            mFileWriter?.flush()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        mFileWriter?.close()
                        mFileWriter = null
                        return@run
                    }
                }
            }
        }

        mScanThread?.start()

        return
    }

    fun stopScan(){
        if (SwingUtilities.isEventDispatchThread()) {
            mScanThread?.interrupt()
        }
        else {
            SwingUtilities.invokeAndWait {
                mScanThread?.interrupt()
            }
        }
        mScanThread = null
        if (mFileWriter != null) {
            try {
                mFileWriter?.flush()
            } catch(e:IOException) {
                e.printStackTrace()
            }
            mFileWriter?.close()
            mFileWriter?.close()
            mFileWriter = null
        }
        return
    }

    fun pauseScan(pause:Boolean) {
        Utils.printlnLog("Pause adb scan $pause")
        mIsPause = pause
    }

    private var mFollowThread:Thread? = null
    private var mIsFollowPause = false
    private var mIsKeepReading = true

    fun isFollowing(): Boolean {
        return mFollowThread != null
    }

    internal inner class MyFileInputStream(currLogFile: File?) : FileInputStream(currLogFile) {
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            var input = super.read(b, off, len)
            while (input == -1) {
                Thread.sleep(1000)
                input = super.read(b, off, len)
            }
            return input
        }
    }

    fun startFollow() {
        if (mLogFile == null) {
            return
        }

        if (SwingUtilities.isEventDispatchThread()) {
            mFollowThread?.interrupt()
        }
        else {
            SwingUtilities.invokeAndWait {
                mFollowThread?.interrupt()
            }
        }

        mGoToLast = true
        mBaseModel?.mGoToLast = true

        mFollowThread = Thread {
            run {
                SwingUtilities.invokeAndWait {
                    mIsKeepReading = true
                    mLogItems.clear()
                    mLogItems = mutableListOf()
                    mBaseModel!!.mLogItems.clear()
                    mBaseModel!!.mLogItems = mutableListOf()
                    mBaseModel!!.mBookmarkManager.clear()
                    fireLogTableDataCleared()
                    mBaseModel!!.fireLogTableDataCleared()
                }
                fireLogTableDataChanged()
                mBaseModel!!.fireLogTableDataChanged()
                makePattenPrintValue()

                val currLogFile: File? = mLogFile
                val scanner = Scanner(MyFileInputStream(currLogFile))
                var line: String? = null
                var startNum = 0

                var nextUpdateTime: Long = 0

                val logLines: MutableList<String> = mutableListOf()

                while (mIsKeepReading) {
                    try {
                        nextUpdateTime = System.currentTimeMillis() + 100
                        logLines.clear()
                        if (!mIsPause) {
                            while (mIsKeepReading) {
                                if (scanner.hasNextLine()) {
                                    line = try {
                                        scanner.nextLine()
                                    } catch (e: NoSuchElementException) {
                                        null
                                    }
                                } else {
                                    line = null
                                }
                                if (line == null) {
                                    Thread.sleep(1000)
                                } else {
                                    break
                                }
                            }

                            while (line != null) {
                                logLines.add(line)

                                if (scanner.hasNextLine()) {
                                    line = try {
                                        scanner.nextLine()
                                    } catch (e: NoSuchElementException) {
                                        null
                                    }
                                } else {
                                    line = null
                                }
                                if (System.currentTimeMillis() > nextUpdateTime) {
                                    if (line != null) {
                                        logLines.add(line)
                                    }
                                    break
                                }
                            }
                        } else {
                            Thread.sleep(1000)
                        }

                        startNum = updateLogItems(logLines, startNum)
                    } catch (e: Exception) {
                        Utils.printlnLog("startFollow thread stop")
                        Utils.printlnLog("stack trace : ${e.stackTraceToString()}")

                        if (e !is InterruptedException) {
                            JOptionPane.showMessageDialog(mMainUI, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                        }

                        return@run
                    }
                }
                Utils.printlnLog("Exit follow")
            }
        }

        mFollowThread?.start()

        return
    }

    fun stopFollow(){
        if (SwingUtilities.isEventDispatchThread()) {
            mIsKeepReading = false
            mFollowThread?.interrupt()
        }
        else {
            SwingUtilities.invokeAndWait {
                mIsKeepReading = false
                mFollowThread?.interrupt()
            }
        }
        mFollowThread = null
        return
    }

    fun pauseFollow(pause:Boolean) {
        Utils.printlnLog("Pause file follow $pause")
        mIsFollowPause = pause
    }

    fun moveToNextSearch() {
        moveToSearch(true)
    }

    fun moveToPrevSearch() {
        moveToSearch(false)
    }

    private infix fun Int.toward(to: Int): IntProgression {
        val step = if (this > to) -1 else 1
        return IntProgression.fromClosedRange(this, to, step)
    }

    private fun moveToSearch(isNext: Boolean) {
        val selectedRow = if (mBaseModel != null) {
            mMainUI.mFilteredLogPanel.getSelectedRow()
        }
        else {
            mMainUI.mFullLogPanel.getSelectedRow()
        }

        var startRow = 0
        var endRow = 0

        if (isNext) {
            startRow = selectedRow + 1
            endRow = mLogItems.count() - 1
            if (startRow >= endRow) {
                mMainUI.showSearchResultTooltip(isNext,"\"${mFilterSearchLog}\" ${Strings.NOT_FOUND}")
                return
            }
        }
        else {
            startRow = selectedRow - 1
            endRow = 0

            if (startRow < endRow) {
                mMainUI.showSearchResultTooltip(isNext,"\"${mFilterSearchLog}\" ${Strings.NOT_FOUND}")
                return
            }
        }

        var idxFound = -1
        for (idx in startRow toward endRow) {
            val item = mLogItems[idx]
            if (mNormalSearchLogSplit != null) {
                var logLine = ""
                logLine = if (mSearchPatternCase == Pattern.CASE_INSENSITIVE) {
                    item.mLogLine.uppercase()
                } else {
                    item.mLogLine
                }
                for (sp in mNormalSearchLogSplit!!) {
                    if (sp.isNotEmpty() && logLine.contains(sp)) {
                        idxFound = idx
                        break
                    }
                }
            }

            if (idxFound < 0 && mRegexSearchLog.isNotEmpty()) {
                mMatcherSearchLog.reset(item.mLogLine)
                if (mMatcherSearchLog.find()) {
                    idxFound = idx
                }
            }

            if (idxFound >= 0) {
                break
            }
        }

        if (idxFound >= 0) {
            if (mBaseModel != null) {
                mMainUI.mFilteredLogPanel.goToRow(idxFound, 0)
            }
            else {
                mMainUI.mFullLogPanel.goToRow(idxFound, 0)
            }
        }
        else {
            mMainUI.showSearchResultTooltip(isNext,"\"${mFilterSearchLog}\" ${Strings.NOT_FOUND}")
        }
    }

    fun getValueProcess(row: Int): String {
        return if (row >= 0 && row < mLogItems.size) {
            if (mSortedPidTokIdx >= 0) {
                mLogItems[row].mTokenFilterLogs[mSortedPidTokIdx]
            }
            else {
                ""
            }
        } else {
            ""
        }
    }

    fun saveFile(target: String) {
        val bufferedWriter = BufferedWriter(FileWriter(target))
        if (SwingUtilities.isEventDispatchThread()) {
            for (item in mLogItems) {
                bufferedWriter.write(item.mLogLine)
                bufferedWriter.newLine()
            }
        }
        else {
            SwingUtilities.invokeAndWait {
                for (item in mLogItems) {
                    bufferedWriter.write(item.mLogLine)
                    bufferedWriter.newLine()
                }
            }
        }
        bufferedWriter.flush()
        bufferedWriter.close()
    }
}
