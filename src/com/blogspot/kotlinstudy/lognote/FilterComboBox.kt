package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.plaf.basic.BasicComboBoxUI

class FilterComboBox<E> : JComboBox<E>() {
    var mTf: JTextField? = null
    var mEnabledTfTooltip = true

    init {
        ui = BasicComboBoxUI()
        ui.installUI(this)

        mTf = editor.editorComponent as JTextField
        mTf!!.border = BorderFactory.createLineBorder(Color.black)
        mTf!!.toolTipText = toolTipText
        mTf!!.addKeyListener(KeyHandler())
    }

    fun setEnabledFilter(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled && editor.item.toString().isEmpty()) {
            isVisible = false
        }
        else {
            isVisible = true
        }
    }

    fun isExistItem(item:String) : Boolean {
        var isExist = false
        for (idx in 0 until itemCount) {
            if (getItemAt(idx).toString() == item) {
                isExist = true
                break
            }
        }
        return isExist
    }

    private fun parsePattern(pattern: String) : Array<String> {
        val patterns: Array<String> = Array<String>(2) { "" }

        val patternSplit = pattern.split("|")
        var prevPatternIdx = -1

        for (item in patternSplit) {
            if (prevPatternIdx != -1) {
                patterns[prevPatternIdx] += "|"
                patterns[prevPatternIdx] += item

                if (!item.substring(item.length - 1).equals("\\")) {
                    prevPatternIdx = -1
                }
                continue
            }

            if (item.isNotEmpty()) {
                if (item[0] != '-') {
                    if (patterns[0].isNotEmpty()) {
                        patterns[0] += "|"
                        patterns[0] += item
                    } else {
                        patterns[0] = item
                    }
                    if (item.substring(item.length - 1).equals("\\")) {
                        prevPatternIdx = 0
                    }
                } else {
                    if (patterns[1].isNotEmpty()) {
                        patterns[1] += "|"
                        patterns[1] += item.substring(1)
                    } else {
                        patterns[1] = item.substring(1)
                    }
                    if (item.substring(item.length - 1).equals("\\")) {
                        prevPatternIdx = 1
                    }
                }
            }
        }

        return patterns
    }

    fun updateTooltip() {
        if (!mEnabledTfTooltip) {
            return;
        }
        val patterns = parsePattern(mTf!!.text)
        var includeStr = patterns[0]
        var excludeStr = patterns[1]

        if (includeStr.isNotEmpty()) {
            includeStr = includeStr.replace("|", "<font color=#303030><b>|</b></font>")
        }

        if (excludeStr.isNotEmpty()) {
            excludeStr = excludeStr.replace("|", "<font color=#303030><b>|</b></font>")
        }

        var tooltip = "<html><b>$toolTipText</b><br>"
        tooltip += "<font color=#000000>INCLUDE : </font><font size=5 color=#0000FF>$includeStr</font><br>"
        tooltip += "<font color=#000000>EXCLUDE : </font><font size=5 color=#FF0000>$excludeStr</font><br>"
        tooltip += "</html>"
        mTf!!.toolTipText = tooltip
    }

    internal inner class KeyHandler : KeyAdapter() {
        override fun keyReleased(e: KeyEvent) {
            updateTooltip()
            ToolTipManager.sharedInstance().mouseMoved(MouseEvent(mTf, 0, 0, 0, 0, preferredSize.height / 3, 0, false))
            super.keyReleased(e)
        }
    }

    internal class ComboBoxRenderer : BasicComboBoxRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>, value: Any,
            index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
                if (-1 < index) {
                    list.toolTipText = list.selectedValue.toString()
                }
            } else {
                background = list.background
                foreground = list.foreground
            }
            font = list.font
            text = value.toString()
            return this
        }
    }
}