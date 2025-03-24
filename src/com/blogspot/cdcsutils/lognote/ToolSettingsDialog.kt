package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*


class ToolSettingsDialog(mainUI: MainUI) : JDialog(mainUI, Strings.TOOL, true), ActionListener {
    private val mMainUI = mainUI
    private val mConfigManager = ConfigManager.getInstance()

    private val mToolsPanel = JPanel()
    private val mScrollPane = JScrollPane()
    private val mLogPanel = LogPanel()

    private val mOkBtn = JButton(Strings.OK)
    private val mCancelBtn = JButton(Strings.CANCEL)

    init {
        addWindowListener(mLogPanel)
        mScrollPane.verticalScrollBar.unitIncrement = 10
        mToolsPanel.layout = BoxLayout(mToolsPanel, BoxLayout.Y_AXIS)
        mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        Utils.addHSeparator(mToolsPanel, " ${Strings.LOG} ")
        mToolsPanel.add(mLogPanel)
//        Utils.addHEmptySeparator(mToolsPanel, 20)
//        Utils.addHSeparator(mToolsPanel, " ${Strings.FILTER_STYLE} ")
//        mToolsPanel.add(mTestPanel)

        mOkBtn.addActionListener(this)
        mCancelBtn.addActionListener(this)
        val bottomPanel = JPanel()
        bottomPanel.add(mOkBtn)
        bottomPanel.add(mCancelBtn)

        val settingsPanelWrapper = JPanel(BorderLayout())
        settingsPanelWrapper.add(mToolsPanel, BorderLayout.NORTH)
        mScrollPane.setViewportView(settingsPanelWrapper)

        contentPane.layout = BorderLayout()
        contentPane.add(mScrollPane, BorderLayout.CENTER)
        contentPane.add(bottomPanel, BorderLayout.SOUTH)

//        preferredSize = Dimension(940, 900)
//        minimumSize = Dimension(940, 500)

        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            if (mLogPanel.actionBtn(true) == false) {
                return
            }
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        } else if (e?.source == mCancelBtn) {
            mLogPanel.actionBtn(false)
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
    }

    inner class LogPanel : JPanel(), WindowListener {
        private var mIsNeedRestore = true
        private var mRangeLabel: JLabel
        private var mPreviousLabel: JLabel
        private val mPreviousTF: JTextField
        private var mNextLabel: JLabel
        private val mNextTF: JTextField
        private val mLogTool = ToolsPane.getInstance().mLogTool

        private val MAX_LINES = 20

        init {
            layout = BorderLayout()

            mRangeLabel = JLabel(" ${Strings.TOOL_LOG_RANGE}")
            add(mRangeLabel, BorderLayout.NORTH)

            val rangePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            mPreviousLabel = JLabel(" ${Strings.TOOL_LOG_RANGE_PREVIOUS}")
            rangePanel.add(mPreviousLabel)
            mPreviousTF = JTextField(mLogTool.mPrevLines.toString())
            mPreviousTF.addKeyListener(Utils.NumberKeyListener)
            rangePanel.add(mPreviousTF)
            mNextLabel = JLabel("   ${Strings.TOOL_LOG_RANGE_NEXT}")
            rangePanel.add(mNextLabel)
            mNextTF = JTextField(mLogTool.mNextLines.toString())
            mNextTF.addKeyListener(Utils.NumberKeyListener)
            rangePanel.add(mNextTF)
            
            add(rangePanel, BorderLayout.CENTER)
        }

        fun actionBtn(isOK: Boolean): Boolean {
            if (isOK) {
                mLogTool.mPrevLines = mPreviousTF.text.toInt()
                mLogTool.mNextLines = mNextTF.text.toInt()

                if (mLogTool.mPrevLines > MAX_LINES || mLogTool.mNextLines > MAX_LINES) {
                    JOptionPane.showMessageDialog(mMainUI, Strings.TOOL_LOG_MSG_MAX_LINES.format(MAX_LINES), Strings.WARNING, JOptionPane.WARNING_MESSAGE)
                    return false
                }
                mIsNeedRestore = false
            }

            return true
        }

        override fun windowOpened(p0: WindowEvent?) {
            // nothing
        }

        override fun windowClosing(p0: WindowEvent?) {
            Utils.printlnLog("exit Log tool, restore $mIsNeedRestore")

            if (mIsNeedRestore) {
                // nothing
            } else {
                mConfigManager.saveItem(ConfigManager.ITEM_TOOL_LOG_RANGE_PREVIOUS, mLogTool.mPrevLines.toString())
                mConfigManager.saveItem(ConfigManager.ITEM_TOOL_LOG_RANGE_NEXT, mLogTool.mNextLines.toString())
            }
        }

        override fun windowClosed(p0: WindowEvent?) {
            // nothing
        }

        override fun windowIconified(p0: WindowEvent?) {
            // nothing
        }

        override fun windowDeiconified(p0: WindowEvent?) {
            // nothing
        }

        override fun windowActivated(p0: WindowEvent?) {
            // nothing
        }

        override fun windowDeactivated(p0: WindowEvent?) {
            // nothing
        }
    }
}