package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.ChangeListener


class AppearanceDialog (parent: MainUI) : JDialog(parent, Strings.APPEARANCE, true), ActionListener {
    private var mFontSlider: JSlider
    private var mDividerSlider: JSlider
    private var mLaFGroup: ButtonGroup
    private var mExampleLabel: JLabel
    private var mParent = parent
    private var mBaseFontSize = 0

    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private val MIN_FONT_POS = 50
    private val MAX_FONT_POS = 200
    private val EXAMPLE_TEXT = "To apply \"Changes\" need to restart"

    private val MIN_DIVIDER_POS = 1
    private val MAX_DIVIDER_POS = 20
    private val mFrevDividerSize = mParent.mLogSplitPane.dividerSize

    init {
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        val lafPanel = JPanel()
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

        lafItem = JRadioButton(MainUI.FLAT_DARK_LAF)
        mLaFGroup.add(lafItem)
        lafPanel.add(lafItem)

        for (item in mLaFGroup.elements) {
            if (ConfigManager.LaF == item.text) {
                item.isSelected = true
                break
            }
        }

        mExampleLabel = JLabel(EXAMPLE_TEXT)
        mExampleLabel.preferredSize = Dimension(350, 50)

        val sliderPanel = JPanel()
        val sliderLabel = JLabel("UI Size(%)")
        sliderPanel.add(sliderLabel)
        mBaseFontSize = mExampleLabel.font.size * 100 / mParent.mUIFontPercent
        mFontSlider = JSlider(MIN_FONT_POS, MAX_FONT_POS, mParent.mUIFontPercent)
        mFontSlider.majorTickSpacing = 50
        mFontSlider.minorTickSpacing = 10
        mFontSlider.paintTicks = true
        mFontSlider.paintLabels = true
        mFontSlider.addChangeListener(ChangeListener {
            mExampleLabel.text = "${mFontSlider.value} % : $EXAMPLE_TEXT"
            mExampleLabel.font = Font(mExampleLabel.font.name, mExampleLabel.font.style, mBaseFontSize * mFontSlider.value / 100)
        })
        sliderPanel.add(mFontSlider)

        val sizePanel = JPanel()
        sizePanel.layout = BoxLayout(sizePanel, BoxLayout.Y_AXIS)
        sizePanel.add(mExampleLabel)
        sizePanel.add(sliderPanel)

        val lafSizePanel = JPanel(BorderLayout())
        lafSizePanel.border = BorderFactory.createTitledBorder(Strings.LOOK_AND_FEEL);
        lafSizePanel.add(lafPanel, BorderLayout.NORTH)
        lafSizePanel.add(sizePanel, BorderLayout.CENTER)

        val dividerPanel = JPanel()
        val dividerLabel = JLabel("Divider Size(1 ~ 20) [${mParent.mLogSplitPane.dividerSize}]")
        dividerPanel.add(dividerLabel)
        mDividerSlider = JSlider(0, MAX_DIVIDER_POS, mParent.mLogSplitPane.dividerSize)
        mDividerSlider.majorTickSpacing = 5
        mDividerSlider.minorTickSpacing = 1
        mDividerSlider.paintTicks = true
        mDividerSlider.paintLabels = true
        mDividerSlider.addChangeListener(ChangeListener {
            if (mDividerSlider.value == 0) {
                mDividerSlider.value = MIN_DIVIDER_POS
            }
            mParent.mLogSplitPane.dividerSize = mDividerSlider.value
            dividerLabel.text = "Divider Size(1 ~ 20) [${mParent.mLogSplitPane.dividerSize}]"
        })
        dividerPanel.add(mDividerSlider)

        val optionsPanel = JPanel(BorderLayout())
        optionsPanel.border = BorderFactory.createTitledBorder(Strings.OPTIONS);
        optionsPanel.add(dividerPanel, BorderLayout.CENTER)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(350, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val dialogPanel = JPanel(BorderLayout())
        dialogPanel.add(lafSizePanel, BorderLayout.NORTH)
        dialogPanel.add(optionsPanel, BorderLayout.CENTER)
        dialogPanel.add(confirmPanel, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout())
        panel.add(dialogPanel, BorderLayout.CENTER)

        contentPane.add(panel)

        pack()
        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            for (item in mLaFGroup.elements) {
                if (item.isSelected) {
                    ConfigManager.getInstance().saveItem(ConfigManager.ITEM_LOOK_AND_FEEL, item.text)
                    ConfigManager.getInstance().saveItem(ConfigManager.ITEM_UI_FONT_SIZE, mFontSlider.value.toString())
                    ConfigManager.getInstance().saveItem(ConfigManager.ITEM_APPEARANCE_DIVIDER_SIZE, mParent.mLogSplitPane.dividerSize.toString())
                    break
                }
            }
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        } else if (e?.source == mCancelBtn) {
            mParent.mLogSplitPane.dividerSize = mFrevDividerSize
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
    }
}

