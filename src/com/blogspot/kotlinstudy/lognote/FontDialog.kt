package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*
import javax.swing.JList
import javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicScrollBarUI


class FontDialog (parent: MainUI) : JDialog(parent, "Font settings", true), ActionListener {
    private var mNameScrollPane: JScrollPane
    private var mNameList: JList<String>
    private var mSizeLabel: JLabel
    private var mSizeSpinner: JSpinner
    private var mExampleLabel: JLabel
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton
    private var mParent = parent
    private val mPrevFont = mParent.mFont

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
        bottomPanel.add(confirmPanel, BorderLayout.CENTER)
        val panel = JPanel(BorderLayout())
        panel.add(namePanel, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mOkBtn) {
            dispose()
        } else if (e?.source == mCancelBtn) {
            mParent.mFont = mPrevFont
            dispose()
        }
    }

    internal inner class ChangeHandler: ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            val selection = mNameList.selectedValue
            val size = mSizeSpinner.model.value as Int
            mExampleLabel.font = Font(selection.toString(), Font.PLAIN, size)

            mParent.mFont = Font(selection.toString(), Font.PLAIN, size)

        }
    }

    internal inner class ListSelectionHandler : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            if (p0?.source == mNameList) {
                val selection = mNameList.selectedValue
                val size = mSizeSpinner.model.value as Int
                mExampleLabel.font = Font(selection.toString(), Font.PLAIN, size)

                mParent.mFont = Font(selection.toString(), Font.PLAIN, size)

            }
        }
    }
}

