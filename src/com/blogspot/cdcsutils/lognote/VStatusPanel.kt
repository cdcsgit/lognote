package com.blogspot.cdcsutils.lognote

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel


class VStatusPanel(logTable: LogTable) : JPanel() {
    private val mLogTable = logTable
    private val mBookmarkManager = BookmarkManager.getInstance()

    companion object {
        const val VIEW_RECT_WIDTH = 20
        const val VIEW_RECT_HEIGHT = 5
    }
    init {
        preferredSize = Dimension(VIEW_RECT_WIDTH, VIEW_RECT_HEIGHT)
        background = if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
            Color(0x46494B)
        }
        else {
            Color.WHITE
        }
        border = BorderFactory.createLineBorder(Color.DARK_GRAY)
        addMouseListener(MouseHandler())
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
            g?.color = Color(0xFFFFFF)
        }
        else {
            g?.color = Color(0x000000)
        }
        for (row in 0 until mLogTable.rowCount) {
            val num = mLogTable.getValueAt(row, 0).toString().trim().toInt()
            if (mBookmarkManager.mBookmarks.contains(num)) {
                g?.fillRect(0, row * height / mLogTable.rowCount, width, 1)
            }
        }

        val visibleY:Long = (mLogTable.visibleRect.y).toLong()
        val totalHeight:Long = (mLogTable.rowHeight * mLogTable.rowCount).toLong()
        if (mLogTable.rowCount != 0 && height != 0) {
            if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                g?.color = Color(0xC0, 0xC0, 0xC0, 0x50)
            }
            else {
                g?.color = Color(0xA0, 0xA0, 0xA0, 0x50)
            }
            var viewHeight = mLogTable.visibleRect.height * height / totalHeight
            if (viewHeight < VIEW_RECT_HEIGHT) {
                viewHeight = VIEW_RECT_HEIGHT.toLong()
            }

            var viewY = visibleY * height / totalHeight
            if (viewY + viewHeight > height) {
                viewY = height - viewHeight
            }
            g?.fillRect(0, viewY.toInt(), width, viewHeight.toInt())
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            val row = p0!!.point.y * mLogTable.rowCount / height
            try {
                // mLogTable.setRowSelectionInterval(row, row)
                mLogTable.scrollRectToVisible(Rectangle(mLogTable.getCellRect(row, 0, true)))
            } catch (e: IllegalArgumentException) {
                println("e : $e")
            }
            super.mouseClicked(p0)
        }
    }
}
