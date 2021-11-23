package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class FiltersManager (mainUI: MainUI, configManager: MainUI.ConfigManager){
    companion object {
        const val MAX_FILTERS = 20
        const val CMD_NEW = 1
        const val CMD_COPY = 2
        const val CMD_EDIT = 3
    }

    private val mMainUI = mainUI
    private val mConfigManager = configManager

    fun showFiltersDialog() {
        val filtersDialog = FiltersDialog(mMainUI)
        filtersDialog.setLocationRelativeTo(mMainUI)
        filtersDialog.isVisible = true
    }

    class FilterElement(title : String, filter : String) {
        var mTitle = title
        var mFilter = filter
    }

    internal inner class FiltersDialog (parent: MainUI) : JDialog(parent, Strings.FILTERS, true), ActionListener {
        private var mFilterScrollPane: JScrollPane
        private var mFilterList = JList<FilterElement>()
        private var mNewBtn: ColorButton
        private var mCopyBtn: ColorButton
        private var mEditBtn: ColorButton
        private var mDeleteBtn: ColorButton
        private var mSaveBtn: ColorButton
        private var mCloseBtn: ColorButton
        private var mParent = parent
        private val CURRENT = "Current"
        private var mSelectionListener = ListSelectionHandler()
        private var mFilterModel = DefaultListModel<FilterElement>()


        init {
            val filtersArray = mConfigManager.loadFilters()
            mFilterModel.clear()
            if (mParent.getTextShowLogCombo().isNotEmpty()) {
                mFilterModel.addElement(FilterElement(CURRENT, mParent.getTextShowLogCombo()))
            }
            for (item in filtersArray) {
                mFilterModel.addElement(item)
            }
            mFilterList = JList<FilterElement>()
            mFilterList.model = mFilterModel
            mFilterList.addListSelectionListener(mSelectionListener)

            mFilterList.cellRenderer = FilterCellRenderer()

            val componentListener: ComponentListener = object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    mFilterList.fixedCellHeight = 10
                    mFilterList.fixedCellHeight = -1
                }
            }

            mFilterList.addComponentListener(componentListener)
            mFilterScrollPane = JScrollPane(mFilterList)
            mFilterScrollPane.preferredSize = Dimension(800, 500)

            mNewBtn = ColorButton(Strings.NEW)
            mNewBtn.addActionListener(this)
            mCopyBtn = ColorButton(Strings.COPY)
            mCopyBtn.addActionListener(this)
            mEditBtn = ColorButton(Strings.EDIT)
            mEditBtn.addActionListener(this)
            mDeleteBtn = ColorButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mSaveBtn = ColorButton(Strings.SAVE)
            mSaveBtn.addActionListener(this)
            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)
            val bottomPanel = JPanel()
            bottomPanel.add(mNewBtn)
            bottomPanel.add(mCopyBtn)
            bottomPanel.add(mEditBtn)
            bottomPanel.add(mDeleteBtn)
            bottomPanel.add(mSaveBtn)
            bottomPanel.add(mCloseBtn)

            contentPane.layout = BorderLayout()
            contentPane.add(mFilterScrollPane, BorderLayout.CENTER)
            contentPane.add(bottomPanel, BorderLayout.SOUTH)

            pack()
        }

        inner class FilterCellRenderer : ListCellRenderer<Any?> {
            private val cellPanel = JPanel()
            private val titlePanel: JPanel
            private val titleLabel: JLabel
            private val filterText: JTextArea
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?, index: Int, isSelected: Boolean,
                hasFocus: Boolean
            ): Component {
                val element = value as FilterElement
                titleLabel.text = element.mTitle
                filterText.text = element.mFilter
                val width = list.width
                if (width > 0) {
                    filterText.setSize(width, Short.MAX_VALUE.toInt())
                }
                if (isSelected) {
                    titlePanel.background = Color.LIGHT_GRAY
                    filterText.background = Color.LIGHT_GRAY
                }
                else {
                    titlePanel.background = Color.WHITE
                    filterText.background = Color.WHITE
                }
                return cellPanel
            }

            init {
                cellPanel.layout = BorderLayout()

                titlePanel = JPanel(BorderLayout())
                titleLabel = JLabel("")
                titleLabel.foreground = Color.BLUE
                titlePanel.add(titleLabel, BorderLayout.NORTH)
                cellPanel.add(titlePanel, BorderLayout.NORTH)

                filterText = JTextArea()
                filterText.lineWrap = true
                filterText.wrapStyleWord = true
                cellPanel.add(filterText, BorderLayout.CENTER)
            }
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mNewBtn) {
                val editDialog = FilterEditDialog(this, CMD_NEW, "", "")
                editDialog.setLocationRelativeTo(this)
                editDialog.isVisible = true
            } else if (e?.source == mCopyBtn) {
                if (mFilterList.selectedIndex >= 0) {
                    val selection = mFilterList.selectedValue
                    val editDialog = FilterEditDialog(this, CMD_COPY, selection.mTitle, selection.mFilter)
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true
                }
            } else if (e?.source == mEditBtn) {
                if (mFilterList.selectedIndex >= 0) {
                    val selection = mFilterList.selectedValue
                    val editDialog = FilterEditDialog(this, CMD_EDIT, selection.mTitle, selection.mFilter)
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true
                }
            } else if (e?.source == mDeleteBtn) {
                if (mFilterList.selectedIndex >= 0) {
                    mFilterList.removeListSelectionListener(mSelectionListener)
                    mFilterModel.remove(mFilterList.selectedIndex)
                    mFilterList.addListSelectionListener(mSelectionListener)
                }
            } else if (e?.source == mSaveBtn) {
                val filtersArray = ArrayList<FilterElement>()
                for (item in mFilterModel.elements()) {
                    if (item.mTitle != CURRENT) {
                        filtersArray.add(item)
                    }
                }

                if (filtersArray.size > 0) {
                    mConfigManager.saveFilters(filtersArray)
                }
            } else if (e?.source == mCloseBtn) {
                dispose()
            }
        }

        private fun updateFilter(cmd: Int, prevTitle: String, filterElement: FiltersManager.FilterElement) {
            if (cmd == CMD_EDIT) {
                for (item in mFilterModel.elements()) {
                    if (item.mTitle == title) {
                        item.mFilter = filterElement.mFilter
                        return
                    }
                }
                mFilterList.removeListSelectionListener(mSelectionListener)
                val selectedIdx = mFilterList.selectedIndex
                mFilterModel.remove(selectedIdx)
                mFilterModel.add(selectedIdx, filterElement)
                mFilterList.addListSelectionListener(mSelectionListener)
            }
            else {
                mFilterModel.addElement(filterElement)
            }
        }

        internal inner class ListSelectionHandler : ListSelectionListener {
            override fun valueChanged(p0: ListSelectionEvent?) {
                if (p0?.source == mFilterList) {
                    val selection = mFilterList.selectedValue

                    mParent.setTextShowLogCombo(selection.mFilter)
                    mParent.applyShowLogCombo()
                }
            }
        }

        internal inner class FilterEditDialog(parent: FiltersDialog, cmd: Int, title: String, filter: String) :JDialog(parent, "Filter Edit", true), ActionListener {
            private var mOkBtn: ColorButton
            private var mCancelBtn: ColorButton

            private var mTitleLabel: JLabel
            private var mFilterLabel: JLabel

            private var mTitleTF: JTextField
            private var mFilterTF: JTextField

            private var mTitleStatusLabel: JLabel
            private var mFilterStatusLabel: JLabel

            private var mParent = parent
            private var mPrevTitle = title
            private var mCmd = cmd
            private var mDocumentHandler = DocumentHandler()

            init {
                mOkBtn = ColorButton(Strings.OK)
                mOkBtn.addActionListener(this)
                mCancelBtn = ColorButton(Strings.CANCEL)
                mCancelBtn.addActionListener(this)

                mTitleLabel = JLabel("Title")
                mFilterLabel = JLabel("Filter")

                mTitleTF = JTextField(title)
                mTitleTF.document.addDocumentListener(mDocumentHandler)
                mTitleTF.preferredSize = Dimension(488, 30)
                mFilterTF = JTextField(filter)
                mFilterTF.document.addDocumentListener(mDocumentHandler)
                mFilterTF.preferredSize = Dimension(488, 30)

                mTitleStatusLabel = JLabel("Good")
                mTitleStatusLabel.foreground = Color.BLUE
                mTitleStatusLabel.border = BorderFactory.createLineBorder(Color.DARK_GRAY)
                mTitleStatusLabel.preferredSize =  Dimension(200, 30)
                mFilterStatusLabel = JLabel("Good")
                mFilterStatusLabel.foreground = Color.BLUE
                mFilterStatusLabel.border = BorderFactory.createLineBorder(Color.DARK_GRAY)
                mFilterStatusLabel.preferredSize =  Dimension(200, 30)

                val titlePanel = JPanel()
                titlePanel.add(mTitleLabel)
                titlePanel.add(mTitleTF)
                titlePanel.add(mTitleStatusLabel)

                val filterPanel = JPanel()
                filterPanel.add(mFilterLabel)
                filterPanel.add(mFilterTF)
                filterPanel.add(mFilterStatusLabel)

                val confirmPanel = JPanel()
                confirmPanel.preferredSize = Dimension(400, 30)
                confirmPanel.add(mOkBtn)
                confirmPanel.add(mCancelBtn)

                val panel = JPanel(GridLayout(3, 1))
                panel.add(titlePanel)
                panel.add(filterPanel)
                panel.add(confirmPanel)

                contentPane.add(panel)
                pack()

                var isValid = true
                if (mTitleTF.text.trim().isEmpty()) {
                    mTitleStatusLabel.foreground = Color.RED
                    mTitleStatusLabel.text = "Empty"
                    isValid = false
                }
                else if (mTitleTF.text.trim() == CURRENT) {
                    mTitleStatusLabel.foreground = Color.RED
                    mTitleStatusLabel.text = "Not allow : $CURRENT"
                    isValid = false
                }
                else if (cmd == CMD_COPY) {
                    mTitleStatusLabel.foreground = Color.RED
                    mTitleStatusLabel.text = "Copy : duplicated"
                    isValid = false
                }

                if (mFilterTF.text.trim().isEmpty()) {
                    mFilterStatusLabel.foreground = Color.RED
                    mFilterStatusLabel.text = "Empty"
                    isValid = false
                }

                mOkBtn.isEnabled = isValid
            }

            override fun actionPerformed(e: ActionEvent?) {
                if (e?.source == mOkBtn) {
                    mParent.updateFilter(mCmd, mPrevTitle, FilterElement(mTitleTF.text, mFilterTF.text))
                    dispose()
                } else if (e?.source == mCancelBtn) {
                    dispose()
                }
            }

            internal inner class DocumentHandler: DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    checkText(e)
                }
                fun checkText(e: DocumentEvent?) {
                    var isValid = true
                    if (e?.document == mTitleTF.document) {
                        val title = mTitleTF.text.trim()
                        if (title.isEmpty()) {
                            mTitleStatusLabel.foreground = Color.RED
                            mTitleStatusLabel.text = "Empty"
                            isValid = false
                        }
                        else if (title == CURRENT) {
                            mTitleStatusLabel.foreground = Color.RED
                            mTitleStatusLabel.text = "Not allow : $CURRENT"
                            isValid = false
                        }
                        else {
                            for (item in mFilterModel.elements()) {
                                if (item.mTitle == title) {
                                    if (mCmd != CMD_EDIT || (mCmd == CMD_EDIT && mPrevTitle != title)) {
                                        mTitleStatusLabel.foreground = Color.RED
                                        mTitleStatusLabel.text = "Duplicated"
                                        isValid = false
                                    }
                                    break
                                }
                            }
                        }
                    }

                    if (e?.document == mFilterTF.document) {
                        val filter = mFilterTF.text.trim()
                        if (filter.isEmpty()) {
                            mFilterStatusLabel.foreground = Color.RED
                            mFilterStatusLabel.text = "Empty"
                            isValid = false
                        }
                    }

                    if (isValid) {
                        mTitleStatusLabel.foreground = Color.BLUE
                        mTitleStatusLabel.text = "Good"
                        mFilterStatusLabel.foreground = Color.BLUE
                        mFilterStatusLabel.text = "Good"
                    }
                    mOkBtn.isEnabled = isValid
                }
            }
        }
    }
}
