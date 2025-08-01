package com.blogspot.cdcsutils.lognote

import java.awt.Component
import java.awt.Dimension
import java.awt.event.InputEvent
import java.awt.event.ItemEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxRenderer
import kotlin.math.max


class ColorComboBox<E>(autoResize:Boolean = false, minWidth:Int = 0) : JComboBox<E>() {
    init {
        val textField = editor.editorComponent as JTextField
        textField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK), "none")

        if (autoResize) {
            addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    val selectedItem = e.item as String
                    val fm = getFontMetrics(font)
                    val width = max(fm.stringWidth(selectedItem) + insets.left + insets.right + 40, minWidth)
                    preferredSize = Dimension(width, preferredSize.height)
                    revalidate()
                }
            }
        }
    }

    internal class ComboBoxRenderer : BasicComboBoxRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>, value: Any?,
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
            text = value?.toString() ?: ""
            return this
        }
    }
}

