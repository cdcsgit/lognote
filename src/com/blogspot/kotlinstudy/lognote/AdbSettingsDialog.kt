package com.blogspot.kotlinstudy.lognote

import java.awt.*
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
        val rowHeight = 30
        mAdbCmdBtn = ColorButton(Strings.SELECT)
        mAdbCmdBtn.addActionListener(this)
        mAdbCmdBtn.preferredSize = Dimension(mAdbCmdBtn.preferredSize.width, rowHeight)
        mAdbSaveBtn = ColorButton(Strings.SELECT)
        mAdbSaveBtn.addActionListener(this)
        mOkBtn = ColorButton(Strings.OK)
        mOkBtn.addActionListener(this)
        mCancelBtn = ColorButton(Strings.CANCEL)
        mCancelBtn.addActionListener(this)

        mAdbCmdLabel = JLabel(Strings.ADB_PATH)
        mAdbCmdLabel.preferredSize = Dimension(mAdbCmdLabel.preferredSize.width, rowHeight)
        mAdbSaveLabel = JLabel(Strings.LOG_PATH)
        mPrefixLabel = JLabel("Prefix")
        mPrefixLabel2 = JLabel("Default : LogNote, Do not use \\ / : * ? \" < > |")

        mAdbCmdTF = JTextField(mAdbManager.mAdbCmd)
        mAdbCmdTF.preferredSize = Dimension(488, rowHeight)
        mAdbSaveTF = JTextField(mAdbManager.mLogSavePath)
        mAdbSaveTF.preferredSize = Dimension(488, rowHeight)
        mPrefixTF = JTextField(mAdbManager.mPrefix)
        mPrefixTF.preferredSize = Dimension(300, rowHeight)

        val panel1 = JPanel(GridLayout(4, 1, 0, 2))
        panel1.add(mAdbCmdLabel)
        panel1.add(mAdbSaveLabel)
        panel1.add(mPrefixLabel)

        val panel2 = JPanel(GridLayout(4, 1, 0, 2))
        panel2.add(mAdbCmdTF)
        panel2.add(mAdbSaveTF)
        panel2.add(mPrefixTF)
        panel2.add(mPrefixLabel2)

        val panel3 = JPanel(GridLayout(4, 1, 0, 2))
        panel3.add(mAdbCmdBtn)
        panel3.add(mAdbSaveBtn)

        val cmdPathPanel = JPanel()
        cmdPathPanel.add(panel1)
        cmdPathPanel.add(panel2)
        cmdPathPanel.add(panel3)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(400, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val panel = JPanel(BorderLayout())
        panel.add(cmdPathPanel, BorderLayout.CENTER)
        panel.add(confirmPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mAdbCmdBtn) {
            val fileDialog = FileDialog(this@AdbSettingsDialog, "Adb command", FileDialog.LOAD)
            fileDialog.isVisible = true
            if (fileDialog.file != null) {
                val file = File(fileDialog.directory + fileDialog.file)
                println("adb command : ${file.absolutePath}")
                mAdbCmdTF.text = file.absolutePath
            } else {
                println("Cancel Open")
            }
        } else if (e?.source == mAdbSaveBtn) {
            val chooser = JFileChooser()
            chooser.currentDirectory = File(".")
            chooser.dialogTitle = "Adb Save Dir"
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            chooser.isAcceptAllFileFilterUsed = false

            if (chooser.showOpenDialog(this@AdbSettingsDialog) == JFileChooser.APPROVE_OPTION) {
                println("getSelectedFile() : ${chooser.selectedFile}")
                mAdbSaveTF.text = chooser.selectedFile.absolutePath
            } else {
                println("No Selection ")
            }
        } else if (e?.source == mOkBtn) {
            mAdbManager.mAdbCmd = mAdbCmdTF.text
            mAdbManager.mLogSavePath = mAdbSaveTF.text
            var prefix = mPrefixTF.text.trim()

            mPrefixLabel2 = JLabel("Default : LogNote, Do not use \\ / : * ? \" < > |")
            if (prefix.contains('\\')
                    || prefix.contains('/')
                    || prefix.contains(':')
                    || prefix.contains('*')
                    || prefix.contains('?')
                    || prefix.contains('"')
                    || prefix.contains("<")
                    || prefix.contains(">")
                    || prefix.contains("|")) {
                JOptionPane.showMessageDialog(this, "Invalid prefix : ${mPrefixTF.text}", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }

            if (prefix.isEmpty()) {
                mAdbManager.mPrefix = mAdbManager.DEFAULT_PREFIX
            }
            else {
                mAdbManager.mPrefix = prefix
            }

            val keys = arrayOf(ConfigManager.ITEM_ADB_CMD, ConfigManager.ITEM_ADB_LOG_SAVE_PATH, ConfigManager.ITEM_ADB_PREFIX)
            val values = arrayOf(mAdbManager.mAdbCmd, mAdbManager.mLogSavePath, mAdbManager.mPrefix)

            ConfigManager.getInstance().saveItems(keys, values)

            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }
}