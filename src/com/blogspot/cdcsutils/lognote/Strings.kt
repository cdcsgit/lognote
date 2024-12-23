package com.blogspot.cdcsutils.lognote

class Strings private constructor() {
    companion object {
        const val KO = 0
        const val EN = 1
        val DEFAULT_STRINGS = StringsEn.STRINGS
        private var currStrings = StringsEn.STRINGS
        var lang = EN
            set(value) {
                currStrings = if (value == KO) {
                    StringsKo.STRINGS
                } else {
                    StringsEn.STRINGS
                }
                field = value
                TooltipStrings.lang = value
            }


        private var idx = 0

        val FILE = idx++.toString()
            get() { return currStrings[field]!! }
        val OPEN: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FOLLOW: String = idx++.toString()
            get() { return currStrings[field]!! }
        val OPEN_FILES: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPEND_FILES: String = idx++.toString()
            get() { return currStrings[field]!! }
        val OPEN_RECENTS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CLOSE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val EXIT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SETTING: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADB: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FONT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG: String = idx++.toString()
            get() { return currStrings[field]!! }
        val START: String = idx++.toString()
            get() { return currStrings[field]!! }
        val STOP: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PAUSE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CLEAR_VIEWS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ROTATION: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FIRST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LAST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val BOLD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val HIDE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TAG: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PID: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TID: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val WINDOWED_MODE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW: String = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_FULL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val HELP: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ABOUT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val BOOKMARKS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FULL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val INCREMENTAL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val HIGHLIGHT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOGLEVEL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CLEAR_SAVE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOGFILE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONNECT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val REFRESH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val DISCONNECT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPLY: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK_LINES: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SPLIT_FILE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val COLOR: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTERS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val KEEP: String = idx++.toString()
            get() { return currStrings[field]!! }
        val OK: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CANCEL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NEW: String = idx++.toString()
            get() { return currStrings[field]!! }
        val COPY: String = idx++.toString()
            get() { return currStrings[field]!! }
        val EDIT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val REPLACE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val DELETE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RESET: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SELECT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADB_PATH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_PATH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SIZE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONNECTED: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NOT_CONNECTED: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPEND: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MSG_SELECT_OPEN_MODE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NONE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_FILTER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_CMD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CMDS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RETRY_ADB: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTER_STYLE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOOK_AND_FEEL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FULL_LOG_TABLE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTER_LOG_TABLE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val BUILT_IN_SCHEMES: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LIGHT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val DARK: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPEARANCE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val OPTIONS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_CMD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NOT_FOUND: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_INCLUDE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_EXCLUDE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_SEARCH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SET_SEARCH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CMD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NOT_LOGCAT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTER_BY_FILE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PROCESS_LIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CHECK_UPDATE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val FAILED_GET_VERSION_INFO: String = idx++.toString()
            get() { return currStrings[field]!! }
        val USING_LATEST_VERSION: String = idx++.toString()
            get() { return currStrings[field]!! }
        val AVAILABLE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CURRENT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RECENT_FILE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPLY_RECENT_FILE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SET_UPDATE_PROCESS_TIMEOUT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val COLOR_TAG_REGEX: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVE_FULL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVE_FILTERED: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_FORMAT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val NAME: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SEPARATOR: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LEVEL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LEVELS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TOKENS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PID_TOKEN_FILTER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val POSITION: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVE_FILTER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val WIDTH: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PID_TOKEN_FILTER_OPTIONAL: String = idx++.toString()
            get() { return currStrings[field]!! }
        val IN_USE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val VAL_CHANGE_SAVE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val INVALID_VALUE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val INVALID_NAME_EXIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val INVALID_INDEX: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVED_FORMAT_LIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW_DIALOG: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RUN_CMD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CMD_RESULT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CMD_RESULT_ERROR: String = idx++.toString()
            get() { return currStrings[field]!! }
        val AGING_TEST_TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val HIDE_LIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW_LIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ACTION: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ACTION_PARAMETER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ONCE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val REPEAT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val STATUS: String = idx++.toString()
            get() { return currStrings[field]!! }
        val STOPPED: String = idx++.toString()
            get() { return currStrings[field]!! }
        val STARTED: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TRIGGER_CANNOT_ADD: String = idx++.toString()
            get() { return currStrings[field]!! }
        val ERROR: String = idx++.toString()
            get() { return currStrings[field]!! }
        val WARNING: String = idx++.toString()
            get() { return currStrings[field]!! }
        val INFO: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MOVE_FIRST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MOVE_UP: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MOVE_DOWN: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MOVE_LAST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TRIGGER_CANNOT_EDIT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TRIGGER_CANNOT_DELETE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TRIGGER_CANNOT_HIDE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RESULT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONFIRM_START_TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONFIRM_STOP_TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONFIRM_DELETE_TRIGGER: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONFIRM_RESET_FORMAT_LIST: String = idx++.toString()
            get() { return currStrings[field]!! }
        val CONFIRM_DELETE_FORMAT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW_ENTIRE_LINE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val BOOKMARK: String = idx++.toString()
            get() { return currStrings[field]!! }
        val RECONNECT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val COLUMN: String = idx++.toString()
            get() { return currStrings[field]!! }
        val TOKEN_COUNT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val DIVIDED_BY_COLUMN: String = idx++.toString()
            get() { return currStrings[field]!! }
        val USED_COLUMN_VIEW_MODE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW_PROCESS_NAME: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SHOW_WITH_COLORBG: String = idx++.toString()
            get() { return currStrings[field]!! }
        val MOVE_FULL_LOG_TO_NEW_WINDOW: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SELECT_PACKAGE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val PACKAGES: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SELECT_UNSELECT_PACKAGE: String = idx++.toString()
            get() { return currStrings[field]!! }
        val SAMPLE_TEXT: String = idx++.toString()
            get() { return currStrings[field]!! }
        val APPLY_CTRL_ENTER: String = idx++.toString()
            get() { return currStrings[field]!! }
    }
}
