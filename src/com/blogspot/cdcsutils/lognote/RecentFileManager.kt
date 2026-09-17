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
    private val mAppDataManager = AppDataManager.getInstance(Companion::class.java.name)

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
        mAppDataManager.mAppHistory.recentFiles.fileItems?.let {
            for ((idx, fileItem) in it.withIndex()) {
                if (idx >= AppConstants.MAX_RECENT_FILE) {
                    break
                }
                val recentItem = RecentItem()
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
    }

    fun saveList() {
        val formatName = mFormatManager.mCurrFormat.mName
        val tokens = mFormatManager.mCurrFormat.mTokenFilters

        val savedList = mutableListOf<String>()
        val fileItems = mutableListOf<RecentFileItem>()
        for ((idx, item) in mRecentList.withIndex()) {
            if (idx >= AppConstants.MAX_RECENT_FILE) {
                break
            }

            if (!savedList.contains(item.mPath)) {
                savedList.add(item.mPath)

                val tokenFilterMap= mutableMapOf<String, String>()
                val tokenCheckMap= mutableMapOf<String, Boolean>()
                for (tokIdx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                    if (tokens[idx].mToken.isNotEmpty()) {
                        val key = "${formatName}_${tokens[tokIdx].mToken}"
                        tokenFilterMap[key] = item.mTokenFilter[tokIdx]
                        tokenCheckMap[key] = item.mTokenCheck[tokIdx]
                    }
                }

                fileItems.add(RecentFileItem(item.mPath, item.mShowLog, tokenFilterMap, item.mBoldLog, item.mFindLog, item.mBookmarks,
                    item.mShowLogCheck, tokenCheckMap, item.mBoldLogCheck, item.mFindMatchCase))
            }
        }

        mAppDataManager.updateAndSaveAppHistory { current -> current.copy(recentFiles = current.recentFiles.copy(fileItems = fileItems)) }
    }

    fun addOpenFile(openItem: OpenItem) {
        mOpenList.add(openItem)
    }
}

