package com.blogspot.cdcsutils.lognote

class TooltipStringsEn private constructor() {
    companion object {
        val STRINGS = mutableMapOf<String, String>()
        init {
            val stringList = listOf(
                "Start adb logcat and receive logs"
                , "Pause adb logcat"
                , "Stop adb logcat"
                , "Clear log view"
                , "Save new file"
                , "List of connected devices or an address to connect to\n When the Enter key (or ctrl-r) is pressed, log reception starts after reconnection"
                , "adb connect [address]"
                , "Get list of connected devices"
                , "adb disconnect"
                , "Number of log lines to keep in the log view (0: no limit)"
                , "Save file is changed every number of scroll lines"
                , "Apply Line Count and File Split"
                , "Log is kept in the log view\nused when the log is pushed up by the number of applied lines when reviewing the log\nMemory shortage occurs when keep logs for a long time"
                , "Change log views position up/down, left/right"
                , "Log filter case-sensitive"
                , "Manage frequently used filters"
                , "Full log filter"
                , "Regex support, filter: search, -filter: exclude search, ex filter1|filter2|-exclude1"
                , "Tag filter"
                , "Regex support, filter: search, -filter: exclude search, ex filter1|filter2|-exclude1"
                , "PID filter"
                , "Regex support, filter: search, -filter: exclude search, ex filter1|filter2|-exclude1"
                , "TID filter"
                , "Regex support, filter: search, -filter: exclude search, ex filter1|filter2|-exclude1"
                , "Change text to bold only"
                , "Regex support ex text1|text2"
                , "Go to first line"
                , "Go to last line"
                , "Highlight PID"
                , "Highlight TID"
                , "Highlight tags"
                , "Show full log"
                , "Show bookmarks only"
                , "Move the view to a new window"
                , "Saved file name"
                , "Open filter list(add filter)"
                , "Open cmd list(add cmd)"
                , "Manage frequently used cmds"
                , "Auto retry when failed to receive log"
                , "Show/Hide ctrl button for follow mode"
                , "Append additional logs as the file grows"
                , "Stop append logs"
                , "Edit Log commands : Setting > adb"
                , "Regex support ex text1|text2"
                , "Search case-sensitive"
                , "F3 : Move to Previous"
                , "F4 : Move to Next"
                , "Searching View : filtered log, full log"
                , "ESC : Close search bar"
                , "Enter only numeric digits(0-9)"
                , "Log Format"
                , "Log Level"
                , "Token filter"
                , "Regex support, filter: search, -filter: exclude search, ex filter1|filter2|-exclude1"
                , "Token highlight"
                , "Invalid Format(Only number)"
                , "Invalid Name(Blank is not allowed)"
                , "Set the package to get the logs from"
                , "This package is not installed, so it not be applied"
                , "The selected packages will be applied when starting logcat"
//                , ""
            )

            for ((idx, str) in stringList.withIndex()) {
                STRINGS[idx.toString()] = str
            }
        }
    }
}
