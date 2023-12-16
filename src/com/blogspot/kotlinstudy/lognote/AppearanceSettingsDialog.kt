package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI


class AppearanceSettingsDialog(mainUI: MainUI) : JDialog(mainUI, Strings.APPEARANCE, true), ActionListener {
    private val mMainUI = mainUI
    private val mConfigManager = ConfigManager.getInstance()

    private val mSettingsPanel = JPanel()
    private val mScrollPane = JScrollPane()
    private val mLnFPanel = LnFPanel()
    private val mFilterComboPanel = FilterComboPanel()
    private val mFontColorPanel = FontColorPanel()

    private val mOkBtn = ColorButton(Strings.OK)
    private val mCancelBtn = ColorButton(Strings.CANCEL)

    init {
        addWindowListener(mFilterComboPanel)
        addWindowListener(mFontColorPanel)
        mScrollPane.verticalScrollBar.unitIncrement = 10
        mSettingsPanel.layout = BoxLayout(mSettingsPanel, BoxLayout.Y_AXIS)
        mScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        addHSeparator(mSettingsPanel, " ${Strings.LOOK_AND_FEEL}, ${Strings.OPTIONS} ")
        mSettingsPanel.add(mLnFPanel)
        addHEmptySeparator(mSettingsPanel, 20)
        addHSeparator(mSettingsPanel, " ${Strings.FILTER_STYLE} ")
        mSettingsPanel.add(mFilterComboPanel)
        addHEmptySeparator(mSettingsPanel, 20)
        addHSeparator(mSettingsPanel, " ${Strings.LOG} ${Strings.FONT} & ${Strings.COLOR} ")
        mSettingsPanel.add(mFontColorPanel)

        mOkBtn.addActionListener(this)
        mCancelBtn.addActionListener(this)
        val bottomPanel = JPanel()
        bottomPanel.add(mOkBtn)
        bottomPanel.add(mCancelBtn)

        val settingsPanelWrapper = JPanel(BorderLayout())
        settingsPanelWrapper.add(mSettingsPanel, BorderLayout.NORTH)
        mScrollPane.setViewportView(settingsPanelWrapper)

        contentPane.layout = BorderLayout()
        contentPane.add(mScrollPane, BorderLayout.CENTER)
        contentPane.add(bottomPanel, BorderLayout.SOUTH)

        preferredSize = Dimension(940, 900)
        minimumSize = Dimension(940, 800)

        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    private fun addHSeparator(target:JPanel, title: String) {
        val titleHtml = title.replace(" ", "&nbsp;")
        val separator = JSeparator(SwingConstants.HORIZONTAL)
        val label = JLabel("<html><b>$titleHtml</b></html>")
        val panel = JPanel(BorderLayout())
        val separPanel = JPanel(BorderLayout())
        separPanel.add(Box.createVerticalStrut(label.font.size / 2), BorderLayout.NORTH)
        separPanel.add(separator, BorderLayout.CENTER)
        panel.add(label, BorderLayout.WEST)
        panel.add(separPanel, BorderLayout.CENTER)
        target.add(panel)
    }

    private fun addHEmptySeparator(target:JPanel, height: Int) {
        val panel = JPanel()
        panel.preferredSize = Dimension(1, height)
        target.add(panel)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            mLnFPanel.actionBtn(true)
            mFilterComboPanel.actionBtn(true)
            mFontColorPanel.actionBtn(true)
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        } else if (e?.source == mCancelBtn) {
            mLnFPanel.actionBtn(false)
            mFilterComboPanel.actionBtn(false)
            mFontColorPanel.actionBtn(false)
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
    }

    inner class LnFPanel : JPanel() {
        private var mFontSlider: JSlider
        private var mDividerSlider: JSlider
        private var mLaFGroup: ButtonGroup
        private var mExampleLabel: JLabel
        private var mBaseFontSize = 0

        private val MIN_FONT_POS = 50
        private val MAX_FONT_POS = 200
        private val EXAMPLE_TEXT = "ABC def GHI jkl 0123456789"

        private val MIN_DIVIDER_POS = 1
        private val MAX_DIVIDER_POS = 20
        private val mPrevDividerSize = mMainUI.mLogSplitPane.dividerSize

        init {
            layout = FlowLayout(FlowLayout.LEFT)

            val lafPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            mLaFGroup = ButtonGroup()

            var lafItem = JRadioButton(MainUI.CROSS_PLATFORM_LAF)
            mLaFGroup.add(lafItem)
            lafPanel.add(lafItem)

            lafItem = JRadioButton(MainUI.SYSTEM_LAF)
            mLaFGroup.add(lafItem)
            lafPanel.add(lafItem)

            lafItem = JRadioButton(MainUI.FLAT_LIGHT_LAF)
            mLaFGroup.add(lafItem)
            lafPanel.add(lafItem)
            lafPanel.add(ImagePanel("/images/appearance_flatlight.png"))

            lafItem = JRadioButton(MainUI.FLAT_DARK_LAF)
            mLaFGroup.add(lafItem)
            lafPanel.add(lafItem)
            lafPanel.add(ImagePanel("/images/appearance_flatdark.png"))

            lafPanel.add(JLabel("   (Restart)"))

            for (item in mLaFGroup.elements) {
                if (ConfigManager.LaF == item.text) {
                    item.isSelected = true
                    break
                }
            }

            val examplePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            mExampleLabel = JLabel(EXAMPLE_TEXT)
            examplePanel.preferredSize = Dimension(mExampleLabel.preferredSize.width, 50)
            examplePanel.add(mExampleLabel)
            examplePanel.border = BorderFactory.createLineBorder(Color.GRAY)

            val sliderPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val sliderLabel = JLabel("UI Size(%, Restart)")
            sliderPanel.add(sliderLabel)
            mBaseFontSize = mExampleLabel.font.size * 100 / mMainUI.mUIFontPercent
            mFontSlider = JSlider(MIN_FONT_POS, MAX_FONT_POS, mMainUI.mUIFontPercent)
            mFontSlider.majorTickSpacing = 50
            mFontSlider.minorTickSpacing = 10
            mFontSlider.paintTicks = true
            mFontSlider.paintLabels = true
            mFontSlider.addChangeListener {
                mExampleLabel.text = "${mFontSlider.value} % : $EXAMPLE_TEXT"
                mExampleLabel.font =
                    Font(mExampleLabel.font.name, mExampleLabel.font.style, mBaseFontSize * mFontSlider.value / 100)
            }
            sliderPanel.add(mFontSlider)

            val sizePanel = JPanel()
            sizePanel.layout = BoxLayout(sizePanel, BoxLayout.Y_AXIS)
            sizePanel.add(examplePanel)
            sizePanel.add(sliderPanel)

            val lafSizePanel = JPanel(BorderLayout())
            lafSizePanel.add(lafPanel, BorderLayout.NORTH)
            lafSizePanel.add(sizePanel, BorderLayout.CENTER)

            val dividerPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val dividerLabel = JLabel("Divider Size(1 ~ 20) [${mMainUI.mLogSplitPane.dividerSize}]")
            dividerPanel.add(dividerLabel)
            mDividerSlider = JSlider(0, MAX_DIVIDER_POS, mMainUI.mLogSplitPane.dividerSize)
            mDividerSlider.majorTickSpacing = 5
            mDividerSlider.minorTickSpacing = 1
            mDividerSlider.paintTicks = true
            mDividerSlider.paintLabels = true
            mDividerSlider.addChangeListener {
                if (mDividerSlider.value == 0) {
                    mDividerSlider.value = MIN_DIVIDER_POS
                }
                mMainUI.mLogSplitPane.dividerSize = mDividerSlider.value
                dividerLabel.text = "Divider Size(1 ~ 20) [${mMainUI.mLogSplitPane.dividerSize}]"
            }
            dividerPanel.add(mDividerSlider)

            val optionsPanel = JPanel(BorderLayout())
            optionsPanel.add(dividerPanel, BorderLayout.CENTER)

            val dialogPanel = JPanel(BorderLayout())
            dialogPanel.add(lafSizePanel, BorderLayout.NORTH)
            dialogPanel.add(optionsPanel, BorderLayout.CENTER)
            val panel = JPanel(BorderLayout())
            panel.add(dialogPanel, BorderLayout.CENTER)

            add(panel)
        }

        inner class ImagePanel(imageResource: String) : JPanel() {
            private val mImgIcon = ImageIcon(this.javaClass.getResource(imageResource))
            init {
                preferredSize = Dimension(150, 106)
                background = Color.RED
            }

            override fun paint(g: Graphics?) {
                super.paint(g)
                g?.drawImage(mImgIcon.image, 0, 0, mImgIcon.iconWidth, mImgIcon.iconHeight, null)
            }
        }

        fun actionBtn(isOK: Boolean) {
            if (isOK) {
                for (item in mLaFGroup.elements) {
                    if (item.isSelected) {
                        ConfigManager.getInstance().saveItem(ConfigManager.ITEM_LOOK_AND_FEEL, item.text)
                        ConfigManager.getInstance().saveItem(ConfigManager.ITEM_UI_FONT_SIZE, mFontSlider.value.toString())
                        ConfigManager.getInstance().saveItem(ConfigManager.ITEM_APPEARANCE_DIVIDER_SIZE, mMainUI.mLogSplitPane.dividerSize.toString())
                        break
                    }
                }
            } else {
                mMainUI.mLogSplitPane.dividerSize = mPrevDividerSize
            }
        }
    }

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

    inner class FilterComboPanel : JPanel(), WindowListener {

        private var mExampleLabel: JLabel
        private var mExampleCombo: FilterComboBox

        private val mComboLabelArray = arrayOfNulls<ColorLabel>(ComboIdx.SIZE.value)
        private val mStyleComboArray = arrayOfNulls<ColorComboBox<String>>(ComboIdx.SIZE.value)

        private var mConfirmLabel: JLabel

        private val mColorManager = ColorManager.getInstance()
        private val mTitleLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mFilterStyle.size)
        private val mColorLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mFilterStyle.size)
        private val mMouseHandler = MouseHandler()
        private val mPrevColorArray = arrayOfNulls<String>(mColorManager.mFilterStyle.size)
        private var mIsNeedRestore = true

