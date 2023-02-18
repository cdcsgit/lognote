package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel

class ButtonPanel : JPanel() {
    internal inner class ButtonFlowLayout(align: Int, hgap: Int, vgap: Int) : FlowLayout(align, hgap, vgap) {
        override fun minimumLayoutSize(target: Container?): Dimension {
            return Dimension(0, 0)
        }
    }
    var mLastComponent: Component? = null
    init {
        layout = ButtonFlowLayout(FlowLayout.LEFT, 2, 0)
        addComponentListener(
                object : ComponentAdapter() {
                    var mPrevPoint: Point? = null
                    override fun componentResized(e: ComponentEvent) {
                        super.componentResized(e)
                        for (item in components) {
                            if (mLastComponent == null) {
                                mLastComponent = item
                            } else {
                                if ((item.location.y + item.height) > (mLastComponent!!.location.y + mLastComponent!!.height)) {
                                    mLastComponent = item
                                }
                            }
                        }
                        if (mPrevPoint == null || mPrevPoint!!.y != mLastComponent!!.location.y) {
                            println("lastComonent moved to ${mLastComponent!!.location}")
                            preferredSize = Dimension(preferredSize.width, mLastComponent!!.location.y + mLastComponent!!.height)
                            updateUI()
                        }
                        mPrevPoint = mLastComponent!!.location
                    }
                })
    }
}