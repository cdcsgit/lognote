package com.blogspot.kotlinstudy.lognote

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class RecentFileManager private constructor() {
    companion object {
        private const val RECENTES_LIST_FILE = "lognote_recents.xml"
        val LOGNOTE_HOME: String = System.getenv("LOGNOTE_HOME") ?: ""
        const val ITEM_VERSION = "VERSION"

        const val ITEM_PATH = "_PATH"
        const val ITEM_SHOW_LOG = "_SHOW_LOG"
        const val ITEM_SHOW_TAG = "_SHOW_TAG"
        const val ITEM_SHOW_PID = "_SHOW_PID"
        const val ITEM_SHOW_TID = "_SHOW_TID"
        const val ITEM_HIGHLIGHT_LOG = "_HIGHLIGHT_LOG"
        const val ITEM_SEARCH_LOG = "_SEARCH_LOG"
        const val ITEM_BOOKMARKS = "_BOOKMARKS"

        const val ITEM_SHOW_LOG_CHECK = "_SHOW_LOG_CHECK"
        const val ITEM_SHOW_TAG_CHECK = "_SHOW_TAG_CHECK"
        const val ITEM_SHOW_PID_CHECK = "_SHOW_PID_CHECK"
        const val ITEM_SHOW_TID_CHECK = "_SHOW_TID_CHECK"
        const val ITEM_HIGHLIGHT_LOG_CHECK = "_HIGHLIGHT_LOG_CHECK"
        const val ITEM_SEARCH_MATCH_CASE = "_SEARCH_MATCH_CASE"

        const val MAX_RECENT_FILE = 30
        private val mInstance: RecentFileManager = RecentFileManager()

        fun getInstance(): RecentFileManager {
            return mInstance
        }
    }

    val mRecentList = mutableListOf<RecentItem>()
    val mOpenList = mutableListOf<OpenItem>()
    private val mProperties = Properties()
    private var mRecentListPath = RECENTES_LIST_FILE

    init {
        if (LOGNOTE_HOME.isNotEmpty()) {
            mRecentListPath = "$LOGNOTE_HOME${File.separator}$RECENTES_LIST_FILE"
        }
        println("Recent list : $mRecentListPath")
        loadList()
//        manageVersion()
    }

    class RecentItem() {
        var mPath = ""
        var mShowLog = ""
        var mShowTag = ""
        var mShowPid = ""
        var mShowTid = ""
        var mHighlightLog = ""
        var mSearchLog = ""
        var mBookmarks = ""

        var mShowLogCheck = true
        var mShowTagCheck = true
        var mShowPidCheck = true
        var mShowTidCheck = true
        var mHighlightLogCheck = true
        var mSearchMatchCase = true
    }

    data class OpenItem(val mPath: String, var mStartLine: Int, var mEndLine: Int)

    fun loadList() {
        var fileInput: FileInputStream? = null

        try {
            fileInput = FileInputStream(mRecentListPath)
            mProperties.loadFromXML(fileInput)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (null != fileInput) {
                try {
                    fileInput.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

        mRecentList.clear()
        for (i in 0 until MAX_RECENT_FILE) {
            val recentItem = RecentItem()
            recentItem.mPath = (mProperties["$i$ITEM_PATH"] ?: "") as String
            if (recentItem.mPath.isEmpty()) {
                break
            }
            recentItem.mShowLog = (mProperties["$i$ITEM_SHOW_LOG"] ?: "") as String
            recentItem.mShowTag = (mProperties["$i$ITEM_SHOW_TAG"] ?: "") as String
            recentItem.mShowPid = (mProperties["$i$ITEM_SHOW_PID"] ?: "") as String
            recentItem.mShowTid = (mProperties["$i$ITEM_SHOW_TID"] ?: "") as String
            recentItem.mHighlightLog = (mProperties["$i$ITEM_HIGHLIGHT_LOG"] ?: "") as String
            recentItem.mSearchLog = (mProperties["$i$ITEM_SEARCH_LOG"] ?: "") as String
            recentItem.mBookmarks = (mProperties["$i$ITEM_BOOKMARKS"] ?: "") as String

            var check = (mProperties["$i$ITEM_SHOW_LOG_CHECK"] ?: "false") as String
            recentItem.mShowLogCheck = check.toBoolean()
            check = (mProperties["$i$ITEM_SHOW_TAG_CHECK"] ?: "false") as String
            recentItem.mShowTagCheck = check.toBoolean()
            check = (mProperties["$i$ITEM_SHOW_PID_CHECK"] ?: "false") as String
            recentItem.mShowPidCheck = check.toBoolean()
            check = (mProperties["$i$ITEM_SHOW_TID_CHECK"] ?: "false") as String
            recentItem.mShowTidCheck = check.toBoolean()
            check = (mProperties["$i$ITEM_HIGHLIGHT_LOG_CHECK"] ?: "false") as String
            recentItem.mHighlightLogCheck = check.toBoolean()
            check = (mProperties["$i$ITEM_SEARCH_MATCH_CASE"] ?: "false") as String
            recentItem.mSearchMatchCase = check.toBoolean()

            mRecentList.add(recentItem)
        }
    }

    fun saveList() {
        for (i in 0 until MAX_RECENT_FILE) {
            if (i >= mRecentList.size) {
                break
            }
            val recentItem = mRecentList[i]
            mProperties["$i$ITEM_PATH"] = recentItem.mPath
            mProperties["$i$ITEM_SHOW_LOG"] = recentItem.mShowLog
            mProperties["$i$ITEM_SHOW_TAG"] = recentItem.mShowTag
            mProperties["$i$ITEM_SHOW_PID"] = recentItem.mShowPid
            mProperties["$i$ITEM_SHOW_TID"] = recentItem.mShowTid
            mProperties["$i$ITEM_HIGHLIGHT_LOG"] = recentItem.mHighlightLog
            mProperties["$i$ITEM_SEARCH_LOG"] = recentItem.mSearchLog
            mProperties["$i$ITEM_BOOKMARKS"] = recentItem.mBookmarks

            mProperties["$i$ITEM_SHOW_LOG_CHECK"] = recentItem.mShowLogCheck.toString()
            mProperties["$i$ITEM_SHOW_TAG_CHECK"] = recentItem.mShowTagCheck.toString()
            mProperties["$i$ITEM_SHOW_PID_CHECK"] = recentItem.mShowPidCheck.toString()
            mProperties["$i$ITEM_SHOW_TID_CHECK"] = recentItem.mShowTidCheck.toString()
            mProperties["$i$ITEM_HIGHLIGHT_LOG_CHECK"] = recentItem.mHighlightLogCheck.toString()
            mProperties["$i$ITEM_SEARCH_MATCH_CASE"] = recentItem.mSearchMatchCase.toString()
        }

        var fileOutput: FileOutputStream? = null
        try {
            fileOutput = FileOutputStream(mRecentListPath)
            mProperties.storeToXML(fileOutput, "")
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (null != fileOutput) {
                try {
                    fileOutput.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun saveItem(key: String, value: String) {
        loadList()
        setItem(key, value)
        saveList()
    }

    fun saveItems(keys: Array<String>, values: Array<String>) {
        loadList()
        setItems(keys, values)
        saveList()
    }

    fun getItem(key: String): String? {
        return mProperties[key] as String?
    }

    fun setItem(key: String, value: String) {
        mProperties[key] = value
    }

    private fun setItems(keys: Array<String>, values: Array<String>) {
        if (keys.size != values.size) {
            println("saveItem : size not match ${keys.size}, ${values.size}")
            return
        }
        for (idx in keys.indices) {
            mProperties[keys[idx]] = values[idx]
        }
    }

    fun removeItem(key: String) {
        mProperties.remove(key)
    }

    fun addOpenFile(openItem: OpenItem) {
        mOpenList.add(openItem)
    }
}

