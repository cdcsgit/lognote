package com.blogspot.cdcsutils.lognote

import java.awt.Color
import javax.swing.JButton

open class ColorButton(title:String) : JButton(title) {
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