        init {
            layout = FlowLayout(FlowLayout.LEFT)
            mConfirmLabel = JLabel("To apply \"Style\" need to restart")

            mExampleLabel = JLabel("Ex : ")
            mExampleCombo = FilterComboBox(FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT, true)
            mExampleCombo.isEditable = true
            mExampleCombo.preferredSize = Dimension(250, 30)
            mExampleCombo.addItem("ABC|DEF|-GHI|JKL")

            val styleLabelPanel = JPanel()
            styleLabelPanel.layout = BoxLayout(styleLabelPanel, BoxLayout.Y_AXIS)

            val styleComboPanel = JPanel()
            styleComboPanel.layout = BoxLayout(styleComboPanel, BoxLayout.Y_AXIS)

            val rightWidth = 240
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
                mStyleComboArray[idx]!!.minimumSize = Dimension(rightWidth, 20)
                mStyleComboArray[idx]!!.preferredSize = Dimension(rightWidth, 20)
                mStyleComboArray[idx]!!.maximumSize = Dimension(rightWidth, 20)
                mStyleComboArray[idx]!!.addItem("SINGLE LINE")
                mStyleComboArray[idx]!!.addItem("SINGLE LINE / HIGHLIGHT")
                mStyleComboArray[idx]!!.addItem("MULTI LINE")
                mStyleComboArray[idx]!!.addItem("MULTI LINE / HIGHLIGHT")
            }

