package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.io.*
import java.util.*
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
        val EVENT_ADDED = 0
        val EVENT_REMOVED = 1
        val EVENT_FILTERED = 2
        val EVENT_CHANGED = 3
        val EVENT_CLEARED = 4

        val FLAG_FIRST_REMOVED = 1
    }
}

interface LogTableModelListener {
    fun tableChanged(event:LogTableModelEvent?)
}

class LogTableModel() : AbstractTableModel() {
    private val mColumnNames = arrayOf("line", "log")
    private var mLogItems:MutableList<LogItem> = mutableListOf()
    private var mBaseModel:LogTableModel? = null
    var mLogFile:File? = null
    private val mAdbManager = AdbManager.getInstance()
    private val mBookmarkManager = BookmarkManager.getInstance()

    val mEventListeners = ArrayList<LogTableModelListener>()

    private val COLUMN_NUM = 0
    private val COLUMN_LOGLINE = 1

    private val PID_INDEX = 2
    private val TID_INDEX = 3
    private val LEVEL_INDEX = 4
    private val TAG_INDEX = 5
    val LEVEL_NONE = -1
    val LEVEL_VERBOSE = 0
    val LEVEL_DEBUG = 1
    val LEVEL_INFO = 2
    val LEVEL_WARNING = 3
    val LEVEL_ERROR = 4
    val LEVEL_FATAL = 5

    private var mIsFilterUpdated = true

    var mSelectionChanged = false

    var mMainUI: MainUI? = null
        set(value) {
            field = value
        }

    var mFilterLevel = 0
        set(value) {
            if (field != value) {
                mIsFilterUpdated = true
            }
            field = value
        }

    var mFilterLog: String = ""
        set(value) {
            val patterns = parsePattern(value)
            mFilterShowLog = patterns[0]
            mFilterHideLog = patterns[1]
        }

