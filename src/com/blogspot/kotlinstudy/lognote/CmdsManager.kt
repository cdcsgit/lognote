package com.blogspot.kotlinstudy.lognote

import java.awt.event.*
import java.io.IOException
import java.util.ArrayList
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class CmdsManager (mainUI: MainUI, configManager: MainUI.ConfigManager, logPanel: LogPanel): CustomListManager (mainUI, logPanel){
    private val mConfigManager = configManager

    companion object {
        const val MAX_CMDS = 20
    }

    init {
        mDialogTitle = "Cmds Manager"
    }

    override fun loadConfig(): ArrayList<CustomElement> {
        return mConfigManager.loadCmds()
    }

    override fun saveConfig(list: ArrayList<CustomElement>) {
        mConfigManager.saveCmds(list)
    }

    override fun getFirstElement(): CustomElement {
        return CustomElement("Example", "adb shell input keyevent POWER", false)
    }

    override fun getListSelectionListener(): ListSelectionListener? {
        return null
    }

    override fun getListMouseListener(): MouseListener? {
        return MouseHandler()
    }

    override fun getListKeyListener(): KeyListener? {
        return KeyHandler()
    }

    private fun runCmd(list: JList<CustomElement>) {
        val selection = list.selectedValue
        var cmd = selection.mValue
        if (cmd.startsWith("adb ")) {
            cmd = cmd.replaceFirst("adb ", "${AdbManager.getInstance().mAdbCmd} -s ${AdbManager.getInstance().mTargetDevice} ")
        } else if (cmd.startsWith("adb.exe ")) {
            cmd = cmd.replaceFirst("adb.exe ", "${AdbManager.getInstance().mAdbCmd} -s ${AdbManager.getInstance().mTargetDevice} ")
        }

        if (cmd.isNotEmpty()) {
            var ret = JOptionPane.showConfirmDialog(
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

    internal inner class MouseHandler: MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
            if (p0?.clickCount == 2) {
                val list = p0?.source as JList<CustomElement>
                runCmd(list)
            }
        }
    }

    internal inner class KeyHandler: KeyAdapter() {
        override fun keyPressed(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ENTER) {
                val list = p0?.source as JList<CustomElement>
                runCmd(list)
            }
        }
    }
}
