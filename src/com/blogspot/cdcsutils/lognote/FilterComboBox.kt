package com.blogspot.cdcsutils.lognote

import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.Highlighter
import javax.swing.text.JTextComponent


class FilterComboBox(mode: Mode, useColorTag: Boolean) : JComboBox<String>() {
    companion object {
        var IsFilterIncremental: () -> Boolean = { false }
    }

    enum class Mode(val value: Int) {
        SINGLE_LINE(0),
        SINGLE_LINE_HIGHLIGHT(1),
        MULTI_LINE(2),
        MULTI_LINE_HIGHLIGHT(3);

        companion object {
            fun fromInt(value: Int) = entries.first { it.value == value }
        }
    }

    private var mEditorComponent: JTextComponent
    var mEnabledTfTooltip = false
        set(value) {
            field = value
            if (value) {
                updateTooltip()
            }
        }
    private val mMode = mode
    val mUseColorTag = useColorTag
    var mErrorMsg: String = ""
        set(value) {
            field = value
            if (value.isEmpty()) {
                updateTooltip(false)
            }
            else {
                updateTooltip(true)
            }
        }

    private var mDialog: ColorTagDialog? = null

    init {
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
        mEditorComponent.toolTipText = toolTipText
        mEditorComponent.addKeyListener(KeyHandler())
        mEditorComponent.document.addDocumentListener(DocumentHandler())

        val inputMap = mEditorComponent.getInputMap(JComponent.WHEN_FOCUSED)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK), "none")

        val copyActionKey = inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK))
        if (copyActionKey != null) {
            mEditorComponent.actionMap.put(copyActionKey, CopyAction())
        } else {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "FilterComboCopy")
            mEditorComponent.actionMap.put("FilterComboCopy", CopyAction())
        }
    }

    inner class CopyAction : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val selectedText = mEditorComponent.selectedText
            val textToCopy: String

            if (!selectedText.isNullOrEmpty()) {
                textToCopy = selectedText
            } else {
                textToCopy = mEditorComponent.text
                mEditorComponent.selectAll()
            }

            if (!textToCopy.isNullOrEmpty()) {
                val stringSelection = StringSelection(textToCopy)
                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)
            }
            else {
                Utils.printlnLog("FilterComboBox : textToCopy is null or empty")
            }
        }
    }

    fun setEnabledFilter(enabled: Boolean) {
        isEnabled = enabled
        isVisible = !(!enabled && editor.item.toString().isEmpty())
    }

    var mApplyFilter: (String) -> Unit = { Utils.printlnLog("ApplyCallback is not set") }
    fun setApplyFilter(callback: (String) -> Unit) {
        mApplyFilter = callback
    }

    fun resetComboItem(item: String) {
        val idx = getItemIdx(item)
        if (idx >= 0) {
            if (idx == 0) {
                return
            }
            removeItem(item)
        }
        insertItemAt(item, 0)
        selectedIndex = 0
        return
    }

    fun setFilterText(text : String) {
        selectedItem = text
        updateTooltip()
    }

    fun applyFilterText(isCheck: Boolean) {
        val item = selectedItem?.toString() ?: return

        if (isCheck) {
            if (isEnabled) {
                resetComboItem(item)
                mApplyFilter(item)
            }
            else {
                Utils.printlnLog("applyFilterText : Show log toggle is not selected")
            }
        }
        else {
            resetComboItem(item)
            mApplyFilter(item)
        }
    }

    fun applyFilterTextEditor() {
        val editorCom = editor.editorComponent as JTextComponent
        val text = editorCom.text
        setFilterText(text)
        applyFilterText(false)
    }

    fun getItemIdx(item:String) : Int {
        var ret = -1
        for (idx in 0 until itemCount) {
            if (getItemAt(idx).toString() == item) {
                ret = idx
                break
            }
        }
        return ret
    }

    fun removeAllColorTags(){
        val textSplit = mEditorComponent.text.split("|")
        var prevPatternIdx = -1
        var result = ""

        for (item in textSplit) {
            if (prevPatternIdx != -1) {
                result += "|"
                result += item

                if (item.isEmpty() || item.substring(item.length - 1) != "\\") {
                    prevPatternIdx = -1
                }
                continue
            }

            if (item.isNotEmpty()) {
                if (item[0] != '-') {
                    if (result.isNotEmpty()) {
                        result += "|"
                    }

                    if (2 < item.length && item[0] == '#' && item[1].isDigit()) {
                        result += item.substring(2)
                    }
                    else {
                        result += item
                    }

                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 0
                    }
                } else {
                    if (result.isNotEmpty()) {
                        result += "|"
                    }

                    if (3 < item.length && item[1] == '#' && item[2].isDigit()) {
                        result += item.substring(3)
                    }
                    else {
                        result += item.substring(1)
                    }

                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 1
                    }
                }
            }
        }

        mEditorComponent.text = result

        when (mMode) {
            Mode.SINGLE_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterSingleLineEditor.HighlighterTextField
                editorComponent.setUpdateHighlighter(true)
            }
            Mode.MULTI_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterMultiLineEditor.HighlighterTextArea
                editorComponent.setUpdateHighlighter(true)
            }
            else -> {

            }
        }
        return
    }

    fun removeColorTag(){
        val text = mEditorComponent.selectedText
        if (text != null) {
            if (2 <= text.length && text[0] == '#' && text[1].isDigit()) {
                mEditorComponent.replaceSelection(text.substring(2))
            }
        }

        when (mMode) {
            Mode.SINGLE_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterSingleLineEditor.HighlighterTextField
                editorComponent.setUpdateHighlighter(true)
            }
            Mode.MULTI_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterMultiLineEditor.HighlighterTextArea
                editorComponent.setUpdateHighlighter(true)
            }
            else -> {

            }
        }
        return
    }

    fun addColorTag(tag: String) {
        val text = mEditorComponent.selectedText
        if (text != null) {
            if (2 <= text.length && text[0] == '#' && text[1].isDigit()) {
                mEditorComponent.replaceSelection(tag + text.substring(2))
            }
            else {
                mEditorComponent.replaceSelection(tag + text)
            }
        }

        when (mMode) {
            Mode.SINGLE_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterSingleLineEditor.HighlighterTextField
                editorComponent.setUpdateHighlighter(true)
            }
            Mode.MULTI_LINE_HIGHLIGHT -> {
                val editorComponent = mEditorComponent as HighlighterMultiLineEditor.HighlighterTextArea
                editorComponent.setUpdateHighlighter(true)
            }
            else -> {

            }
        }
        return
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
                    }

                    if (2 < item.length && item[0] == '#' && item[1].isDigit()) {
                        patterns[0] += item.substring(2)
                    }
                    else {
                        patterns[0] += item
                    }

                    if (item.substring(item.length - 1) == "\\") {
                        prevPatternIdx = 0
                    }
                } else {
                    if (patterns[1].isNotEmpty()) {
                        patterns[1] += "|"
                    }

                    if (3 < item.length && item[1] == '#' && item[2].isDigit()) {
                        patterns[1] += item.substring(3)
                    }
                    else {
                        patterns[1] += item.substring(1)
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
        updateTooltip(false)
    }

    private fun updateTooltip(isShow: Boolean) {
        if (!mEnabledTfTooltip) {
            return
        }

        if (mErrorMsg.isNotEmpty()) {
            var tooltip = "<html><b>"
            tooltip += if (MainUI.IsFlatLaf && !MainUI.IsFlatLightLaf) {
                "<font size=5 color=#C07070>$mErrorMsg</font>"
            } else {
                "<font size=5 color=#FF0000>$mErrorMsg</font>"
            }
            tooltip += "</b></html>"
            mEditorComponent.toolTipText = tooltip
        }
        else {
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
            if (MainUI.IsFlatLaf && !MainUI.IsFlatLightLaf) {
                tooltip += "<font>INCLUDE : </font>\"<font size=5 color=#7070C0>$includeStr</font>\"<br>"
                tooltip += "<font>EXCLUDE : </font>\"<font size=5 color=#C07070>$excludeStr</font>\"<br>"
            } else {
                tooltip += "<font>INCLUDE : </font>\"<font size=5 color=#0000FF>$includeStr</font>\"<br>"
                tooltip += "<font>EXCLUDE : </font>\"<font size=5 color=#FF0000>$excludeStr</font>\"<br>"
            }
            tooltip += "</html>"
            mEditorComponent.toolTipText = tooltip
        }

        if (isShow) {
            ToolTipManager.sharedInstance().mouseMoved(MouseEvent(mEditorComponent, 0, 0, 0, 0, 0, 0, false))
        }
    }

    internal inner class KeyHandler : KeyAdapter() {
        var mPressedChar: Char = 0.toChar()
        override fun keyPressed(e: KeyEvent) {
            super.keyPressed(e)
            mPressedChar = e.keyChar
            if (!isEnabled) {
                return
            }
        }

        override fun keyReleased(e: KeyEvent) {
            super.keyReleased(e)
            if (!isEnabled) {
                return
            }

            if (mUseColorTag) {
                if (mDialog == null) {
                    mDialog = ColorTagDialog(MainUI.getInstance())
                }
                if ('#' == mPressedChar) {
                    if (mEditorComponent.caretPosition == 1 || (mEditorComponent.caretPosition > 1 && mEditorComponent.text[mEditorComponent.caretPosition - 2] == '|')) {
                        mDialog!!.updateLocation()
                        mDialog!!.focusableWindowState = false
                        mDialog!!.isVisible = true
                    }
                } else {
                    if (mDialog!!.isVisible) {
                        mDialog!!.isVisible = false
                    }
                }
            }

            if (KeyEvent.VK_ENTER == e.keyCode) {
                val combo = this@FilterComboBox
                val item = combo.selectedItem?.toString()
                item?.let {
                    resetComboItem(it)
                    mApplyFilter(it)
                }
            } else if (IsFilterIncremental()) {
                mApplyFilter(mEditorComponent.text)
            }
        }
    }

    internal inner class DocumentHandler: DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            if (mEnabledTfTooltip && !isPopupVisible) {
                updateTooltip()
            }
        }

        override fun removeUpdate(e: DocumentEvent?) {
            if (mEnabledTfTooltip && !isPopupVisible) {
                updateTooltip()
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

    internal abstract inner class HighlighterEditor : ComboBoxEditor {
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
                            else if (mUseColorTag && text[startPos] == '#' && startPos < (endPos - 1) && text[startPos + 1].isDigit()) {
                                val color = Color.decode(mColorManager.mFilterTableColor.mStrFilteredBGs[text[startPos + 1].digitToInt()])
                                val painterColor: Highlighter.HighlightPainter = DefaultHighlighter.DefaultHighlightPainter(color)
                                textComponent.highlighter.addHighlight(startPos, startPos + 2, painterColor)
                                textComponent.highlighter.addHighlight(startPos + 2, endPos, painterInclude)
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

    internal inner class HighlighterSingleLineEditor : HighlighterEditor() {
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
            private var mFgColor: Color = foreground
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
                        if (mDialog != null && mDialog!!.isVisible) {
                            mDialog!!.isVisible = false
                        }
                    }
                })

                val colorEventListener = object: ColorManager.ColorEventListener{
                    override fun colorChanged(event: ColorManager.ColorEvent?) {
                        setUpdateHighlighter(true)
                        repaint()
                    }
                }

                mColorManager.addFilterStyleEventListener(colorEventListener)
                mColorManager.addColorEventListener(colorEventListener)
            }

            fun setUpdateHighlighter(mUpdateHighlighter: Boolean) {
                this.mUpdateHighlighter = mUpdateHighlighter
            }

            private var mEnableHighlighter = false
            private var mUpdateHighlighter = false
            override fun paint(g: Graphics?) {
                if (mErrorMsg.isNotEmpty()) {
                    foreground = if (MainUI.IsFlatLaf && !MainUI.IsFlatLightLaf) {
                        Color(0xC0, 0x70, 0x70)
                    } else {
                        Color(0xFF, 0x00, 0x00)
                    }
                }
                else {
                    foreground = mFgColor
                }
                
                if (mEnableHighlighter && mUpdateHighlighter) {
                    updateHighlighter(this)
                    mUpdateHighlighter = false
                }
                super.paint(g)
            }

            fun setEnableHighlighter(enable: Boolean) {
                mEnableHighlighter = enable
            }

            override fun getToolTipLocation(event: MouseEvent?): Point {
                return Point(0, height)
            }

            override fun updateUI() {
                foreground?.let { mFgColor = it }
                super.updateUI()
            }

        }
    }

    internal inner class HighlighterMultiLineEditor : HighlighterEditor() {
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

            private var mFgColor = foreground
            private val initialPreferredHeight = preferredSize.height
            private var mPreferredHeightGap = 0

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

                        mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height + mPreferredHeightGap)
                        mCombo.parent.revalidate()
                        mCombo.parent.repaint()
                    }
                })
                addFocusListener(object : FocusListener {
                    override fun focusGained(e: FocusEvent) {}
                    override fun focusLost(e: FocusEvent) {
                        setUpdateHighlighter(true)
                        if (mDialog != null && mDialog!!.isVisible) {
                            mDialog!!.isVisible = false
                        }
                    }
                })

                val colorEventListener = object: ColorManager.ColorEventListener{
                    override fun colorChanged(event: ColorManager.ColorEvent?) {
                        setUpdateHighlighter(true)
                        repaint()
                    }
                }

                mColorManager.addFilterStyleEventListener(colorEventListener)
                mColorManager.addColorEventListener(colorEventListener)
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
                if (mErrorMsg.isNotEmpty()) {
                    foreground = if (MainUI.IsFlatLaf && !MainUI.IsFlatLightLaf) {
                        Color(0xC0, 0x70, 0x70)
                    } else {
                        Color(0xFF, 0x00, 0x00)
                    }
                }
                else {
                    foreground = mFgColor
                }

                if (mEnableHighlighter && mUpdateHighlighter) {
                    updateHighlighter(this)
                    mUpdateHighlighter = false
                }
                super.paint(g)
            }

            override fun updateUI() {
                mFgColor = foreground
                super.updateUI()
            }

            fun addActionListener(l: ActionListener) {
                mActionListeners.add(l)
            }
            fun removeActionListener(l: ActionListener) {
                mActionListeners.remove(l)
            }

            fun setComboBox(filterComboBox: FilterComboBox) {
                mCombo = filterComboBox
                mPreferredHeightGap = mCombo.preferredSize.height - initialPreferredHeight
            }

            override fun setText(t: String?) {
                super.setText(t)
                if (t != null) {
                    mCombo.preferredSize = Dimension(mCombo.preferredSize.width, preferredSize.height + mPreferredHeightGap)
                }
            }

            override fun getToolTipLocation(event: MouseEvent?): Point {
                return Point(0, height)
            }
        }
    }

    internal inner class ColorTagDialog (mainUI: MainUI) : JDialog(mainUI, "", false) {
        private val mColorManager = ColorManager.getInstance()
        init {
            this.isUndecorated = true
            val tf = JTextField()
            tf.border = BorderFactory.createEmptyBorder()
            updateHighlighter(tf)
            add(tf)
            pack()
        }

        fun updateLocation() {
            val rect = this@FilterComboBox.mEditorComponent.modelToView(this@FilterComboBox.mEditorComponent.caretPosition)
            var x = rect.x
            val y = this@FilterComboBox.locationOnScreen.y + this@FilterComboBox.height
            if (x + width > this@FilterComboBox.width) {
                x = this@FilterComboBox.width - width
            }
            x += this@FilterComboBox.locationOnScreen.x
            mDialog!!.setLocation(x, y)
        }

        private fun updateHighlighter(textComponent: JTextComponent) {
            textComponent.text = "#0  #1  #2  #3  #4  #5  #6  #7  #8  #9  "
            val text = textComponent.text
            val separator = "#"
            try {
                textComponent.highlighter.removeAllHighlights()
                var currPos = 1
                while (currPos < text.length) {
                    val startPos = currPos - 1
                    var endPos = text.indexOf(separator, currPos)
                    if (endPos < 0) {
                        endPos = text.length
                    }
                    if (startPos in 0 until endPos) {
                        val color = Color.decode(mColorManager.mFilterTableColor.mStrFilteredBGs[text[startPos + 1].digitToInt()])
                        val painterColor: Highlighter.HighlightPainter = DefaultHighlighter.DefaultHighlightPainter(color)
                        textComponent.highlighter.addHighlight(startPos, endPos, painterColor)
                    }
                    currPos = endPos + 1
                }
            } catch (ex: BadLocationException) {
                ex.printStackTrace()
            }
        }
    }
}
