package com.blogspot.kotlinstudy.lognote

class TooltipStrings private constructor() {
    companion object {
        var currStrings = TooltipStringsEn.STRINGS
        var lang = Strings.EN
            set(value) {
                if (value == Strings.KO) {
                    currStrings = TooltipStringsKo.STRINGS
                } else {
                    currStrings = TooltipStringsEn.STRINGS
                }
                field = value
            }


        private var idx = 0
        private val IDX_START_BTN = idx++
        private val IDX_STOP_BTN = idx++
        private val IDX_CLEAR_BTN = idx++
        private val IDX_SAVE_BTN = idx++
        private val IDX_DEVICES_COMBO = idx++
        private val IDX_CONNECT_BTN = idx++
        private val IDX_REFRESH_BTN = idx++
        private val IDX_DISCONNECT_BTN = idx++
        private val IDX_SCROLLBACK_TF = idx++
        private val IDX_SCROLLBACK_SPLIT_CHK = idx++
        private val IDX_SCROLLBACK_APPLY_BTN = idx++
        private val IDX_SCROLLBACK_KEEP_TOGGLE = idx++
        private val IDX_ROTATION_BTN = idx++
        private val IDX_CASE_TOGGLE = idx++
        private val IDX_FILTER_LIST_BTN = idx++
        private val IDX_LOG_TOGGLE = idx++
        private val IDX_LOG_COMBO = idx++
        private val IDX_TAG_TOGGLE = idx++
        private val IDX_TAG_COMBO = idx++
        private val IDX_PID_TOGGLE = idx++
        private val IDX_PID_COMBO = idx++
        private val IDX_TID_TOGGLE = idx++
        private val IDX_TID_COMBO = idx++
        private val IDX_BOLD_TOGGLE = idx++
        private val IDX_BOLD_COMBO = idx++
        private val IDX_VIEW_FIRST_BTN = idx++
        private val IDX_VIEW_LAST_BTN = idx++
        private val IDX_VIEW_PID_TOGGLE = idx++
        private val IDX_VIEW_TID_TOGGLE = idx++
        private val IDX_VIEW_TAG_TOGGLE = idx++
        private val IDX_VIEW_FULL_TOGGLE = idx++
        private val IDX_VIEW_BOOKMARKS_TOGGLE = idx++
        private val IDX_VIEW__WINDOWED_MODE_BTN = idx++
        private val IDX_SAVED_FILE_TF = idx++
        private val IDX_ADD_FILTER_BTN = idx++
//        private val IDX_ = idx++

        val START_BTN: String
            get() { return currStrings[IDX_START_BTN] }
        val STOP_BTN: String
            get() { return currStrings[IDX_STOP_BTN] }
        val CLEAR_BTN: String
            get() { return currStrings[IDX_CLEAR_BTN] }
        val SAVE_BTN: String
            get() { return currStrings[IDX_SAVE_BTN] }
        val DEVICES_COMBO: String
            get() { return currStrings[IDX_DEVICES_COMBO] }
        val CONNECT_BTN: String
            get() { return currStrings[IDX_CONNECT_BTN] }
        val REFRESH_BTN: String
            get() { return currStrings[IDX_REFRESH_BTN] }
        val DISCONNECT_BTN: String
            get() { return currStrings[IDX_DISCONNECT_BTN] }
        val SCROLLBACK_TF: String
            get() { return currStrings[IDX_SCROLLBACK_TF] }
        val SCROLLBACK_SPLIT_CHK: String
            get() { return currStrings[IDX_SCROLLBACK_SPLIT_CHK] }
        val SCROLLBACK_APPLY_BTN: String
            get() { return currStrings[IDX_SCROLLBACK_APPLY_BTN] }
        val SCROLLBACK_KEEP_TOGGLE: String
            get() { return currStrings[IDX_SCROLLBACK_KEEP_TOGGLE] }
        val ROTATION_BTN: String
            get() { return currStrings[IDX_ROTATION_BTN] }
        val CASE_TOGGLE: String
            get() { return currStrings[IDX_CASE_TOGGLE] }
        val FILTER_LIST_BTN: String
            get() { return currStrings[IDX_FILTER_LIST_BTN] }
        val LOG_TOGGLE: String
            get() { return currStrings[IDX_LOG_TOGGLE] }
        val LOG_COMBO: String
            get() { return currStrings[IDX_LOG_COMBO] }
        val TAG_TOGGLE: String
            get() { return currStrings[IDX_TAG_TOGGLE] }
        val TAG_COMBO: String
            get() { return currStrings[IDX_TAG_COMBO] }
        val PID_TOGGLE: String
            get() { return currStrings[IDX_PID_TOGGLE] }
        val PID_COMBO: String
            get() { return currStrings[IDX_PID_COMBO] }
        val TID_TOGGLE: String
            get() { return currStrings[IDX_TID_TOGGLE] }
        val TID_COMBO: String
            get() { return currStrings[IDX_TID_COMBO] }
        val BOLD_TOGGLE: String
            get() { return currStrings[IDX_BOLD_TOGGLE] }
        val BOLD_COMBO: String
            get() { return currStrings[IDX_BOLD_COMBO] }
        val VIEW_FIRST_BTN: String
            get() { return currStrings[IDX_VIEW_FIRST_BTN] }
        val VIEW_LAST_BTN: String
            get() { return currStrings[IDX_VIEW_LAST_BTN] }
        val VIEW_PID_TOGGLE: String
            get() { return currStrings[IDX_VIEW_PID_TOGGLE] }
        val VIEW_TID_TOGGLE: String
            get() { return currStrings[IDX_VIEW_TID_TOGGLE] }
        val VIEW_TAG_TOGGLE: String
            get() { return currStrings[IDX_VIEW_TAG_TOGGLE] }
        val VIEW_FULL_TOGGLE: String
            get() { return currStrings[IDX_VIEW_FULL_TOGGLE] }
        val VIEW_BOOKMARKS_TOGGLE: String
            get() { return currStrings[IDX_VIEW_BOOKMARKS_TOGGLE] }
        val VIEW__WINDOWED_MODE_BTN: String
            get() { return currStrings[IDX_VIEW__WINDOWED_MODE_BTN] }
        val SAVED_FILE_TF: String
            get() { return currStrings[IDX_SAVED_FILE_TF] }
        val ADD_FILTER_BTN: String
            get() { return currStrings[IDX_ADD_FILTER_BTN] }
    }
}