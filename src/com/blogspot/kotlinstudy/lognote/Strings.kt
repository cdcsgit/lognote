package com.blogspot.kotlinstudy.lognote

class Strings private constructor() {
    companion object {
        const val KO = 0
        const val EN = 1
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
        private val IDX_FILE = idx++
        private val IDX_OPEN = idx++
        private val IDX_FOLLOW = idx++
        private val IDX_OPEN_FILES = idx++
        private val IDX_APPEND_FILES = idx++
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
        private val IDX_CLEAR_VIEWS = idx++
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
        private val IDX_CONNECT = idx++
        private val IDX_REFRESH = idx++
        private val IDX_DISCONNECT = idx++
        private val IDX_APPLY = idx++
        private val IDX_SCROLLBACK_LINES = idx++
        private val IDX_SPLIT_FILE = idx++
        private val IDX_COLOR = idx++
        private val IDX_FILTERS = idx++
        private val IDX_KEEP = idx++
        private val IDX_OK = idx++
        private val IDX_CANCEL = idx++
        private val IDX_NEW = idx++
        private val IDX_COPY = idx++
        private val IDX_EDIT = idx++
        private val IDX_ADD = idx++
        private val IDX_REPLACE = idx++
        private val IDX_DELETE = idx++
        private val IDX_RESET = idx++
        private val IDX_SAVE = idx++
        private val IDX_SELECT = idx++
        private val IDX_ADB_PATH = idx++
        private val IDX_LOG_PATH = idx++
        private val IDX_SIZE = idx++
        private val IDX_CONNECTED = idx++
        private val IDX_NOT_CONNECTED = idx++
        private val IDX_APPEND = idx++
        private val IDX_MSG_SELECT_OPEN_MODE = idx++
        private val IDX_NONE = idx++
        private val IDX_ADD_FILTER = idx++
        private val IDX_ADD_CMD = idx++
        private val IDX_CMDS = idx++
        private val IDX_RETRY_ADB = idx++
        private val IDX_FILTER_STYLE = idx++
        private val IDX_LOOK_AND_FEEL = idx++
        private val IDX_FULL_LOG_TABLE = idx++
        private val IDX_FILTER_LOG_TABLE = idx++
        private val IDX_BUILT_IN_SCHEMES = idx++
        private val IDX_LIGHT = idx++
        private val IDX_DARK = idx++
        private val IDX_APPEARANCE = idx++
        private val IDX_OPTIONS = idx++
        private val IDX_LOG_CMD = idx++
        private val IDX_NOT_FOUND = idx++
        private val IDX_ADD_INCLUDE = idx++
        private val IDX_ADD_EXCLUDE = idx++
        private val IDX_ADD_SEARCH = idx++
        private val IDX_SET_SEARCH = idx++
        private val IDX_SEARCH = idx++
        private val IDX_CMD = idx++
        private val IDX_NOT_LOGCAT = idx++
        private val IDX_FILTER_BY_FILE = idx++
        private val IDX_PROCESS_LIST = idx++
        private val IDX_CHECK_UPDATE = idx++
        private val IDX_FAILED_GET_VERSION_INFO = idx++
        private val IDX_USING_LATEST_VERSION = idx++
        private val IDX_AVAILABLE = idx++
        private val IDX_CURRENT = idx++
        private val IDX_RECENT_FILE = idx++
        private val IDX_APPLY_RECENT_FILE = idx++
        private val IDX_SET_UPDATE_PROCESS_TIMEOUT = idx++
        private val IDX_COLOR_TAG_REGEX = idx++
        private val IDX_SAVE_FULL = idx++
        private val IDX_SAVE_FILTERED = idx++
        private val IDX_LOG_FORMAT = idx++
        private val IDX_NAME = idx++
        private val IDX_SEPARATOR = idx++
        private val IDX_LEVEL_NTH = idx++
        private val IDX_LEVELS = idx++
        private val IDX_TOKENS = idx++
        private val IDX_PID_TOKEN = idx++
        private val IDX_NTH = idx++
        private val IDX_SAVE_FILTER = idx++
        private val IDX_WIDTH = idx++
        private val IDX_OPTIONAL = idx++
        private val IDX_IN_USE = idx++
        private val IDX_VAL_CHANGE_SAVE = idx++
        private val IDX_INVALID_VALUE = idx++
        private val IDX_INVALID_NAME_EXIST = idx++
        private val IDX_INVALID_INDEX = idx++
        private val IDX_SAVED_FORMAT_LIST = idx++
        private val IDX_SHOW_DIALOG = idx++
        private val IDX_RUN_CMD = idx++
        private val IDX_CMD_RESULT = idx++
        private val IDX_CMD_RESULT_ERROR = idx++
        private val IDX_AGING_TEST_TRIGGER = idx++
        private val IDX_HIDE_LIST = idx++
        private val IDX_SHOW_LIST = idx++
        private val IDX_ACTION = idx++
        private val IDX_ACTION_PARAMETER = idx++
        private val IDX_ONCE = idx++
        private val IDX_REPEAT = idx++
        private val IDX_STATUS = idx++
        private val IDX_STOPPED = idx++
        private val IDX_STARTED = idx++
        private val IDX_TRIGGER_CANNOT_ADD = idx++
        private val IDX_ERROR = idx++
        private val IDX_WARNING = idx++
        private val IDX_INFO = idx++
        private val IDX_TRIGGER = idx++
        private val IDX_MOVE_FIRST = idx++
        private val IDX_MOVE_UP = idx++
        private val IDX_MOVE_DOWN = idx++
        private val IDX_MOVE_LAST = idx++
        private val IDX_TRIGGER_CANNOT_EDIT = idx++
        private val IDX_TRIGGER_CANNOT_DELETE = idx++
        private val IDX_TRIGGER_CANNOT_HIDE = idx++
        private val IDX_LOG_TRIGGER = idx++
//        private val IDX_ = idx++

        val FILE: String
            get() { return currStrings[IDX_FILE] }
        val OPEN: String
            get() { return currStrings[IDX_OPEN] }
        val FOLLOW: String
            get() { return currStrings[IDX_FOLLOW] }
        val OPEN_FILES: String
            get() { return currStrings[IDX_OPEN_FILES] }
        val APPEND_FILES: String
            get() { return currStrings[IDX_APPEND_FILES] }
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
        val CLEAR_VIEWS: String
            get() { return currStrings[IDX_CLEAR_VIEWS] }
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
        val CONNECT: String
            get() { return currStrings[IDX_CONNECT] }
        val REFRESH: String
            get() { return currStrings[IDX_REFRESH] }
        val DISCONNECT: String
            get() { return currStrings[IDX_DISCONNECT] }
        val APPLY: String
            get() { return currStrings[IDX_APPLY] }
        val SCROLLBACK_LINES: String
            get() { return currStrings[IDX_SCROLLBACK_LINES] }
        val SPLIT_FILE: String
            get() { return currStrings[IDX_SPLIT_FILE] }
        val COLOR: String
            get() { return currStrings[IDX_COLOR] }
        val FILTERS: String
            get() { return currStrings[IDX_FILTERS] }
        val KEEP: String
            get() { return currStrings[IDX_KEEP] }
        val OK: String
            get() { return currStrings[IDX_OK] }
        val CANCEL: String
            get() { return currStrings[IDX_CANCEL] }
        val NEW: String
            get() { return currStrings[IDX_NEW] }
        val COPY: String
            get() { return currStrings[IDX_COPY] }
        val EDIT: String
            get() { return currStrings[IDX_EDIT] }
        val ADD: String
            get() { return currStrings[IDX_ADD] }
        val REPLACE: String
            get() { return currStrings[IDX_REPLACE] }
        val DELETE: String
            get() { return currStrings[IDX_DELETE] }
        val RESET: String
            get() { return currStrings[IDX_RESET] }
        val SAVE: String
            get() { return currStrings[IDX_SAVE] }
        val SELECT: String
            get() { return currStrings[IDX_SELECT] }
        val ADB_PATH: String
            get() { return currStrings[IDX_ADB_PATH] }
        val LOG_PATH: String
            get() { return currStrings[IDX_LOG_PATH] }
        val SIZE: String
            get() { return currStrings[IDX_SIZE] }
        val CONNECTED: String
            get() { return currStrings[IDX_CONNECTED] }
        val NOT_CONNECTED: String
            get() { return currStrings[IDX_NOT_CONNECTED] }
        val APPEND: String
            get() { return currStrings[IDX_APPEND] }
        val MSG_SELECT_OPEN_MODE: String
            get() { return currStrings[IDX_MSG_SELECT_OPEN_MODE] }
        val NONE: String
            get() { return currStrings[IDX_NONE] }
        val ADD_FILTER: String
            get() { return currStrings[IDX_ADD_FILTER] }
        val ADD_CMD: String
            get() { return currStrings[IDX_ADD_CMD] }
        val CMDS: String
            get() { return currStrings[IDX_CMDS] }
        val RETRY_ADB: String
            get() { return currStrings[IDX_RETRY_ADB] }
        val FILTER_STYLE: String
            get() { return currStrings[IDX_FILTER_STYLE] }
        val LOOK_AND_FEEL: String
            get() { return currStrings[IDX_LOOK_AND_FEEL] }
        val FULL_LOG_TABLE: String
            get() { return currStrings[IDX_FULL_LOG_TABLE] }
        val FILTER_LOG_TABLE: String
            get() { return currStrings[IDX_FILTER_LOG_TABLE] }
        val BUILT_IN_SCHEMES: String
            get() { return currStrings[IDX_BUILT_IN_SCHEMES] }
        val LIGHT: String
            get() { return currStrings[IDX_LIGHT] }
        val DARK: String
            get() { return currStrings[IDX_DARK] }
        val APPEARANCE: String
            get() { return currStrings[IDX_APPEARANCE] }
        val OPTIONS: String
            get() { return currStrings[IDX_OPTIONS] }
        val LOG_CMD: String
            get() { return currStrings[IDX_LOG_CMD] }
        val NOT_FOUND: String
            get() { return currStrings[IDX_NOT_FOUND] }
        val ADD_INCLUDE: String
            get() { return currStrings[IDX_ADD_INCLUDE] }
        val ADD_EXCLUDE: String
            get() { return currStrings[IDX_ADD_EXCLUDE] }
        val ADD_SEARCH: String
            get() { return currStrings[IDX_ADD_SEARCH] }
        val SET_SEARCH: String
            get() { return currStrings[IDX_SET_SEARCH] }
        val SEARCH: String
            get() { return currStrings[IDX_SEARCH] }
        val CMD: String
            get() { return currStrings[IDX_CMD] }
        val NOT_LOGCAT: String
            get() { return currStrings[IDX_NOT_LOGCAT] }
        val FILTER_BY_FILE: String
            get() { return currStrings[IDX_FILTER_BY_FILE] }
        val PROCESS_LIST: String
            get() { return currStrings[IDX_PROCESS_LIST] }
        val CHECK_UPDATE: String
            get() { return currStrings[IDX_CHECK_UPDATE] }
        val FAILED_GET_VERSION_INFO: String
            get() { return currStrings[IDX_FAILED_GET_VERSION_INFO] }
        val USING_LATEST_VERSION: String
            get() { return currStrings[IDX_USING_LATEST_VERSION] }
        val AVAILABLE: String
            get() { return currStrings[IDX_AVAILABLE] }
        val CURRENT: String
            get() { return currStrings[IDX_CURRENT] }
        val RECENT_FILE: String
            get() { return currStrings[IDX_RECENT_FILE] }
        val APPLY_RECENT_FILE: String
            get() { return currStrings[IDX_APPLY_RECENT_FILE] }
        val SET_UPDATE_PROCESS_TIMEOUT: String
            get() { return currStrings[IDX_SET_UPDATE_PROCESS_TIMEOUT] }
        val COLOR_TAG_REGEX: String
            get() { return currStrings[IDX_COLOR_TAG_REGEX] }
        val SAVE_FULL: String
            get() { return currStrings[IDX_SAVE_FULL] }
        val SAVE_FILTERED: String
            get() { return currStrings[IDX_SAVE_FILTERED] }
        val LOG_FORMAT: String
            get() { return currStrings[IDX_LOG_FORMAT] }
        val NAME: String
            get() { return currStrings[IDX_NAME] }
        val SEPARATOR: String
            get() { return currStrings[IDX_SEPARATOR] }
        val LEVEL_NTH: String
            get() { return currStrings[IDX_LEVEL_NTH] }
        val LEVELS: String
            get() { return currStrings[IDX_LEVELS] }
        val TOKENS: String
            get() { return currStrings[IDX_TOKENS] }
        val PID_TOKEN: String
            get() { return currStrings[IDX_PID_TOKEN] }
        val NTH: String
            get() { return currStrings[IDX_NTH] }
        val SAVE_FILTER: String
            get() { return currStrings[IDX_SAVE_FILTER] }
        val WIDTH: String
            get() { return currStrings[IDX_WIDTH] }
        val OPTIONAL: String
            get() { return currStrings[IDX_OPTIONAL] }
        val IN_USE: String
            get() { return currStrings[IDX_IN_USE] }
        val VAL_CHANGE_SAVE: String
            get() { return currStrings[IDX_VAL_CHANGE_SAVE] }
        val INVALID_VALUE: String
            get() { return currStrings[IDX_INVALID_VALUE] }
        val INVALID_NAME_EXIST: String
            get() { return currStrings[IDX_INVALID_NAME_EXIST] }
        val INVALID_INDEX: String
            get() { return currStrings[IDX_INVALID_INDEX] }
        val SAVED_FORMAT_LIST: String
            get() { return currStrings[IDX_SAVED_FORMAT_LIST] }
        val SHOW_DIALOG: String
            get() { return currStrings[IDX_SHOW_DIALOG] }
        val RUN_CMD: String
            get() { return currStrings[IDX_RUN_CMD] }
        val CMD_RESULT: String
            get() { return currStrings[IDX_CMD_RESULT] }
        val CMD_RESULT_ERROR: String
            get() { return currStrings[IDX_CMD_RESULT_ERROR] }
        val AGING_TEST_TRIGGER: String
            get() { return currStrings[IDX_AGING_TEST_TRIGGER] }
        val HIDE_LIST: String
            get() { return currStrings[IDX_HIDE_LIST] }
        val SHOW_LIST: String
            get() { return currStrings[IDX_SHOW_LIST] }
        val ACTION: String
            get() { return currStrings[IDX_ACTION] }
        val ACTION_PARAMETER: String
            get() { return currStrings[IDX_ACTION_PARAMETER] }
        val ONCE: String
            get() { return currStrings[IDX_ONCE] }
        val REPEAT: String
            get() { return currStrings[IDX_REPEAT] }
        val STATUS: String
            get() { return currStrings[IDX_STATUS] }
        val STOPPED: String
            get() { return currStrings[IDX_STOPPED] }
        val STARTED: String
            get() { return currStrings[IDX_STARTED] }
        val TRIGGER_CANNOT_ADD: String
            get() { return currStrings[IDX_TRIGGER_CANNOT_ADD] }
        val ERROR: String
            get() { return currStrings[IDX_ERROR] }
        val WARNING: String
            get() { return currStrings[IDX_WARNING] }
        val INFO: String
            get() { return currStrings[IDX_INFO] }
        val TRIGGER: String
            get() { return currStrings[IDX_TRIGGER] }
        val MOVE_FIRST: String
            get() { return currStrings[IDX_MOVE_FIRST] }
        val MOVE_UP: String
            get() { return currStrings[IDX_MOVE_UP] }
        val MOVE_DOWN: String
            get() { return currStrings[IDX_MOVE_DOWN] }
        val MOVE_LAST: String
            get() { return currStrings[IDX_MOVE_LAST] }
        val TRIGGER_CANNOT_EDIT: String
            get() { return currStrings[IDX_TRIGGER_CANNOT_EDIT] }
        val TRIGGER_CANNOT_DELETE: String
            get() { return currStrings[IDX_TRIGGER_CANNOT_DELETE] }
        val TRIGGER_CANNOT_HIDE: String
            get() { return currStrings[IDX_TRIGGER_CANNOT_HIDE] }
        val LOG_TRIGGER: String
            get() { return currStrings[IDX_LOG_TRIGGER] }
    }
}
