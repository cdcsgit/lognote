package com.blogspot.kotlinstudy.lognote

import com.blogspot.kotlinstudy.lognote.FormatManager.FormatItem.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class FormatManager private constructor(){
    interface FormatEventListener {
        fun formatChanged(format: FormatItem)
    }

    private val mEventListeners = ArrayList<FormatEventListener>()

    fun addFormatEventListener(listener: FormatEventListener) {
        mEventListeners.add(listener)
    }

    fun removeFormatEventListener(listener: FormatEventListener) {
        mEventListeners.remove(listener)
    }

    fun notifyFormatChanged() {
        for (listener in mEventListeners) {
            listener.formatChanged(mCurrFormat)
        }
    }

    data class FormatItem(val mName: String, val mSeparator: String, val mLevels: Map<String, Int>, val mLevelNth: Int, val mTokens: Array<Token>, val mPidTokIdx: Int) {
        data class Token(val mToken: String, val mNth: Int, val mIsSaveFilter: Boolean, var mUiWidth: Int)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FormatItem

            return mName == other.mName
        }

        override fun hashCode(): Int {
            return mName.hashCode()
        }
    }

    companion object {
        const val MAX_LEVEL = 7
        const val MAX_TOKEN_COUNT = 3
        const val DEFAULT_LOGCAT = "logcat"
        val TEXT_LEVEL = arrayOf("None", "Verbose", "Debug", "Info", "Warning", "Error", "Fatal")

        const val LEVEL_NONE = 0
        const val LEVEL_VERBOSE = 1
        const val LEVEL_DEBUG = 2
        const val LEVEL_INFO = 3
        const val LEVEL_WARNING = 4
        const val LEVEL_ERROR = 5
        const val LEVEL_FATAL = 6

        private val mInstance: FormatManager = FormatManager()
        fun getInstance(): FormatManager {
            return mInstance
        }
    }

    private val mFormats = mutableMapOf<String, FormatItem>()
    var mCurrFormat: FormatItem
    init {
        val separator = ":?\\s+"
        val levels = mapOf("V" to LEVEL_VERBOSE
            , "D" to LEVEL_DEBUG
            , "I" to LEVEL_INFO
            , "W" to LEVEL_WARNING
            , "E" to LEVEL_ERROR
            , "F" to LEVEL_FATAL
        )
        val tokens: Array<Token> = arrayOf(
            Token("PID", 2, false, 120),
            Token("TID", 3, false, 120),
            Token("Tag", 5, true, 250),
        )
        val levelIdx = 4
        val pidTokIdx = 0

        mFormats[DEFAULT_LOGCAT] = FormatItem(DEFAULT_LOGCAT, separator, levels, levelIdx, tokens, pidTokIdx)
        mCurrFormat = mFormats[DEFAULT_LOGCAT]!!
    }

    fun updateFormat(name: String, value: FormatItem) {
        mFormats[name] = value
    }

    fun removeFormat(name: String) {
        mFormats.remove(name)
    }

    fun clear() {
        mFormats.clear()
    }

    fun getNames(): List<String> {
        return mFormats.keys.toList()
    }

    fun getLevels(name: String): Map<String, Int> {
        return mFormats[name]?.mLevels ?: mapOf("" to -1)
    }

    fun showFormatListDialog(parent: JFrame) {
        val formatListDialog = FormatListDialog(parent)
        formatListDialog.isVisible = true
    }

    inner class FormatListDialog(parent: JFrame) : JDialog(parent, Strings.LOGFORMAT, true), ActionListener {
        private val mOkBtn: ColorButton
        private val mCancelBtn: ColorButton

        init {
            mOkBtn = ColorButton(Strings.OK)
            mOkBtn.addActionListener(this)
            mCancelBtn = ColorButton(Strings.CANCEL)
            mCancelBtn.addActionListener(this)

            val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
            confirmPanel.preferredSize = Dimension(400, 40)
            confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
            confirmPanel.add(mOkBtn)
            confirmPanel.add(mCancelBtn)

            val panel = JPanel(BorderLayout())
            panel.add(confirmPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mOkBtn) {
                dispose()
            } else if (e?.source == mCancelBtn) {
                dispose()
            }
        }
    }
}