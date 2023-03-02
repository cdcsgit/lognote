package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


class AdbSettingsDialog(parent: MainUI) :JDialog(parent, "${Strings.LOG_CMD} ${Strings.SETTING}", true), ActionListener {
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

    private var mLogCmdTable: JTable
    private var mLogCmdTableModel: LogCmdTableModel
    private var mLogCmdLabel1: JLabel
    private var mLogCmdLabel2: JLabel

    inner class LogCmdTableModel(logCmds: Array<Array<Any>>, columnNames: Array<String>) : DefaultTableModel(logCmds, columnNames) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }

    inner class LogCmdMouseHandler() : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (e != null) {
                if (e.clickCount == 2) {
                    if (mLogCmdTable.selectedRow > 0) {
                        val logCmdDialog = LogCmdDialog(this@AdbSettingsDialog)
                        logCmdDialog.setLocationRelativeTo(this@AdbSettingsDialog)
                        logCmdDialog.isVisible = true
                    }
                }
            }
            super.mouseClicked(e)
        }
    }

    private val mAdbManager = AdbManager.getInstance()
    private val mConfigManager = ConfigManager.getInstance()
    private val mMainUI = parent

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

        val columnNames = arrayOf("Num", "Cmd")

        // logCmds num = AdbManager.LOG_CMD_MAX
        val logCmds = arrayOf(
                arrayOf<Any>("1(fixed)", AdbManager.LOG_CMD),
                arrayOf<Any>("2", ""),
                arrayOf<Any>("3", ""),
                arrayOf<Any>("4", ""),
                arrayOf<Any>("5", ""),
                arrayOf<Any>("6", ""),
                arrayOf<Any>("7", ""),
                arrayOf<Any>("8", ""),
                arrayOf<Any>("9", ""),
                arrayOf<Any>("10", ""),
        )

        for (idx in logCmds.indices) {
            val item = mConfigManager.getItem("${ConfigManager.ITEM_ADB_LOG_CMD}_$idx")
            if (idx != 0 && item != null) {
                logCmds[idx][1] = item
            }
        }

        mLogCmdTableModel = LogCmdTableModel(logCmds, columnNames)
        mLogCmdTable = JTable(mLogCmdTableModel)
        mLogCmdTable.preferredSize = Dimension(488, 200)
        mLogCmdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        mLogCmdTable.showHorizontalLines = true
        mLogCmdTable.showVerticalLines = true
        val renderer = DefaultTableCellRenderer()
        renderer.horizontalAlignment = JLabel.CENTER
        mLogCmdTable.columnModel.getColumn(0).cellRenderer = renderer
        mLogCmdTable.addMouseListener(LogCmdMouseHandler())

        mLogCmdTableModel.rowCount = AdbManager.LOG_CMD_MAX
        mLogCmdTable.columnModel.getColumn(0).preferredWidth = 70
        mLogCmdTable.columnModel.getColumn(1).preferredWidth = 330

        mLogCmdLabel1 = JLabel("<html><b><font color=\"#7070FF\">logcat -v threadtime</font></b> <br>&nbsp;&nbsp;&nbsp;&nbsp => RUN : <b><font color=\"#7070FF\">adb -s DEVICE logcat -v threadtime</font></b></html>")
        mLogCmdLabel1.preferredSize = Dimension(488, mLogCmdLabel1.preferredSize.height)
        mLogCmdLabel2 = JLabel("<html><b><font color=\"#7070FF\">CMD:cmdABC</font></b> <br>&nbsp;&nbsp;&nbsp;&nbsp => RUN : <b><font color=\"#7070FF\">cmdABC DEVICE</font></b></html>")
        mLogCmdLabel2.preferredSize = Dimension(488, mLogCmdLabel2.preferredSize.height)

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

        val cmdPathPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        cmdPathPanel.add(panel1)
        cmdPathPanel.add(panel2)
        cmdPathPanel.add(panel3)

        val pathPanel = JPanel()
        pathPanel.layout = BoxLayout(pathPanel, BoxLayout.Y_AXIS)
        addHSeparator(pathPanel, "ADB " + Strings.SETTING)
        pathPanel.add(cmdPathPanel, BorderLayout.NORTH)

        val cmdPanel = JPanel(BorderLayout())
        cmdPanel.add(pathPanel, BorderLayout.NORTH)

        val logCmdTablePanel = JPanel()
        logCmdTablePanel.add(mLogCmdTable)

        val logCmdLable1Panel = JPanel()
        logCmdLable1Panel.add(mLogCmdLabel1)

        val logCmdLable2Panel = JPanel()
        logCmdLable2Panel.add(mLogCmdLabel2)

        val logCmdPanel = JPanel()
        logCmdPanel.layout = BoxLayout(logCmdPanel, BoxLayout.Y_AXIS)
        addHSeparator(logCmdPanel, Strings.LOG_CMD)
        logCmdPanel.add(logCmdTablePanel)
        logCmdPanel.add(logCmdLable1Panel)
        logCmdPanel.add(logCmdLable2Panel)

        cmdPanel.add(logCmdPanel, BorderLayout.CENTER)

        val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        confirmPanel.preferredSize = Dimension(400, 40)
        confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
        confirmPanel.add(mOkBtn)
        confirmPanel.add(mCancelBtn)

        val panel = JPanel(BorderLayout())
        panel.add(cmdPanel, BorderLayout.CENTER)
        panel.add(confirmPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
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
            val prefix = mPrefixTF.text.trim()

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

            for (idx in 0 until mLogCmdTable.rowCount) {
                mConfigManager.setItem("${ConfigManager.ITEM_ADB_LOG_CMD}_$idx", mLogCmdTableModel.getValueAt(idx, 1).toString())
            }
            mConfigManager.saveConfig()

            val keys = arrayOf(ConfigManager.ITEM_ADB_CMD, ConfigManager.ITEM_ADB_LOG_SAVE_PATH, ConfigManager.ITEM_ADB_PREFIX, ConfigManager.ITEM_ADB_LOG_CMD)
            val values = arrayOf(mAdbManager.mAdbCmd, mAdbManager.mLogSavePath, mAdbManager.mPrefix, mAdbManager.mLogCmd)

            mConfigManager.saveItems(keys, values)
            mMainUI.updateLogCmdCombo(true)

            dispose()
        } else if (e?.source == mCancelBtn) {
            dispose()
        }
    }

    inner class LogCmdDialog(parent: JDialog) :JDialog(parent, Strings.LOG_CMD, true), ActionListener, FocusListener {
        private var mAdbRadio: JRadioButton
        private var mCmdRadio: JRadioButton

        private var mAdbTF: JTextField
        private var mCmdTF: JTextField

        private var mCmdBtn: ColorButton

        private var mOkBtn: ColorButton
        private var mCancelBtn: ColorButton

        init {
            val rowHeight = 30
            mAdbRadio = JRadioButton(Strings.ADB)
            mAdbRadio.preferredSize = Dimension(60, rowHeight)
            mCmdRadio = JRadioButton(Strings.CMD)

            val buttonGroup = ButtonGroup()
            buttonGroup.add(mAdbRadio)
            buttonGroup.add(mCmdRadio)

            mAdbTF = JTextField()
            mAdbTF.preferredSize = Dimension(488, rowHeight)
            mAdbTF.addFocusListener(this)
            mCmdTF = JTextField()
            mCmdTF.addFocusListener(this)

            val initCmd = mLogCmdTable.getValueAt(mLogCmdTable.selectedRow, 1) as String?
            if (initCmd?.startsWith("CMD:") == true) {
                mCmdTF.text = initCmd.substring(4)
                mCmdRadio.isSelected = true
            } else {
                mAdbTF.text = initCmd
                mAdbRadio.isSelected = true
            }

            mCmdBtn = ColorButton(Strings.SELECT)
            mCmdBtn.addActionListener(this)
            mCmdBtn.preferredSize = Dimension(mCmdBtn.preferredSize.width, rowHeight)

            mOkBtn = ColorButton(Strings.OK)
            mOkBtn.addActionListener(this)
            mCancelBtn = ColorButton(Strings.CANCEL)
            mCancelBtn.addActionListener(this)

            val panel1 = JPanel(GridLayout(2, 1, 0, 2))
            panel1.add(mAdbRadio)
            panel1.add(mCmdRadio)

            val panel2 = JPanel(GridLayout(2, 1, 0, 2))
            panel2.add(mAdbTF)
            panel2.add(mCmdTF)

            val panel3 = JPanel(GridLayout(2, 1, 0, 2))
            panel3.add(JPanel())
            panel3.add(mCmdBtn)

            val cmdPathPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            cmdPathPanel.add(panel1)
            cmdPathPanel.add(panel2)
            cmdPathPanel.add(panel3)

            val pathPanel = JPanel()
            pathPanel.layout = BoxLayout(pathPanel, BoxLayout.Y_AXIS)
            pathPanel.add(cmdPathPanel, BorderLayout.NORTH)

            val cmdPanel = JPanel(BorderLayout())
            cmdPanel.add(pathPanel, BorderLayout.NORTH)

            val confirmPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
            confirmPanel.preferredSize = Dimension(400, 40)
            confirmPanel.alignmentX = JPanel.RIGHT_ALIGNMENT
            confirmPanel.add(mOkBtn)
            confirmPanel.add(mCancelBtn)

            val panel = JPanel(BorderLayout())
            panel.add(cmdPanel, BorderLayout.CENTER)
            panel.add(confirmPanel, BorderLayout.SOUTH)

            contentPane.add(panel)
            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mCmdBtn) {
                val fileDialog = FileDialog(this@LogCmdDialog, Strings.CMD, FileDialog.LOAD)
                fileDialog.isVisible = true
                if (fileDialog.file != null) {
                    val file = File(fileDialog.directory + fileDialog.file)
                    println("command : ${file.absolutePath}")
                    mCmdTF.text = file.absolutePath
                } else {
                    println("Cancel Open")
                }
            } else if (e?.source == mOkBtn) {
                val text = if (mCmdRadio.isSelected) {
                    if (mCmdTF.text.isNotEmpty()) {
                        "CMD:${mCmdTF.text}"
                    }
                    else {
                        ""
                    }
                }
                else {
                    mAdbTF.text
                }
                mLogCmdTable.setValueAt(text, mLogCmdTable.selectedRow, 1)
                dispose()
            } else if (e?.source == mCancelBtn) {
                dispose()
            }
        }

        override fun focusGained(e: FocusEvent?) {
            if (e?.source == mAdbTF) {
                mAdbRadio.isSelected = true
            }
            else if (e?.source == mCmdTF) {
                mCmdRadio.isSelected = true
            }
        }

        override fun focusLost(e: FocusEvent?) {

        }
    }
}
