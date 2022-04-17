package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.*
import javax.swing.*
import javax.swing.event.ChangeListener


class LaFDialog (parent: MainUI) : JDialog(parent, Strings.LOOK_AND_FEEL, true), ActionListener {
    private var mSlider: JSlider
    private var mLaFGroup: ButtonGroup
    private var mExampleLabel: JLabel
    private var mParent = parent
    private var mBaseFontSize = 0

    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private val MIN_POS = 50
    private val MAX_POS = 200
    private val EXAMPLE_TEXT = "To apply \"Changes\" need to restart"

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
        mSlider = JSlider(MIN_POS, MAX_POS, mParent.mUIFontPercent)
        mSlider.majorTickSpacing = 50
        mSlider.minorTickSpacing = 10
        mSlider.paintTicks = true
        mSlider.paintLabels = true
        mSlider.addChangeListener(ChangeListener {
            mExampleLabel.text = "${mSlider.value} % : $EXAMPLE_TEXT"
            mExampleLabel.font = Font(mExampleLabel.font.name, mExampleLabel.font.style, mBaseFontSize * mSlider.value / 100)
        })
        sliderPanel.add(mSlider)

        val sizePanel = JPanel()
        sizePanel.layout = BoxLayout(sizePanel, BoxLayout.Y_AXIS)
        sizePanel.add(mExampleLabel)
        sizePanel.add(sliderPanel)

        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(lafPanel, BorderLayout.NORTH)
        centerPanel.add(sizePanel, BorderLayout.CENTER)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(350, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(centerPanel, BorderLayout.NORTH)
        bottomPanel.add(confirmPanel, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout())
        panel.add(bottomPanel, BorderLayout.CENTER)

        contentPane.add(panel)

        pack()
        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            for (item in mLaFGroup.elements) {
                if (item.isSelected) {
                    ConfigManager.getInstance().saveItem(ConfigManager.ITEM_LOOK_AND_FEEL, item.text)
                    ConfigManager.getInstance().saveItem(ConfigManager.ITEM_UI_FONT_SIZE, mSlider.value.toString())
                    break
                }
            }
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        } else if (e?.source == mCancelBtn) {
            this.dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
    }
}

