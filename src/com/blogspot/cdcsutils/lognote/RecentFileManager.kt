package com.blogspot.cdcsutils.lognote

class RecentFileManager private constructor() {
    companion object {
        private val mInstance: RecentFileManager = RecentFileManager()

        fun getInstance(): RecentFileManager {
            return mInstance
        }
    }

    val mRecentList = mutableListOf<RecentItem>()
    val mOpenList = mutableListOf<OpenItem>()
    private val mFormatManager = FormatManager.getInstance()
    private val mAppDataManager = AppDataManager.getInstance()

    init {
        loadList()
    }

    class RecentItem() {
        var mPath = ""
        var mShowLog = ""
        var mTokenFilter = Array(AppConstants.MAX_TOKEN_COUNT) { "" }
        var mBoldLog = ""
        var mFindLog = ""
        var mBookmarks = ""

        var mShowLogCheck = true
        var mTokenCheck = Array(AppConstants.MAX_TOKEN_COUNT) { true }
        var mBoldLogCheck = true
        var mFindMatchCase = true
    }

    data class OpenItem(val mPath: String, var mStartLine: Int, var mEndLine: Int)

    fun loadList() {
        val formatName = mFormatManager.mCurrFormat.mName
        val tokens = mFormatManager.mCurrFormat.mTokenFilters

        mRecentList.clear()
        for (i in 0 until AppConstants.MAX_RECENT_FILE) {
            val recentItem = RecentItem()
            val fileItem = mAppDataManager.mAppHistory.recentFiles.fileItems?.get(i)
            if (fileItem == null) {
                break
            }
            recentItem.mPath = fileItem.path
            if (recentItem.mPath.isBlank()) {
                break
            }
            recentItem.mShowLog = fileItem.showLog
            for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                val key = "${formatName}_${tokens[idx].mToken}"
                if (tokens[idx].mToken.isEmpty()) {
                    recentItem.mTokenFilter[idx] = ""
                }
                else {
                    recentItem.mTokenFilter[idx] = fileItem.tokenFilterMap[key] ?: ""
                }
            }
            recentItem.mBoldLog = fileItem.boldLog
            recentItem.mFindLog = fileItem.findLog
            recentItem.mBookmarks = fileItem.bookmarks

            recentItem.mShowLogCheck = fileItem.showLogCheck
            for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                val key = "${formatName}_${tokens[idx].mToken}"
                if (tokens[idx].mToken.isEmpty()) {
                    recentItem.mTokenCheck[idx] = false
                }
                else {
                    recentItem.mTokenCheck[idx] = fileItem.tokenCheckMap[key] ?: false
                }
            }
            recentItem.mBoldLogCheck = fileItem.boldLogCheck
            recentItem.mFindMatchCase = fileItem.findMatchCase

            mRecentList.add(recentItem)
        }
    }

    fun saveList() {
        val formatName = mFormatManager.mCurrFormat.mName
        val tokens = mFormatManager.mCurrFormat.mTokenFilters
        for (i in 0 until AppConstants.MAX_RECENT_FILE) {
            mProperties.remove("$i$ITEM_PATH")
            mProperties.remove("$i$ITEM_SHOW_LOG")
            for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                mProperties.remove("$i$ITEM_TOKEN_FILTER${formatName}_${tokens[idx].mToken}")
            }
            mProperties.remove("$i$ITEM_HIGHLIGHT_LOG")
            mProperties.remove("$i$ITEM_FIND_LOG")
            mProperties.remove("$i$ITEM_BOOKMARKS")

            mProperties.remove("$i$ITEM_SHOW_LOG_CHECK")
            for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                mProperties.remove("$i$ITEM_TOKEN_CHECK${formatName}_${tokens[idx].mToken}")
            }
            mProperties.remove("$i$ITEM_HIGHLIGHT_LOG_CHECK")
            mProperties.remove("$i$ITEM_FIND_MATCH_CASE")
        }

        val mSaveList = mutableListOf<String>()
        for (i in 0 until AppConstants.MAX_RECENT_FILE) {
            if (i >= mRecentList.size) {
                break
            }
            val recentItem = mRecentList[i]
            if (!mSaveList.contains(recentItem.mPath)) {
                mSaveList.add(recentItem.mPath)
                mProperties["$i$ITEM_PATH"] = recentItem.mPath
                mProperties["$i$ITEM_SHOW_LOG"] = recentItem.mShowLog
                for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                    if (tokens[idx].mToken.isNotEmpty()) {
                        mProperties["$i$ITEM_TOKEN_FILTER${formatName}_${tokens[idx].mToken}"] =
                            recentItem.mTokenFilter[idx]
                    }
                }
                mProperties["$i$ITEM_HIGHLIGHT_LOG"] = recentItem.mBoldLog
                mProperties["$i$ITEM_FIND_LOG"] = recentItem.mFindLog
                mProperties["$i$ITEM_BOOKMARKS"] = recentItem.mBookmarks

                mProperties["$i$ITEM_SHOW_LOG_CHECK"] = recentItem.mShowLogCheck.toString()
                for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                    if (tokens[idx].mToken.isNotEmpty()) {
                        mProperties["$i$ITEM_TOKEN_CHECK${formatName}_${tokens[idx].mToken}"] =
                            recentItem.mTokenCheck[idx].toString()
                    }
                }
                mProperties["$i$ITEM_HIGHLIGHT_LOG_CHECK"] = recentItem.mBoldLogCheck.toString()
                mProperties["$i$ITEM_FIND_MATCH_CASE"] = recentItem.mFindMatchCase.toString()
            }
        }

        saveXml()
    }

    fun addOpenFile(openItem: OpenItem) {
        mOpenList.add(openItem)
    }
}