            mComboLabelArray[ComboIdx.LOG.value]!!.text = "Combo Style : Log"
            mStyleComboArray[ComboIdx.LOG.value]!!.selectedIndex = mMainUI.mShowLogComboStyle.value
            mComboLabelArray[ComboIdx.TAG.value]!!.text = "Combo Style : Tag"
            mStyleComboArray[ComboIdx.TAG.value]!!.selectedIndex = mMainUI.mShowTagComboStyle.value
            mComboLabelArray[ComboIdx.PID.value]!!.text = "Combo Style : PID"
            mStyleComboArray[ComboIdx.PID.value]!!.selectedIndex = mMainUI.mShowPidComboStyle.value
            mComboLabelArray[ComboIdx.TID.value]!!.text = "Combo Style : TID"
            mStyleComboArray[ComboIdx.TID.value]!!.selectedIndex = mMainUI.mShowTidComboStyle.value
            mComboLabelArray[ComboIdx.BOLD.value]!!.text = "Combo Style : BOLD"
            mStyleComboArray[ComboIdx.BOLD.value]!!.selectedIndex = mMainUI.mBoldLogComboStyle.value
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
                mColorLabelArray[idx]!!.minimumSize = Dimension(rightWidth, 20)
                mColorLabelArray[idx]!!.preferredSize = Dimension(rightWidth, 20)
                mColorLabelArray[idx]!!.maximumSize = Dimension(rightWidth, 20)
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

