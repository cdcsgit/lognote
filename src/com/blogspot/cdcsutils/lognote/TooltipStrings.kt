package com.blogspot.cdcsutils.lognote

class TooltipStrings private constructor() {
    companion object {
        val DEFAULT_STRINGS = TooltipStringsEn.STRINGS
        private var currStrings = TooltipStringsEn.STRINGS
        var lang = Strings.EN
            set(value) {
                currStrings = if (value == Strings.KO) {
                    TooltipStringsKo.STRINGS
                } else {
                    TooltipStringsEn.STRINGS
                }
                field = value
            }

        private var idx = 0

        val START_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val PAUSE_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val STOP_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val CLEAR_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVE_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val DEVICES_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val CONNECT_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val REFRESH_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val DISCONNECT_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK_TF = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK_SPLIT_CHK = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK_APPLY_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SCROLLBACK_KEEP_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val ROTATION_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val CASE_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val FILTER_LIST_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val TAG_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val TAG_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val PID_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val PID_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val TID_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val TID_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val BOLD_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val BOLD_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_FIRST_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_LAST_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_PID_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_TID_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_TAG_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_FULL_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW_BOOKMARKS_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val VIEW__WINDOWED_MODE_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SAVED_FILE_TF = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_FILTER_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_CMD_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val CMD_LIST_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val RETRY_ADB_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val FOLLOW_LABEL = idx++.toString()
            get() { return currStrings[field]!! }
        val START_FOLLOW_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val STOP_FOLLOW_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_CMD_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_CASE_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_PREV_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_NEXT_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_TARGET_LABEL = idx++.toString()
            get() { return currStrings[field]!! }
        val SEARCH_CLOSE_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val ONLY_NUMBER = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_FORMAT_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val LOG_LEVEL_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val TOKEN_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val TOKEN_COMBO = idx++.toString()
            get() { return currStrings[field]!! }
        val TOKEN_VIEW_TOGGLE = idx++.toString()
            get() { return currStrings[field]!! }
        val INVALID_NUMBER_FORMAT = idx++.toString()
            get() { return currStrings[field]!! }
        val INVALID_NAME = idx++.toString()
            get() { return currStrings[field]!! }
        val ADD_PACKAGE_BTN = idx++.toString()
            get() { return currStrings[field]!! }
        val PACKAGE_NOT_INSTALLED = idx++.toString()
            get() { return currStrings[field]!! }
        val PACKAGE_APPLY_START = idx++.toString()
            get() { return currStrings[field]!! }
    }
}
