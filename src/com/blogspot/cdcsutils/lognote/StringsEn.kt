package com.blogspot.cdcsutils.lognote

class StringsEn private constructor() {
    companion object {
        val STRINGS = mutableMapOf<String, String>()
        init {
            val stringList = listOf(
                "File"
                , "Open"
                , "Follow"
                , "Open files"
                , "Append files"
                , "Open Recents"
                , "Close"
                , "Exit"
                , "Setting"
                , "Adb"
                , "Font"
                , "Log"
                , "Start"
                , "Stop"
                , "Pause"
                , "Clear Views"
                , "Rotation"
                , "First"
                , "Last"
                , "Bold"
                , "Hide"
                , "Tag"
                , "PID"
                , "TID"
                , "Filter"
                , "Windowed Mode"
                , "View"
                , "Full Logs"
                , "Help"
                , "About"
                , "Bookmarks"
                , "Full"
                , "Incremental"
                , "Highlight"
                , "LogLevel"
                , "Scrollback"
                , "Clear/Save"
                , "Log file"
                , "Connect"
                , "Refresh"
                , "Disconnect"
                , "Apply"
                , "Scrollback(lines)"
                , "Split File"
                , "Color"
                , "Filters"
                , "Keep Log"
                , "OK"
                , "Cancel"
                , "New"
                , "Copy"
                , "Edit"
                , "Add"
                , "Replace"
                , "Delete"
                , "Reset"
                , "Save"
                , "Select"
                , "Adb path"
                , "Log path"
                , "Size"
                , "Connected"
                , "Not connected"
                , "Append"
                , "Select file open mode"
                , "None"
                , "Add filter"
                , "Add cmd"
                , "Cmds"
                , "Retry"
                , "Filter Style"
                , "Look and Feel"
                , "Full Log Table"
                , "Filter Log Table"
                , "Built-in schemes"
                , "Light"
                , "Dark"
                , "Appearance"
                , "Options"
                , "Log cmd"
                , "Not Found"
                , "Add Include"
                , "Add Exclude"
                , "Add Search"
                , "Set Search"
                , "Search"
                , "Cmd"
                , "Not logcat log"
                , "Save filters by file"
                , "Process list"
                , "Check update"
                , "Failed to get version information"
                , "Using the latest version"
                , "Available"
                , "Current"
                , "Recent file"
                , "Apply filters of recent file"
                , "Update process info timeout(sec, 0 : off, max : %d)"
                , "Color tag : regex filter"
                , "Save full log"
                , "Save filtered log"
                , "Log Format"
                , "Name"
                , "Separator"
                , "Level Position(Nth)"
                , "Levels"
                , "Tokens"
                , "PID Token filter"
                , "Position(Nth)"
                , "Save Filter"
                , "Width"
                , "Optional - Used when show process name tooltips in logs"
                , "In use"
                , "The values changed. Save it?"
                , "Invalid Value"
                , "Invalid Value(Name is exist)"
                , "Invalid Index"
                , "Saved Format List"
                , "Show dialog"
                , "Run cmd"
                , "Cmd result"
                , "Cmd result error"
                , "Aging Test Trigger"
                , "Hide List"
                , "Show List"
                , "Action"
                , "Action Parameter"
                , "Once"
                , "Repeat"
                , "Status"
                , "Stopped"
                , "Started"
                , "Trigger cannot be added, Max count"
                , "Error"
                , "Warning"
                , "Info"
                , "Trigger"
                , "Move First"
                , "Move Up"
                , "Move Down"
                , "Move Last"
                , "Trigger(%s) cannot be edited while in use"
                , "Trigger(%s) cannot be deleted while in use"
                , "Trigger View cannot be hidden while in use"
                , "Log Trigger(Aging Test)"
                , "Result"
                , "Do you want to start trigger \"%s\"?"
                , "Do you want to stop trigger \"%s\"?"
                , "Do you want to delete trigger \"%s\"?"
                , "The format list will be reset to default. Do you want to reset?"
                , "Do you want to delete format \"%s\"?"
                , "Show entire line"
                , "Bookmark"
                , "Reconnect"
                , "Column"
                , "Token count"
                , "Show divided by column"
                , "Used in column view mode"
                , "Show process name(adb mode)"
                , "Show"
                , "Show with color bg"
                , "Move Full log to new window"
//            , ""
            )

            for ((idx, str) in stringList.withIndex()) {
                STRINGS[idx.toString()] = str
            }
        }
    }
}
