package com.blogspot.cdcsutils.lognote

import java.util.regex.Pattern

data class LogColumnItem(val mName: String, val mNth: Int, val mWidth: Int )
class LogColumnTableModel(mainUI: MainUI, baseModel: LogTableModel?) : LogTableModel(mainUI, baseModel) {

    private val mCurrFormat = FormatManager.getInstance().mCurrFormat
    private val mLogNth = FormatManager.getInstance().mCurrFormat.mLogNth
    companion object {
    }

    val mColumnItems = arrayOfNulls<LogColumnItem>(mTokenCount + COLUMN_LOG_START)
    init {
        var idx = 0
        mColumnItems[idx] = LogColumnItem("Line", -1, 0)
        idx++
        mColumnItems[idx] = LogColumnItem("Process", -1, 0)
        idx++
        val nameArr = mCurrFormat.mColumnNames.split("|")
        if (nameArr.isNotEmpty()) {
            nameArr.forEach {
                val nameSplit = it.split(",")
                if (nameSplit.size == 3) {
                    mColumnItems[idx] = LogColumnItem(nameSplit[0], nameSplit[1].toInt(), nameSplit[2].toInt())
                    idx++
                }
            }
        }
    }

    override fun getColumnName(column: Int): String {
        return mColumnItems[column]?.mName ?: ""
    }

    override fun getColumnCount(): Int {
        return mColumnItems.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        try {
            if (rowIndex >= 0 && mLogItems.size > rowIndex) {
                val logItem = mLogItems[rowIndex]
                if (columnIndex == COLUMN_NUM) {
                    return logItem.mNum + " "
                } else if (columnIndex == COLUMN_PROCESS_NAME) {
                    if (TypeShowProcessName != SHOW_PROCESS_NONE) {
                        if (logItem.mProcessName == null) {
                            if (mSortedPidTokIdx >= 0 && logItem.mTokenFilterLogs.size > mSortedPidTokIdx) {
                                return if (logItem.mTokenFilterLogs[mSortedPidTokIdx] == "0") {
                                    "0"
                                } else {
                                    ProcessList.getInstance().getProcessName(logItem.mTokenFilterLogs[mSortedPidTokIdx])
                                        ?: ""
                                }
                            }
                        } else {
                            return logItem.mProcessName
                        }
                    }
                    else {
                        return ""
                    }
                } else {
                    logItem.mTokenLogs?.let {
                        val tokenIdx = mColumnItems[columnIndex]?.mNth ?: 0
                        if (tokenIdx < (it.size)) {
                            return it[tokenIdx]
                        }
                    }

                    if (logItem.mTokenLogs == null && ((mColumnItems[columnIndex]?.mNth ?: -1) == mLogNth)) {
                        return logItem.mLogLine
                    }
                }
            }
        } catch (e:ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return if (columnIndex == COLUMN_NUM) {
            -1
        } else {
            ""
        }
    }

    override fun makeLogItem(num: Int, logLine: String): LogItem {
        val level: Int
        val tokenFilterLogs: Array<String>
        val tokenLogs: List<String>?
        val log: String

        val textSplited = logLine.split(Regex(mSeparator), mTokenCount)
        if (textSplited.size == mTokenCount) {
            level = if (mFilterLevel == LEVEL_NONE) {
                LEVEL_NONE
            } else {
                mLevelMap[textSplited[mLevelIdx]] ?: LEVEL_NONE
            }
            tokenFilterLogs = Array(mSortedTokenFilters.size) {
                if (mSortedTokenFilters[it].mNth >= 0) {
                    textSplited[mSortedTokenFilters[it].mNth]
                } else {
                    ""
                }
            }
            log = textSplited[mLogNth]
            tokenLogs = textSplited
        } else {
            level = LEVEL_NONE
            tokenFilterLogs = mEmptyTokenFilters
            log = logLine
            tokenLogs = null
        }

        val processName = if (TypeShowProcessName != SHOW_PROCESS_NONE && mSortedPidTokIdx >= 0 && tokenFilterLogs.size > mSortedPidTokIdx) {
            ProcessList.getInstance().getProcessName(tokenFilterLogs[mSortedPidTokIdx])
        } else {
            null
        }

        return LogItem(num.toString(), log, level, tokenFilterLogs, tokenLogs, processName)
    }

    override fun getPatternPrintFilter(col: Int): Pattern? {
        var pattern: Pattern? = null
        if ((mColumnItems[col]?.mNth ?: -1) == mLogNth) {
            pattern = mPatternPrintFilter
        }
        else {
            val tokenNth = mColumnItems[col]?.mNth ?: -1
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                if (tokenNth == mTokenFilters[idx].mNth) {
                    pattern = mPatternShowTokens[idx]
                    break
                }
            }
        }

        return pattern
    }
}
