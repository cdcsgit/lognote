package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.plaf.basic.BasicComboBoxUI
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.Highlighter
import javax.swing.text.JTextComponent


class FilterComboBox(mode: Mode) : JComboBox<String>() {
    enum class Mode(val value: Int) {
        SINGLE_LINE(0),
        SINGLE_LINE_HIGHLIGHT(1),
        MULTI_LINE(2),
        MULTI_LINE_HIGHLIGHT(3);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    private var mEditorComponent: JTextComponent
    var mEnabledTfTooltip = false
    private val mMode = mode

    init {
        if (ConfigManager.LaF == MainUI.CROSS_PLATFORM_LAF) {
            ui = BasicComboBoxUI()
            ui.installUI(this)
        }
        when (mMode) {
            Mode.SINGLE_LINE -> {
                editor = HighlighterSingleLineEditor()
                val editorComponent = editor.editorComponent as HighlighterSingleLineEditor.HighlighterTextField
                editorComponent.setEnableHighlighter(false)
                mEditorComponent = editorComponent
            }
            Mode.SINGLE_LINE_HIGHLIGHT -> {
                editor = HighlighterSingleLineEditor()
                val editorComponent = editor.editorComponent as HighlighterSingleLineEditor.HighlighterTextField
                editorComponent.setEnableHighlighter(true)
                mEditorComponent = editorComponent
            }
            Mode.MULTI_LINE -> {
                editor = HighlighterMultiLineEditor()
                val editorComponent = editor.editorComponent as HighlighterMultiLineEditor.HighlighterTextArea
                editorComponent.setComboBox(this)
                editorComponent.setEnableHighlighter(false)
                mEditorComponent = editorComponent
            }
            Mode.MULTI_LINE_HIGHLIGHT -> {
                editor = HighlighterMultiLineEditor()
                val editorComponent = editor.editorComponent as HighlighterMultiLineEditor.HighlighterTextArea
                editorComponent.setComboBox(this)
                editorComponent.setEnableHighlighter(true)
                mEditorComponent = editorComponent
            }
        }
        if (ConfigManager.LaF == MainUI.CROSS_PLATFORM_LAF) {
            mEditorComponent.border = BorderFactory.createLineBorder(Color.black)
        }
        mEditorComponent.toolTipText = toolTipText
        mEditorComponent.addKeyListener(KeyHandler())
        mEditorComponent.document.addDocumentListener(DocumentHandler())
    }

    fun setEnabledFilter(enabled: Boolean) {
        isEnabled = enabled
        isVisible = !(!enabled && editor.item.toString().isEmpty())
    }

    fun isExistItem(item:String) : Boolean {
        var isExist = false
        for (idx in 0 until itemCount) {
            if (getItemAt(idx).toString() == item) {
                isExist = true
                break
            }
        }
        return isExist
    }

    private fun parsePattern(pattern: String) : Array<String> {
        val patterns: Array<String> = Array(2) { "" }

        val patternSplit = pattern.split("|")
        var prevPatternIdx = -1

        for (item in patternSplit) {
            if (prevPatternIdx != -1) {
                patterns[prevPatternIdx] += "|"
                patterns[prevPatternIdx] += item

                if (item.isEmpty() || item.substring(item.length - 1) != "\\") {
                    prevPatternIdx = -1
                }
                continue
            }

            if (item.isNotEmpty()) {
                if (item[0] != '-') {
                    if (patterns[0].isNotEmpty()) {
                        patterns[0] += "|"
                        patterns[0] += item
                    } else {
                        patterns[0] = item
                    }
                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 0
                    }
                } else {
                    if (patterns[1].isNotEmpty()) {
                        patterns[1] += "|"
                        patterns[1] += item.substring(1)
                    } else {
                        patterns[1] = item.substring(1)
                    }
                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 1
                    }
                }
            }
        }

