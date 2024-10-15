package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.icons.FlatAbstractIcon
import java.awt.*

class Icons {
    class AccentColorIcon(private val mColor: String) : FlatAbstractIcon(16, 16, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.decode(mColor)
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5)
        }
    }

    class FiltersCmdsIcon(private val mColor: String) : FlatAbstractIcon(15, 15, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.GRAY
            g.drawRect(1, 1, width - 2, height - 2)

            g.color = Color.decode(mColor)
            g.fillRect(4, 4, width - 7, height - 7)
        }
    }

    class FiltersCmdsItemIcon(private val mColor: String) : FlatAbstractIcon(11, 11, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.decode(mColor)
            g.fillRoundRect(3, 1, width - 2, height - 2, 5, 5)
        }
    }

    class TopIcon(private val mColor: String) : FlatAbstractIcon(15, 15, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.decode(mColor)
            g.fillRect(0, 0, width, 2)

            g.color = Color.GRAY
            g.fillPolygon(intArrayOf(8, 13, 10, 10, 6, 6, 3), intArrayOf(2, 7, 7, 13, 13, 7, 7), 7)
        }
    }

    class BottomIcon(private val mColor: String) : FlatAbstractIcon(15, 15, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.decode(mColor)
            g.fillRect(0, height - 2, width, 2)

            g.color = Color.GRAY
            g.fillPolygon(intArrayOf(7, 12, 9, 9, 5, 5, 2), intArrayOf(13, 8, 8, 1, 1, 8, 8), 7)
        }
    }
    class ToggleOnIcon(private val mColor: String) : FlatAbstractIcon(15, 15, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.decode(mColor)
            g.fillRoundRect(1, 1, width - 3, height - 3, 10, 10)
            g.color = Color.BLACK
            g.drawRoundRect(1, 1, width - 3, height - 3, 10, 10)
        }
    }

    class ToggleOffIcon(private val mColor: String) : FlatAbstractIcon(15, 15, null) {
        override fun paintIcon(c: Component, g: Graphics2D) {
            g.color = Color.LIGHT_GRAY
            g.drawRoundRect(1, 1, width - 3, height - 3, 10, 10)
        }
    }
}