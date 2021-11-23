package com.blogspot.kotlinstudy.lognote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.plaf.basic.BasicScrollBarUI


class HelpDialog(parent: JFrame) :JDialog(parent, Strings.HELP, true), ActionListener {
    private var mHelpTextPane: JTextPane
    private var mCloseBtn : ColorButton

    init {
        mCloseBtn = ColorButton(Strings.CLOSE)
        mCloseBtn.addActionListener(this)

        mHelpTextPane = JTextPane()
        mHelpTextPane.contentType = "text/html"

        if (Strings.lang == Strings.KO) {
            mHelpTextPane.text = HelpText.textKo
        }
        else {
            mHelpTextPane.text = HelpText.textEn
        }

        mHelpTextPane.caretPosition = 0
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
//        val text =
//            """
//            <html>
//            <body>
//            <center><font size=7>Android logcat viewer</font><br>
//            <font size=5>==================================================================================</font></center>
//            <font size=5>File > Open</font><br>
//            <font size=4>Open logfile</font><br>
//            <font size=4>or Drag and drop</font><br>
//            <font size=5>View > Full logs</font><br>
//            <font size=4>Show/Hide full log view</font><br>
//            <font size=5>Setting > Adb</font><br>
//            <font size=4>Adb path : installed adb path</font><br>
//            <font size=4>Log path : path to save logcat log</font><br>
//            <font size=4>Prefix : prefix of save logcat log, default - connected device(ip or name)</font><br>
//            <font size=5>Setting > Font</font><br>
//            <font size=4>select font for log</font><br>
//            <font size=5>Setting > Filter-incremental</font><br>
//            <font size=4>if check, Immediate view update when filter text is changed</font><br>
//            <font size=4>if uncheck, view update when "enter" key</font><br>
//            <font size=5>Setting > log level</font><br>
//            <font size=4>set minimum log level</font><br>
//            <font size=5>Help > help</font><br>
//            <font size=4>Show this dialog</font><br>
//            <font size=5>Help > about</font><br>
//            <font size=4>about me</font><br>
//            <br>
//            <font size=5>adb connect and show log</font><br>
//            <font size=4>1. enter ip address to combobox or select item of combobox, and "enter" key</font><br>
//            <font size=4>2. enter ip address to combobox or select item of combobox, and click "Start" button</font><br>
//            <font size=5>adb reconnect and show log</font><br>
//            <font size=4>1. set focus to address combobox, and "enter" key</font><br>
//            <font size=4>2. "Ctrl-r" key</font><br>
//            <font size=5>Scrollback setting</font><br>
//            <font size=4>set max scrollback to textfield and "apply" button(0 : unlimited, but use many system resource</font><br>
//            <font size=4>set "split file", If the saved line is greater than the scrollback textfield value, create a new file</font><br>
//            <font size=5>Rotation</font><br>
//            <font size=4>"View" position rotates clockwise</font><br>
//            <br>
//            <font size=5>Filter(Range - Log : all log text, Tag : tag, PID : pid, TID : tid)</font><br>
//            <font size=4>https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html</font><br>
//            <font size=4>if set multiple filter(ex:Log, Tag), filter work with "AND"</font><br>
//            <font size=4>if start with "-", it works exclude(ex:"-aaa" : exclude aaa)</font><br>
//            <font size=4>Bold : only change to Bold text, without filtering</font><br>
//            <br>
//            <font size=5>"∧""∨"</font><br>
//            <font size=4>goto first, goto last</font><br>
//            <font size=5>PID, TID, Tag button</font><br>
//            <font size=4>set bold</font><br>
//            <font size=5>Windowed Mode</font><br>
//            <font size=4>create new window for full log</font><br>
//            <font size=5>Bookmarks</font><br>
//            <font size=4>set bootmark : double click line number</font><br>
//            <font size=4>"Bookmarks" button : show only bookmark</font><br>
//            <font size=5>show text</font><br>
//            <font size=4>duble click log</font><br>
//            <br>
//            <font size=5>Shortcut</font><br>
//            <font size=4>Ctrl-r : reconnect adb</font><br>
//            <font size=4>Ctrl-g : goto line</font><br>
//            <font size=4>Ctrl-b : toggle bookmark</font><br>
//            <font size=5></font><br>
//            <font size=4></font><br>
//            </body>
//            </html>
//        """.trimIndent()

        val textEn =
            """
            <html>
            <body>
            <center><font size=7>Android logcat viewer</font><br>
            <font size=5>==================================================================================</font></center>
            <pre>
            File > Open
                    Open logfile
                    or Drag and drop
            View > Full logs
                    Show/Hide full log view
            Setting > Adb
                    Adb path : installed adb path
                    Log path : path to save logcat log
                    Prefix : prefix of save logcat log, default - connected device(ip or name)
            Setting > Font
                    select font for log
            Setting > Filter-incremental
                    if check, Immediate view update when filter text is changed
                    if uncheck, view update when "enter" key
            Setting > log level
                    set minimum log level
            Help > help
                    Show this dialog
            Help > about
                    about me
            <br>            
            adb connect and show log
                    1. enter ip address to combobox or select item of combobox, and "enter" key
                    2. enter ip address to combobox or select item of combobox, and click "Start" button
            adb reconnect and show log
                    1. set focus to address combobox, and "enter" key
                    2. "Ctrl-r" key
            Scrollback setting
                    set max scrollback to textfield and "apply" button(0 : unlimited, but use many system resource
                    set "split file", If the saved line is greater than the scrollback textfield value, create a new file
            Rotation
                    "View" position rotates clockwise
            <br>            
            Filter(Range - Log : all log text, Tag : tag, PID : pid, TID : tid)
                    https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
                    if set multiple filter(ex:Log, Tag), filter work with "AND"
                    if start with "-", it works exclude(ex:"-aaa" : exclude aaa)
                    Bold : only change to Bold text, without filtering
            <br>            
            "∧""∨"
                    goto first, goto last
            PID, TID, Tag button
                    set bold
            Windowed Mode
                    create new window for full log
            Bookmarks
                    set bootmark : double click line number
                    "Bookmarks" button : show only bookmark
            show text
                    duble click log
            <br>            
            Shortcut
                    Ctrl-r : reconnect adb
                    Ctrl-g : goto line
                    Ctrl-b : toggle bookmark

            </pre>
            </body>
            </html>
        """.trimIndent()

        val textKo =
            """
            <html>
            <body>
            <center><font size=7>Android logcat viewer</font><br>
            <font size=5>==================================================================================</font></center>
            <pre>
            파일 > 열기
                    로그 파일 열기
                    또는 드래그 앤 드롭
            보기 > 전체 로그
                    전체 로그 보기 표시/숨기기
            설정 > ADB
                    Adb 경로 : 설치된 adb 경로
                    로그 경로 : logcat 로그를 저장할 경로
                    접두사 : 저장 logcat 로그의 접두사, 기본값 - 연결된 장치(ip 또는 이름)
            설정 > 글꼴
                    로그 글꼴 선택
            설정 > 필터 증분
                    체크하면 필터 텍스트 변경 시 바로 보기 업데이트
                    선택을 취소하면 "Enter" 키가 표시될 때 업데이트
            설정 > 로그 레벨
                    최소 로그 수준 설정
            도움말 > 도움말
                    이 대화 상자 표시
            도움말 > 정보
            <br>
            adb 연결 및 로그 표시
                    1. 콤보박스에 ip 주소를 입력하거나 콤보박스 항목을 선택하고 "Enter" 키 입력
                    2. 콤보박스에 ip 주소를 입력하거나 콤보박스 항목을 선택하고 "시작" 버튼을 클릭
            adb 재연결 및 로그 표시
                    1. 주소 콤보 상자에 포커스를 설정하고 "Enter" 키 입력
                    2. "Ctrl-r" 키
            스크롤백 설정
                    최대 스크롤백을 텍스트 필드에 설정하고 "적용" 버튼(0 : 무제한이지만 많은 시스템 리소스를 사용함)
                    "split file" 설정, 저장된 줄이 스크롤백 텍스트 필드 값보다 크면 새 파일 생성
            회전
                    "View" 위치가 시계 방향으로 회전
            <br>
            Filter(범위 - Log : 모든 로그 텍스트, Tag : tag, PID : pid, TID : tid)
                    https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
                    여러 개의 필터(예:로그, 태그)를 설정하는 경우 필터는 "AND"로 작동합니다.
                    "-"로 시작하면 제외(ex:"-aaa" : exclude aaa)가 작동합니다.
                    Bold : 필터링 없이 Bold 텍스트로만 변경
            <br>
            "∧""∨"
                    첫라인, 마지막 라인으로 이동
            PID, TID, 태그 버튼
                    진하게 보기
            창 모드
                    로그에 대한 새 창 만들기
            책갈피
                    북마크 설정 : 줄 번호 더블 클릭
                    "북마크" 버튼 : 북마크만 표시
            텍스트 표시
                    로그 더블 클릭 : 팝업으로 로그 보기
            <br>
            단축키
                    Ctrl-r : adb 다시 연결
                    Ctrl-g : 줄로 이동
                    Ctrl-b : 북마크 설정
                    
            </pre>
            </body>
            </html>
        """.trimIndent()
    }
}