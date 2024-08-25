package com.blogspot.cdcsutils.lognote

import java.util.regex.Pattern

data class LogColumnItem(val mName: String, val mNth: Int, val mWidth: Int )
class LogColumnTableModel(mainUI: MainUI, baseModel: LogTableModel?) : LogTableModel(mainUI, baseModel) {

    private val mCurrFormat = FormatManager.getInstance().mCurrFormat
    companion object {
    }

    val mColumnItems = arrayOfNulls<LogColumnItem>(mCurrFormat.mTokenCount + 1)
    init {
        var idx = 0
        mColumnItems[idx] = LogColumnItem("Line", -1, 0)
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
                } else {
                    logItem.mTokens?.let {
                        val tokenIdx = mColumnItems[columnIndex]?.mNth ?: 0
                        if (tokenIdx < (it.size)) {
                            return it[tokenIdx]
                        }
                    }

                    if (logItem.mTokens == null && ((mColumnItems[columnIndex]?.mNth ?: -1) == mCurrFormat.mLogNth)) {
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
        val tokenFilters: Array<String>
        val tokens: List<String>?
        val log: String

        val textSplited = logLine.trim().split(Regex(mSeparator), mCurrFormat.mTokenCount)
        if (textSplited.size == mCurrFormat.mTokenCount) {
            level = if (mFilterLevel == LEVEL_NONE) {
                LEVEL_NONE
            } else {
                mLevelMap[textSplited[mLevelIdx]] ?: LEVEL_NONE
            }
            tokenFilters = Array(mSortedTokenFilters.size) {
                if (mSortedTokenFilters[it].mToken.isNotBlank() && mSortedTokenFilters[it].mNth >= 0) {
                    textSplited[mSortedTokenFilters[it].mNth]
                } else {
                    ""
                }
            }
            log = textSplited[mCurrFormat.mTokenCount - 1]
            tokens = textSplited
        } else {
            level = LEVEL_NONE
            tokenFilters = mEmptyTokenFilters
            log = logLine
            tokens = null
        }

        return LogItem(num.toString(), log, level, tokenFilters, tokens)
    }

    override fun getPatternPrintFilter(col: Int): Pattern? {
        var pattern: Pattern? = null
        if ((mColumnItems[col]?.mNth ?: -1) == mCurrFormat.mLogNth) {
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
