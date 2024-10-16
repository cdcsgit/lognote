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
}

class PackageToggleButton(title:String) : JToggleButton(title){
    override fun updateUI() {
        icon = Icons.PackageToggleOffIcon(ConfigManager.LaFAccentColor)
        selectedIcon = Icons.PackageToggleOnIcon(ConfigManager.LaFAccentColor)
        super.updateUI()
    }
}