            val examplePanel = JPanel()
            examplePanel.add(mExampleLabel)
            examplePanel.add(mExampleCombo)

            val schemePanel = JPanel()
            val schemeLabel = JLabel("${Strings.BUILT_IN_SCHEMES} : ")
            val radioLight = JRadioButton(Strings.LIGHT)
            val radioDark = JRadioButton(Strings.DARK)
            val buttonGroup = ButtonGroup()
            val schemeBtn = JButton(Strings.APPLY)

            schemeBtn.addActionListener {
                if (radioLight.isSelected) {
                    applyColorScheme(ColorManager.getInstance().mFilterColorSchemeLight)
                } else if (radioDark.isSelected) {
                    applyColorScheme(ColorManager.getInstance().mFilterColorSchemeDark)
                }
            }

            buttonGroup.add(radioLight)
            buttonGroup.add(radioDark)
            schemePanel.add(schemeLabel)
            schemePanel.add(radioLight)
            schemePanel.add(radioDark)
            schemePanel.add(schemeBtn)

            val colorSettingPanel = JPanel(BorderLayout())
            colorSettingPanel.add(examplePanel, BorderLayout.NORTH)
            colorSettingPanel.add(colorPanel, BorderLayout.CENTER)
            colorSettingPanel.add(schemePanel, BorderLayout.SOUTH)

            val panel = JPanel(BorderLayout())
            panel.add(stylePanel, BorderLayout.WEST)
            panel.add(JLabel("   "), BorderLayout.CENTER)
            panel.add(colorSettingPanel, BorderLayout.EAST)

