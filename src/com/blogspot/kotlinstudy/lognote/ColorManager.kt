package com.blogspot.kotlinstudy.lognote

import java.awt.Color

class ColorManager private constructor(){
    companion object {
        private val mInstance: ColorManager = ColorManager()

        fun getInstance(): ColorManager {
            return mInstance
        }

        var StrFilteredFG = "#000000"
            set(value) {
                field = value
                FilteredFG = Color.decode(value)
            }
        var FilteredFG: Color = Color.decode(StrFilteredFG)
            private set

        var StrPidFG = "#000000"
            set(value) {
                field = value
                PidFG = Color.decode(value)
            }
        var PidFG: Color = Color.decode(StrPidFG)
            private set

        var StrTidFG = "#000000"
            set(value) {
                field = value
                TidFG = Color.decode(value)
            }
        var TidFG: Color = Color.decode(StrTidFG)
            private set

        var StrTagFG = "#000000"
            set(value) {
                field = value
                TagFG = Color.decode(value)
            }
        var TagFG: Color = Color.decode(StrTagFG)
            private set

        var StrHighlightFG = "#000000"
            set(value) {
                field = value
                HighlightFG = Color.decode(value)
            }
        var HighlightFG: Color = Color.decode(StrHighlightFG)
            private set

        var StrSelectedBG = "#000000"
            set(value) {
                field = value
                SelectedBG = Color.decode(value)
            }
        var SelectedBG: Color = Color.decode(StrSelectedBG)
            private set

        var StrFullLogBG = "#000000"
            set(value) {
                field = value
                FullLogBG = Color.decode(value)
            }
        var FullLogBG: Color = Color.decode(StrFullLogBG)
            private set

        var StrFilterLogBG = "#000000"
            set(value) {
                field = value
                FilterLogBG = Color.decode(value)
            }
        var FilterLogBG: Color = Color.decode(StrFilterLogBG)
            private set

        var StrLineNumBG = "#000000"
            set(value) {
                field = value
                LineNumBG = Color.decode(value)
            }
        var LineNumBG: Color = Color.decode(StrLineNumBG)
            private set

        var StrBookmarkBG = "#000000"
            set(value) {
                field = value
                BookmarkBG = Color.decode(value)
            }
        var BookmarkBG: Color = Color.decode(StrBookmarkBG)
            private set

        var StrLogLevelNone = "#000000"
            set(value) {
                field = value
                LogLevelNone = Color.decode(value)
            }
        var LogLevelNone: Color = Color.decode(StrLogLevelNone)
            private set

        var StrLogLevelVerbose = "#000000"
            set(value) {
                field = value
                LogLevelVerbose = Color.decode(value)
            }
        var LogLevelVerbose: Color = Color.decode(StrLogLevelVerbose)
            private set

        var StrLogLevelDebug = "#000000"
            set(value) {
                field = value
                LogLevelDebug = Color.decode(value)
            }
        var LogLevelDebug: Color = Color.decode(StrLogLevelDebug)
            private set

        var StrLogLevelInfo = "#000000"
            set(value) {
                field = value
                LogLevelInfo = Color.decode(value)
            }
        var LogLevelInfo: Color = Color.decode(StrLogLevelInfo)
            private set

        var StrLogLevelWarning = "#000000"
            set(value) {
                field = value
                LogLevelWarning = Color.decode(value)
            }
        var LogLevelWarning: Color = Color.decode(StrLogLevelWarning)
            private set

        var StrLogLevelError = "#000000"
            set(value) {
                field = value
                LogLevelError = Color.decode(value)
            }
        var LogLevelError: Color = Color.decode(StrLogLevelError)
            private set

        var StrLogLevelFatal = "#000000"
            set(value) {
                field = value
                LogLevelFatal = Color.decode(value)
            }
        var LogLevelFatal: Color = Color.decode(StrLogLevelFatal)
            private set

        var StrLineNumFG = "#000000"
            set(value) {
                field = value
                LineNumFG = Color.decode(value)
            }
        var LineNumFG: Color = Color.decode(StrLineNumBG)
            private set

        var StrNumLogSeperatorBG = "#000000"
            set(value) {
                field = value
                NumLogSeperatorBG = Color.decode(value)
            }
        var NumLogSeperatorBG: Color = Color.decode(StrNumLogSeperatorBG)
            private set

        var StrBookmarkSelectedBG = "#000000"
            set(value) {
                field = value
                BookmarkSelectedBG = Color.decode(value)
            }
        var BookmarkSelectedBG: Color = Color.decode(StrBookmarkSelectedBG)
            private set

        var StrNumBookmarkSelectedBG = "#000000"
            set(value) {
                field = value
                NumBookmarkSelectedBG = Color.decode(value)
            }
        var NumBookmarkSelectedBG: Color = Color.decode(StrNumBookmarkSelectedBG)
            private set

        var StrNumBookmarkBG = "#000000"
            set(value) {
                field = value
                NumBookmarkBG = Color.decode(value)
            }
        var NumBookmarkBG: Color = Color.decode(StrNumBookmarkBG)
            private set

        var StrNumSelectedBG = "#000000"
            set(value) {
                field = value
                NumSelectedBG = Color.decode(value)
            }
        var NumSelectedBG: Color = Color.decode(StrNumSelectedBG)
            private set
    }

    class ColorItem(order: Int, name: String, strColor: String) {
        val mOrder = order
        val mName = name
        var mStrColor = strColor
    }

