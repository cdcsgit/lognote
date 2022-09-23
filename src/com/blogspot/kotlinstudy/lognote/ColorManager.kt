package com.blogspot.kotlinstudy.lognote

import java.awt.Color

class ColorManager private constructor(){
    class ColorEvent(change:Int) {
        val mColorChange = change
    }

    interface ColorEventListener {
        fun colorChanged(event:ColorEvent?)
    }

    companion object {
        private val mInstance: ColorManager = ColorManager()

        fun getInstance(): ColorManager {
            return mInstance
        }
    }

    private val mColorEventListeners = ArrayList<ColorEventListener>()
    private val mFilterStyleEventListeners = ArrayList<ColorEventListener>()
    
    fun addColorEventListener(listener:ColorEventListener) {
        mColorEventListeners.add(listener)
    }

    fun removeColorEventListener(listener:ColorEventListener) {
        mColorEventListeners.remove(listener)
    }

    class ColorItem(order: Int, name: String, strColor: String) {
        val mOrder = order
        val mName = name
        var mStrColor = strColor
    }

    private val mConfigManager = ConfigManager.getInstance()

    enum class TableColorType(val value: Int) {
        FULL_LOG_TABLE(0),
        FILTER_LOG_TABLE(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    enum class TableColorIdx(val value: Int) {
        FILTERED_FG(0),
        SELECTED_BG(1),
        FILTERED_BG(2),
        LOG_BG(3),
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
        NUM_SELECTED_BG(22),
        HIGHLIGHT_BG(23),
        FILTERED_START_FG(24),
        FILTERED_START_BG(33),
        ;

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    var mColorSchemeLight = arrayOf(
            "#FFFFFF",
            "#E0E0E0",
            "#20B020",
            "#FFFAFA",
            "#D0D0D0",
            "#E0E0FF",
            "#000000",
            "#000000",
            "#209000",
            "#0080DF",
            "#F07000",    // 10
            "#D00000",
            "#700000",
            "#0000FF",
            "#0000FF",
            "#0000FF",
            "#FFFFFF",
            "#333333",
            "#FFFFFF",
            "#D0D0DF",
            "#C0C0CF",   // 20
            "#E0E0EF",
            "#C0C0C0",
            "#3030B0",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",   // 30
            "#FFFFFF",
            "#FFFFFF",
            "#2070C0",
            "#E07020",
            "#10C050",
            "#B09020",
            "#B02020",
            "#2020B0",
            "#A050C0",
            "#2050A0",   // 40
            "#707020",
    )

    var mColorSchemeDark = arrayOf(
            "#000000",
            "#3A3D41",
            "#00A000",
            "#151515",
            "#151515",
            "#501010",
            "#F0F0F0",
            "#F0F0F0",
            "#6C9876",
            "#5084C4",
            "#CB8742",   // 10
            "#CD6C79",
            "#ED3030",
            "#FFFFCC",
            "#FFCCFF",
            "#CCFFFF",
            "#000000",
            "#F0F0F0",
            "#A0A0A0",
            "#503030",
            "#503030",   // 20
            "#301010",
            "#3A3D41",
            "#B0B0B0",
            "#000000",
            "#000000",
            "#000000",
            "#000000",
            "#000000",
            "#000000",
            "#000000",   // 30
            "#000000",
            "#000000",
            "#E06000",
            "#0090E0",
            "#A0A000",
            "#F070A0",
            "#E0E0E0",
            "#00C060",
            "#20B0A0",
            "#9050E0",   // 40
            "#C0C060",
    )

    // Must be declared after mColorSchemeLight (internally mColorSchemeLight is used)
    val mFullTableColor = TableColor(TableColorType.FULL_LOG_TABLE)
    val mFilterTableColor = TableColor(TableColorType.FILTER_LOG_TABLE)

    inner class TableColor(type: TableColorType) {
        private val mType = type

        var StrFilteredFG = "#000000"
            set(value) {
                field = value
                FilteredFG = Color.decode(value)
            }
        var FilteredFG: Color = Color.decode(StrFilteredFG)
            private set

        var StrFilteredBG = "#000000"
            set(value) {
                field = value
                FilteredBG = Color.decode(value)
            }
        var FilteredBG: Color = Color.decode(StrFilteredBG)
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

        var StrLogBG = "#000000"
            set(value) {
                field = value
                LogBG = Color.decode(value)
            }
        var LogBG: Color = Color.decode(StrLogBG)
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

        var StrHighlightBG = "#000000"
            set(value) {
                field = value
                HighlightBG = Color.decode(value)
            }
        var HighlightBG: Color = Color.decode(StrHighlightBG)
            private set

        var StrFilteredFGs = arrayOf(mColorSchemeLight[0],
                mColorSchemeLight[24],
                mColorSchemeLight[25],
                mColorSchemeLight[26],
                mColorSchemeLight[27],
                mColorSchemeLight[28],
                mColorSchemeLight[29],
                mColorSchemeLight[30],
                mColorSchemeLight[31],
                mColorSchemeLight[32],
        )

        var StrFilteredBGs = arrayOf(mColorSchemeLight[2],
                mColorSchemeLight[33],
                mColorSchemeLight[34],
                mColorSchemeLight[35],
                mColorSchemeLight[36],
                mColorSchemeLight[37],
                mColorSchemeLight[38],
                mColorSchemeLight[39],
                mColorSchemeLight[40],
                mColorSchemeLight[41],
        )

        var mColorArray = arrayOf(
                ColorItem(0, "Filtered FG", mColorSchemeLight[0]),
                ColorItem(13, "Selected BG", mColorSchemeLight[1]),
                ColorItem(14, "Filtered BG", mColorSchemeLight[2]),
                ColorItem(15, "Log BG", mColorSchemeLight[3]),
                ColorItem(16, "LineNum BG", mColorSchemeLight[4]),
                ColorItem(17, "Bookmark BG", mColorSchemeLight[5]),
                ColorItem(1, "Log Level None", mColorSchemeLight[6]),
                ColorItem(2, "Log Level Verbose", mColorSchemeLight[7]),
                ColorItem(3, "Log Level Debug", mColorSchemeLight[8]),
                ColorItem(4, "Log Level Info", mColorSchemeLight[9]),
                ColorItem(5, "Log Level Warning", mColorSchemeLight[10]),
                ColorItem(6, "Log Level Error", mColorSchemeLight[11]),
                ColorItem(7, "Log Level Fatal", mColorSchemeLight[12]),
                ColorItem(8, "PID FG", mColorSchemeLight[13]),
                ColorItem(9, "TID FG", mColorSchemeLight[14]),
                ColorItem(10, "Tag FG", mColorSchemeLight[15]),
                ColorItem(11, "Highlight FG", mColorSchemeLight[16]),
                ColorItem(12, "LineNum FG", mColorSchemeLight[17]),
                ColorItem(18, "NumLogSeperator BG", mColorSchemeLight[18]),
                ColorItem(19, "Bookmark Selected BG", mColorSchemeLight[19]),
                ColorItem(20, "LineNum Bookmark Selected BG", mColorSchemeLight[20]),
                ColorItem(21, "LineNum Bookmark BG", mColorSchemeLight[21]),
                ColorItem(22, "LineNum Selected BG", mColorSchemeLight[22]),
                ColorItem(23, "Highlight BG", mColorSchemeLight[23]),
                ColorItem(24, "Filtered 1 FG", mColorSchemeLight[24]),
                ColorItem(25, "Filtered 2 FG", mColorSchemeLight[25]),
                ColorItem(26, "Filtered 3 FG", mColorSchemeLight[26]),
                ColorItem(27, "Filtered 4 FG", mColorSchemeLight[27]),
                ColorItem(28, "Filtered 5 FG", mColorSchemeLight[28]),
                ColorItem(29, "Filtered 6 FG", mColorSchemeLight[29]),
                ColorItem(30, "Filtered 7 FG", mColorSchemeLight[30]),
                ColorItem(31, "Filtered 8 FG", mColorSchemeLight[31]),
                ColorItem(32, "Filtered 9 FG", mColorSchemeLight[32]),
                ColorItem(33, "Filtered 1 BG", mColorSchemeLight[33]),
                ColorItem(34, "Filtered 2 BG", mColorSchemeLight[34]),
                ColorItem(35, "Filtered 3 BG", mColorSchemeLight[35]),
                ColorItem(36, "Filtered 4 BG", mColorSchemeLight[36]),
                ColorItem(37, "Filtered 5 BG", mColorSchemeLight[37]),
                ColorItem(38, "Filtered 6 BG", mColorSchemeLight[38]),
                ColorItem(39, "Filtered 7 BG", mColorSchemeLight[39]),
                ColorItem(40, "Filtered 8 BG", mColorSchemeLight[40]),
                ColorItem(41, "Filtered 9 BG", mColorSchemeLight[41]),
        )

        fun getConfig() {
            for (idx in mColorArray.indices) {
                val item = mConfigManager.getItem("${ConfigManager.ITEM_COLOR_MANAGER}${mType}_$idx")
                if (item != null) {
                    mColorArray[idx].mStrColor = item
                }
            }
        }

        fun putConfig() {
            for (idx in mColorArray.indices) {
                mConfigManager.setItem("${ConfigManager.ITEM_COLOR_MANAGER}${mType}_$idx", mColorArray[idx].mStrColor)
            }
        }

        fun applyColor() {
            StrFilteredFG = mColorArray[TableColorIdx.FILTERED_FG.value].mStrColor
            StrSelectedBG = mColorArray[TableColorIdx.SELECTED_BG.value].mStrColor
            StrFilteredBG = mColorArray[TableColorIdx.FILTERED_BG.value].mStrColor
            StrLogBG = mColorArray[TableColorIdx.LOG_BG.value].mStrColor
            StrLineNumBG = mColorArray[TableColorIdx.LINE_NUM_BG.value].mStrColor
            StrBookmarkBG = mColorArray[TableColorIdx.BOOKMARK_BG.value].mStrColor
            StrLogLevelNone = mColorArray[TableColorIdx.LOG_LEVEL_NONE.value].mStrColor
            StrLogLevelVerbose = mColorArray[TableColorIdx.LOG_LEVEL_VERBOSE.value].mStrColor
            StrLogLevelDebug = mColorArray[TableColorIdx.LOG_LEVEL_DEBUG.value].mStrColor
            StrLogLevelInfo = mColorArray[TableColorIdx.LOG_LEVEL_INFO.value].mStrColor
            StrLogLevelWarning = mColorArray[TableColorIdx.LOG_LEVEL_WARNING.value].mStrColor
            StrLogLevelError = mColorArray[TableColorIdx.LOG_LEVEL_ERROR.value].mStrColor
            StrLogLevelFatal = mColorArray[TableColorIdx.LOG_LEVEL_FATAL.value].mStrColor
            StrPidFG = mColorArray[TableColorIdx.PID_FG.value].mStrColor
            StrTidFG = mColorArray[TableColorIdx.TID_FG.value].mStrColor
            StrTagFG = mColorArray[TableColorIdx.TAG_FG.value].mStrColor
            StrHighlightFG = mColorArray[TableColorIdx.HIGHLIGHT_FG.value].mStrColor
            StrLineNumFG = mColorArray[TableColorIdx.LINE_NUM_FG.value].mStrColor
            StrNumLogSeperatorBG = mColorArray[TableColorIdx.NUM_LOG_SEPERATOR_BG.value].mStrColor
            StrBookmarkSelectedBG = mColorArray[TableColorIdx.BOOKMARK_SELECTED_BG.value].mStrColor
            StrNumBookmarkSelectedBG = mColorArray[TableColorIdx.NUM_BOOKMARK_SELECTED_BG.value].mStrColor
            StrNumBookmarkBG = mColorArray[TableColorIdx.NUM_BOOKMARK_BG.value].mStrColor
            StrNumSelectedBG = mColorArray[TableColorIdx.NUM_SELECTED_BG.value].mStrColor
            StrHighlightBG = mColorArray[TableColorIdx.HIGHLIGHT_BG.value].mStrColor

            for (idx in StrFilteredFGs.indices) {
                if (idx == 0) {
                    StrFilteredFGs[idx] = StrFilteredFG
                    StrFilteredBGs[idx] = StrFilteredBG
                }
                else {
                    StrFilteredFGs[idx] = mColorArray[TableColorIdx.FILTERED_START_FG.value + idx - 1].mStrColor
                    StrFilteredBGs[idx] = mColorArray[TableColorIdx.FILTERED_START_BG.value + idx - 1].mStrColor
                }
            }

            for (listener in mColorEventListeners) {
                listener.colorChanged(ColorEvent(0))
            }
        }
    }

    var mFilterColorSchemeLight = arrayOf(
            "#FFFFFF",
            "#FFA0A0",
            "#00FF00",
    )

    var mFilterColorSchemeDark = arrayOf(
            "#46494B",
            "#AA5050",
            "#007700",
    )

    var mFilterStyle = arrayOf(
            ColorItem(0, "Include Text", mFilterColorSchemeLight[0]),
            ColorItem(1, "Exclude Text", mFilterColorSchemeLight[1]),
            ColorItem(2, "Separator", mFilterColorSchemeLight[2]),
    )

    var mFilterStyleInclude: Color = Color.decode(mFilterStyle[0].mStrColor)
    var mFilterStyleExclude: Color = Color.decode(mFilterStyle[1].mStrColor)
    var mFilterStyleSeparator: Color = Color.decode(mFilterStyle[2].mStrColor)

    fun applyFilterStyle() {
        mFilterStyleInclude = Color.decode(mFilterStyle[0].mStrColor)
        mFilterStyleExclude = Color.decode(mFilterStyle[1].mStrColor)
        mFilterStyleSeparator = Color.decode(mFilterStyle[2].mStrColor)

        for (listener in mFilterStyleEventListeners) {
            listener.colorChanged(ColorEvent(0))
        }
    }

    fun getConfigFilterStyle() {
        for (idx in mFilterStyle.indices) {
            val item = mConfigManager.getItem(ConfigManager.ITEM_COLOR_FILTER_STYLE + idx)
            if (item != null) {
                mFilterStyle[idx].mStrColor = item
            }
        }
    }

    fun putConfigFilterStyle() {
        for (idx in mFilterStyle.indices) {
            mConfigManager.setItem(ConfigManager.ITEM_COLOR_FILTER_STYLE + idx, mFilterStyle[idx].mStrColor)
        }
    }

    fun addFilterStyleEventListener(listener:ColorEventListener) {
        mFilterStyleEventListeners.add(listener)
    }

    fun removeFilterStyleEventListener(listener:ColorEventListener) {
        mFilterStyleEventListeners.remove(listener)
    }
}

