package com.blogspot.kotlinstudy.lognote

import java.awt.Dimension
import java.awt.FileDialog
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*

class AdbSettingsDialog(parent: JFrame) :JDialog(parent, "ADB settings", true), ActionListener {
    private var mAdbCmdBtn: ColorButton
    private var mAdbSaveBtn: ColorButton
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private var mAdbCmdLabel: JLabel
    private var mAdbSaveLabel: JLabel
    private var mPrefixLabel: JLabel

    private var mAdbCmdTextField: JTextField
    private var mAdbSaveTextField: JTextField
    private var mPrefixTextField: JTextField

    private val mAdbManager = AdbManager.getInstance()

    init {
        mAdbCmdBtn = ColorButton("Select")
        mAdbCmdBtn.addActionListener(this)
        mAdbSaveBtn = ColorButton("Select")
        mAdbSaveBtn.addActionListener(this)
        mOkBtn = ColorButton("OK")
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton("Cancel")
        mCancelBtn.addActionListener(this)

        mAdbCmdLabel = JLabel("Adb path")
        mAdbSaveLabel = JLabel("Log path")
        mPrefixLabel = JLabel("Prefix(default : device)")

        mAdbCmdTextField = JTextField(mAdbManager.mAdbCmd)
        mAdbCmdTextField.preferredSize = Dimension(300, 30)
        mAdbSaveTextField = JTextField(mAdbManager.mLogSavePath)
        mAdbSaveTextField.preferredSize = Dimension(300, 30)
        mPrefixTextField = JTextField(mAdbManager.mPrefix)
        mPrefixTextField.preferredSize = Dimension(300, 30)

        val cmdPanel = JPanel()
        cmdPanel.add(mAdbCmdLabel)
        cmdPanel.add(mAdbCmdTextField)
        cmdPanel.add(mAdbCmdBtn)

        val savePanel = JPanel()
        savePanel.add(mAdbSaveLabel)
        savePanel.add(mAdbSaveTextField)
        savePanel.add(mAdbSaveBtn)

        val prefixPanel = JPanel()
        prefixPanel.add(mPrefixLabel)
        prefixPanel.add(mPrefixTextField)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(400, 30)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val panel = JPanel(GridLayout(4, 1))
        panel.add(cmdPanel)
        panel.add(savePanel)
        panel.add(prefixPanel)
        panel.add(confirmPanel)

        contentPane.add(panel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mAdbCmdBtn) {
            val fileDialog = FileDialog(this@AdbSettingsDialog, "Adb command", FileDialog.LOAD)
            fileDialog.setVisible(true)
            if (fileDialog.getFile() != null) {
                val file = File(fileDialog.getDirectory() + fileDialog.getFile())
                System.out.println("adb command : " + file.absolutePath)
                mAdbCmdTextField.text = file.absolutePath
            } else {
                System.out.println("Cancel Open")
            }
        } else if (e?.source == mAdbSaveBtn) {
            val chooser = JFileChooser()
            chooser.currentDirectory = File(".")
            chooser.dialogTitle = "Adb Save Dir"
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            chooser.isAcceptAllFileFilterUsed = false

            if (chooser.showOpenDialog(this@AdbSettingsDialog) == JFileChooser.APPROVE_OPTION) {
                println("getSelectedFile() : " + chooser.selectedFile)
                mAdbSaveTextField.text = chooser.selectedFile.absolutePath
            } else {
                println("No Selection ")
            }
        } else if (e?.source == mOkBtn) {
            mAdbManager.mAdbCmd = mAdbCmdTextField.text
            mAdbManager.mLogSavePath = mAdbSaveTextField.text
            mAdbManager.mPrefix = mPrefixTextField.text
            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }
}