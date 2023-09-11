package com.blogspot.kotlinstudy.lognote

class FormatManager private constructor(){
    class FormatItem(val mName: String, val mLevels: Map<String, Int>) {
//    val levels: Array<String> = Array(FormatManager.MAX_LEVEL) { "" }
    }

    companion object {
        const val MAX_LEVEL = 7
        const val DEFAULT_LOGCAT = "logcat"
        val TEXT_LEVEL = arrayOf("None", "Verbose", "Debug", "Info", "Warning", "Error", "Fatal")

        const val LEVEL_NONE = 0
        const val LEVEL_VERBOSE = 1
        const val LEVEL_DEBUG = 2
        const val LEVEL_INFO = 3
        const val LEVEL_WARNING = 4
        const val LEVEL_ERROR = 5
        const val LEVEL_FATAL = 6

        private val mInstance: FormatManager = FormatManager()
        fun getInstance(): FormatManager {
            return mInstance
        }
    }

    private val mFormats = mutableMapOf<String, FormatItem>()
    var mCurrFormat: FormatItem
    init {
        val levels = mapOf("V" to LEVEL_VERBOSE
            , "D" to LEVEL_DEBUG
            , "I" to LEVEL_INFO
            , "W" to LEVEL_WARNING
            , "E" to LEVEL_ERROR
            , "F" to LEVEL_FATAL
        )

        mFormats[DEFAULT_LOGCAT] = FormatItem(DEFAULT_LOGCAT, levels)
        mCurrFormat = mFormats[DEFAULT_LOGCAT]!!
    }

    fun updateFormat(name: String, value: FormatItem) {
        mFormats[name] = value
    }

    fun removeFormat(name: String) {
        mFormats.remove(name)
    }

    fun clear() {
        mFormats.clear()
    }

    fun getNames(): List<String> {
        return mFormats.keys.toList()
    }

    fun getLevels(name: String): Map<String, Int> {
        return mFormats[name]?.mLevels ?: mapOf("" to -1)
    }
}