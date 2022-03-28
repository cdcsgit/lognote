package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.*
import javax.swing.*

class FilterStyleDialog (parent: MainUI) : JDialog(parent, "${Strings.FILTER_STYLE} ${Strings.SETTING}", true), ActionListener {
    enum class ComboIdx(val value: Int) {
        LOG(0),
        TAG(1),
        PID(2),
        TID(3),
        BOLD(4),
        SIZE(5);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }
    private var mExampleLabel: JLabel
    private var mExampleCombo: FilterComboBox
    private var mParent = parent

    private val mComboLabelArray = arrayOfNulls<ColorLabel>(ComboIdx.SIZE.value)
    private val mStyleComboArray = arrayOfNulls<ColorComboBox<String>>(ComboIdx.SIZE.value)

    private var mConfirmLabel: JLabel
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private val mColorManager = ColorManager.getInstance()
    private val mTitleLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mFilterStyle.size)
    private val mColorLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mFilterStyle.size)
    private val mMouseHandler = MouseHandler()
    private val mPrevColorArray = arrayOfNulls<String>(mColorManager.mFilterStyle.size)
    private var mIsNeedRestore = true

    init {
        mConfirmLabel = JLabel("To apply \"Combo Style\" need to restart")
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        mExampleLabel = JLabel("Ex : ")
        mExampleCombo = FilterComboBox(FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT)
        mExampleCombo.isEditable = true
        mExampleCombo.preferredSize = Dimension(250, 30)
        mExampleCombo.addItem("ABC|DEF|-GHI|JKL")

        val styleLabelPanel = JPanel()
        styleLabelPanel.layout = BoxLayout(styleLabelPanel, BoxLayout.Y_AXIS)

        val styleComboPanel = JPanel()
        styleComboPanel.layout = BoxLayout(styleComboPanel, BoxLayout.Y_AXIS)

        for (idx in mComboLabelArray.indices) {
            mComboLabelArray[idx] = ColorLabel(idx)
            mComboLabelArray[idx]!!.isOpaque = true
            mComboLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            mComboLabelArray[idx]!!.foreground = Color.DARK_GRAY
            mComboLabelArray[idx]!!.background = Color.WHITE

            mComboLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mComboLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mComboLabelArray[idx]!!.minimumSize = Dimension(200, 20)
            mComboLabelArray[idx]!!.preferredSize = Dimension(200, 20)
            mComboLabelArray[idx]!!.maximumSize = Dimension(200, 20)

            mStyleComboArray[idx] = ColorComboBox()
            mStyleComboArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mStyleComboArray[idx]!!.minimumSize = Dimension(200, 20)
            mStyleComboArray[idx]!!.preferredSize = Dimension(200, 20)
            mStyleComboArray[idx]!!.maximumSize = Dimension(200, 20)
            mStyleComboArray[idx]!!.addItem("SINGLE LINE")
            mStyleComboArray[idx]!!.addItem("SINGLE LINE / HIGHLIGHT")
            mStyleComboArray[idx]!!.addItem("MULTI LINE")
            mStyleComboArray[idx]!!.addItem("MULTI LINE / HIGHLIGHT")
        }

        mComboLabelArray[ComboIdx.LOG.value]!!.text = "Combo Style : Log"
        mStyleComboArray[ComboIdx.LOG.value]!!.selectedIndex = mParent.mShowLogComboStyle.value
        mComboLabelArray[ComboIdx.TAG.value]!!.text = "Combo Style : Tag"
        mStyleComboArray[ComboIdx.TAG.value]!!.selectedIndex = mParent.mShowTagComboStyle.value
        mComboLabelArray[ComboIdx.PID.value]!!.text = "Combo Style : PID"
        mStyleComboArray[ComboIdx.PID.value]!!.selectedIndex = mParent.mShowPidComboStyle.value
        mComboLabelArray[ComboIdx.TID.value]!!.text = "Combo Style : TID"
        mStyleComboArray[ComboIdx.TID.value]!!.selectedIndex = mParent.mShowTidComboStyle.value
        mComboLabelArray[ComboIdx.BOLD.value]!!.text = "Combo Style : BOLD"
        mStyleComboArray[ComboIdx.BOLD.value]!!.selectedIndex = mParent.mBoldLogComboStyle.value
//            mComboLabelArray[idx]!!.toolTipText = mColorLabelArray[idx]!!.text

        for (idx in mComboLabelArray.indices) {
            styleLabelPanel.add(mComboLabelArray[idx])
            styleLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))
            styleComboPanel.add(mStyleComboArray[idx])
            styleComboPanel.add(Box.createRigidArea(Dimension(5, 3)))
        }

        val stylePanel = JPanel()
        stylePanel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        stylePanel.add(styleLabelPanel)
        stylePanel.add(styleComboPanel)

        val colorLabelPanel = JPanel()
        colorLabelPanel.layout = BoxLayout(colorLabelPanel, BoxLayout.Y_AXIS)

        val titleLabelPanel = JPanel()
        titleLabelPanel.layout = BoxLayout(titleLabelPanel, BoxLayout.Y_AXIS)
        
        for (idx in mColorLabelArray.indices) {
            mPrevColorArray[idx] = mColorManager.mFilterStyle[idx].mStrColor
            mColorLabelArray[idx] = ColorLabel(idx)
            mColorLabelArray[idx]!!.text = " ${mColorManager.mFilterStyle[idx].mName} ${mColorManager.mFilterStyle[idx].mStrColor} "
            mColorLabelArray[idx]!!.toolTipText = mColorLabelArray[idx]!!.text
            mColorLabelArray[idx]!!.isOpaque = true
            mColorLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT

            mColorLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mColorLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mColorLabelArray[idx]!!.minimumSize = Dimension(200, 20)
            mColorLabelArray[idx]!!.preferredSize = Dimension(200, 20)
            mColorLabelArray[idx]!!.maximumSize = Dimension(200, 20)
            mColorLabelArray[idx]!!.addMouseListener(mMouseHandler)

            mTitleLabelArray[idx] = ColorLabel(idx)
            mTitleLabelArray[idx]!!.text = " ${mColorManager.mFilterStyle[idx].mName}"
            mTitleLabelArray[idx]!!.toolTipText = mColorLabelArray[idx]!!.text
            mTitleLabelArray[idx]!!.isOpaque = true
            mTitleLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            mTitleLabelArray[idx]!!.foreground = Color.DARK_GRAY
            mTitleLabelArray[idx]!!.background = Color.WHITE

            mTitleLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mTitleLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mTitleLabelArray[idx]!!.minimumSize = Dimension(200, 20)
            mTitleLabelArray[idx]!!.preferredSize = Dimension(200, 20)
            mTitleLabelArray[idx]!!.maximumSize = Dimension(200, 20)
            mTitleLabelArray[idx]!!.addMouseListener(mMouseHandler)
        }

        for (order in mColorLabelArray.indices) {
            for (idx in mColorLabelArray.indices) {
                if (order == mColorManager.mFilterStyle[idx].mOrder) {
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

        val sizePanel = JPanel()
        sizePanel.add(mExampleLabel)
        sizePanel.add(mExampleCombo)

        val topPanel = JPanel(BorderLayout())
        topPanel.add(stylePanel, BorderLayout.CENTER)
        topPanel.add(sizePanel, BorderLayout.SOUTH)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(300, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mConfirmLabel)
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(topPanel, BorderLayout.NORTH)
        bottomPanel.add(colorPanel, BorderLayout.CENTER)
        bottomPanel.add(confirmPanel, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout())
        panel.add(bottomPanel, BorderLayout.CENTER)

        contentPane.add(panel)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                println("exit Filter Style dialog, restore $mIsNeedRestore")

                if (mIsNeedRestore) {
                    for (idx in mColorLabelArray.indices) {
                        mColorManager.mFilterStyle[idx].mStrColor = mPrevColorArray[idx]!!
                    }
                    mColorManager.applyFilterStyle()
                }
                else {
                    val keys = arrayOf(ConfigManager.ITEM_SHOW_LOG_STYLE,
                            ConfigManager.ITEM_SHOW_TAG_STYLE,
                            ConfigManager.ITEM_SHOW_PID_STYLE,
                            ConfigManager.ITEM_SHOW_TID_STYLE,
                            ConfigManager.ITEM_BOLD_LOG_STYLE)
                    val values = arrayOf(mStyleComboArray[ComboIdx.LOG.value]!!.selectedIndex.toString(),
                            mStyleComboArray[ComboIdx.TAG.value]!!.selectedIndex.toString(),
                            mStyleComboArray[ComboIdx.PID.value]!!.selectedIndex.toString(),
                            mStyleComboArray[ComboIdx.TID.value]!!.selectedIndex.toString(),
                            mStyleComboArray[ComboIdx.BOLD.value]!!.selectedIndex.toString())

                    mParent.mConfigManager.saveFilterStyle(keys, values)
                }
            }
        })

        pack()
        Utils.installKeyStrokeEscClosing(this)
    }

    fun updateLabelColor() {
        val commonFg = Color.BLACK

        for (idx in mColorLabelArray.indices) {
            mColorLabelArray[idx]!!.foreground = commonFg
            mColorLabelArray[idx]!!.background = Color.decode(mColorManager.mFilterStyle[idx].mStrColor)
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
                colorChooser.color = colorLabel.background

                val ret = JOptionPane.showConfirmDialog(this@FilterStyleDialog, rgbPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                if (ret == JOptionPane.OK_OPTION) {
                    val hex = "#" + Integer.toHexString(colorChooser.color.rgb).substring(2).uppercase()
                    colorLabel.text = " ${mColorManager.mFilterStyle[idx].mName} $hex "
                    mColorManager.mFilterStyle[idx].mStrColor = hex
                    colorLabel.background = colorChooser.color
                    mColorManager.applyFilterStyle()
                    updateLabelColor()
                    val selectedItem = mExampleCombo.selectedItem
                    mExampleCombo.selectedItem = ""
                    mExampleCombo.selectedItem = selectedItem
                }
            }

            super.mouseClicked(e)
        }
    }
}

