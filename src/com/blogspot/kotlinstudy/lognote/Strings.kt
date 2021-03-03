package com.blogspot.kotlinstudy.lognote

class Strings private constructor() {
    companion object {
        val KO = 0
        val EN = 1
        var lang = EN
            set(value) {
                if (value == KO) {
                    currStrings = StringsKo.STRINGS
                } else {
                    currStrings = StringsEn.STRINGS
                }
                field = value
            }

        var currStrings = StringsEn.STRINGS
//        var currStrings = StringsKo.STRINGS

        private var idx = 0
        private val IDX_FILE = idx++
        private val IDX_OPEN = idx++
        private val IDX_OPEN_RECENTS = idx++
        private val IDX_CLOSE = idx++
        private val IDX_EXIT = idx++
        private val IDX_SETTING = idx++
        private val IDX_ADB = idx++
        private val IDX_FONT = idx++
        private val IDX_LOG = idx++
        private val IDX_START = idx++
        private val IDX_STOP = idx++
        private val IDX_PAUSE = idx++
        private val IDX_CLEAR = idx++
        private val IDX_ROTATION = idx++
        private val IDX_FIRST = idx++
        private val IDX_LAST = idx++
        private val IDX_BOLD = idx++
        private val IDX_HIDE = idx++
        private val IDX_TAG = idx++
        private val IDX_PID = idx++
        private val IDX_TID = idx++
        private val IDX_FILTER = idx++
        private val IDX_WINDOWED_MODE = idx++
        private val IDX_VIEW = idx++
        private val IDX_VIEW_FULL = idx++
        private val IDX_HELP = idx++
        private val IDX_ABOUT = idx++
        private val IDX_BOOKMARKS = idx++
        private val IDX_FULL = idx++
        private val IDX_INCREMENTAL = idx++
        private val IDX_HIGHLIGHT = idx++
        private val IDX_LOGLEVEL = idx++
        private val IDX_SCROLLBACK = idx++
        private val IDX_CLEAR_SAVE = idx++
        private val IDX_LOGFILE = idx++
//        private val IDX_ = idx++

        val FILE: String
            get() { return currStrings[IDX_FILE] }
        val OPEN: String
            get() { return currStrings[IDX_OPEN] }
        val OPEN_RECENTS: String
            get() { return currStrings[IDX_OPEN_RECENTS] }
        val CLOSE: String
            get() { return currStrings[IDX_CLOSE] }
        val EXIT: String
            get() { return currStrings[IDX_EXIT] }
        val SETTING: String
            get() { return currStrings[IDX_SETTING] }
        val ADB: String
            get() { return currStrings[IDX_ADB] }
        val FONT: String
            get() { return currStrings[IDX_FONT] }
        val LOG: String
            get() { return currStrings[IDX_LOG] }
        val START: String
            get() { return currStrings[IDX_START] }
        val STOP: String
            get() { return currStrings[IDX_STOP] }
        val PAUSE: String
            get() { return currStrings[IDX_PAUSE] }
        val CLEAR: String
            get() { return currStrings[IDX_CLEAR] }
        val ROTATION: String
            get() { return currStrings[IDX_ROTATION] }
        val FIRST: String
            get() { return currStrings[IDX_FIRST] }
        val LAST: String
            get() { return currStrings[IDX_LAST] }
        val BOLD: String
            get() { return currStrings[IDX_BOLD] }
        val HIDE: String
            get() { return currStrings[IDX_HIDE] }
        val TAG: String
            get() { return currStrings[IDX_TAG] }
        val PID: String
            get() { return currStrings[IDX_PID] }
        val TID: String
            get() { return currStrings[IDX_TID] }
        val FILTER: String
            get() { return currStrings[IDX_FILTER] }
        val WINDOWED_MODE: String
            get() { return currStrings[IDX_WINDOWED_MODE] }
        val VIEW: String
            get() { return currStrings[IDX_VIEW] }
        val VIEW_FULL: String
            get() { return currStrings[IDX_VIEW_FULL] }
        val HELP: String
            get() { return currStrings[IDX_HELP] }
        val ABOUT: String
            get() { return currStrings[IDX_ABOUT] }
        val BOOKMARKS: String
            get() { return currStrings[IDX_BOOKMARKS] }
        val FULL: String
            get() { return currStrings[IDX_FULL] }
        val INCREMENTAL: String
            get() { return currStrings[IDX_INCREMENTAL] }
        val HIGHLIGHT: String
            get() { return currStrings[IDX_HIGHLIGHT] }
        val LOGLEVEL: String
            get() { return currStrings[IDX_LOGLEVEL] }
        val SCROLLBACK: String
            get() { return currStrings[IDX_SCROLLBACK] }
        val CLEAR_SAVE: String
            get() { return currStrings[IDX_CLEAR_SAVE] }
        val LOGFILE: String
            get() { return currStrings[IDX_LOGFILE] }
    }
}