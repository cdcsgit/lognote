package com.blogspot.kotlinstudy.lognote

import java.awt.Dimension
import java.awt.FileDialog
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*

class AdbSettingsDialog(parent: JFrame) :JDialog(parent, "ADB " + Strings.SETTING, true), ActionListener {
    private var mAdbCmdBtn: ColorButton
    private var mAdbSaveBtn: ColorButton
    private var mOkBtn: ColorButton
    private var mCancelBtn: ColorButton

    private var mAdbCmdLabel: JLabel
    private var mAdbSaveLabel: JLabel
    private var mPrefixLabel: JLabel
    private var mPrefixLabel2: JLabel

    private var mAdbCmdTF: JTextField
    private var mAdbSaveTF: JTextField
    private var mPrefixTF: JTextField

    private val mAdbManager = AdbManager.getInstance()

    init {
        mAdbCmdBtn = ColorButton(Strings.SELECT)
        mAdbCmdBtn.addActionListener(this)
        mAdbSaveBtn = ColorButton(Strings.SELECT)
        mAdbSaveBtn.addActionListener(this)
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        mAdbCmdLabel = JLabel(Strings.ADB_PATH)
        mAdbSaveLabel = JLabel(Strings.LOG_PATH)
        mPrefixLabel = JLabel("Prefix")
        mPrefixLabel2 = JLabel("Default : device, Do not use \\ / : * ? \" < > |")

        mAdbCmdTF = JTextField(mAdbManager.mAdbCmd)
        mAdbCmdTF.preferredSize = Dimension(488, 30)
        mAdbSaveTF = JTextField(mAdbManager.mLogSavePath)
        mAdbSaveTF.preferredSize = Dimension(488, 30)
        mPrefixTF = JTextField(mAdbManager.mPrefix)
        mPrefixTF.preferredSize = Dimension(300, 30)

        val cmdPanel = JPanel()
        cmdPanel.add(mAdbCmdLabel)
        cmdPanel.add(mAdbCmdTF)
        cmdPanel.add(mAdbCmdBtn)

        val savePanel = JPanel()
        savePanel.add(mAdbSaveLabel)
        savePanel.add(mAdbSaveTF)
        savePanel.add(mAdbSaveBtn)

        val prefixPanel = JPanel()
        prefixPanel.add(mPrefixLabel)
        prefixPanel.add(mPrefixTF)
        prefixPanel.add(mPrefixLabel2)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(400, 30)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val panel = JPanel(GridLayout(5, 1))
        panel.add(cmdPanel)
        panel.add(savePanel)
        panel.add(prefixPanel)
//        panel.add(mPrefixLabel2)
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
                mAdbCmdTF.text = file.absolutePath
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
                mAdbSaveTF.text = chooser.selectedFile.absolutePath
            } else {
                println("No Selection ")
            }
        } else if (e?.source == mOkBtn) {
            mAdbManager.mAdbCmd = mAdbCmdTF.text
            mAdbManager.mLogSavePath = mAdbSaveTF.text
            mAdbManager.mPrefix = mPrefixTF.text
            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }
}