        return patterns
    }

    fun updateTooltip() {
        if (!mEnabledTfTooltip) {
            return
        }
        val patterns = parsePattern(mEditorComponent.text)
        var includeStr = patterns[0]
        var excludeStr = patterns[1]

        if (includeStr.isNotEmpty()) {
            includeStr = includeStr.replace("&#09", "&amp;#09")
            includeStr = includeStr.replace("\t", "&#09;")
            includeStr = includeStr.replace("&nbsp", "&amp;nbsp")
            includeStr = includeStr.replace(" ", "&nbsp;")
            includeStr = includeStr.replace("|", "<font color=#303030><b>|</b></font>")
        }

        if (excludeStr.isNotEmpty()) {
            excludeStr = excludeStr.replace("&#09", "&amp;#09")
            excludeStr = excludeStr.replace("\t", "&#09;")
            excludeStr = excludeStr.replace("&nbsp", "&amp;nbsp")
            excludeStr = excludeStr.replace(" ", "&nbsp;")
            excludeStr = excludeStr.replace("|", "<font color=#303030><b>|</b></font>")
        }

        var tooltip = "<html><b>$toolTipText</b><br>"
        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
            tooltip += "<font>INCLUDE : </font>\"<font size=5 color=#7070C0>$includeStr</font>\"<br>"
            tooltip += "<font>EXCLUDE : </font>\"<font size=5 color=#C07070>$excludeStr</font>\"<br>"
        }
        else {
            tooltip += "<font>INCLUDE : </font>\"<font size=5 color=#0000FF>$includeStr</font>\"<br>"
            tooltip += "<font>EXCLUDE : </font>\"<font size=5 color=#FF0000>$excludeStr</font>\"<br>"
        }
        tooltip += "</html>"
        mEditorComponent.toolTipText = tooltip
    }

    internal inner class KeyHandler : KeyAdapter() {
        override fun keyReleased(e: KeyEvent) {
            super.keyReleased(e)
        }
    }

    internal inner class DocumentHandler: DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            if (mEnabledTfTooltip && !isPopupVisible) {
                updateTooltip()
//                ToolTipManager.sharedInstance().mouseMoved(MouseEvent(mEditorComponent, 0, 0, 0, 0, preferredSize.height / 3, 0, false))
            }
        }

        override fun removeUpdate(e: DocumentEvent?) {
            if (mEnabledTfTooltip && !isPopupVisible) {
                updateTooltip()
//                ToolTipManager.sharedInstance().mouseMoved(MouseEvent(mEditorComponent, 0, 0, 0, 0, preferredSize.height / 3, 0, false))
            }
        }

        override fun changedUpdate(e: DocumentEvent?) {
        }

    }

    internal class ComboBoxRenderer : BasicComboBoxRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>, value: Any,
            index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
                if (-1 < index) {
                    list.toolTipText = list.selectedValue.toString()
                }
            } else {
                background = list.background
                foreground = list.foreground
            }
            font = list.font
            text = value.toString()
            return this
        }
    }

    internal abstract class HighlighterEditor : ComboBoxEditor {
        val mColorManager = ColorManager.getInstance()
        fun updateHighlighter(textComponent: JTextComponent) {
            if (textComponent.selectedText == null) {
                val painterInclude: Highlighter.HighlightPainter = DefaultHighlighter.DefaultHighlightPainter(mColorManager.mFilterStyleInclude)
                val painterExclude: Highlighter.HighlightPainter = DefaultHighlighter.DefaultHighlightPainter(mColorManager.mFilterStyleExclude)
                val painterSeparator: Highlighter.HighlightPainter = DefaultHighlighter.DefaultHighlightPainter(mColorManager.mFilterStyleSeparator)
                val text = textComponent.text
                val separator = "|"
                try {
                    textComponent.highlighter.removeAllHighlights()
                    var currPos = 0
                    while (currPos < text.length) {
                        val startPos = currPos
                        val separatorPos = text.indexOf(separator, currPos)
                        var endPos = separatorPos
                        if (separatorPos < 0) {
                            endPos = text.length
                        }
                        if (startPos in 0 until endPos) {
                            if (text[startPos] == '-') {
                                textComponent.highlighter.addHighlight(startPos, endPos, painterExclude)
                            }
                            else {
                                textComponent.highlighter.addHighlight(startPos, endPos, painterInclude)
                            }
                        }
                        if (separatorPos >= 0) {
                            textComponent.highlighter.addHighlight(separatorPos, separatorPos + 1, painterSeparator)
                        }
                        currPos = endPos + 1
                    }
                } catch (ex: BadLocationException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    internal class HighlighterSingleLineEditor : HighlighterEditor() {
        private val textEditor: HighlighterTextField = HighlighterTextField()
        override fun getEditorComponent(): Component {
            return textEditor
        }

        override fun setItem(item: Any?) {
            if (item is String) {
                textEditor.text = item
                textEditor.setUpdateHighlighter(true)
            } else {
                textEditor.text = null
            }
        }

        override fun getItem(): Any {
            return textEditor.text
        }

        override fun selectAll() {
            textEditor.selectAll()
        }

        override fun addActionListener(l: ActionListener) {
            textEditor.addActionListener(l)
        }
        override fun removeActionListener(l: ActionListener) {
            textEditor.removeActionListener(l)
        }

        internal inner class HighlighterTextField : JTextField() {
            init {
                (highlighter as DefaultHighlighter).drawsLayeredHighlights = false

                addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) {
                    }
                    override fun keyPressed(e: KeyEvent?) {
                        setUpdateHighlighter(true)
                    }
                    override fun keyReleased(e: KeyEvent?) {
                    }
                })
                addFocusListener(object : FocusListener {
                    override fun focusGained(e: FocusEvent) {}
                    override fun focusLost(e: FocusEvent) {
                        setUpdateHighlighter(true)
                    }
                })

                mColorManager.addFilterStyleEventListener(object: ColorManager.ColorEventListener{
                    override fun colorChanged(event: ColorManager.ColorEvent?) {
                        setUpdateHighlighter(true)
                        repaint()
                    }
                })
            }

            fun setUpdateHighlighter(mUpdateHighlighter: Boolean) {
                this.mUpdateHighlighter = mUpdateHighlighter
            }

            private var mEnableHighlighter = false
            private var mUpdateHighlighter = false
            override fun paint(g: Graphics?) {
                if (mEnableHighlighter && mUpdateHighlighter) {
                    updateHighlighter(this)
                    mUpdateHighlighter = false
                }
                super.paint(g)
            }

            fun setEnableHighlighter(enable: Boolean) {
                mEnableHighlighter = enable
            }
        }
    }

    internal class HighlighterMultiLineEditor : HighlighterEditor() {
        private val textEditor: HighlighterTextArea = HighlighterTextArea()
        override fun getEditorComponent(): Component {
            return textEditor
        }

        override fun setItem(item: Any?) {
            if (item is String) {
                textEditor.text = item
                textEditor.setUpdateHighlighter(true)
            } else {
                textEditor.text = null
            }
        }

        override fun getItem(): Any {
            return textEditor.text
        }

        override fun selectAll() {
            textEditor.selectAll()
        }

        override fun addActionListener(l: ActionListener) {
            textEditor.addActionListener(l)
        }
        override fun removeActionListener(l: ActionListener) {
            textEditor.removeActionListener(l)
        }

        inner class HighlighterTextArea : JTextArea() {
            private var mEnableHighlighter = false
            private lateinit var mCombo: FilterComboBox
            private var mUpdateHighlighter = false
            private var mPrevCaret = 0
            private val mActionListeners = ArrayList<ActionListener>()

            init {
                lineWrap = true

                (highlighter as DefaultHighlighter).drawsLayeredHighlights = false
                addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) {
                    }
                    override fun keyPressed(e: KeyEvent?) {
                        when (e?.keyCode) {
                            KeyEvent.VK_ENTER -> {
                                e.consume()
                            }
                            KeyEvent.VK_DOWN -> {
                                mPrevCaret = caretPosition
                                return
                            }
                            KeyEvent.VK_UP -> {
                                if (mCombo.isPopupVisible) {
                                    e.consume()
                                }
                                return
                            }
                            KeyEvent.VK_TAB -> {
                                if (e.modifiers > 0) {
                                    transferFocusBackward()
                                }
                                else {
                                    transferFocus()
                                }

                                e.consume()
                            }
                        }

                        setUpdateHighlighter(true)
                    }
                    override fun keyReleased(e: KeyEvent?) {
                        when (e?.keyCode) {
                            KeyEvent.VK_ENTER -> {
                                e.consume()
                                for (listener in mActionListeners) {
                                    listener.actionPerformed(ActionEvent(this, ActionEvent.ACTION_PERFORMED, text))
                                }
                            }
                            KeyEvent.VK_DOWN -> {
                                if (mPrevCaret == caretPosition) {
                                    e.consume()
                                    if (!mCombo.isPopupVisible) {
                                        mCombo.showPopup()
                                    }
                                    if (mCombo.selectedIndex < (mCombo.itemCount - 1)) {
                                        mCombo.selectedIndex++
                                    }
                                }
                                return
                            }
                            KeyEvent.VK_UP -> {
                                if (mCombo.isPopupVisible) {
                                    e.consume()
                                    if (mCombo.selectedIndex > 0) {
                                        mCombo.selectedIndex--
                                    }
                                }
                                return
                            }
                        }

                        if (ConfigManager.LaF == MainUI.CROSS_PLATFORM_LAF) {
                            mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height + 6)
                        }
                        else {
                            mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height)
                        }
                        mCombo.parent.revalidate()
                        mCombo.parent.repaint()
                    }
                })
                addFocusListener(object : FocusListener {
                    override fun focusGained(e: FocusEvent) {}
                    override fun focusLost(e: FocusEvent) {
                        setUpdateHighlighter(true)
                    }
                })

                mColorManager.addFilterStyleEventListener(object: ColorManager.ColorEventListener{
                    override fun colorChanged(event: ColorManager.ColorEvent?) {
                        setUpdateHighlighter(true)
                        repaint()
                    }
                })
            }

            fun setUpdateHighlighter(mUpdateHighlighter: Boolean) {
                this.mUpdateHighlighter = mUpdateHighlighter
            }

            fun setEnableHighlighter(enable: Boolean) {
                mEnableHighlighter = enable
                if (mEnableHighlighter) {
                    setUpdateHighlighter(true)
                    repaint()
                }
            }

            override fun paint(g: Graphics?) {
                if (mEnableHighlighter && mUpdateHighlighter) {
                    updateHighlighter(this)
                    mUpdateHighlighter = false
                }
                super.paint(g)
            }
            fun addActionListener(l: ActionListener) {
                mActionListeners.add(l)
            }
            fun removeActionListener(l: ActionListener) {
                mActionListeners.remove(l)
            }

            fun setComboBox(filterComboBox: FilterComboBox) {
                mCombo = filterComboBox
            }

            override fun setText(t: String?) {
                super.setText(t)
                if (t != null) {
                    if (ConfigManager.LaF == MainUI.CROSS_PLATFORM_LAF) {
                        mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height + 6)
                    }
                    else {
                        mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height)
                    }
                }
            }
        }
    }
}
