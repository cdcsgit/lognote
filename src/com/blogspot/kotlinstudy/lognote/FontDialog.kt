package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI


class FontDialog (parent: MainUI) : JDialog(parent, Strings.FONT + " & " + Strings.COLOR + " " + Strings.SETTING, true), ActionListener {
    private var mNameScrollPane: JScrollPane
    private var mNameList: JList<String>
    private var mSizeLabel: JLabel
    private var mSizeSpinner: JSpinner
    private var mExampleLabel: JLabel
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton
    private var mParent = parent
    private val mPrevFont = mParent.mFont
    private val mColorManager = ColorManager.getInstance()
    private val mTitleLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mColorArray.size)
    private val mColorLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mColorArray.size)
    private val mMouseHandler = MouseHandler()
    private val mPrevColorArray = arrayOfNulls<String>(mColorManager.mColorArray.size)
    private var mIsNeedRestore = true

    init {
        mNameList = JList(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
        mNameList.selectionMode = SINGLE_SELECTION
        mNameScrollPane = JScrollPane(mNameList)
        mNameScrollPane.preferredSize = Dimension(550, 200)
        mNameList.setSelectedValue(parent.mFont.family, true)
        mNameScrollPane.verticalScrollBar.ui = BasicScrollBarUI()
        mNameScrollPane.horizontalScrollBar.ui = BasicScrollBarUI()
        mNameList.addListSelectionListener(ListSelectionHandler())
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        mSizeLabel = JLabel(Strings.SIZE)
        mSizeSpinner = JSpinner(SpinnerNumberModel())
        mSizeSpinner.model.value = parent.mFont.size
        mSizeSpinner.preferredSize = Dimension(40, 30)
        mSizeSpinner.addChangeListener(ChangeHandler())
        mExampleLabel = JLabel("123 가나다 ABC abc", SwingConstants.CENTER)
        mExampleLabel.font = parent.mFont
        mExampleLabel.border = BorderFactory.createLineBorder(Color(0x50, 0x50, 0x50))
        mExampleLabel.preferredSize = Dimension(250, 30)

        val colorLabelPanel = JPanel()
        colorLabelPanel.layout = BoxLayout(colorLabelPanel, BoxLayout.Y_AXIS)

        val titleLabelPanel = JPanel()
        titleLabelPanel.layout = BoxLayout(titleLabelPanel, BoxLayout.Y_AXIS)

        for (idx in mColorLabelArray.indices) {
            mPrevColorArray[idx] = mColorManager.mColorArray[idx].mStrColor
            mColorLabelArray[idx] = ColorLabel(idx)
            mColorLabelArray[idx]!!.text = " ${mColorManager.mColorArray[idx].mName} ${mColorManager.mColorArray[idx].mStrColor} "
            mColorLabelArray[idx]!!.toolTipText = mColorLabelArray[idx]!!.text
            mColorLabelArray[idx]!!.isOpaque = true
            if (mColorManager.mColorArray[idx].mName.contains("BG")) {
                mColorLabelArray[idx]!!.horizontalAlignment = JLabel.RIGHT
            }
            else {
                mColorLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            }

            mColorLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mColorLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mColorLabelArray[idx]!!.minimumSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.preferredSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.maximumSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.addMouseListener(mMouseHandler)

            mTitleLabelArray[idx] = ColorLabel(idx)
            mTitleLabelArray[idx]!!.text = " ${mColorManager.mColorArray[idx].mName}"
            mTitleLabelArray[idx]!!.toolTipText = mColorLabelArray[idx]!!.text
            mTitleLabelArray[idx]!!.isOpaque = true
            mTitleLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            if (mColorManager.mColorArray[idx].mName.contains("BG")) {
                mTitleLabelArray[idx]!!.foreground = Color.WHITE
                mTitleLabelArray[idx]!!.background = Color.DARK_GRAY
            }
            else {
                mTitleLabelArray[idx]!!.foreground = Color.DARK_GRAY
                mTitleLabelArray[idx]!!.background = Color.WHITE
            }

            mTitleLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mTitleLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mTitleLabelArray[idx]!!.minimumSize = Dimension(220, 20)
            mTitleLabelArray[idx]!!.preferredSize = Dimension(220, 20)
            mTitleLabelArray[idx]!!.maximumSize = Dimension(220, 20)
            mTitleLabelArray[idx]!!.addMouseListener(mMouseHandler)
        }

        for (order in mColorLabelArray.indices) {
            for (idx in mColorLabelArray.indices) {
                if (order == mColorManager.mColorArray[idx].mOrder) {
                    colorLabelPanel.add(mColorLabelArray[idx])
                    colorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))
                    titleLabelPanel.add(mTitleLabelArray[idx])
                    titleLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))
                    break
                }
            }
        }

        val colorPanel = JPanel()
        colorPanel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        colorPanel.add(titleLabelPanel)
        colorPanel.add(colorLabelPanel)

        updateLabelColor()

        val namePanel = JPanel()
        namePanel.layout = FlowLayout(FlowLayout.LEFT, 3, 0)
        namePanel.add(mNameScrollPane)

        val sizePanel = JPanel()
        sizePanel.add(mSizeLabel)
        sizePanel.add(mSizeSpinner)
        sizePanel.add(mExampleLabel)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(300, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(sizePanel, BorderLayout.NORTH)
        bottomPanel.add(colorPanel, BorderLayout.CENTER)
        bottomPanel.add(confirmPanel, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout())
        panel.add(namePanel, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        contentPane.add(panel)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                println("exit Font Color dialog, restore $mIsNeedRestore")

                if (mIsNeedRestore) {
                    for (idx in mColorLabelArray.indices) {
                        mColorManager.mColorArray[idx].mStrColor = mPrevColorArray[idx]!!
                    }
                    mColorManager.applyColor()
                    mParent.mFont = mPrevFont
                }
                else {
                    mParent.mConfigManager.saveFontColors(mParent.mFont.family, mParent.mFont.size)
                }
            }
        })

        pack()
        Utils.installKeyStrokeEscClosing(this)
    }

    fun updateLabelColor() {
        var commonBg:Color? = null
        var commonFg:Color? = null
        var lineNumBg:Color? = null
        var lineNumFg:Color? = null

        for (idx in mColorLabelArray.indices) {
            when (mColorManager.mColorArray[idx].mName) {
                "FullLog BG"->commonBg = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                "Log Level None"->commonFg = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                "LineNum BG"->lineNumBg = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                "LineNum FG"->lineNumFg = Color.decode(mColorManager.mColorArray[idx].mStrColor)
            }
        }

        if (commonFg == null) {
            commonFg = Color.BLACK
        }

        if (commonBg == null) {
            commonBg = Color.WHITE
        }

        if (lineNumFg == null) {
            lineNumFg = Color.BLACK
        }

        if (lineNumBg == null) {
            lineNumBg = Color.WHITE
        }

        for (idx in mColorLabelArray.indices) {
            if (mColorManager.mColorArray[idx].mName.contains("BG")) {
                mColorLabelArray[idx]!!.background = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                if (mColorManager.mColorArray[idx].mName == "LineNum BG") {
                    mColorLabelArray[idx]!!.foreground = lineNumFg
                }
                else {
                    mColorLabelArray[idx]!!.foreground = commonFg
                }
            }
            else {
                mColorLabelArray[idx]!!.foreground = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                if (mColorManager.mColorArray[idx].mName == "LineNum FG") {
                    mColorLabelArray[idx]!!.background = lineNumBg
                }
                else {
                    mColorLabelArray[idx]!!.background = commonBg
                }
            }
        }
    }

    class ColorLabel(idx: Int) :JLabel() {
        val mIdx: Int = idx
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            mIsNeedRestore = false
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        } else if (e?.source == mCancelBtn) {
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
    }

    private fun setFont() {
        val selection = mNameList.selectedValue
        val size = mSizeSpinner.model.value as Int
        mExampleLabel.font = Font(selection.toString(), Font.PLAIN, size)

        mParent.mFont = Font(selection.toString(), Font.PLAIN, size)
    }

    internal inner class ChangeHandler: ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            setFont()
        }
    }

    internal inner class ListSelectionHandler : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            if (p0?.source == mNameList) {
                setFont()
            }
        }
    }

    internal inner class MouseHandler: MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            val colorChooser = JColorChooser()
            val panels = colorChooser.chooserPanels
            var rgbPanel:JPanel? = null
            for (panel in panels) {
                if (panel.displayName.contains("RGB", true)) {
                    rgbPanel = panel
                }
            }

            if (rgbPanel != null) {
                val tmpColorLabel = e!!.source as ColorLabel
                val idx = tmpColorLabel.mIdx
                val colorLabel = mColorLabelArray[idx]!!
                if (colorLabel.text.contains("BG")) {
                    colorChooser.color = colorLabel.background
                } else {
                    colorChooser.color = colorLabel.foreground
                }

                val ret = JOptionPane.showConfirmDialog(this@FontDialog, rgbPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                if (ret == JOptionPane.OK_OPTION) {
                    val hex = "#" + Integer.toHexString(colorChooser.color.rgb).substring(2).uppercase()
                    colorLabel.text = " ${mColorManager.mColorArray[idx].mName} $hex "
                    mColorManager.mColorArray[idx].mStrColor = hex
                    if (colorLabel.text.contains("BG")) {
                        colorLabel.background = colorChooser.color
                    } else {
                        colorLabel.foreground = colorChooser.color
                    }
                    mColorManager.applyColor()
                    updateLabelColor()
                    setFont() // refresh log table
                }
            }

            super.mouseClicked(e)
        }
    }
}

