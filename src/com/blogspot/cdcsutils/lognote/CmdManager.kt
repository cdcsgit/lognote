package com.blogspot.cdcsutils.lognote

import java.awt.event.*
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class CmdManager(mainUI: MainUI, logPanel: LogPanel): CustomListManager (mainUI, logPanel){
    private val mConfigManager = ConfigManager.getInstance()
    private val mListSelectionHandler = ListSelectionHandler()
    private val mMouseHandler = MouseHandler()
    private val mKeyHandler = KeyHandler()

    companion object {
        const val MAX_CMD_COUNT = 20
    }

    init {
        mDialogTitle = "Cmd Manager"
    }

    override fun loadList(): ArrayList<CustomElement> {
        return mConfigManager.loadCmds()
    }

    override fun saveList(list: ArrayList<CustomElement>) {
        mConfigManager.saveCmds(list)
    }

    override fun getFirstElement(): CustomElement {
        return CustomElement("Example", "adb shell input keyevent POWER", false)
    }

    override fun getListSelectionListener(): ListSelectionListener {
        return mListSelectionHandler
    }

    override fun getListMouseListener(): MouseListener {
        return mMouseHandler
    }

    override fun getListKeyListener(): KeyListener {
        return mKeyHandler
    }

    private fun runCmd(list: JList<CustomElement>) {
        val selection = list.selectedValue
        var cmd = selection.mValue
        if (cmd.startsWith("adb ")) {
            cmd = cmd.replaceFirst("adb ", "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} ")
        } else if (cmd.startsWith("adb.exe ")) {
            cmd = cmd.replaceFirst("adb.exe ", "${LogCmdManager.getInstance().mAdbCmd} -s ${LogCmdManager.getInstance().mTargetDevice} ")
        }

        if (cmd.isNotEmpty()) {
            val ret = JOptionPane.showConfirmDialog(
                list,
                "Run : $cmd",
                "Run command",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            )
            if (ret == JOptionPane.OK_OPTION) {
                val runtime = Runtime.getRuntime()
                runtime.exec(cmd)
            }
        }
    }

    internal inner class ListSelectionHandler : ListSelectionListener {
        override fun valueChanged(e: ListSelectionEvent?) {
            println("Not implemented")
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal inner class MouseHandler: MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
            if (p0?.clickCount == 2) {
                val list = p0.source as JList<CustomElement>
                runCmd(list)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal inner class KeyHandler: KeyAdapter() {
        override fun keyPressed(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ENTER) {
                val list = p0.source as JList<CustomElement>
                runCmd(list)
            }
        }
    }
}
