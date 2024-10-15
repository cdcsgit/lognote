package com.blogspot.cdcsutils.lognote

import java.awt.Color
import java.awt.Graphics
import javax.swing.JToggleButton


class FilterToggleButton(title:String) : JToggleButton(title){
    var mSelectedFg: Color? = null
    var mSelectedBg: Color? = null

    override fun updateUI() {
        icon = Icons.ToggleOffIcon(ConfigManager.LaFAccentColor)
        selectedIcon = Icons.ToggleOnIcon(ConfigManager.LaFAccentColor)
        super.updateUI()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
    }
}
