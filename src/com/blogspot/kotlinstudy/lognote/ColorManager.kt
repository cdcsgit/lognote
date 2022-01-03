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
        var FullLogBG = Color.decode(StrFullLogBG)
            private set

        var StrFilterLogBG = "#000000"
            set(value) {
                field = value
                FilterLogBG = Color.decode(value)
            }
        var FilterLogBG = Color.decode(StrFilterLogBG)
            private set

        var StrLineNumBG = "#000000"
            set(value) {
                field = value
                LineNumBG = Color.decode(value)
            }
        var LineNumBG = Color.decode(StrLineNumBG)
            private set

        var StrBookmarkBG = "#000000"
            set(value) {
                field = value
                BookmarkBG = Color.decode(value)
            }
        var BookmarkBG = Color.decode(StrBookmarkBG)
            private set

        var StrLogLevelNone = "#000000"
            set(value) {
                field = value
                LogLevelNone = Color.decode(value)
            }
        var LogLevelNone = Color.decode(StrLogLevelNone)
            private set

        var StrLogLevelVerbose = "#000000"
            set(value) {
                field = value
                LogLevelVerbose = Color.decode(value)
            }
        var LogLevelVerbose = Color.decode(StrLogLevelVerbose)
            private set

        var StrLogLevelDebug = "#000000"
            set(value) {
                field = value
                LogLevelDebug = Color.decode(value)
            }
        var LogLevelDebug = Color.decode(StrLogLevelDebug)
            private set

        var StrLogLevelInfo = "#000000"
            set(value) {
                field = value
                LogLevelInfo = Color.decode(value)
            }
        var LogLevelInfo = Color.decode(StrLogLevelInfo)
            private set

        var StrLogLevelWarning = "#000000"
            set(value) {
                field = value
                LogLevelWarning = Color.decode(value)
            }
        var LogLevelWarning = Color.decode(StrLogLevelWarning)
            private set

        var StrLogLevelError = "#000000"
            set(value) {
                field = value
                LogLevelError = Color.decode(value)
            }
        var LogLevelError = Color.decode(StrLogLevelError)
            private set

        var StrLogLevelFatal = "#000000"
            set(value) {
                field = value
                LogLevelFatal = Color.decode(value)
            }
        var LogLevelFatal = Color.decode(StrLogLevelFatal)
            private set

        var StrLineNumFG = "#000000"
            set(value) {
                field = value
                LineNumFG = Color.decode(value)
            }
        var LineNumFG = Color.decode(StrLineNumBG)
            private set

    }

    class ColorItem(val name: String, var strColor: String) {
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
        LINE_NUM_FG(17);

        companion object {
            fun fromInt(value: Int) = ColorIdx.values().first { it.value == value }
        }
    }

    var mColorArray = arrayOf(
        ColorItem("Filtered FG", "#FF0000"),
        ColorItem("Selected BG", "#E0E0E0"),
        ColorItem("FullLog BG", "#FAFAFF"),
        ColorItem("FilterLog BG", "#FFFAFA"),
        ColorItem("LineNum BG", "#D0D0D0"),
        ColorItem("Bookmark BG", "#E0E0FF"),
        ColorItem("Log Level None", "#000000"),
        ColorItem("Log Level Verbose", "#000000"),
        ColorItem("Log Level Debug", "#209000"),
        ColorItem("Log Level Info", "#0080DF"),
        ColorItem("Log Level Warning", "#F07000"),
        ColorItem("Log Level Error", "#D00000"),
        ColorItem("Log Level Fatal", "#700000"),
        ColorItem("PID FG", "#0000FF"),
        ColorItem("TID FG", "#0000FF"),
        ColorItem("Tag FG", "#0000FF"),
        ColorItem("Highlight FG", "#0000FF"),
        ColorItem("LineNum FG", "#333333")
    )

    fun getConfig(configManager: MainUI.ConfigManager) {
        for (idx in mColorArray.indices) {
            var item = configManager.mProperties[configManager.ITEM_COLOR_MANAGER + idx] as? String
            if (item != null) {
                mColorArray[idx].mStrColor = item
            }
        }
    }

    fun putConfig(configManager: MainUI.ConfigManager) {
        for (idx in mColorArray.indices) {
            configManager.mProperties[configManager.ITEM_COLOR_MANAGER + idx] = mColorArray[idx].mStrColor
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
    }
}

