package com.blogspot.cdcsutils.lognote

import java.awt.Color
import java.awt.Graphics
import javax.swing.JToggleButton
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class FilterToggleButton(title:String) : JToggleButton(title){
    var mSelectedFg: Color? = null
    var mSelectedBg: Color? = null

    override fun updateUI() {
        icon = Icons.ToggleOffIcon(ConfigManager.LaFAccentColor)
        selectedIcon = Icons.ToggleOnIcon(ConfigManager.LaFAccentColor)
        super.updateUI()
    }
}

class PackageToggleButton(title:String) : JToggleButton(title), ChangeListener {
    var mIsValid = true
        set(value) {
            field = value
            updateIcon()
            updateToolTipText()
        }

    private fun updateToolTipText() {
        toolTipText = if (isSelected) {
            if (mIsValid) {
                TooltipStrings.PACKAGE_APPLY_START
            } else {
                TooltipStrings.PACKAGE_NOT_INSTALLED
            }
        } else {
            TooltipStrings.PACKAGE_APPLY_START
        }
    }

    init {
        addChangeListener(this)
    }

    private fun updateIcon() {
        selectedIcon = if (mIsValid) {
            Icons.PackageToggleOnIcon(ConfigManager.LaFAccentColor)
        } else {
            Icons.PackageToggleInvalidIcon("#FF0000")
        }

        icon = Icons.PackageToggleOffIcon(ConfigManager.LaFAccentColor)
    }

    override fun updateUI() {
        updateIcon()
        super.updateUI()
    }

    override fun stateChanged(p0: ChangeEvent?) {
        updateToolTipText()
    }
}
