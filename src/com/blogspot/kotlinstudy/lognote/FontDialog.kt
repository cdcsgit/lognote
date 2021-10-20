package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI


class FontDialog (parent: MainUI) : JDialog(parent, "Font & Color settings", true), ActionListener {
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
    private val mColorLabelArray = arrayOfNulls<ColorLabel>(mColorManager.mColorArray.size)
    private val mMouseHandler = MouseHandler()
    private val mPrevColorArray = arrayOfNulls<String>(mColorManager.mColorArray.size)

    init {
        mNameList = JList(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
        mNameList.selectionMode = SINGLE_SELECTION
        mNameScrollPane = JScrollPane(mNameList)
        mNameScrollPane.preferredSize = Dimension(330, 200)
        mNameList.setSelectedValue(parent.mFont.family, true)
        mNameScrollPane.verticalScrollBar.ui = BasicScrollBarUI()
        mNameScrollPane.horizontalScrollBar.ui = BasicScrollBarUI()
        mNameList.addListSelectionListener(ListSelectionHandler())
        mOkBtn = ColorButton("OK")
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton("Cancel")
        mCancelBtn.addActionListener(this)

        mSizeLabel = JLabel("Size")
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
        for (idx in mColorLabelArray.indices) {
            mPrevColorArray[idx] = mColorManager.mColorArray[idx].mStrColor
            mColorLabelArray[idx] = ColorLabel(idx)
            mColorLabelArray[idx]!!.text = mColorManager.mColorArray[idx].mName + " " + mColorManager.mColorArray[idx].mStrColor
            mColorLabelArray[idx]!!.isOpaque = true
            if (mColorManager.mColorArray[idx].mName.contains("BG")) {
                mColorLabelArray[idx]!!.background = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                mColorLabelArray[idx]!!.horizontalAlignment = JLabel.RIGHT;
            }
            else {
                mColorLabelArray[idx]!!.foreground = Color.decode(mColorManager.mColorArray[idx].mStrColor)
                mColorLabelArray[idx]!!.horizontalAlignment = JLabel.LEFT;
            }

            mColorLabelArray[idx]!!.verticalAlignment = JLabel.CENTER;
            mColorLabelArray[idx]!!.border = BorderFactory.createLineBorder(Color.BLACK)
            mColorLabelArray[idx]!!.minimumSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.preferredSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.maximumSize = Dimension(330, 20)
            mColorLabelArray[idx]!!.addMouseListener(mMouseHandler)
            colorLabelPanel.add(mColorLabelArray[idx])
            colorLabelPanel.add(Box.createRigidArea(Dimension(5, 3)))
        }

        val namePanel = JPanel()
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
        bottomPanel.add(colorLabelPanel, BorderLayout.CENTER)
        bottomPanel.add(confirmPanel, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout())
        panel.add(namePanel, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
    }

    class ColorLabel(idx: Int) :JLabel() {
        val mIdx: Int = idx
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            dispose()
        } else if (e?.source == mCancelBtn) {
            for (idx in mColorLabelArray.indices) {
                mColorManager.mColorArray[idx].mStrColor = mPrevColorArray[idx]!!
            }
            mColorManager.applyColor()
            mParent.mFont = mPrevFont
            dispose()
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

                var ret = JOptionPane.showConfirmDialog(null, rgbPanel, "Color Chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                if (ret == JOptionPane.OK_OPTION) {
                    val hex = "#" + Integer.toHexString(colorChooser.color.rgb).substring(2).uppercase()
                    colorLabel.text = mColorManager.mColorArray[colorLabel.mIdx].mName + " " + hex
                    mColorManager.mColorArray[colorLabel.mIdx].mStrColor = hex
                    if (colorLabel.text.contains("BG")) {
                        colorLabel.background = colorChooser.color
                    } else {
                        colorLabel.foreground = colorChooser.color
                    }
                    mColorManager.applyColor()
                    setFont() // refresh log table
                }
            }

            super.mouseClicked(e)
        }
    }
}

