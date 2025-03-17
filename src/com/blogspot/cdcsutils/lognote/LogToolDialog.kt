package com.blogspot.cdcsutils.lognote

import java.awt.Dimension
import java.awt.event.*
import javax.swing.*


class LogToolDialog(mainUI: MainUI, log: Pair<String, Int>) : JDialog(mainUI, "Log", false) {

    private val mMainUI = mainUI
    private val mLogTool = ToolsPane.LogTool()

    init {
        isUndecorated = true
        mLogTool.addFocusListenerToEditor(FocusHandler())
        mLogTool.addFocusListenerToPopup(PopupFocusHandler())
        mLogTool.setLog(log)
        var width = mainUI.width - 100
        if (width < 960) {
            width = 960
        }

        mLogTool.preferredSize = Dimension(width, mLogTool.preferredSize.height)

        contentPane.add(mLogTool)
        pack()
    }

    internal inner class FocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            if (!mLogTool.isVisiblePopupMenu()) {
                dispose()
            }
        }
    }

    internal inner class PopupFocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            if (!this@LogToolDialog.hasFocus()) {
                dispose()
            }
        }
    }
}