    enum class ColorIdx(val value: Int) {
        FILTERED_FG(0),
        SELECTED_BG(1),
        FULL_LOG_BG(2),
        FILTER_LOG_BG(3),
        LINE_NUM_BG(4),
        BOOKMARK_BG(5),
        LOG_LEVEL_NONE(6),
        LOG_LEVEL_VERBOSE(7),
        LOG_LEVEL_DEBUG(8),
        LOG_LEVEL_INFO(9),
        LOG_LEVEL_WARNING(10),
        LOG_LEVEL_ERROR(11),
        LOG_LEVEL_FATAL(12),
        PID_FG(13),
        TID_FG(14),
        TAG_FG(15),
        HIGHLIGHT_FG(16),
        LINE_NUM_FG(17),
        NUM_LOG_SEPERATOR_BG(18),
        BOOKMARK_SELECTED_BG(19),
        NUM_BOOKMARK_SELECTED_BG(20),
        NUM_BOOKMARK_BG(21),
        NUM_SELECTED_BG(22);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    var mColorArray = arrayOf(
        ColorItem(0, "Filtered FG", "#FF0000"),
        ColorItem(13, "Selected BG", "#E0E0E0"),
        ColorItem(14, "FullLog BG", "#FAFAFF"),
        ColorItem(15, "FilterLog BG", "#FFFAFA"),
        ColorItem(16, "LineNum BG", "#D0D0D0"),
        ColorItem(17, "Bookmark BG", "#E0E0FF"),
        ColorItem(1, "Log Level None", "#000000"),
        ColorItem(2, "Log Level Verbose", "#000000"),
        ColorItem(3, "Log Level Debug", "#209000"),
        ColorItem(4, "Log Level Info", "#0080DF"),
        ColorItem(5, "Log Level Warning", "#F07000"),
        ColorItem(6, "Log Level Error", "#D00000"),
        ColorItem(7, "Log Level Fatal", "#700000"),
        ColorItem(8, "PID FG", "#0000FF"),
        ColorItem(9, "TID FG", "#0000FF"),
        ColorItem(10, "Tag FG", "#0000FF"),
        ColorItem(11, "Highlight FG", "#0000FF"),
        ColorItem(12, "LineNum FG", "#333333"),
        ColorItem(18, "NumLogSeperator BG", "#FFFFFF"),
        ColorItem(19, "Bookmark Selected BG", "#D0D0DF"),
        ColorItem(20, "LineNum Bookmark Selected BG", "#C0C0CF"),
        ColorItem(21, "LineNum Bookmark BG", "#E0E0EF"),
        ColorItem(22, "LineNum Selected BG", "#C0C0C0")
    )

    private val mConfigManager = ConfigManager.getInstance()

    fun getConfig() {
        for (idx in mColorArray.indices) {
            val item = mConfigManager.getItem(ConfigManager.ITEM_COLOR_MANAGER + idx)
            if (item != null) {
                mColorArray[idx].mStrColor = item
            }
        }
    }

    fun putConfig() {
        for (idx in mColorArray.indices) {
            mConfigManager.setItem(ConfigManager.ITEM_COLOR_MANAGER + idx, mColorArray[idx].mStrColor)
        }
    }

    fun applyColor() {
        StrFilteredFG = mColorArray[ColorIdx.FILTERED_FG.value].mStrColor
        StrSelectedBG = mColorArray[ColorIdx.SELECTED_BG.value].mStrColor
        StrFullLogBG = mColorArray[ColorIdx.FULL_LOG_BG.value].mStrColor
        StrFilterLogBG = mColorArray[ColorIdx.FILTER_LOG_BG.value].mStrColor
        StrLineNumBG = mColorArray[ColorIdx.LINE_NUM_BG.value].mStrColor
        StrBookmarkBG = mColorArray[ColorIdx.BOOKMARK_BG.value].mStrColor
        StrLogLevelNone = mColorArray[ColorIdx.LOG_LEVEL_NONE.value].mStrColor
        StrLogLevelVerbose = mColorArray[ColorIdx.LOG_LEVEL_VERBOSE.value].mStrColor
        StrLogLevelDebug = mColorArray[ColorIdx.LOG_LEVEL_DEBUG.value].mStrColor
        StrLogLevelInfo = mColorArray[ColorIdx.LOG_LEVEL_INFO.value].mStrColor
        StrLogLevelWarning = mColorArray[ColorIdx.LOG_LEVEL_WARNING.value].mStrColor
        StrLogLevelError = mColorArray[ColorIdx.LOG_LEVEL_ERROR.value].mStrColor
        StrLogLevelFatal = mColorArray[ColorIdx.LOG_LEVEL_FATAL.value].mStrColor
        StrPidFG = mColorArray[ColorIdx.PID_FG.value].mStrColor
        StrTidFG = mColorArray[ColorIdx.TID_FG.value].mStrColor
        StrTagFG = mColorArray[ColorIdx.TAG_FG.value].mStrColor
        StrHighlightFG = mColorArray[ColorIdx.HIGHLIGHT_FG.value].mStrColor
        StrLineNumFG = mColorArray[ColorIdx.LINE_NUM_FG.value].mStrColor
        StrNumLogSeperatorBG = mColorArray[ColorIdx.NUM_LOG_SEPERATOR_BG.value].mStrColor
        StrBookmarkSelectedBG = mColorArray[ColorIdx.BOOKMARK_SELECTED_BG.value].mStrColor
        StrNumBookmarkSelectedBG = mColorArray[ColorIdx.NUM_BOOKMARK_SELECTED_BG.value].mStrColor
        StrNumBookmarkBG = mColorArray[ColorIdx.NUM_BOOKMARK_BG.value].mStrColor
        StrNumSelectedBG = mColorArray[ColorIdx.NUM_SELECTED_BG.value].mStrColor
    }
}

