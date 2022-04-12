package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.plaf.basic.BasicComboBoxUI


class ColorComboBox<E> : JComboBox<E>() {
    init {
        if (ConfigManager.LaF == MainUI.CROSS_PLATFORM_LAF) {
            ui = BasicComboBoxUI()
            ui.installUI(this)
            val textField = editor.editorComponent as JTextField
            textField.border = BorderFactory.createLineBorder(Color.black)
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

