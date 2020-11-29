package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import javax.swing.JToggleButton

class ColorToggleButton(title:String) : JToggleButton(title){
    init {
        background = Color(0xE5, 0xE5, 0xE5)
    }
}