    var mFilterShowLog: String = ""
        set(value) {
            try {
                mPatternShowLog = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }
    var mFilterHideLog: String = ""
        set(value) {
            try {
                mPatternHideLog = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }
    var mFilterHighlightLog: String = ""
        set(value) {
            try {
                mPatternHighlightLog = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }

    var mFilterTag: String = ""
        set(value) {
            val patterns = parsePattern(value)
            mFilterShowTag = patterns[0]
            mFilterHideTag = patterns[1]
        }

    var mFilterShowTag: String = ""
        set(value) {
            try {
                mPatternShowTag = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }
    var mFilterHideTag: String = ""
        set(value) {
            try {
                mPatternHideTag = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }

    var mFilterPid: String = ""
        set(value) {
            val patterns = parsePattern(value)
            mFilterShowPid = patterns[0]
            mFilterHidePid = patterns[1]
        }

    var mFilterShowPid: String = ""
        set(value) {
            try {
                mPatternShowPid = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }

    var mFilterHidePid: String = ""
        set(value) {
            try {
                mPatternHidePid = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }

    var mFilterTid: String = ""
        set(value) {
            val patterns = parsePattern(value)
            patterns[0].let { mFilterShowTid = it}
            patterns[1].let { mFilterHideTid = it}
        }

    var mFilterShowTid: String = ""
        set(value) {
            try {
                mPatternShowTid = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }
    var mFilterHideTid: String = ""
        set(value) {
            try {
                mPatternHideTid = Pattern.compile(value, mPatternCase)
                if (field != value) {
                    mIsFilterUpdated = true
                }
                field = value
            } catch(ex: java.util.regex.PatternSyntaxException) {
                ex.printStackTrace()
            }
        }

    private var mPatternCase = Pattern.CASE_INSENSITIVE
    var mMatchCase: Boolean = false
        set(value) {
            if (field != value) {
                if (value == false) {
                    mPatternCase = Pattern.CASE_INSENSITIVE
                } else {
                    mPatternCase = 0
                }

                mPatternShowLog = Pattern.compile(mFilterShowLog, mPatternCase)
                mPatternHideLog = Pattern.compile(mFilterHideLog, mPatternCase)
                mPatternHighlightLog = Pattern.compile(mFilterHighlightLog, mPatternCase)
                mPatternShowTag = Pattern.compile(mFilterShowTag, mPatternCase)
                mPatternHideTag = Pattern.compile(mFilterHideTag, mPatternCase)
                mPatternShowPid = Pattern.compile(mFilterShowPid, mPatternCase)
                mPatternHidePid = Pattern.compile(mFilterHidePid, mPatternCase)
                mPatternShowTid = Pattern.compile(mFilterShowTid, mPatternCase)
                mPatternHideTid = Pattern.compile(mFilterHideTid, mPatternCase)

                mIsFilterUpdated = true

                field = value
            }
        }

    var mGoToLast = true
//        set(value) {
//            field = value
//            Exception().printStackTrace()
//            println("tid = " + Thread.currentThread().id)
//        }
    var mBoldTag = false
    var mBoldPid = false
    var mBoldTid = false
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
                mBaseModel!!.mLogItems.clear()
                mBaseModel!!.mBookmarkManager.clear()
                fireLogTableDataCleared()
                mBaseModel!!.fireLogTableDataCleared()
            } else {
                SwingUtilities.invokeAndWait {
                    mLogItems.clear()
                    mBaseModel!!.mLogItems.clear()
                    mBaseModel!!.mBookmarkManager.clear()
                    fireLogTableDataCleared()
                    mBaseModel!!.fireLogTableDataCleared()
                }
            }
        }

    var mScrollbackSplitFile = false
        set(value) {
            field = value
        }

    var mScrollbackKeep = false

    var mPatternShowLog = Pattern.compile(mFilterShowLog, mPatternCase)
    var mPatternHideLog = Pattern.compile(mFilterHideLog, mPatternCase)
    var mPatternHighlightLog = Pattern.compile(mFilterHighlightLog, mPatternCase)
    var mPatternShowTag = Pattern.compile(mFilterShowTag, mPatternCase)
    var mPatternHideTag = Pattern.compile(mFilterHideTag, mPatternCase)
    var mPatternShowPid = Pattern.compile(mFilterShowPid, mPatternCase)
    var mPatternHidePid = Pattern.compile(mFilterHidePid, mPatternCase)
    var mPatternShowTid = Pattern.compile(mFilterShowTid, mPatternCase)
    var mPatternHideTid = Pattern.compile(mFilterHideTid, mPatternCase)

    constructor(baseModel: LogTableModel?) : this() {
        mBaseModel = baseModel
        loadItems(false)
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

    private fun parsePattern(pattern: String) : Array<String> {
        val patterns: Array<String> = Array<String>(2) { "" }

        val strs = pattern.split("|")
        var prevPatternIdx = -1

        for (item in strs) {
            if (prevPatternIdx != -1) {
                patterns[prevPatternIdx] += "|"
                patterns[prevPatternIdx] += item

                if (!item.substring(item.length - 1).equals("\\")) {
                    prevPatternIdx = -1
                }
                continue
            }

            if (item.length > 0) {
                if (item[0] != '-') {
                    if (patterns[0].isNotEmpty()) {
                        patterns[0] += "|"
                        patterns[0] += item
                    }
                    else {
                        patterns[0] = item
                    }
                    if (item.substring(item.length - 1).equals("\\")) {
                        prevPatternIdx = 0
                    }
                } else {
                    if (patterns[1].isNotEmpty()) {
                        patterns[1] += "|"
                        patterns[1] += item.substring(1)
                    }
                    else {
                        patterns[1] = item.substring(1)
                    }
                    if (item.substring(item.length - 1).equals("\\")) {
                        prevPatternIdx = 1
                    }
                }
            }
        }

//        if (patterns[0] == null) {
//            patterns[0] = ""
//        }
//
//        if (patterns[1] == null) {
//            patterns[1] = ""
//        }

        return patterns
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
                mFilteredItemsThread = Thread(Runnable {
                    run {
                        while (true) {
                            try {
                                if (mIsFilterUpdated) {
                                    mMainUI?.markLine()
                                    makeFilteredItems(true)
                                }
                                Thread.sleep(100)
                            } catch (e:Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })

                mFilteredItemsThread?.start()
            }
        }
    }

    fun clearItems() {
        println("isEventDispatchThread =" + SwingUtilities.isEventDispatchThread())

        if (mBaseModel != null) {
            mBaseModel!!.mGoToLast = true
            mGoToLast = true
            mBaseModel!!.mLogItems.clear()
            mBaseModel!!.mBookmarkManager.clear()
            mLogItems.clear()
            mIsFilterUpdated = true
            System.gc()
        }
    }

    fun setLogFile(path: String) {
        mLogFile = File(path)
    }

    private fun levelToInt(text:String) : Int {
        var level = LEVEL_NONE
        if (text == "V") {
            level = LEVEL_VERBOSE
        } else if (text == "D") {
            level = LEVEL_DEBUG
        } else if (text == "I") {
            level = LEVEL_INFO
        } else if (text == "W") {
            level = LEVEL_WARNING
        } else if (text == "E") {
            level = LEVEL_ERROR
        } else if (text == "F") {
            level = LEVEL_FATAL
        }

        return level
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
                mLogItems.add(LogItem(num.toString(), "APPEND LOG : $mLogFile", "", "", "", LEVEL_ERROR))
                num++
            }
        } else {
            mLogItems.clear()
            mBookmarkManager.clear()
        }

        val bufferedReader = BufferedReader(FileReader(mLogFile!!))
        var line: String?
        var level:Int
        var tag:String
        var pid:String
        var tid:String

        line = bufferedReader.readLine()
        while (line != null) {
            val textSplited = line.trim().split(Regex("\\s+"))

            if (textSplited.size > TAG_INDEX) {
                if (Character.isDigit(textSplited[PID_INDEX][0])) {
                    level = levelToInt(textSplited[LEVEL_INDEX])
                    tag = textSplited[TAG_INDEX]
                    pid = textSplited[PID_INDEX]
                    tid = textSplited[TID_INDEX]
                }
                else if (Character.isAlphabetic(textSplited[PID_INDEX][0].toInt())) {
                    level = levelToInt(Character.toString(textSplited[PID_INDEX][0]))
                    tag = ""
                    pid = ""
                    tid = ""
                }
                else {
                    level = LEVEL_NONE
                    tag = ""
                    pid = ""
                    tid = ""
                }
            }  else {
                level = LEVEL_NONE
                tag = ""
                pid = ""
                tid = ""
            }

            mLogItems.add(LogItem(num.toString(), line, tag, pid, tid, level))
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
        return 2 // line + log
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        try {
            if (rowIndex >= 0 && mLogItems.size > rowIndex) {
                val logItem = mLogItems[rowIndex]
                if (columnIndex == COLUMN_NUM) {
                    return logItem.mNum + " "
                } else if (columnIndex == COLUMN_LOGLINE) {
                    return logItem.mLogLine
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
                ColorManager.LogLevelVerbose
            }
            LEVEL_DEBUG -> {
                ColorManager.LogLevelDebug
            }
            LEVEL_INFO -> {
                ColorManager.LogLevelInfo
            }
            LEVEL_WARNING -> {
                ColorManager.LogLevelWarning
            }
            LEVEL_ERROR -> {
                ColorManager.LogLevelError
            }
            LEVEL_FATAL -> {
                ColorManager.LogLevelFatal
            }
            else -> ColorManager.LogLevelNone
        }
    }

    fun getFgStrColor(row: Int) : String {
        return when (mLogItems[row].mLevel) {
            LEVEL_VERBOSE -> {
                ColorManager.StrLogLevelVerbose
            }
            LEVEL_DEBUG -> {
                ColorManager.StrLogLevelDebug
            }
            LEVEL_INFO -> {
                ColorManager.StrLogLevelInfo
            }
            LEVEL_WARNING -> {
                ColorManager.StrLogLevelWarning
            }
            LEVEL_ERROR -> {
                ColorManager.StrLogLevelError
            }
            LEVEL_FATAL -> {
                ColorManager.StrLogLevelFatal
            }
            else -> ColorManager.StrLogLevelNone
        }
    }

    protected var mPatternPrintValue:Pattern? = null
    fun getPrintValue(value:String, row: Int) : String {
        val starts = Stack<Int>()
        val ends = Stack<Int>()
        var newValue = value

        if (newValue.indexOf("<") >= 0) {
            newValue = newValue.replace("<", "&lt;")
        }
        if (newValue.indexOf(">") >= 0) {
            newValue = newValue.replace(">", "&gt;")
        }

        val stringBuilder = StringBuilder(newValue)

        if (mBoldTag) {
            val item = mLogItems[row]
            if (item.mTag.isNotEmpty()) {
                val start = newValue.indexOf(item.mTag)
                stringBuilder.replace(start, start + item.mTag.length, "<b><font color=${ColorManager.StrFilteredFG}>" + item.mTag + "</font></b>")
                newValue = stringBuilder.toString()
            }
        }

        if (mBoldPid) {
            val item = mLogItems[row]
            if (item.mPid.isNotEmpty()) {
                val start = newValue.indexOf(item.mPid)
                stringBuilder.replace(start, start + item.mPid.length, "<b><font color=${ColorManager.StrFilteredFG}>" + item.mPid + "</font></b>")
                newValue = stringBuilder.toString()
            }
        }

        if (mBoldTid) {
            val item = mLogItems[row]
            if (item.mTid.isNotEmpty()) {
                var start = newValue.indexOf(item.mTid)
                if (item.mTid == item.mPid) {
                    start = newValue.indexOf(item.mTid, start + 1)
                }
                stringBuilder.replace(start, start + item.mTid.length, "<b><font color=${ColorManager.StrFilteredFG}>" + item.mTid + "</font></b>")
                newValue = stringBuilder.toString()
            }
        }

        if (mPatternPrintValue != null) {
            val matcher = mPatternPrintValue!!.matcher(stringBuilder.toString())
            while (matcher.find()) {
                starts.push(matcher.start(0))
                ends.push(matcher.end(0))
            }

            if (starts.size == 0) {
                if (newValue == value) {
                    return ""
                }
                stringBuilder.replace(0, newValue.length, newValue.replace(" ", "&nbsp;"))
            }
            else {
                var beforeStart = 0
                while (!starts.isEmpty()) {
                    val start = starts.pop()
                    val end = ends.pop()

                    if (beforeStart > end) {
                        stringBuilder.replace(
                            end,
                            beforeStart,
                            newValue.substring(end, beforeStart).replace(" ", "&nbsp;")
                        )
                    }
                    if (start >= 0 && end >= 0) {
                        stringBuilder.replace(
                            start,
                            end,
                            "<b><font color=${ColorManager.StrFilteredFG}>" + newValue.substring(start, end) + "</font></b>"
                        )
                    }
                    beforeStart = start
                }
                if (beforeStart > 0) {
                    stringBuilder.replace(0, beforeStart, newValue.substring(0, beforeStart).replace(" ", "&nbsp;"))
                }
            }
        } else {
            if (newValue == value) {
                return ""
            }
            stringBuilder.replace(0, newValue.length, newValue.replace(" ", "&nbsp;"))
        }

        val color = getFgStrColor(row)
        stringBuilder.replace(0, 0, "<html><p><nobr><font color=$color>")
        stringBuilder.append("</font></nobr></p></html>")
        return stringBuilder.toString()
    }

    internal inner class LogItem(num:String, logLine:String, tag:String, pid:String, tid:String, level:Int) {
        val mNum = num
        val mLogLine = logLine
        val mTag = tag
        val mPid = pid
        val mTid = tid
        val mLevel = level
    }

    private fun makePattenPrintValue() {
        if (mBaseModel == null) {
            return
        }

        var filterPrintValue = ""
        if (!mFilterShowLog.isEmpty()) {
            if (filterPrintValue.length > 0) {
                filterPrintValue += "|" + mFilterShowLog
            }
            else {
                filterPrintValue += mFilterShowLog
            }
        }
        if (!mFilterHighlightLog.isEmpty()) {
            if (filterPrintValue.length > 0) {
                filterPrintValue += "|" + mFilterHighlightLog
            }
            else {
                filterPrintValue += mFilterHighlightLog
            }
        }
//        if (!mFilterShowTag.isEmpty()) {
//            if (filterPrintValue.length > 0) {
//                filterPrintValue += "|" + mFilterShowTag
//            }
//            else {
//                filterPrintValue += mFilterShowTag
//            }
//        }
//        if (!mFilterShowPid.isEmpty()) {
//            if (filterPrintValue.length > 0) {
//                filterPrintValue += "|" + mFilterShowPid
//            }
//            else {
//                filterPrintValue += mFilterShowPid
//            }
//        }
//        if (!mFilterShowTid.isEmpty()) {
//            if (filterPrintValue.length > 0) {
//                filterPrintValue += "|" + mFilterShowTid
//            }
//            else {
//                filterPrintValue += mFilterShowTid
//            }
//        }

        if (filterPrintValue.isEmpty()) {
            mPatternPrintValue = null
            mBaseModel?.mPatternPrintValue = null
        } else {
            var start = 0
            var index = 0
            var skip = false

            while (index != -1) {
                index = filterPrintValue.indexOf('|', start)
                start = index + 1
                if (index == 0 || index == filterPrintValue.lastIndex || filterPrintValue.get(index + 1) == '|') {
                    skip = true
                    break
                }
            }

            if (!skip) {
                mPatternPrintValue = Pattern.compile(filterPrintValue, mPatternCase)
                mBaseModel?.mPatternPrintValue = mPatternPrintValue
            }
        }

        return
    }

    private fun makeFilteredItems(isRedraw: Boolean) {
        if (mBaseModel == null || !mIsFilterUpdated) {
            println("skip makeFilteredItems " + mBaseModel + ", " + mIsFilterUpdated)
            return
        }
        else {
            mIsFilterUpdated = false
        }
//            mGoToLast = false
//            mBaseModel?.mGoToLast = false
        SwingUtilities.invokeAndWait {
            mLogItems.clear()

            if (mBookmarkMode) {
                for (item in mBaseModel!!.mLogItems) {
                    if (mBookmarkManager.mBookmarks.contains(item.mNum.toInt())) {
                        mLogItems.add(item)
                    }
                }
            } else {
                makePattenPrintValue()

                var isShow: Boolean
                for (item in mBaseModel!!.mLogItems) {
                    if (mIsFilterUpdated) {
                        break
                    }

                    isShow = true

                    if (!mFullMode) {
                        if (item.mLevel != LEVEL_NONE && item.mLevel < mFilterLevel) {
                            isShow = false
                        }
                        else if ((!mFilterHideLog.isEmpty() && mPatternHideLog.matcher(item.mLogLine).find())
                                || (!mFilterHideTag.isEmpty() && mPatternHideTag.matcher(item.mTag).find())
                                || (!mFilterHidePid.isEmpty() && mPatternHidePid.matcher(item.mPid).find())
                                || (!mFilterHideTid.isEmpty() && mPatternHideTid.matcher(item.mTid).find())) {
                            isShow = false
                        }
                        else if (!mFilterShowLog.isEmpty() && !mPatternShowLog.matcher(item.mLogLine).find()) {
                            isShow = false
                        }
                        else if ((!mFilterShowTag.isEmpty() && !mPatternShowTag.matcher(item.mTag).find())
                                || (!mFilterShowPid.isEmpty() && !mPatternShowPid.matcher(item.mPid).find())
                                || (!mFilterShowTid.isEmpty() && !mPatternShowTid.matcher(item.mTid).find())) {
                            isShow = false
                        }
                    }

                    if (isShow || mBookmarkManager.mBookmarks.contains(item.mNum.toInt())) {
                        mLogItems.add(item)
                    }
                }
            }
        }
        if (!mIsFilterUpdated && isRedraw) {
            fireLogTableDataFiltered()
            mBaseModel?.fireLogTableDataFiltered()
        }
    }

    private var mScanThread:Thread? = null
    private var mFileWriter:FileWriter? = null
    fun startScan() {
        if (mLogFile == null) {
            return
        }

        mScanThread?.interrupt()

        mGoToLast = true
        mBaseModel?.mGoToLast = true

        mScanThread = Thread(Runnable { run {
            SwingUtilities.invokeAndWait {
                mLogItems.clear()
                mBaseModel!!.mLogItems.clear()
                mBaseModel!!.mBookmarkManager.clear()
                fireLogTableDataCleared()
                mBaseModel!!.fireLogTableDataCleared()
            }
            fireLogTableDataChanged()
            mBaseModel!!.fireLogTableDataChanged()
            makePattenPrintValue()

            var currLogFile: File? = mLogFile
            val bufferedReader = BufferedReader(InputStreamReader(mAdbManager.mProcessLogcat?.inputStream))
            var line: String?
            var num = 0
            var saveNum = 0
            var level:Int
            var tag:String
            var pid:String
            var tid:String

            var isShow: Boolean
            var nextItemCount = 0
            var nextBaseItemCount = 0
            var nextUpdateTime:Long = 0
            var nextBaseUpdateTime:Long = 0

            var removedCount = 0
            var baseRemovedCount = 0

            var nextCount = 200
            var nextBaseCount = 500

            var item:LogItem
            line = bufferedReader.readLine()
            while (line != null) {
                try {
                    if (currLogFile != mLogFile) {
                        try {
                            mFileWriter?.flush()
                        } catch(e:IOException) {
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
                    synchronized(this) {
                        val textSplited = line!!.trim().split(Regex("\\s+"))
                        if (textSplited.size > TAG_INDEX) {
                            level = levelToInt(textSplited[LEVEL_INDEX])
                            tag = textSplited[TAG_INDEX]
                            pid = textSplited[PID_INDEX]
                            tid = textSplited[TID_INDEX]
                        } else {
                            level = LEVEL_NONE
                            tag = ""
                            pid = ""
                            tid = ""
                        }

                        item = LogItem(num.toString(), line!!, tag, pid, tid, level)

                        isShow = true

                        if (mBookmarkMode) {
                            isShow = false
                        }

                        if (!mFullMode) {
                            if (isShow && item.mLevel < mFilterLevel) {
                                isShow = false
                            }
                            if (isShow
                                && (!mFilterHideLog.isEmpty() && mPatternHideLog.matcher(item.mLogLine).find())
                                || (!mFilterShowLog.isEmpty() && !mPatternShowLog.matcher(item.mLogLine).find())
                            ) {
                                isShow = false
                            }
                            if (isShow
                                && ((!mFilterHideTag.isEmpty() && mPatternHideTag.matcher(item.mTag).find())
                                        || (!mFilterShowTag.isEmpty() && !mPatternShowTag.matcher(item.mTag).find()))
                            ) {
                                isShow = false
                            }
                            if (isShow
                                && ((!mFilterHidePid.isEmpty() && mPatternHidePid.matcher(item.mPid).find())
                                        || (!mFilterShowPid.isEmpty() && !mPatternShowPid.matcher(item.mPid).find()))
                            ) {
                                isShow = false
                            }
                            if (isShow
                                && ((!mFilterHideTid.isEmpty() && mPatternHideTid.matcher(item.mTid).find())
                                        || (!mFilterShowTid.isEmpty() && !mPatternShowTid.matcher(item.mTid).find()))
                            ) {
                                isShow = false
                            }
                        }

                    }

                    SwingUtilities.invokeAndWait {
                        if (mScanThread == null) {
                            return@invokeAndWait
                        }
                        if (mSelectionChanged) {
                            baseRemovedCount = 0
                            removedCount = 0
                            nextUpdateTime = System.currentTimeMillis() - 1
                            mSelectionChanged = false
                        }

                        mBaseModel!!.mLogItems.add(item)
                        while (!mScrollbackKeep && mScrollback > 0 && mBaseModel!!.mLogItems.count() > mScrollback) {
                            mBaseModel!!.mLogItems.removeAt(0)
                            baseRemovedCount++
                        }
                        if (isShow || mBookmarkManager.mBookmarks.contains(item.mNum.toInt())) {
                            mLogItems.add(item)
                            while (!mScrollbackKeep && mScrollback > 0 && mLogItems.count() > mScrollback) {
                                mLogItems.removeAt(0)
                                removedCount++
                            }
                        }
                    }

                    val millis = System.currentTimeMillis()
                    if (mLogItems.size > nextItemCount || millis > nextUpdateTime) {
                        if (mLogItems.size > nextItemCount) {
                            nextCount *= 2
                        }
                        else {
                            nextCount = 200
                        }
                        nextItemCount = mLogItems.size + nextCount
                        nextUpdateTime = millis + 300
                        fireLogTableDataChanged(removedCount)
                        removedCount = 0
                    }
                    if (mBaseModel!!.mLogItems.size > nextBaseItemCount || millis > nextBaseUpdateTime) {
                        if (mBaseModel!!.mLogItems.size > nextBaseItemCount) {
                            nextBaseCount *= 2
                        }
                        else {
                            nextBaseCount = 500
                        }

                        nextBaseItemCount = mBaseModel!!.mLogItems.size + nextBaseCount
                        nextBaseUpdateTime = millis + 500
                        mBaseModel!!.fireLogTableDataChanged(baseRemovedCount)
                        baseRemovedCount = 0
                    }
                    num++
                    saveNum++

                    if (mScrollbackSplitFile && mScrollback > 0 && saveNum >= mScrollback) {
                        mMainUI?.setSaveLogFile()

                        println("Change save file : " + mLogFile?.absolutePath)
                    }
                    line = bufferedReader.readLine()
                } catch (e:Exception) {
                    println("Start scan : $e")
                    if (e !is InterruptedException) {
                        JOptionPane.showMessageDialog(mMainUI, e.toString(), "Error", JOptionPane.ERROR_MESSAGE)
                    }

                    try {
                        mFileWriter?.flush()
                    } catch(e:IOException) {
                        e.printStackTrace()
                    }
                    mFileWriter?.close()
                    mFileWriter = null
                    return@run
                }
            }
        }})

        mScanThread?.start()

        return
    }

    fun stopScan(){
        mScanThread?.interrupt()
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
}