            add(panel)
        }

        override fun windowClosing(e: WindowEvent?) {
            println("exit Filter Style, restore $mIsNeedRestore")

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

                mConfigManager.saveFilterStyle(keys, values)
            }
        }

        override fun windowOpened(e: WindowEvent?) {
            // nothing
        }

        override fun windowClosed(e: WindowEvent?) {
            // nothing
        }

        override fun windowIconified(e: WindowEvent?) {
            // nothing
        }

        override fun windowDeiconified(e: WindowEvent?) {
            // nothing
        }

        override fun windowActivated(e: WindowEvent?) {
            // nothing
        }

        override fun windowDeactivated(e: WindowEvent?) {
            // nothing
        }

        private fun applyColorScheme(scheme: Array<String>) {
            for(idx in scheme.indices) {
                mColorLabelArray[idx]!!.text = " ${mColorManager.mFilterStyle[idx].mName} ${scheme[idx]} "
                mColorManager.mFilterStyle[idx].mStrColor = scheme[idx]
                mColorLabelArray[idx]!!.background = Color.decode(scheme[idx])
            }

            mColorManager.applyFilterStyle()
            updateLabelColor()
            val selectedItem = mExampleCombo.selectedItem
            mExampleCombo.selectedItem = ""
            mExampleCombo.selectedItem = selectedItem
        }

        fun updateLabelColor() {
            val commonFg = Color.BLACK

            for (idx in mColorLabelArray.indices) {
                mColorLabelArray[idx]!!.foreground = commonFg
                mColorLabelArray[idx]!!.background = Color.decode(mColorManager.mFilterStyle[idx].mStrColor)
            }
        }

        inner class ColorLabel(idx: Int) :JLabel() {
            val mIdx: Int = idx
        }

        fun actionBtn(isOK: Boolean) {
            if (isOK) {
                mIsNeedRestore = false
            }
        }

        internal inner class MouseHandler: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                val colorChooser = JColorChooser()
                val panels = colorChooser.chooserPanels
                var rgbPanel: JPanel? = null
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

                    val ret = JOptionPane.showConfirmDialog(this@AppearanceSettingsDialog, rgbPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
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

    inner class FontColorPanel : JPanel(), WindowListener {
        private var mNameScrollPane: JScrollPane
        private var mNameList: JList<String>
        private var mSizeLabel: JLabel
        private var mSizeSpinner: JSpinner
        private var mExampleLabel: JLabel
        private val mPrevFont = mMainUI.mFont

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
            layout = FlowLayout(FlowLayout.LEFT)
            mNameList = JList(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
            mNameList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            mNameScrollPane = JScrollPane(mNameList)
            mNameScrollPane.preferredSize = Dimension(400, 100)
            mNameList.setSelectedValue(mMainUI.mFont.family, true)
            mNameScrollPane.verticalScrollBar.setUI(BasicScrollBarUI())
            mNameScrollPane.horizontalScrollBar.setUI(BasicScrollBarUI())
            mNameList.addListSelectionListener(ListSelectionHandler())

            mSizeLabel = JLabel(Strings.SIZE)
            mSizeSpinner = JSpinner(SpinnerNumberModel())
            mSizeSpinner.model.value = mMainUI.mFont.size
            mSizeSpinner.preferredSize = Dimension(70, 30)
            mSizeSpinner.addChangeListener(ChangeHandler())
            mExampleLabel = JLabel("123 가나다 ABC abc", SwingConstants.CENTER)
            mExampleLabel.font = mMainUI.mFont
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

            label = JLabel("<html><font color=\"#000000\">--- <b><font color=\"#FF0000\">${Strings.FULL}</font></b> ${Strings.LOG} ${Strings.COLOR} ---</font></html>")
            label.horizontalAlignment = JLabel.CENTER
            label.isOpaque = true
            label.preferredSize = Dimension(330, 20)
            label.background = Color.WHITE
            fullColorLabelPanel.add(label)
            fullColorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))

            label = JLabel("<html><font color=\"#000000\">--- <b><font color=\"#0000FF\">${Strings.FILTER}</font></b> ${Strings.LOG} ${Strings.COLOR} ---</font></html>")
            label.horizontalAlignment = JLabel.CENTER
            label.isOpaque = true
            label.preferredSize = Dimension(330, 20)
            label.background = Color.WHITE
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
            val fullCheckbox = JCheckBox("${Strings.FULL} ${Strings.LOG}", true)
            val filterCheckbox = JCheckBox("${Strings.FILTER} ${Strings.LOG}", true)
            val radioLight = JRadioButton(Strings.LIGHT)
            val radioDark = JRadioButton(Strings.DARK)
            val buttonGroup = ButtonGroup()
            val schemeBtn = JButton(Strings.APPLY)

            schemeBtn.addActionListener(ActionListener {
                val scheme: Array<String> = if (radioLight.isSelected) {
                    ColorManager.getInstance().mColorSchemeLight
                } else if (radioDark.isSelected) {
                    ColorManager.getInstance().mColorSchemeDark
                } else {
                    println("Scheme is not selected")
                    return@ActionListener
                }

                if (fullCheckbox.isSelected && filterCheckbox.isSelected) {
                    applyColorScheme(scheme)
                }
                else if (fullCheckbox.isSelected) {
                    applyColorScheme(ColorManager.TableColorType.FULL_LOG_TABLE, scheme, true)
                }
                else if (filterCheckbox.isSelected) {
                    applyColorScheme(ColorManager.TableColorType.FILTER_LOG_TABLE, scheme, true)
                }
                else {
                    println("Target log(full/filter) is not selected")
                }
            })

            buttonGroup.add(radioLight)
            buttonGroup.add(radioDark)

            val schemePanelSub = JPanel(BorderLayout())
            val schemePanelSubNorth = JPanel()
            val schemePanelSubSouth = JPanel()

            schemePanelSubNorth.add(fullCheckbox)
            schemePanelSubNorth.add(filterCheckbox)
            schemePanelSubSouth.add(radioLight)
            schemePanelSubSouth.add(radioDark)
            schemePanelSubSouth.add(schemeBtn)

            schemePanelSub.add(schemePanelSubNorth, BorderLayout.NORTH)
            schemePanelSub.add(schemePanelSubSouth, BorderLayout.SOUTH)

            schemePanel.add(schemeLabel)
            schemePanel.add(schemePanelSub)


            val sizeSchemePanel = JPanel()
            sizeSchemePanel.layout = BoxLayout(sizeSchemePanel, BoxLayout.Y_AXIS)
            sizeSchemePanel.add(sizePanel)
            sizeSchemePanel.add(schemePanel)

            val namePanel = JPanel()
            namePanel.layout = GridLayout(1, 2, 3, 3)
            namePanel.add(mNameScrollPane)
            namePanel.add(sizeSchemePanel)

            val bottomPanel = JPanel(BorderLayout())
            bottomPanel.add(JLabel("   "), BorderLayout.NORTH)
            bottomPanel.add(colorPanel, BorderLayout.CENTER)
            val panel = JPanel(BorderLayout())
            panel.add(namePanel, BorderLayout.CENTER)
            panel.add(bottomPanel, BorderLayout.SOUTH)

            add(panel)
        }

        override fun windowClosing(e: WindowEvent?) {
            println("exit Font Color, restore $mIsNeedRestore")

            if (mIsNeedRestore) {
                for (idx in mFullColorLabelArray.indices) {
                    mFullTableColor.mColorArray[idx].mStrColor = mFullPrevColorArray[idx]!!
                    mFilterTableColor.mColorArray[idx].mStrColor = mFilterPrevColorArray[idx]!!
                }
                mFullTableColor.applyColor()
                mFilterTableColor.applyColor()
                mMainUI.mFont = mPrevFont
            }
            else {
                mConfigManager.saveFontColors(mMainUI.mFont.family, mMainUI.mFont.size)
            }
        }

        override fun windowOpened(e: WindowEvent?) {
            // nothing
        }

        override fun windowClosed(e: WindowEvent?) {
            // nothing
        }

        override fun windowIconified(e: WindowEvent?) {
            // nothing
        }

        override fun windowDeiconified(e: WindowEvent?) {
            // nothing
        }

        override fun windowActivated(e: WindowEvent?) {
            // nothing
        }

        override fun windowDeactivated(e: WindowEvent?) {
            // nothing
        }

        private fun applyColorScheme(type: ColorManager.TableColorType, scheme: Array<String>, isUpdateUI: Boolean) {
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

            val logPanel = if (type == ColorManager.TableColorType.FULL_LOG_TABLE) {
                mMainUI.mFullLogPanel
            } else {
                mMainUI.mFilteredLogPanel
            }

            for (idx in colorLabelArray.indices) {
                tableColor.mColorArray[idx].mStrColor = scheme[idx]
                colorLabelArray[idx]!!.text = " ${mFullTableColor.mColorArray[idx].mName} ${scheme[idx]} "

                if (colorLabelArray[idx]!!.text.contains("BG")) {
                    colorLabelArray[idx]!!.background = Color.decode(scheme[idx])
                } else {
                    colorLabelArray[idx]!!.foreground = Color.decode(scheme[idx])
                }
            }
            tableColor.applyColor()
            updateLabelColor(type)

            if (isUpdateUI) {
                logPanel.repaint()
            }
        }

        private fun applyColorScheme(scheme: Array<String>) {
            applyColorScheme(ColorManager.TableColorType.FULL_LOG_TABLE, scheme, false)
            applyColorScheme(ColorManager.TableColorType.FILTER_LOG_TABLE, scheme, false)
            mMainUI.mFullLogPanel.repaint()
            mMainUI.mFilteredLogPanel.repaint()
        }

        fun updateLabelColor(type: ColorManager.TableColorType) {
            var logBg:Color? = null
            var logFg:Color? = null
            var lineNumBg:Color? = null
            var lineNumFg:Color? = null
            var filteredBg:Color? = null
            var filteredFg:Color? = null
            var highlightBg:Color? = null
            var highlightFg:Color? = null
            var searchBg:Color? = null
            var searchFg:Color? = null

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
                    "Log BG"->logBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Log Level None"->logFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "LineNum BG"->lineNumBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "LineNum FG"->lineNumFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Filtered BG"->filteredBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Filtered FG"->filteredFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Highlight BG"->highlightBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Highlight FG"->highlightFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Search BG"->searchBg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    "Search FG"->searchFg = Color.decode(tableColor.mColorArray[idx].mStrColor)
                }
            }

            logFg = logFg ?: Color.BLACK
            logBg = logBg ?: Color.WHITE
            lineNumFg = lineNumFg ?: Color.BLACK
            lineNumBg = lineNumBg ?: Color.WHITE
            filteredFg = filteredFg ?: Color.BLACK
            filteredBg = filteredBg ?: Color.WHITE
            highlightFg = highlightFg ?: Color.BLACK
            highlightBg = highlightBg ?: Color.WHITE
            searchFg = searchFg ?: Color.BLACK
            searchBg = searchBg ?: Color.WHITE

            for (idx in colorLabelArray.indices) {
                if (tableColor.mColorArray[idx].mName.contains("BG")) {
                    colorLabelArray[idx]!!.background = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    when (tableColor.mColorArray[idx].mName) {
                        "LineNum BG" -> {
                            colorLabelArray[idx]!!.foreground = lineNumFg
                        }
                        "Filtered BG" -> {
                            colorLabelArray[idx]!!.foreground = filteredFg
                        }
                        "Highlight BG" -> {
                            colorLabelArray[idx]!!.foreground = highlightFg
                        }
                        "Search BG" -> {
                            colorLabelArray[idx]!!.foreground = searchFg
                        }
                        else -> {
                            if ((tableColor.mColorArray[idx].mName == "Filtered 1 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 2 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 3 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 4 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 5 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 6 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 7 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 8 BG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 9 BG")) {
                                colorLabelArray[idx]!!.foreground = Color.decode(tableColor.mColorArray[idx - 9].mStrColor)
                            }
                            else {
                                colorLabelArray[idx]!!.foreground = logFg
                            }
                        }
                    }
                }
                else {
                    colorLabelArray[idx]!!.foreground = Color.decode(tableColor.mColorArray[idx].mStrColor)
                    when (tableColor.mColorArray[idx].mName) {
                        "LineNum FG" -> {
                            colorLabelArray[idx]!!.background = lineNumBg
                        }
                        "Filtered FG" -> {
                            colorLabelArray[idx]!!.background = filteredBg
                        }
                        "Highlight FG" -> {
                            colorLabelArray[idx]!!.background = highlightBg
                        }
                        "Search FG" -> {
                            colorLabelArray[idx]!!.background = searchBg
                        }
                        else -> {
                            if ((tableColor.mColorArray[idx].mName == "Filtered 1 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 2 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 3 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 4 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 5 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 6 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 7 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 8 FG")
                                    || (tableColor.mColorArray[idx].mName == "Filtered 9 FG")) {
                                colorLabelArray[idx]!!.background = Color.decode(tableColor.mColorArray[idx + 9].mStrColor)
                            }
                            else {
                                colorLabelArray[idx]!!.background = logBg
                            }
                        }
                    }
                }
            }
        }

        inner class ColorLabel(type: ColorManager.TableColorType, idx: Int) :JLabel() {
            val mType = type
            val mIdx = idx
        }

        fun actionBtn(isOK: Boolean) {
            if (isOK) {
                mIsNeedRestore = false
            }
        }

        private fun setFont() {
            val selection = mNameList.selectedValue
            val size = mSizeSpinner.model.value as Int
            mExampleLabel.font = Font(selection.toString(), Font.PLAIN, size)
            mMainUI.mFont = Font(selection.toString(), Font.PLAIN, size)
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

        val optionFullCheckbox = JCheckBox(Strings.FULL_LOG_TABLE)
        val optionFilterCheckbox = JCheckBox(Strings.FILTER_LOG_TABLE)
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

                    val optionPanel = JPanel()
                    val optionTitleLabel = JLabel("${mTitleLabelArray[colorLabel.mIdx]!!.text} : ")
                    if (!optionFullCheckbox.isSelected || !optionFilterCheckbox.isSelected) {
                        if (colorLabel.mType == ColorManager.TableColorType.FULL_LOG_TABLE) {
                            optionFullCheckbox.isSelected = true
                            optionFilterCheckbox.isSelected = false
                        } else {
                            optionFullCheckbox.isSelected = false
                            optionFilterCheckbox.isSelected = true
                        }
                    }

                    optionPanel.add(optionTitleLabel)
                    optionPanel.add(optionFullCheckbox)
                    optionPanel.add(optionFilterCheckbox)

                    val colorPanel = JPanel(BorderLayout())
                    colorPanel.add(rgbPanel, BorderLayout.CENTER)
                    colorPanel.add(optionPanel, BorderLayout.SOUTH)

                    val ret = JOptionPane.showConfirmDialog(this@AppearanceSettingsDialog, colorPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    if (ret == JOptionPane.OK_OPTION) {
                        if (optionFullCheckbox.isSelected) {
                            updateColor(mFullColorLabelArray[colorLabel.mIdx]!!, colorChooser.color)
                        }
                        if (optionFilterCheckbox.isSelected) {
                            updateColor(mFilterColorLabelArray[colorLabel.mIdx]!!, colorChooser.color)
                        }

//                        SwingUtilities.updateComponentTreeUI(mMainUI)
                        mMainUI.mFullLogPanel.repaint()
                        mMainUI.mFilteredLogPanel.repaint()
                    }
                }

                super.mouseClicked(e)
            }

            private fun updateColor(colorLabel: ColorLabel, color: Color) {
                val hex = "#" + Integer.toHexString(color.rgb).substring(2).uppercase()
                colorLabel.text = " ${mFullTableColor.mColorArray[colorLabel.mIdx].mName} $hex "
                val tableColor = if (colorLabel.mType == ColorManager.TableColorType.FULL_LOG_TABLE) {
                    mFullTableColor
                }
                else {
                    mFilterTableColor
                }
                tableColor.mColorArray[colorLabel.mIdx].mStrColor = hex
                if (colorLabel.text.contains("BG")) {
                    colorLabel.background = color
                } else {
                    colorLabel.foreground = color
                }
                tableColor.applyColor()
                updateLabelColor(colorLabel.mType)
            }
        }
    }
}