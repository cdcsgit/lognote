package com.blogspot.cdcsutils.lognote

import java.awt.Color
import java.awt.Dimension
import java.awt.event.*
import javax.swing.*


class ToolSelectionDialog(mainUI: MainUI, log: Triple<String, Int, Int>, colors: Triple<Color, Color, Color>) : JDialog(mainUI, "Log", false) {
    private val mToolSelection = ToolsPane.ToolSelection(true)

    init {
        mToolSelection.addFocusListenerToEditor(FocusHandler())
        mToolSelection.addFocusListenerToPopup(PopupFocusHandler())
        mToolSelection.setLogColor(colors)
        mToolSelection.setSelectionLog(log)
        var width = mainUI.width - 100
        if (width < 960) {
            width = 960
        }

        mToolSelection.preferredSize = Dimension(width, mToolSelection.getLogHeight(width))
        contentPane.add(mToolSelection)
        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    internal inner class FocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            if (!mToolSelection.isVisiblePopupMenu()) {
                dispose()
            }
        }
    }

    internal inner class PopupFocusHandler: FocusAdapter() {
        override fun focusLost(p0: FocusEvent?) {
            super.focusLost(p0)
            if (!this@ToolSelectionDialog.hasFocus()) {
                dispose()
            }
        }
    }
}
