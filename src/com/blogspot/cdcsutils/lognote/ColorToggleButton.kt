package com.blogspot.cdcsutils.lognote

import java.awt.*
import javax.swing.ImageIcon
import javax.swing.JToggleButton


class ColorToggleButton(title:String) : JToggleButton(title){
    var mSelectedFg: Color? = null
    var mSelectedBg: Color? = null

    init {
        icon = ImageIcon(this.javaClass.getResource("/images/toggle_off.png"))
        selectedIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on.png"))
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
    }

    fun setPreferredHeight(height: Int) {
        preferredSize = Dimension(preferredSize.width, height)
    }
}
