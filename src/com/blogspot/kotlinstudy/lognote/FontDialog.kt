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
    private val mFullTableColor = mColorManager.mFullTableColor
    private val mFilterTableColor = mColorManager.mFilterTableColor
    
    private val mTitleLabelArray = arrayOfNulls<ColorLabel>(mFullTableColor.mColorArray.size)
    private val mFullColorLabelArray = arrayOfNulls<ColorLabel>(mFullTableColor.mColorArray.size)
    private val mFullPrevColorArray = arrayOfNulls<String>(mFullTableColor.mColorArray.size)
    private val mFilterColorLabelArray = arrayOfNulls<ColorLabel>(mFilterTableColor.mColorArray.size)
    private val mFilterPrevColorArray = arrayOfNulls<String>(mFilterTableColor.mColorArray.size)
    private val mMouseHandler = MouseHandler()
    private var mIsNeedRestore = true

    init {
        mNameList = JList(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
        mNameList.selectionMode = SINGLE_SELECTION
        mNameScrollPane = JScrollPane(mNameList)
        mNameScrollPane.preferredSize = Dimension(400, 150)
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
        mSizeSpinner.preferredSize = Dimension(70, 30)
        mSizeSpinner.addChangeListener(ChangeHandler())
        mExampleLabel = JLabel("123 가나다 ABC abc", SwingConstants.CENTER)
        mExampleLabel.font = parent.mFont
        mExampleLabel.border = BorderFactory.createLineBorder(Color(0x50, 0x50, 0x50))
        mExampleLabel.preferredSize = Dimension(250, 30)

        val fullColorLabelPanel = JPanel()
        fullColorLabelPanel.layout = BoxLayout(fullColorLabelPanel, BoxLayout.Y_AXIS)

        val filterColorLabelPanel = JPanel()
        filterColorLabelPanel.layout = BoxLayout(filterColorLabelPanel, BoxLayout.Y_AXIS)

        val titleLabelPanel = JPanel()
        titleLabelPanel.layout = BoxLayout(titleLabelPanel, BoxLayout.Y_AXIS)

        for (idx in mTitleLabelArray.indices) {
            mFullPrevColorArray[idx] = mFullTableColor.mColorArray[idx].mStrColor
            mFullColorLabelArray[idx] = ColorLabel(ColorManager.TableColorType.FULL_LOG_TABLE, idx)
            mFullColorLabelArray[idx]!!.text = " ${mFullTableColor.mColorArray[idx].mName} ${mFullTableColor.mColorArray[idx].mStrColor} "
            mFullColorLabelArray[idx]!!.toolTipText = mFullColorLabelArray[idx]!!.text
            mFullColorLabelArray[idx]!!.isOpaque = true
            if (mFullTableColor.mColorArray[idx].mName.contains("BG")) {
                mFullColorLabelArray[idx]!!.horizontalAlignment = JLabel.RIGHT
            }
            else {
                mFullColorLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            }

            mFullColorLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mFullColorLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mFullColorLabelArray[idx]!!.minimumSize = Dimension(330, 20)
            mFullColorLabelArray[idx]!!.preferredSize = Dimension(330, 20)
            mFullColorLabelArray[idx]!!.maximumSize = Dimension(330, 20)
            mFullColorLabelArray[idx]!!.addMouseListener(mMouseHandler)

            mFilterPrevColorArray[idx] = mFilterTableColor.mColorArray[idx].mStrColor
            mFilterColorLabelArray[idx] = ColorLabel(ColorManager.TableColorType.FILTER_LOG_TABLE, idx)
            mFilterColorLabelArray[idx]!!.text = " ${mFilterTableColor.mColorArray[idx].mName} ${mFilterTableColor.mColorArray[idx].mStrColor} "
            mFilterColorLabelArray[idx]!!.toolTipText = mFilterColorLabelArray[idx]!!.text
            mFilterColorLabelArray[idx]!!.isOpaque = true
            if (mFilterTableColor.mColorArray[idx].mName.contains("BG")) {
                mFilterColorLabelArray[idx]!!.horizontalAlignment = JLabel.RIGHT
            }
            else {
                mFilterColorLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            }

            mFilterColorLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mFilterColorLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mFilterColorLabelArray[idx]!!.minimumSize = Dimension(330, 20)
            mFilterColorLabelArray[idx]!!.preferredSize = Dimension(330, 20)
            mFilterColorLabelArray[idx]!!.maximumSize = Dimension(330, 20)
            mFilterColorLabelArray[idx]!!.addMouseListener(mMouseHandler)
            
            mTitleLabelArray[idx] = ColorLabel(ColorManager.TableColorType.FULL_LOG_TABLE, idx)
            mTitleLabelArray[idx]!!.text = " ${mFullTableColor.mColorArray[idx].mName}"
            mTitleLabelArray[idx]!!.toolTipText = mFullColorLabelArray[idx]!!.text
            mTitleLabelArray[idx]!!.isOpaque = true
            mTitleLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT
            if (mTitleLabelArray[idx]!!.text.contains("BG")) {
                mTitleLabelArray[idx]!!.foreground = Color.WHITE
                mTitleLabelArray[idx]!!.background = Color.DARK_GRAY
            }
            else {
                mTitleLabelArray[idx]!!.foreground = Color.DARK_GRAY
                mTitleLabelArray[idx]!!.background = Color.WHITE
            }

            mTitleLabelArray[idx]!!.verticalAlignment = JLabel.CENTER
            mTitleLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mTitleLabelArray[idx]!!.minimumSize = Dimension(250, 20)
            mTitleLabelArray[idx]!!.preferredSize = Dimension(250, 20)
            mTitleLabelArray[idx]!!.maximumSize = Dimension(250, 20)
        }

        var label = JLabel("  ")
        label.horizontalAlignment = JLabel.CENTER
        label.maximumSize = Dimension(250, 20)
        titleLabelPanel.add(label)
        titleLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

        label = JLabel(Strings.FULL_LOG_TABLE)
        label.horizontalAlignment = JLabel.CENTER
        label.maximumSize = Dimension(330, 20)
        fullColorLabelPanel.add(label)
        fullColorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

        label = JLabel(Strings.FILTER_LOG_TABLE)
        label.horizontalAlignment = JLabel.CENTER
        label.maximumSize = Dimension(330, 20)
        filterColorLabelPanel.add(label)
        filterColorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))


        for (order in mTitleLabelArray.indices) {
            for (idx in mTitleLabelArray.indices) {
                if (order == mFullTableColor.mColorArray[idx].mOrder) {
                    titleLabelPanel.add(mTitleLabelArray[idx])
                    titleLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

                    fullColorLabelPanel.add(mFullColorLabelArray[idx])
                    fullColorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

                    filterColorLabelPanel.add(mFilterColorLabelArray[idx])
                    filterColorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

                    break
                }
            }
        }

        val colorPanel = JPanel()
        colorPanel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        colorPanel.add(titleLabelPanel)
        colorPanel.add(fullColorLabelPanel)
        colorPanel.add(filterColorLabelPanel)

        updateLabelColor(ColorManager.TableColorType.FULL_LOG_TABLE)
        updateLabelColor(ColorManager.TableColorType.FILTER_LOG_TABLE)


        val sizePanel = JPanel()
        sizePanel.add(mSizeLabel)
        sizePanel.add(mSizeSpinner)
        sizePanel.add(mExampleLabel)

        val schemePanel = JPanel()
        val schemeLabel = JLabel("${Strings.BUILT_IN_SCHEMES} : ")
        val radioLight = JRadioButton(Strings.LIGHT)
        val radioDark = JRadioButton(Strings.DARK)
        val buttonGroup = ButtonGroup()
        val schemeBtn = JButton(Strings.APPLY)

        schemeBtn.addActionListener(ActionListener { if (radioLight.isSelected) {
            applyColorScheme(ColorManager.getInstance().mColorSchemeLight)
        } else if (radioDark.isSelected) {
            applyColorScheme(ColorManager.getInstance().mColorSchemeDark)
        }
        })
        
        buttonGroup.add(radioLight)
        buttonGroup.add(radioDark)
        schemePanel.add(schemeLabel)
        schemePanel.add(radioLight)
        schemePanel.add(radioDark)
        schemePanel.add(schemeBtn)

        val sizeSchemePanel = JPanel()
        sizeSchemePanel.layout = BoxLayout(sizeSchemePanel, BoxLayout.Y_AXIS)
        sizeSchemePanel.add(sizePanel);
        sizeSchemePanel.add(schemePanel)

        val namePanel = JPanel()
        namePanel.layout = GridLayout(1, 2, 3, 3)
        namePanel.add(mNameScrollPane)
        namePanel.add(sizeSchemePanel)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(300, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val bottomPanel = JPanel(BorderLayout())
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
                    for (idx in mFullColorLabelArray.indices) {
                        mFullTableColor.mColorArray[idx].mStrColor = mFullPrevColorArray[idx]!!
                        mFilterTableColor.mColorArray[idx].mStrColor = mFilterPrevColorArray[idx]!!
                    }
                    mFullTableColor.applyColor()
                    mFilterTableColor.applyColor()
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

    private fun applyColorScheme(type: ColorManager.TableColorType, scheme: Array<String>) {
        val colorLabelArray = if (type == ColorManager.TableColorType.FULL_LOG_TABLE) {
            mFullColorLabelArray
        } else {
            mFilterColorLabelArray
        }

        val tableColor = if (type == ColorManager.TableColorType.FULL_LOG_TABLE) {
            mFullTableColor
        } else {
            mFilterTableColor
        }
        
        for (idx in colorLabelArray.indices) {
            tableColor.mColorArray[idx].mStrColor = scheme[idx]
            colorLabelArray[idx]!!.text = " ${mFullTableColor.mColorArray[idx].mName} ${scheme[idx]} "

            if (colorLabelArray[idx]!!.text.contains("BG")) {
                colorLabelArray[idx]!!.background = Color.decode(scheme[idx])
            } else {
                colorLabelArray[idx]!!.foreground = Color.decode(scheme[idx])
            }
            tableColor.applyColor()
            updateLabelColor(type)
        }
    }

    private fun applyColorScheme(scheme: Array<String>) {
        applyColorScheme(ColorManager.TableColorType.FULL_LOG_TABLE, scheme)
        applyColorScheme(ColorManager.TableColorType.FILTER_LOG_TABLE, scheme)
        setFont()
    }

    fun updateLabelColor(type: ColorManager.TableColorType) {
        var commonBg:Color? = null
        var commonFg:Color? = null
        var lineNumBg:Color? = null
        var lineNumFg:Color? = null

        val colorLabelArray = if (type == ColorManager.TableColorType.FULL_LOG_TABLE) {
            mFullColorLabelArray
        } else {
            mFilterColorLabelArray
        }

        val tableColor = if (type == ColorManager.TableColorType.FULL_LOG_TABLE) {
            mFullTableColor
        } else {
            mFilterTableColor
        }

        for (idx in colorLabelArray.indices) {
            when (tableColor.mColorArray[idx].mName) {
                "FullLog BG"->commonBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                "Log Level None"->commonFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                "LineNum BG"->lineNumBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                "LineNum FG"->lineNumFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
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

        for (idx in colorLabelArray.indices) {
            if (tableColor.mColorArray[idx].mName.contains("BG")) {
                colorLabelArray[idx]!!.background = Color.decode(tableColor.mColorArray[idx].mStrColor)
                if (tableColor.mColorArray[idx].mName == "LineNum BG") {
                    colorLabelArray[idx]!!.foreground = lineNumFg
                }
                else {
                    colorLabelArray[idx]!!.foreground = commonFg
                }
            }
            else {
                colorLabelArray[idx]!!.foreground = Color.decode(tableColor.mColorArray[idx].mStrColor)
                if (tableColor.mColorArray[idx].mName == "LineNum FG") {
                    colorLabelArray[idx]!!.background = lineNumBg
                }
                else {
                    colorLabelArray[idx]!!.background = commonBg
                }
            }
        }
    }

    class ColorLabel(type: ColorManager.TableColorType, idx: Int) :JLabel() {
        val mType = type;
        val mIdx = idx
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
                val colorLabel = e!!.source as ColorLabel
                if (colorLabel.text.contains("BG")) {
                    colorChooser.color = colorLabel.background
                } else {
                    colorChooser.color = colorLabel.foreground
                }

                val ret = JOptionPane.showConfirmDialog(this@FontDialog, rgbPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                if (ret == JOptionPane.OK_OPTION) {
                    val hex = "#" + Integer.toHexString(colorChooser.color.rgb).substring(2).uppercase()
                    colorLabel.text = " ${mFullTableColor.mColorArray[colorLabel.mIdx].mName} $hex "
                    val tableColor = if (colorLabel.mType == ColorManager.TableColorType.FULL_LOG_TABLE) {
                        mFullTableColor
                    }
                    else {
                        mFilterTableColor
                    }
                    tableColor.mColorArray[colorLabel.mIdx].mStrColor = hex
                    if (colorLabel.text.contains("BG")) {
                        colorLabel.background = colorChooser.color
                    } else {
                        colorLabel.foreground = colorChooser.color
                    }
                    tableColor.applyColor()
                    updateLabelColor(colorLabel.mType)
                    setFont() // refresh log table
                }
            }

            super.mouseClicked(e)
        }
    }
}

