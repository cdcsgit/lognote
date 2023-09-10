package com.blogspot.kotlinstudy.lognote

class FormatManager private constructor(){
    class FormatItem(val mName: String, val mLevels: Map<String, Int>) {
//    val levels: Array<String> = Array(FormatManager.MAX_LEVEL) { "" }
    }

    companion object {
        const val MAX_LEVEL = 7
        private const val DEFAULT_LOGCAT = "logcat"
        private const val TEXT_NONE = "None"
        private const val TEXT_VERBOSE = "Verbose"
        private const val TEXT_DEBUG = "Debug"
        private const val TEXT_INFO = "Info"
        private const val TEXT_WARNING = "Warning"
        private const val TEXT_ERROR = "Error"
        private const val TEXT_FATAL = "Fatal"

        const val LEVEL_NONE = -1
        const val LEVEL_VERBOSE = 0
        const val LEVEL_DEBUG = 1
        const val LEVEL_INFO = 2
        const val LEVEL_WARNING = 3
        const val LEVEL_ERROR = 4
        const val LEVEL_FATAL = 5

        private val mInstance: FormatManager = FormatManager()
        fun getInstance(): FormatManager {
            return mInstance
        }
    }

    private val mFormats = mutableMapOf<String, FormatItem>()
    var mCurrFormat: FormatItem
    init {
        val levels = mapOf(TEXT_NONE to LEVEL_NONE
            , TEXT_VERBOSE to LEVEL_VERBOSE
            , TEXT_DEBUG to LEVEL_DEBUG
            , TEXT_INFO to LEVEL_INFO
            , TEXT_WARNING to LEVEL_WARNING
            , TEXT_ERROR to LEVEL_ERROR
            , TEXT_FATAL to LEVEL_FATAL
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