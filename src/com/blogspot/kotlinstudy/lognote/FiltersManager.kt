package com.blogspot.cdcsutils.lognote

import java.awt.event.*
import javax.swing.JList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class FiltersManager (mainUI: MainUI, logPanel: LogPanel): CustomListManager (mainUI, logPanel){
    private val mConfigManager = ConfigManager.getInstance()
    private val CURRENT_FILTER = "Current"

    private val mListSelectionHandler = ListSelectionHandler()
    private val mMouseHandler = MouseHandler()
    private val mKeyHandler = KeyHandler()

    init {
        mDialogTitle = "Filters Manager"
    }
    companion object {
        const val MAX_FILTERS = 20
    }

    override fun loadList(): ArrayList<CustomElement> {
        return mConfigManager.loadFilters()
    }

    override fun saveList(list: ArrayList<CustomElement>) {
        mConfigManager.saveFilters(list)
    }

    override fun getFirstElement(): CustomElement {
        return CustomElement(CURRENT_FILTER, mMainUI.getTextShowLogCombo(), false)
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

    internal inner class ListSelectionHandler : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            if (p0?.valueIsAdjusting == false) {
//                val list = p0?.source as JList<CustomElement>
//                val selection = list.selectedValue
//                mMainUI.setTextShowLogCombo(selection.mValue)
//                mMainUI.applyShowLogCombo()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal inner class MouseHandler: MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
            if (p0?.clickCount == 2) {
                val list = p0.source as JList<CustomElement>
                val selection = list.selectedValue
                if ((ActionEvent.CTRL_MASK and p0.modifiers) != 0) {
                    val filterText = mMainUI.getTextShowLogCombo()
                    if (filterText.isEmpty()) {
                        mMainUI.setTextShowLogCombo(selection.mValue)
                    }
                    else {
                        if (filterText.substring(filterText.length - 1) == "|") {
                            mMainUI.setTextShowLogCombo(filterText + selection.mValue)
                        }
                        else {
                            mMainUI.setTextShowLogCombo(filterText + "|" + selection.mValue)
                        }
                    }
                }
                else {
                    mMainUI.setTextShowLogCombo(selection.mValue)
                }
                mMainUI.applyShowLogCombo(false)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal inner class KeyHandler: KeyAdapter() {
        override fun keyPressed(p0: KeyEvent?) {
            if (p0?.keyCode == KeyEvent.VK_ENTER) {
                val list = p0.source as JList<CustomElement>
                val selection = list.selectedValue
                if ((ActionEvent.CTRL_MASK and p0.modifiers) != 0) {
                    val filterText = mMainUI.getTextShowLogCombo()
                    if (filterText.isEmpty()) {
                        mMainUI.setTextShowLogCombo(selection.mValue)
                    }
                    else {
                        if (filterText.substring(filterText.length - 1) == "|") {
                            mMainUI.setTextShowLogCombo(filterText + selection.mValue)
                        }
                        else {
                            mMainUI.setTextShowLogCombo(filterText + "|" + selection.mValue)
                        }
                    }
                }
                else {
                    mMainUI.setTextShowLogCombo(selection.mValue)
                }
                mMainUI.applyShowLogCombo(false)
            }
        }
    }

}
