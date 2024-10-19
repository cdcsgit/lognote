package com.blogspot.cdcsutils.lognote

import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.*


class CheckUpdateDialog(parent: JFrame) :JDialog(parent, Strings.CHECK_UPDATE, true), ActionListener, MouseListener {
    private var mUpdateLabel: JLabel
    private var mGoUpdateBtn: JButton
    private var mCloseBtn : JButton
    private val RELEASE_ADDRESS = "https://github.com/cdcsgit/lognote/releases/latest"
    private var mLatestVersion = ""

    init {
        val url = URL("https://api.github.com/repos/cdcsgit/lognote/releases/latest")
        val input = url.openStream()
        val inputStreamReader = InputStreamReader(input)
        val bufferedReader = BufferedReader(inputStreamReader)
        val json = StringBuilder()
        var ch: Int
        while (bufferedReader.read().also { ch = it } != -1) {
            json.append(ch.toChar())
        }
        val tagPattern: Pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^,]*)\",")
        val tagMatcher: Matcher = tagPattern.matcher(json)

        if (tagMatcher.find()) {
            mLatestVersion = tagMatcher.group(1).trim()
        }

        mUpdateLabel = if (mLatestVersion.isEmpty()) {
            JLabel(Strings.FAILED_GET_VERSION_INFO)
        } else if (mLatestVersion == Main.VERSION) {
            JLabel("${Strings.USING_LATEST_VERSION} : ${Main.VERSION}")
        } else {
            JLabel("${Strings.AVAILABLE} : $mLatestVersion (${Strings.CURRENT} : ${Main.VERSION})")
        }
        mUpdateLabel.horizontalAlignment = JLabel.CENTER

        mCloseBtn = JButton(Strings.CLOSE)
        mCloseBtn.addActionListener(this)

        mGoUpdateBtn = JButton(RELEASE_ADDRESS)
        mGoUpdateBtn.addMouseListener(this)

        val panel = JPanel()
        panel.layout = BorderLayout()

        panel.add(mUpdateLabel, BorderLayout.NORTH)

        val goUpdatePanel = JPanel()
        goUpdatePanel.preferredSize = Dimension(350, 40)
        goUpdatePanel.add(mGoUpdateBtn)
        panel.add(goUpdatePanel, BorderLayout.CENTER)

        val btnPanel = JPanel()
        btnPanel.add(mCloseBtn)
        panel.add(btnPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()

        Utils.installKeyStrokeEscClosing(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mCloseBtn) {
            dispose()
        }
    }

    override fun mouseClicked(e: MouseEvent?) {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if ((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI(RELEASE_ADDRESS))
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}
