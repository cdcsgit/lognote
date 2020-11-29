package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.plaf.basic.BasicScrollBarUI


class HelpDialog(parent: JFrame) :JDialog(parent, "Help", true), ActionListener {
    private var mHelpTextPane: JTextPane
    private var mCloseBtn : ColorButton

    init {
        mCloseBtn = ColorButton("Close")
        mCloseBtn.addActionListener(this)

        mHelpTextPane = JTextPane()
        mHelpTextPane.contentType = "text/html"
        mHelpTextPane.text = HelpText.text

        val scrollPane = JScrollPane(mHelpTextPane)
        val aboutPanel = JPanel()
        scrollPane.preferredSize = Dimension(850, 800)
        scrollPane.verticalScrollBar.ui = BasicScrollBarUI()
        scrollPane.horizontalScrollBar.ui = BasicScrollBarUI()
        aboutPanel.add(scrollPane)

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(aboutPanel, BorderLayout.CENTER)

        val btnPanel = JPanel()
        btnPanel.add(mCloseBtn)
        panel.add(btnPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mCloseBtn) {
            dispose()
        }
    }
}

private class HelpText() {
    companion object {
        val text =
            """
            <html>
            <body>
            <center><font size=7>Android logcat viewer</font><br>
            <font size=5>==================================================================================</font></center>
            <font size=5>File > Open</font><br>
            <font size=4>Open logfile</font><br>
            <font size=4>or Drag and drop</font><br>
            <font size=5>View > Full logs</font><br>
            <font size=4>Show/Hide full log view</font><br>
            <font size=5>Setting > Adb</font><br>
            <font size=4>Adb path : installed adb path</font><br>
            <font size=4>Log path : path to save logcat log</font><br>
            <font size=4>Prefix : prefix of save logcat log, default - connected device(ip or name)</font><br>
            <font size=5>Setting > Font</font><br>
            <font size=4>select font for log</font><br>
            <font size=5>Setting > Filter-incremental</font><br>
            <font size=4>if check, Immediate view update when filter text is changed</font><br>
            <font size=4>if uncheck, view update when "enter" key</font><br>
            <font size=5>Setting > log level</font><br>
            <font size=4>set minimum log level</font><br>
            <font size=5>Help > help</font><br>
            <font size=4>Show this dialog</font><br>
            <font size=5>Help > about</font><br>
            <font size=4>about me</font><br>
            <br>            
            <font size=5>adb connect and show log</font><br>
            <font size=4>1. enter ip address to combobox or select item of combobox, and "enter" key</font><br>
            <font size=4>2. enter ip address to combobox or select item of combobox, and click "Start" button</font><br>
            <font size=5>adb reconnect and show log</font><br>
            <font size=4>1. set focus to address combobox, and "enter" key</font><br>
            <font size=4>2. "Ctrl-r" key</font><br>
            <font size=5>Scrollback setting</font><br>
            <font size=4>set max scrollback to textfield and "apply" button(0 : unlimited, but use many system resource</font><br>
            <font size=4>set "split file", If the saved line is greater than the scrollback textfield value, create a new file</font><br>
            <font size=5>Rotation</font><br>
            <font size=4>"View" position rotates clockwise</font><br>
            <br>            
            <font size=5>Filter(Range - Log : all log text, Tag : tag, PID : pid, TID : tid)</font><br>
            <font size=4>https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html</font><br>
            <font size=4>if set multiple filter(ex:Log, Tag), filter work with "AND"</font><br>
            <font size=4>if start with "-", it works exclude(ex:"-aaa" : exclude aaa)</font><br>
            <font size=4>Bold : only change to Bold text, without filtering</font><br>
            <br>            
            <font size=5>"∧""∨"</font><br>
            <font size=4>goto first, goto last</font><br>
            <font size=5>PID, TID, Tag button</font><br>
            <font size=4>set bold</font><br>
            <font size=5>Windowed Mode</font><br>
            <font size=4>create new window for full log</font><br>
            <font size=5>Bookmarks</font><br>
            <font size=4>set bootmark : double click line number</font><br>
            <font size=4>"Bookmarks" button : show only bookmark</font><br>
            <font size=5>show text</font><br>
            <font size=4>duble click log</font><br>
            <br>            
            <font size=5>Shortcut</font><br>
            <font size=4>Ctrl-r : reconnect adb</font><br>
            <font size=4>Ctrl-g : goto line</font><br>
            <font size=4>Ctrl-b : toggle bookmark</font><br>
            <font size=5></font><br>
            <font size=4></font><br>
            </body>
            </html>
        """.trimIndent()
    }
}