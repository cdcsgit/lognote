package com.blogspot.cdcsutils.lognote

import javax.swing.BorderFactory
import javax.swing.JButton

open class ColorButton(title:String) : JButton(title) {
    init {
        putClientProperty("JButton.buttonType", "square")
    }
}

class TableBarButton(title:String) : ColorButton(title) {
    var mValue = ""

    companion object {
        private const val MAX_TITLE = 15
    }

    init {
        border = BorderFactory.createEmptyBorder()

//        putClientProperty("JButton.buttonType", "roundRect")
        if (title.length > MAX_TITLE) {
            text = title.substring(0, MAX_TITLE) + ".."
        }
    }
}