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

    data class ColorItem(val mOrder: Int, val mName: String, var mStrColor: String)

    private val mConfigManager = ConfigManager.getInstance()

    enum class TableColorType(val value: Int) {
        FULL_LOG_TABLE(0),
        FILTER_LOG_TABLE(1);

        companion object {
            fun fromInt(value: Int) = entries.first { it.value == value }
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
        TOKEN0_FG(13),
        TOKEN1_FG(14),
        TOKEN2_FG(15),
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
        SEARCH_FG(42),
        SEARCH_BG(43),
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
            "#FFFFFF",
            "#3030B0",
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
            "#000000",
            "#B0B0B0",
    )

    // Must be declared after mColorSchemeLight (internally mColorSchemeLight is used)
    val mFullTableColor = TableColor(TableColorType.FULL_LOG_TABLE)
    val mFilterTableColor = TableColor(TableColorType.FILTER_LOG_TABLE)

    inner class TableColor(type: TableColorType) {
        private val mType = type

        var mStrFilteredFG = "#000000"
            set(value) {
                field = value
                mFilteredFG = Color.decode(value)
            }
        var mFilteredFG: Color = Color.decode(mStrFilteredFG)
            private set

        var mStrFilteredBG = "#000000"
            set(value) {
                field = value
                mFilteredBG = Color.decode(value)
            }
        var mFilteredBG: Color = Color.decode(mStrFilteredBG)
            private set

        var mStrToken0FG = "#000000"
            set(value) {
                field = value
                mToken0FG = Color.decode(value)
            }
        var mToken0FG: Color = Color.decode(mStrToken0FG)
            private set

        var mStrToken1FG = "#000000"
            set(value) {
                field = value
                mToken1FG = Color.decode(value)
            }
        var mToken1FG: Color = Color.decode(mStrToken1FG)
            private set

        var mStrToken2FG = "#000000"
            set(value) {
                field = value
                mToken2FG = Color.decode(value)
            }
        var mToken2FG: Color = Color.decode(mStrToken2FG)
            private set

        var mStrHighlightFG = "#000000"
            set(value) {
                field = value
                mHighlightFG = Color.decode(value)
            }
        var mHighlightFG: Color = Color.decode(mStrHighlightFG)
            private set

        var mStrSearchFG = "#000000"
            set(value) {
                field = value
                mSearchFG = Color.decode(value)
            }
        var mSearchFG: Color = Color.decode(mStrSearchFG)
            private set

        var mStrSelectedBG = "#000000"
            set(value) {
                field = value
                mSelectedBG = Color.decode(value)
            }
        var mSelectedBG: Color = Color.decode(mStrSelectedBG)
            private set

        var mStrLogBG = "#000000"
            set(value) {
                field = value
                mLogBG = Color.decode(value)
            }
        var mLogBG: Color = Color.decode(mStrLogBG)
            private set

        var mStrLineNumBG = "#000000"
            set(value) {
                field = value
                mLineNumBG = Color.decode(value)
            }
        var mLineNumBG: Color = Color.decode(mStrLineNumBG)
            private set

        var mStrBookmarkBG = "#000000"
            set(value) {
                field = value
                mBookmarkBG = Color.decode(value)
            }
        var mBookmarkBG: Color = Color.decode(mStrBookmarkBG)
            private set

        var mStrLogLevelNone = "#000000"
            set(value) {
                field = value
                mLogLevelNone = Color.decode(value)
            }
        var mLogLevelNone: Color = Color.decode(mStrLogLevelNone)
            private set

        var mStrLogLevelVerbose = "#000000"
            set(value) {
                field = value
                mLogLevelVerbose = Color.decode(value)
            }
        var mLogLevelVerbose: Color = Color.decode(mStrLogLevelVerbose)
            private set

        var mStrLogLevelDebug = "#000000"
            set(value) {
                field = value
                mLogLevelDebug = Color.decode(value)
            }
        var mLogLevelDebug: Color = Color.decode(mStrLogLevelDebug)
            private set

        var mStrLogLevelInfo = "#000000"
            set(value) {
                field = value
                mLogLevelInfo = Color.decode(value)
            }
        var mLogLevelInfo: Color = Color.decode(mStrLogLevelInfo)
            private set

        var mStrLogLevelWarning = "#000000"
            set(value) {
                field = value
                mLogLevelWarning = Color.decode(value)
            }
        var mLogLevelWarning: Color = Color.decode(mStrLogLevelWarning)
            private set

        var mStrLogLevelError = "#000000"
            set(value) {
                field = value
                mLogLevelError = Color.decode(value)
            }
        var mLogLevelError: Color = Color.decode(mStrLogLevelError)
            private set

        var mStrLogLevelFatal = "#000000"
            set(value) {
                field = value
                mLogLevelFatal = Color.decode(value)
            }
        var mLogLevelFatal: Color = Color.decode(mStrLogLevelFatal)
            private set

        var mStrLineNumFG = "#000000"
            set(value) {
                field = value
                mLineNumFG = Color.decode(value)
            }
        var mLineNumFG: Color = Color.decode(mStrLineNumBG)
            private set

        var mStrNumLogSeperatorBG = "#000000"
            set(value) {
                field = value
                mNumLogSeperatorBG = Color.decode(value)
            }
        var mNumLogSeperatorBG: Color = Color.decode(mStrNumLogSeperatorBG)
            private set

        var mStrBookmarkSelectedBG = "#000000"
            set(value) {
                field = value
                mBookmarkSelectedBG = Color.decode(value)
            }
        var mBookmarkSelectedBG: Color = Color.decode(mStrBookmarkSelectedBG)
            private set

        var mStrNumBookmarkSelectedBG = "#000000"
            set(value) {
                field = value
                mNumBookmarkSelectedBG = Color.decode(value)
            }
        var mNumBookmarkSelectedBG: Color = Color.decode(mStrNumBookmarkSelectedBG)
            private set

        var mStrNumBookmarkBG = "#000000"
            set(value) {
                field = value
                mNumBookmarkBG = Color.decode(value)
            }
        var mNumBookmarkBG: Color = Color.decode(mStrNumBookmarkBG)
            private set

        var mStrNumSelectedBG = "#000000"
            set(value) {
                field = value
                mNumSelectedBG = Color.decode(value)
            }
        var mNumSelectedBG: Color = Color.decode(mStrNumSelectedBG)
            private set

        var mStrHighlightBG = "#000000"
            set(value) {
                field = value
                mHighlightBG = Color.decode(value)
            }
        var mHighlightBG: Color = Color.decode(mStrHighlightBG)
            private set

        var mStrSearchBG = "#000000"
            set(value) {
                field = value
                mSearchBG = Color.decode(value)
            }
        var mSearchBG: Color = Color.decode(mStrSearchBG)
            private set
        
        var mStrFilteredFGs = arrayOf(mColorSchemeLight[0],
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

        var mStrFilteredBGs = arrayOf(mColorSchemeLight[2],
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
                ColorItem(14, "Selected BG", mColorSchemeLight[1]),
                ColorItem(15, "Filtered BG", mColorSchemeLight[2]),
                ColorItem(16, "Log BG", mColorSchemeLight[3]),
                ColorItem(17, "LineNum BG", mColorSchemeLight[4]),
                ColorItem(18, "Bookmark BG", mColorSchemeLight[5]),
                ColorItem(1, "Log Level None", mColorSchemeLight[6]),
                ColorItem(2, "Log Level Verbose", mColorSchemeLight[7]),
                ColorItem(3, "Log Level Debug", mColorSchemeLight[8]),
                ColorItem(4, "Log Level Info", mColorSchemeLight[9]),
                ColorItem(5, "Log Level Warning", mColorSchemeLight[10]),
                ColorItem(6, "Log Level Error", mColorSchemeLight[11]),
                ColorItem(7, "Log Level Fatal", mColorSchemeLight[12]),
                ColorItem(8, "Token0 FG", mColorSchemeLight[13]),
                ColorItem(9, "Token1 FG", mColorSchemeLight[14]),
                ColorItem(10, "Token2 FG", mColorSchemeLight[15]),
                ColorItem(11, "Highlight FG", mColorSchemeLight[16]),
                ColorItem(13, "LineNum FG", mColorSchemeLight[17]),
                ColorItem(19, "NumLogSeperator BG", mColorSchemeLight[18]),
                ColorItem(20, "Bookmark Selected BG", mColorSchemeLight[19]),
                ColorItem(21, "LineNum Bookmark Selected BG", mColorSchemeLight[20]),
                ColorItem(22, "LineNum Bookmark BG", mColorSchemeLight[21]),
                ColorItem(23, "LineNum Selected BG", mColorSchemeLight[22]),
                ColorItem(24, "Highlight BG", mColorSchemeLight[23]),
                ColorItem(26, "Filtered 1 FG", mColorSchemeLight[24]),
                ColorItem(27, "Filtered 2 FG", mColorSchemeLight[25]),
                ColorItem(28, "Filtered 3 FG", mColorSchemeLight[26]),
                ColorItem(29, "Filtered 4 FG", mColorSchemeLight[27]),
                ColorItem(30, "Filtered 5 FG", mColorSchemeLight[28]),
                ColorItem(31, "Filtered 6 FG", mColorSchemeLight[29]),
                ColorItem(32, "Filtered 7 FG", mColorSchemeLight[30]),
                ColorItem(33, "Filtered 8 FG", mColorSchemeLight[31]),
                ColorItem(34, "Filtered 9 FG", mColorSchemeLight[32]),
                ColorItem(35, "Filtered 1 BG", mColorSchemeLight[33]),
                ColorItem(36, "Filtered 2 BG", mColorSchemeLight[34]),
                ColorItem(37, "Filtered 3 BG", mColorSchemeLight[35]),
                ColorItem(38, "Filtered 4 BG", mColorSchemeLight[36]),
                ColorItem(39, "Filtered 5 BG", mColorSchemeLight[37]),
                ColorItem(40, "Filtered 6 BG", mColorSchemeLight[38]),
                ColorItem(41, "Filtered 7 BG", mColorSchemeLight[39]),
                ColorItem(42, "Filtered 8 BG", mColorSchemeLight[40]),
                ColorItem(43, "Filtered 9 BG", mColorSchemeLight[41]),
                ColorItem(12, "Search FG", mColorSchemeLight[42]),
                ColorItem(25, "Search BG", mColorSchemeLight[43]),
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
            mStrFilteredFG = mColorArray[TableColorIdx.FILTERED_FG.value].mStrColor
            mStrSelectedBG = mColorArray[TableColorIdx.SELECTED_BG.value].mStrColor
            mStrFilteredBG = mColorArray[TableColorIdx.FILTERED_BG.value].mStrColor
            mStrLogBG = mColorArray[TableColorIdx.LOG_BG.value].mStrColor
            mStrLineNumBG = mColorArray[TableColorIdx.LINE_NUM_BG.value].mStrColor
            mStrBookmarkBG = mColorArray[TableColorIdx.BOOKMARK_BG.value].mStrColor
            mStrLogLevelNone = mColorArray[TableColorIdx.LOG_LEVEL_NONE.value].mStrColor
            mStrLogLevelVerbose = mColorArray[TableColorIdx.LOG_LEVEL_VERBOSE.value].mStrColor
            mStrLogLevelDebug = mColorArray[TableColorIdx.LOG_LEVEL_DEBUG.value].mStrColor
            mStrLogLevelInfo = mColorArray[TableColorIdx.LOG_LEVEL_INFO.value].mStrColor
            mStrLogLevelWarning = mColorArray[TableColorIdx.LOG_LEVEL_WARNING.value].mStrColor
            mStrLogLevelError = mColorArray[TableColorIdx.LOG_LEVEL_ERROR.value].mStrColor
            mStrLogLevelFatal = mColorArray[TableColorIdx.LOG_LEVEL_FATAL.value].mStrColor
            mStrToken0FG = mColorArray[TableColorIdx.TOKEN0_FG.value].mStrColor
            mStrToken1FG = mColorArray[TableColorIdx.TOKEN1_FG.value].mStrColor
            mStrToken2FG = mColorArray[TableColorIdx.TOKEN2_FG.value].mStrColor
            mStrHighlightFG = mColorArray[TableColorIdx.HIGHLIGHT_FG.value].mStrColor
            mStrSearchFG = mColorArray[TableColorIdx.SEARCH_FG.value].mStrColor
            mStrLineNumFG = mColorArray[TableColorIdx.LINE_NUM_FG.value].mStrColor
            mStrNumLogSeperatorBG = mColorArray[TableColorIdx.NUM_LOG_SEPERATOR_BG.value].mStrColor
            mStrBookmarkSelectedBG = mColorArray[TableColorIdx.BOOKMARK_SELECTED_BG.value].mStrColor
            mStrNumBookmarkSelectedBG = mColorArray[TableColorIdx.NUM_BOOKMARK_SELECTED_BG.value].mStrColor
            mStrNumBookmarkBG = mColorArray[TableColorIdx.NUM_BOOKMARK_BG.value].mStrColor
            mStrNumSelectedBG = mColorArray[TableColorIdx.NUM_SELECTED_BG.value].mStrColor
            mStrHighlightBG = mColorArray[TableColorIdx.HIGHLIGHT_BG.value].mStrColor
            mStrSearchBG = mColorArray[TableColorIdx.SEARCH_BG.value].mStrColor

            for (idx in mStrFilteredFGs.indices) {
                if (idx == 0) {
                    mStrFilteredFGs[idx] = mStrFilteredFG
                    mStrFilteredBGs[idx] = mStrFilteredBG
                }
                else {
                    mStrFilteredFGs[idx] = mColorArray[TableColorIdx.FILTERED_START_FG.value + idx - 1].mStrColor
                    mStrFilteredBGs[idx] = mColorArray[TableColorIdx.FILTERED_START_BG.value + idx - 1].mStrColor
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

