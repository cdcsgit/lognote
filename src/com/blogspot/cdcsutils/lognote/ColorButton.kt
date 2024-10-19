package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.ui.FlatButtonBorder
import java.awt.Color
import javax.swing.JButton
class ColorButtonBorder(val color: Color) : FlatButtonBorder() {
    init {
        borderColor = color
    }
}

open class ColorButton(title:String) : JButton(title) {
    init {
    }
}

class TableBarButton(title:String) : ColorButton(title) {
    var mValue = ""

    companion object {
        private const val MAX_TITLE = 15
    }

    init {
        if (title.length > MAX_TITLE) {
            text = title.substring(0, MAX_TITLE) + ".."
        }
    }
}