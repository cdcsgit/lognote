package com.blogspot.cdcsutils.lognote

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

    init {
        layout = ButtonFlowLayout(FlowLayout.LEFT, 2, 0)
        addComponentListener(
                object : ComponentAdapter() {
                    lateinit var mLastComponent: Component
                    var mPrevPoint: Point = Point(0, 0)
                    override fun componentResized(e: ComponentEvent) {
                        super.componentResized(e)

                        if (components.isNotEmpty()) {
                            mLastComponent = components[0]
                            for (item in components) {
                                if ((item.location.y + item.height) > (mLastComponent.location.y + mLastComponent.height)) {
                                    mLastComponent = item
                                }
                            }

                            if (mPrevPoint.y != mLastComponent.location.y) {
                                Utils.printlnLog("lastComponent moved to ${mLastComponent.location}")
                                preferredSize = Dimension(preferredSize.width, mLastComponent.location.y + mLastComponent.height)
                                updateUI()
                            }
                            mPrevPoint = mLastComponent.location
                        }
                    }
                })
    }
}