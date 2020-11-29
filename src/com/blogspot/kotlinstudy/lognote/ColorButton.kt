package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import javax.swing.JButton

class ColorButton(title:String) : JButton(title){
    init {
        background = Color(0xE5, 0xE5, 0xE5)
    }
}