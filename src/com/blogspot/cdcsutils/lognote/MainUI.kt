package com.blogspot.cdcsutils.lognote

import com.formdev.flatlaf.*
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.event.*
import javax.swing.plaf.FontUIResource
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.text.JTextComponent
import kotlin.math.roundToInt
import kotlin.system.exitProcess

class MainUI private constructor() : JFrame(), FormatManager.FormatEventListener {
    companion object {
        private const val SPLIT_WEIGHT = 0.7

        private const val ROTATION_LEFT_RIGHT = 0
        const val ROTATION_TOP_BOTTOM = 1
        private const val ROTATION_RIGHT_LEFT = 2
        const val ROTATION_BOTTOM_TOP = 3
        private const val ROTATION_MAX = ROTATION_BOTTOM_TOP

        const val DEFAULT_FONT_NAME = "DialogInput"

        const val SYSTEM_LAF = "System"
        const val FLAT_LIGHT_LAF = "FlatLaf Light"
        const val FLAT_DARK_LAF = "FlatLaf Dark"
        const val FLAT_INTELLIJ_LAF = "FlatLaf Intellij"
        const val FLAT_DARCULA_LAF = "FlatLaf Darcula"
        const val FLAT_MACOS_LIGHT_LAF = "FlatLaf macOS Light"
        const val FLAT_MACOS_DARK_LAF = "FlatLaf macOS Dark"
        var IsFlatLaf = true
        var IsFlatLightLaf = true

        private const val LAF_ACCENT_DEFAULT = "#4B6EAF"
        private const val LAF_ACCENT_BLUE = "#0A84FF"
        private const val LAF_ACCENT_PURPLE = "#BF5AF2"
        private const val LAF_ACCENT_RED = "#FF453A"
        private const val LAF_ACCENT_ORANGE = "#FF9F0A"
        private const val LAF_ACCENT_YELLOW = "#FFCC00"
        private const val LAF_ACCENT_GREEN = "#32D74B"
        val LAF_ACCENT_COLORS = arrayOf(LAF_ACCENT_DEFAULT, LAF_ACCENT_BLUE, LAF_ACCENT_PURPLE, LAF_ACCENT_RED, LAF_ACCENT_ORANGE, LAF_ACCENT_YELLOW, LAF_ACCENT_GREEN)

        const val METHOD_NONE = 0
        const val METHOD_OPEN = 1
        const val METHOD_ADB = 2
        const val METHOD_CMD = 3
        const val METHOD_FOLLOW = 4
        var CurrentMethod = METHOD_NONE

        var IsCreatingUI = true

        private val mInstance: MainUI = MainUI()
        fun getInstance(): MainUI {
            return mInstance
        }
    }

    private val mConfigManager = ConfigManager.getInstance()
    init {
        val prop = mConfigManager.getItem(ConfigManager.ITEM_LANG)
        if (!prop.isNullOrEmpty()) {
            Strings.lang = prop.toInt()
        } else {
            Strings.lang = Strings.EN
        }
    }

    private lateinit var mMenuBar: JMenuBar
    private lateinit var mMenuFile: JMenu
    private lateinit var mItemFileOpen: JMenuItem
    private lateinit var mItemFileFollow: JMenuItem
    private lateinit var mItemFileOpenFiles: JMenuItem
    private lateinit var mItemFileAppendFiles: JMenuItem
    private lateinit var mItemFileOpenRecents: JMenu
    private lateinit var mItemFileSaveFull: JMenuItem
    private lateinit var mItemFileSaveFiltered: JMenuItem
    private var mFileSaveDir: String = ""
    private lateinit var mItemFileExit: JMenuItem
    private lateinit var mMenuView: JMenu
    private lateinit var mItemToolWindows: JMenu
    lateinit var mItemToolPanel: JCheckBoxMenuItem
    lateinit var mItemToolSelection: JCheckBoxMenuItem
    var mToolTestEnable = false
    lateinit var mItemToolTest: JCheckBoxMenuItem
    lateinit var mItemFull: JCheckBoxMenuItem
    lateinit var mItemFullLogToNewWindow: JCheckBoxMenuItem
    private lateinit var mItemColumnMode: JCheckBoxMenuItem
    private lateinit var mItemProcessName: JMenu
    private lateinit var mItemProcessNameNone: JRadioButtonMenuItem
    private lateinit var mItemProcessNameShow: JRadioButtonMenuItem
    private lateinit var mItemProcessNameColor: JRadioButtonMenuItem
    private lateinit var mItemFind: JCheckBoxMenuItem
    private lateinit var mItemTrigger: JCheckBoxMenuItem
    private lateinit var mItemRotation: JMenuItem
    private lateinit var mMenuSettings: JMenu
    private lateinit var mItemLogCmd: JMenuItem
    private lateinit var mItemLogFile: JMenuItem
    private lateinit var mItemLogFormat: JMenuItem
    private lateinit var mItemFilterIncremental: JCheckBoxMenuItem
    private lateinit var mItemFilterByFile: JCheckBoxMenuItem
    private lateinit var mItemColorTagRegex: JCheckBoxMenuItem
    private lateinit var mItemAppearance: JMenuItem
    private lateinit var mItemTool: JMenuItem
    private lateinit var mMenuHelp: JMenu
    private lateinit var mItemHelp: JMenuItem
    private lateinit var mItemCheckUpdate: JMenuItem
    private lateinit var mItemAbout: JMenuItem

    private lateinit var mFilterPanel: JPanel
    private lateinit var mFilterLeftPanel: JPanel

    private lateinit var mLogToolBar: ButtonPanel
    private lateinit var mStartBtn: ColorButton
    private lateinit var mRetryAdbToggle: ColorToggleButton
    private lateinit var mStopBtn: ColorButton
    private lateinit var mPauseToggle: ColorToggleButton
    private lateinit var mClearViewsBtn: ColorButton
    private lateinit var mSaveBtn: ColorButton
//    private lateinit var mRotationBtn: ColorButton
//    lateinit var mFiltersBtn: ColorButton
//    lateinit var mCmdsBtn: ColorButton
    internal lateinit var mFindPanel: FindPanel

    private lateinit var mLogPanel: JPanel
    private lateinit var mShowLogPanel: JPanel
    private lateinit var mMatchCaseToggle: FilterToggleButton
    private lateinit var mMatchCaseTogglePanel: JPanel
    lateinit var mShowLogCombo: FilterComboBox
    var mShowLogComboStyle: FilterComboBox.Mode
    private lateinit var mShowLogToggle: FilterToggleButton
    private lateinit var mShowLogTogglePanel: JPanel

    private lateinit var mBoldLogPanel: JPanel
    private lateinit var mBoldLogCombo: FilterComboBox
    var mBoldLogComboStyle: FilterComboBox.Mode
    private lateinit var mBoldLogToggle: FilterToggleButton
    private lateinit var mBoldLogTogglePanel: JPanel

    private lateinit var mTokenPanel: Array<JPanel>
    lateinit var mTokenCombo: Array<FilterComboBox>
    var mTokenComboStyle: Array<FilterComboBox.Mode>
    private lateinit var mTokenToggle: Array<FilterToggleButton>
    private lateinit var mTokenTogglePanel: Array<JPanel>

    private lateinit var mLogCmdCombo: ColorComboBox<String>

    private lateinit var mDeviceCombo: ColorComboBox<String>
    private lateinit var mAdbConnectBtn: ColorButton
    private lateinit var mAdbRefreshBtn: ColorButton
    private lateinit var mAdbDisconnectBtn: ColorButton

    private lateinit var mScrollbackLabel: JLabel
    private lateinit var mScrollbackTF: JTextField
    private lateinit var mScrollbackSplitFileToggle: ColorToggleButton
    private lateinit var mScrollbackApplyBtn: ColorButton
    private lateinit var mScrollbackKeepToggle: ColorToggleButton

    lateinit var mToolsPane: ToolsPane
    lateinit var mToolSplitPane: JSplitPane
    private var mToolSplitDividerLocation = -1
    private var mToolSplitLastDividerLocation = -1
    lateinit var mLogSplitPane: JSplitPane

    lateinit var mFilteredLogPanel: LogPanel
    lateinit var mFullLogPanel: LogPanel
    private var mSelectedLine = 0

    private lateinit var mStatusBar: JPanel
    private lateinit var mStatusMethod: JLabel
    private lateinit var mStatusReloadBtn: ColorButton
    private lateinit var mStatusTF: JTextField

    private lateinit var mFollowLabel: JLabel
    private lateinit var mStartFollowBtn: ColorButton
    private lateinit var mStopFollowBtn: ColorButton
    private lateinit var mPauseFollowToggle: ColorToggleButton

    private lateinit var mLogFormatCombo: ColorComboBox<String>
    lateinit var mLogLevelCombo: ColorComboBox<String>

    private val mFrameMouseListener = FrameMouseListener(this)
    private val mKeyHandler = KeyHandler()
    private val mItemHandler = ItemHandler()
    private val mActionHandler = ActionHandler()
    private val mPopupMenuHandler = PopupMenuHandler()
    private val mMouseHandler = MouseHandler()
    private val mComponentHandler = ComponentHandler()
    private val mStatusChangeListener = StatusChangeListener()

    private val mRecentFileManager = RecentFileManager.getInstance()
    private val mColorManager = ColorManager.getInstance()
    private val mBookmarkManager = BookmarkManager.getInstance()
    private val mFormatManager = FormatManager.getInstance()
    private val mAgingTestManager = AgingTestManager.getInstance()

    private val mLogCmdManager = LogCmdManager.getInstance()
    lateinit var mFiltersManager:FiltersManager
    lateinit var mCmdManager:CmdManager

    private var mFrameX = 0
    private var mFrameY = 0
    private var mFrameWidth = 1280
    private var mFrameHeight = 720
    private var mFrameExtendedState = Frame.MAXIMIZED_BOTH

    private var mRotationStatus = ROTATION_LEFT_RIGHT
    var mToolRotationStatus = ROTATION_BOTTOM_TOP

    private var mLogTableDialog: LogTableDialog? = null

    var mFont: Font = Font(DEFAULT_FONT_NAME, Font.PLAIN, 12)
        set(value) {
            field = value
            if (!IsCreatingUI) {
                mFilteredLogPanel.mFont = value
                mFullLogPanel.mFont = value
            }
        }

    var mUIFontPercent = 100
    private var mColumnMode = false
    private val mOpenFileList = mutableListOf<String>()

    init {
        loadConfigOnCreate()

        val laf = mConfigManager.getItem(ConfigManager.ITEM_LOOK_AND_FEEL)

        if (laf == null || laf == SYSTEM_LAF) {
            ConfigManager.LaF = FLAT_LIGHT_LAF
        }
        else {
            ConfigManager.LaF = laf
        }

        val lafAccentColor = mConfigManager.getItem(ConfigManager.ITEM_LAF_ACCENT_COLOR)

        if (lafAccentColor == null) {
            ConfigManager.LaFAccentColor = LAF_ACCENT_COLORS[0]
        }
        else {
            ConfigManager.LaFAccentColor = lafAccentColor
        }
        FlatLaf.setSystemColorGetter { name: String -> if (name == "accent") Color.decode(ConfigManager.LaFAccentColor) else null }

        setLaF()

        val cmd = mConfigManager.getItem(ConfigManager.ITEM_ADB_CMD)
        if (!cmd.isNullOrEmpty()) {
            mLogCmdManager.mAdbCmd = cmd
        } else {
            val os = System.getProperty("os.name")
            Utils.printlnLog("OS : $os")
            if (os.lowercase().contains("windows")) {
                mLogCmdManager.mAdbCmd = "adb.exe"
            } else {
                mLogCmdManager.mAdbCmd = "adb"
            }
        }
        mLogCmdManager.addEventListener(AdbHandler())
        val logSavePath = mConfigManager.getItem(ConfigManager.ITEM_ADB_LOG_SAVE_PATH)
        if (logSavePath.isNullOrEmpty()) {
            mLogCmdManager.mLogSavePath = "."
        } else {
            mLogCmdManager.mLogSavePath = logSavePath
        }

        val logCmd = mConfigManager.getItem(ConfigManager.ITEM_ADB_LOG_CMD)
        if (logCmd.isNullOrEmpty()) {
            mLogCmdManager.mLogCmd = LogCmdManager.DEFAULT_LOGCAT
        } else {
            mLogCmdManager.mLogCmd = logCmd
        }

        val prefix = mConfigManager.getItem(ConfigManager.ITEM_ADB_PREFIX)
        if (prefix.isNullOrEmpty()) {
            mLogCmdManager.mPrefix = LogCmdManager.DEFAULT_PREFIX
        } else {
            mLogCmdManager.mPrefix = prefix
        }

        val adbOption1 = mConfigManager.getItem(ConfigManager.ITEM_ADB_OPTION_1)
        if (adbOption1.isNullOrEmpty()) {
            ProcessList.UpdateTime = ProcessList.DEFAULT_UPDATE_TIME
        } else {
            ProcessList.UpdateTime = adbOption1.toInt()
        }

        var prop = mConfigManager.getItem(ConfigManager.ITEM_FRAME_X)
        if (!prop.isNullOrEmpty()) {
            mFrameX = prop.toInt()
        }
        prop = mConfigManager.getItem(ConfigManager.ITEM_FRAME_Y)
        if (!prop.isNullOrEmpty()) {
            mFrameY = prop.toInt()
        }
        prop = mConfigManager.getItem(ConfigManager.ITEM_FRAME_WIDTH)
        if (!prop.isNullOrEmpty()) {
            mFrameWidth = prop.toInt()
        }
        prop = mConfigManager.getItem(ConfigManager.ITEM_FRAME_HEIGHT)
        if (!prop.isNullOrEmpty()) {
            mFrameHeight = prop.toInt()
        }
        prop = mConfigManager.getItem(ConfigManager.ITEM_FRAME_EXTENDED_STATE)
        if (!prop.isNullOrEmpty()) {
            mFrameExtendedState = prop.toInt()
        }
        prop = mConfigManager.getItem(ConfigManager.ITEM_ROTATION)
        if (!prop.isNullOrEmpty()) {
            mRotationStatus = prop.toInt()
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_TOOL_ROTATION)
        if (!prop.isNullOrEmpty()) {
            mToolRotationStatus = prop.toInt()
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_SHOW_LOG_STYLE)
        mShowLogComboStyle = if (!prop.isNullOrEmpty()) {
            FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_BOLD_LOG_STYLE)
        mBoldLogComboStyle = if (!prop.isNullOrEmpty()) {
            FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT
        }

        mTokenComboStyle = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT }
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            prop = mConfigManager.getItem(ConfigManager.ITEM_TOKEN_COMBO_STYLE + idx)
            if (!prop.isNullOrEmpty()) {
                mTokenComboStyle[idx] = FilterComboBox.Mode.fromInt(prop.toInt())
            }
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_VIEW_COLUMN_MODE)
        if (!prop.isNullOrEmpty()) {
            mColumnMode = prop.toBoolean()
        } else {
            mColumnMode = false
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_VIEW_PROCESS_NAME)
        if (!prop.isNullOrEmpty()) {
            LogTableModel.TypeShowProcessName = try {
                    prop.toInt()
                } catch (ex: NumberFormatException) {
                    LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR
                }
        } else {
            LogTableModel.TypeShowProcessName = LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_TOOL_TEST_ENABLE)
        if (!prop.isNullOrEmpty()) {
            mToolTestEnable = prop.toBoolean()
        } else {
            mToolTestEnable = false
        }

        createUI()

        if (mLogCmdManager.getType() == LogCmdManager.TYPE_LOGCAT) {
            mLogCmdManager.getDevices()
        }
        mFormatManager.addFormatEventListener(this)
        mFormatManager.setCurrFormat(mFormatManager.mCurrFormat.mName)
    }

    private fun exit() {
        Utils.printlnLog("Exit Lognote")
        saveConfigOnDestroy()
        saveRecentFile()
        mFilteredLogPanel.mTableModel.stopScan()
        mFilteredLogPanel.mTableModel.stopFollow()
        mFullLogPanel.mTableModel.stopScan()
        mFullLogPanel.mTableModel.stopFollow()
        mLogCmdManager.stop()
        exitProcess(0)
    }

    private fun loadConfigOnCreate() {
        mConfigManager.loadConfig()
        mColorManager.mFullTableColor.getConfig()
        mColorManager.mFullTableColor.applyColor()
        mColorManager.mFilterTableColor.getConfig()
        mColorManager.mFilterTableColor.applyColor()
        mColorManager.getConfigFilterStyle()
        mConfigManager.saveConfig()
    }

    private fun saveFilterCombo(combo: FilterComboBox, keyPrefix: String, saveCount: Int, maxCount: Int) {
        val filterList = mutableListOf<String>()
        for (i in 0 until combo.itemCount) {
            if (filterList.size == saveCount) {
                break
            }
            val item = combo.getItemAt(i).toString()
            if (!filterList.contains(item)) {
                filterList.add(item)
            }
        }

        var item: String?
        for (i in 0 until maxCount) {
            item = mConfigManager.getItem(keyPrefix + i)
            if (item == null || filterList.size == maxCount) {
                break
            }

            if (!filterList.contains(item)) {
                filterList.add(item)
            }
        }

        for (i in 0 until filterList.size) {
            mConfigManager.setItem(keyPrefix + i, filterList[i])
        }

        for (i in filterList.size until maxCount) {
            mConfigManager.removeConfigItem(keyPrefix + i)
        }
    }

    private fun saveConfigOnDestroy() {
        mConfigManager.loadConfig()

        try {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_X, location.x.toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_X, "0")
        }

        try {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_Y, location.y.toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_Y, "0")
        }

        try {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_WIDTH, size.width.toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_WIDTH, "1280")
        }

        try {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_HEIGHT, size.height.toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_FRAME_HEIGHT, "720")
        }

        mConfigManager.setItem(ConfigManager.ITEM_FRAME_EXTENDED_STATE, extendedState.toString())

        mShowLogCombo.resetComboItem(mShowLogCombo.editor.item.toString())

        saveFilterCombo(mShowLogCombo, ConfigManager.ITEM_SHOW_LOG, ConfigManager.SAVE_FILTER_COUNT, ConfigManager.COUNT_SHOW_LOG)

        val formatName = mFormatManager.mCurrFormat.mName
        val tokenFilters = mFormatManager.mCurrFormat.mTokenFilters

        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mFormatManager.mCurrFormat.mTokenFilters[idx].mIsSaveFilter) {
                saveFilterCombo(mTokenCombo[idx], "${ConfigManager.ITEM_TOKEN_FILTER}${formatName}_${tokenFilters[idx].mToken}_", ConfigManager.SAVE_FILTER_COUNT, ConfigManager.COUNT_TOKEN_FILTER)
            }
        }

        saveFilterCombo(mBoldLogCombo, ConfigManager.ITEM_HIGHLIGHT_LOG, ConfigManager.SAVE_FILTER_COUNT, ConfigManager.COUNT_HIGHLIGHT_LOG)
        saveFilterCombo(mFindPanel.mFindCombo, ConfigManager.ITEM_FIND_LOG, ConfigManager.SAVE_FILTER_COUNT, ConfigManager.COUNT_FIND_LOG)

        try {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_DEVICE, mLogCmdManager.mTargetDevice)
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_DEVICE, "0.0.0.0")
        }

        try {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_LOG_CMD, mLogCmdCombo.editor.item.toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_LOG_CMD, LogCmdManager.DEFAULT_LOGCAT)
        }

        mConfigManager.setItem(ConfigManager.ITEM_DIVIDER_LOCATION, mLogSplitPane.dividerLocation.toString())
        if (mLogSplitPane.lastDividerLocation != -1) {
            mConfigManager.setItem(ConfigManager.ITEM_LAST_DIVIDER_LOCATION, mLogSplitPane.lastDividerLocation.toString())
        }

        mConfigManager.setItem(ConfigManager.ITEM_TOOL_DIVIDER_LOCATION, mToolSplitDividerLocation.toString())
        if (mToolSplitLastDividerLocation != -1) {
            mConfigManager.setItem(ConfigManager.ITEM_TOOL_LAST_DIVIDER_LOCATION, mToolSplitLastDividerLocation.toString())
        }

//            mProperties.put(ITEM_LANG, Strings.lang.toString())

        mConfigManager.saveConfig()
        PackageManager.getInstance().saveConfigPackages()
    }

    private fun updateRecentFiles() {
        mItemFileOpenRecents.removeAll()

        for (item in mRecentFileManager.mRecentList) {
            val path = Paths.get(item.mPath)
            val menuItem = JMenuItem(path.fileName.toString())
            menuItem.toolTipText = item.mPath
            menuItem.addActionListener { e: ActionEvent? ->
                openFile((e?.source as JMenuItem).toolTipText ?: "", false, false)
            }
            mItemFileOpenRecents.add(menuItem)
        }
    }

    private fun applyRecentOpen(path: String, startLine: Int) {
        if (!mItemFilterByFile.state || path.isEmpty()) {
            return
        }

        var recentItem = RecentFileManager.RecentItem()
        for (item in mRecentFileManager.mRecentList) {
            if (path == item.mPath) {
                recentItem = item
                break
            }
        }

        if (path == recentItem.mPath) {
            if (startLine == 0) {
                val result = JOptionPane.showConfirmDialog(this, Strings.APPLY_RECENT_FILE, Strings.RECENT_FILE, JOptionPane.YES_NO_OPTION)
                if (result == JOptionPane.YES_OPTION) {
                    mShowLogToggle.isSelected = recentItem.mShowLogCheck
                    mShowLogCombo.setEnabledFilter(mShowLogToggle.isSelected)
                    for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                        mTokenToggle[idx].isSelected = recentItem.mTokenCheck[idx]
                        mTokenCombo[idx].setEnabledFilter(mTokenToggle[idx].isSelected)
                    }
                    
                    mBoldLogToggle.isSelected = recentItem.mHighlightLogCheck
                    mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)
                    mFindPanel.mFindMatchCaseToggle.isSelected = recentItem.mFindMatchCase

                    mShowLogCombo.setFilterText(recentItem.mShowLog)
                    mShowLogCombo.applyFilterText(true)
                    for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                        mTokenCombo[idx].setFilterText(recentItem.mTokenFilter[idx])
                    }
                    mBoldLogCombo.setFilterText(recentItem.mHighlightLog)
                    mBoldLogCombo.applyFilterText(true)
                    mFindPanel.mFindCombo.setFilterText(recentItem.mFindLog)
                    mFindPanel.mFindCombo.applyFilterText(true)
                }
            }

            val bookmarks = recentItem.mBookmarks.split(",")
            for (bookmark in bookmarks) {
                if (bookmark.isNotBlank()) {
                    mBookmarkManager.addBookmark(bookmark.toInt() + startLine)
                }
            }
        }
    }

    private fun createUI() {
        title = Main.NAME

        val img = ImageIcon(this.javaClass.getResource("/images/logo.png"))
        iconImage = img.image

        defaultCloseOperation = EXIT_ON_CLOSE
        setLocation(mFrameX, mFrameY)
        setSize(mFrameWidth, mFrameHeight)
        extendedState = mFrameExtendedState
        addComponentListener(mComponentHandler)

        mMenuBar = JMenuBar()
        mMenuFile = JMenu(Strings.FILE)
        mMenuFile.mnemonic = KeyEvent.VK_F

        mItemFileOpen = JMenuItem(Strings.OPEN)
        mItemFileOpen.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpen)

        mItemFileOpenRecents = JMenu(Strings.OPEN_RECENTS)
        mItemFileOpenRecents.addActionListener(mActionHandler)
        mItemFileOpenRecents.addMenuListener(MenuHandler())
        mMenuFile.add(mItemFileOpenRecents)

        mItemFileFollow = JMenuItem(Strings.FOLLOW)
        mItemFileFollow.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileFollow)

        mItemFileOpenFiles = JMenuItem(Strings.OPEN_FILES)
        mItemFileOpenFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpenFiles)

        mItemFileAppendFiles = JMenuItem(Strings.APPEND_FILES)
        mItemFileAppendFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileAppendFiles)

        mMenuFile.addSeparator()

        mItemFileSaveFull = JMenuItem(Strings.SAVE_FULL)
        mItemFileSaveFull.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileSaveFull)

        mItemFileSaveFiltered = JMenuItem(Strings.SAVE_FILTERED)
        mItemFileSaveFiltered.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileSaveFiltered)

        mMenuFile.addSeparator()

        mItemFileExit = JMenuItem(Strings.EXIT).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)
            addActionListener { exit() }
        }
        mMenuFile.add(mItemFileExit)
        mMenuBar.add(mMenuFile)

        mMenuView = JMenu(Strings.VIEW)
        mMenuView.mnemonic = KeyEvent.VK_V
        mMenuView.addMenuListener(MenuHandler())

        mItemToolWindows = JMenu(Strings.TOOL_WINDOWS)
        mItemToolWindows.addActionListener(mActionHandler)
        mMenuView.add(mItemToolWindows)

        mItemToolPanel = JCheckBoxMenuItem(Strings.PANEL)
        mItemToolPanel.addActionListener(mActionHandler)
        mItemToolWindows.add(mItemToolPanel)

        mItemToolWindows.addSeparator()

        mItemToolSelection = JCheckBoxMenuItem(Strings.TOOL_SELECTION)
        mItemToolSelection.toolTipText = TooltipStrings.TOOL_SELECTION
        mItemToolSelection.addActionListener(mActionHandler)
        mItemToolWindows.add(mItemToolSelection)

        mItemToolTest = JCheckBoxMenuItem("Test")
        mItemToolTest.addActionListener(mActionHandler)
        if (mToolTestEnable) {
            mItemToolWindows.add(mItemToolTest)
        }

        mMenuView.addSeparator()

        mItemFull = JCheckBoxMenuItem(Strings.VIEW_FULL)
        mItemFull.addActionListener(mActionHandler)
        mMenuView.add(mItemFull)

        mItemFullLogToNewWindow = JCheckBoxMenuItem(Strings.MOVE_FULL_LOG_TO_NEW_WINDOW)
        mItemFullLogToNewWindow.addActionListener(mActionHandler)
        mMenuView.add(mItemFullLogToNewWindow)

        mMenuView.addSeparator()

        mItemColumnMode = JCheckBoxMenuItem(Strings.DIVIDED_BY_COLUMN)
        mItemColumnMode.state = mColumnMode
        mItemColumnMode.addActionListener(mActionHandler)
        mMenuView.add(mItemColumnMode)

        mItemProcessName = JMenu(Strings.SHOW_PROCESS_NAME)
        mItemProcessName.addActionListener(mActionHandler)
        mMenuView.add(mItemProcessName)

        val buttonGroup = ButtonGroup()
        mItemProcessNameNone = JRadioButtonMenuItem(Strings.NONE)
        mItemProcessName.add(mItemProcessNameNone)
        buttonGroup.add(mItemProcessNameNone)
        if (LogTableModel.TypeShowProcessName == LogTableModel.SHOW_PROCESS_NONE) {
            mItemProcessNameNone.isSelected = true
        }

        mItemProcessNameShow = JRadioButtonMenuItem(Strings.SHOW)
        mItemProcessName.add(mItemProcessNameShow)
        buttonGroup.add(mItemProcessNameShow)
        if (LogTableModel.TypeShowProcessName == LogTableModel.SHOW_PROCESS_SHOW) {
            mItemProcessNameShow.isSelected = true
        }

        mItemProcessNameColor = JRadioButtonMenuItem(Strings.SHOW_WITH_COLORBG)
        mItemProcessName.add(mItemProcessNameColor)
        buttonGroup.add(mItemProcessNameColor)
        if (LogTableModel.TypeShowProcessName == LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR) {
            mItemProcessNameColor.isSelected = true
        }

        mItemProcessNameNone.addItemListener(mItemHandler)
        mItemProcessNameShow.addItemListener(mItemHandler)
        mItemProcessNameColor.addItemListener(mItemHandler)

        mMenuView.addSeparator()

        mItemFind = JCheckBoxMenuItem(Strings.FIND).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK)
            addActionListener {
                mFindPanel.isVisible = true
                mItemFind.state = mFindPanel.isVisible
            }
        }
        mMenuView.add(mItemFind)

        mItemTrigger = JCheckBoxMenuItem(Strings.LOG_TRIGGER).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK)
            addActionListener {
                if (!mAgingTestManager.mTriggerPanel.isVisible) {
                    mAgingTestManager.mTriggerPanel.isVisible = true
                }
                else {
                    if (mAgingTestManager.mTriggerPanel.canHide()) {
                        mAgingTestManager.mTriggerPanel.isVisible = false
                    }
                    else {
                        JOptionPane.showMessageDialog(this@MainUI, Strings.TRIGGER_CANNOT_HIDE, Strings.WARNING, JOptionPane.WARNING_MESSAGE)
                    }
                }
            }
        }
        mMenuView.add(mItemTrigger)

        mMenuView.addSeparator()

        mItemRotation = JMenuItem(Strings.ROTATION)
        mItemRotation.addActionListener(mActionHandler)
        mMenuView.add(mItemRotation)

        mMenuBar.add(mMenuView)

        mMenuSettings = JMenu(Strings.SETTING)
        mMenuSettings.mnemonic = KeyEvent.VK_S

        mItemLogCmd = JMenuItem("${Strings.LOG_CMD}(${Strings.ADB})")
        mItemLogCmd.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLogCmd)
        mItemLogFile = JMenuItem(Strings.LOGFILE)
        mItemLogFile.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLogFile)
        mItemLogFormat = JMenuItem(Strings.LOG_FORMAT)
        mItemLogFormat.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLogFormat)

        mMenuSettings.addSeparator()

        mItemFilterIncremental = JCheckBoxMenuItem(Strings.FILTER + "-" + Strings.INCREMENTAL)
        mItemFilterIncremental.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFilterIncremental)

        mItemFilterByFile = JCheckBoxMenuItem(Strings.FILTER_BY_FILE)
        mItemFilterByFile.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFilterByFile)

        mItemColorTagRegex = JCheckBoxMenuItem(Strings.COLOR_TAG_REGEX)
        mItemColorTagRegex.addActionListener(mActionHandler)
        mMenuSettings.add(mItemColorTagRegex)

        mMenuSettings.addSeparator()

        mItemAppearance = JMenuItem(Strings.APPEARANCE)
        mItemAppearance.addActionListener(mActionHandler)
        mMenuSettings.add(mItemAppearance)

        mItemTool = JMenuItem(Strings.TOOL)
        mItemTool.addActionListener(mActionHandler)
        mMenuSettings.add(mItemTool)

        mMenuBar.add(mMenuSettings)

        mMenuHelp = JMenu(Strings.HELP)
        mMenuHelp.mnemonic = KeyEvent.VK_H

        mItemHelp = JMenuItem(Strings.HELP)
        mItemHelp.addActionListener(mActionHandler)
        mMenuHelp.add(mItemHelp)

        mItemCheckUpdate = JMenuItem(Strings.CHECK_UPDATE)
        mItemCheckUpdate.addActionListener(mActionHandler)
        mMenuHelp.add(mItemCheckUpdate)

        mMenuHelp.addSeparator()

        mItemAbout = JMenuItem(Strings.ABOUT)
        mItemAbout.addActionListener(mActionHandler)
        mMenuHelp.add(mItemAbout)
        mMenuBar.add(mMenuHelp)

        jMenuBar = mMenuBar

        addMouseListener(mFrameMouseListener)
        addMouseMotionListener(mFrameMouseListener)
        contentPane.addMouseListener(mFrameMouseListener)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })


        mFilterPanel = JPanel()
        mFilterLeftPanel = JPanel()

        mLogToolBar = ButtonPanel()
        mLogToolBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mLogToolBar.addMouseListener(mMouseHandler)

        mFindPanel = FindPanel()

        val btnMargin = Insets(2, 5, 2, 5)
//        mLogToolBar = JPanel()
//        mLogToolBar.background = Color(0xE5, 0xE5, 0xE5)
        mStartBtn = ColorButton(Strings.START)
        mStartBtn.margin = btnMargin
        mStartBtn.toolTipText = TooltipStrings.START_BTN
        mStartBtn.icon = ImageIcon(this.javaClass.getResource("/images/start.png"))
        mStartBtn.addActionListener(mActionHandler)
        mStartBtn.addMouseListener(mMouseHandler)
        mRetryAdbToggle = ColorToggleButton(Strings.RETRY_ADB)
        mRetryAdbToggle.toolTipText = TooltipStrings.RETRY_ADB_TOGGLE
        mRetryAdbToggle.margin = btnMargin
//        mRetryAdbToggle.margin = Insets(mRetryAdbToggle.margin.top, 0, mRetryAdbToggle.margin.bottom, 0)
        mRetryAdbToggle.addItemListener(mItemHandler)

        mPauseToggle = ColorToggleButton(Strings.PAUSE)
        mPauseToggle.toolTipText = TooltipStrings.PAUSE_BTN
        mPauseToggle.margin = btnMargin
//        mPauseToggle.margin = Insets(mPauseToggle.margin.top, 0, mPauseToggle.margin.bottom, 0)
        mPauseToggle.addItemListener(mItemHandler)


        mStopBtn = ColorButton(Strings.STOP)
        mStopBtn.margin = btnMargin
        mStopBtn.toolTipText = TooltipStrings.STOP_BTN
        mStopBtn.addActionListener(mActionHandler)
        mStopBtn.addMouseListener(mMouseHandler)
        mClearViewsBtn = ColorButton(Strings.CLEAR_VIEWS)
        mClearViewsBtn.margin = btnMargin
        mClearViewsBtn.toolTipText = TooltipStrings.CLEAR_BTN
        mClearViewsBtn.icon = ImageIcon(this.javaClass.getResource("/images/clear.png"))

        mClearViewsBtn.addActionListener(mActionHandler)
        mClearViewsBtn.addMouseListener(mMouseHandler)
        mSaveBtn = ColorButton(Strings.SAVE)
        mSaveBtn.margin = btnMargin
        mSaveBtn.toolTipText = TooltipStrings.SAVE_BTN
        mSaveBtn.addActionListener(mActionHandler)
        mSaveBtn.addMouseListener(mMouseHandler)

        mLogPanel = JPanel()
        mShowLogPanel = JPanel()
        mShowLogCombo = FilterComboBox(mShowLogComboStyle, true)
        mShowLogCombo.toolTipText = TooltipStrings.LOG_COMBO
        mShowLogCombo.isEditable = true
        mShowLogCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mShowLogCombo.addItemListener(mItemHandler)
        mShowLogCombo.addPopupMenuListener(mPopupMenuHandler)
        mShowLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowLogToggle = FilterToggleButton(Strings.LOG)
        mShowLogToggle.toolTipText = TooltipStrings.LOG_TOGGLE
        mShowLogToggle.margin = Insets(0, 0, 0, 0)
        mShowLogToggle.preferredSize = Dimension(mShowLogToggle.preferredSize.width, mShowLogCombo.preferredSize.height)
        mShowLogTogglePanel = JPanel(GridLayout(1, 1))
        mShowLogTogglePanel.add(mShowLogToggle)
        mShowLogTogglePanel.border = BorderFactory.createEmptyBorder(0,3,0,3)
        mShowLogToggle.addItemListener(mItemHandler)

        mBoldLogPanel = JPanel()
        mBoldLogCombo = FilterComboBox(mBoldLogComboStyle, false)
        mBoldLogCombo.toolTipText = TooltipStrings.BOLD_COMBO
        mBoldLogCombo.mEnabledTfTooltip = false
        mBoldLogCombo.isEditable = true
        mBoldLogCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mBoldLogCombo.addItemListener(mItemHandler)
        mBoldLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mBoldLogToggle = FilterToggleButton(Strings.BOLD)
        mBoldLogToggle.toolTipText = TooltipStrings.BOLD_TOGGLE
        mBoldLogToggle.margin = Insets(0, 0, 0, 0)
        mBoldLogToggle.preferredSize = Dimension(mBoldLogToggle.preferredSize.width, mBoldLogCombo.preferredSize.height)
        mBoldLogTogglePanel = JPanel(GridLayout(1, 1))
        mBoldLogTogglePanel.add(mBoldLogToggle)
        mBoldLogTogglePanel.border = BorderFactory.createEmptyBorder(0,3,0,3)
        mBoldLogToggle.addItemListener(mItemHandler)


        mTokenPanel = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { JPanel() }
        mTokenCombo = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { FilterComboBox(mTokenComboStyle[it], false) }
        mTokenToggle = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { FilterToggleButton(mFormatManager.mCurrFormat.mTokenFilters[it].mToken) }
        mTokenTogglePanel = Array(FormatManager.MAX_TOKEN_FILTER_COUNT) { JPanel(GridLayout(1, 1)) }

        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            mTokenCombo[idx].toolTipText = TooltipStrings.TOKEN_COMBO
            mTokenCombo[idx].isEditable = true
            mTokenCombo[idx].renderer = FilterComboBox.ComboBoxRenderer()
            mTokenCombo[idx].addItemListener(mItemHandler)
            mTokenCombo[idx].editor.editorComponent.addMouseListener(mMouseHandler)
            mTokenToggle[idx].toolTipText = TooltipStrings.TOKEN_TOGGLE
            mTokenToggle[idx].margin = Insets(0, 0, 0, 0)
            mTokenToggle[idx].preferredSize = Dimension(mTokenToggle[idx].preferredSize.width, mTokenCombo[idx].preferredSize.height)
            mTokenTogglePanel[idx].add(mTokenToggle[idx])
            mTokenTogglePanel[idx].border = BorderFactory.createEmptyBorder(0, 3, 0, 3)
            mTokenToggle[idx].addItemListener(mItemHandler)
        }

        mLogCmdCombo = ColorComboBox(true, 100)
        mLogCmdCombo.toolTipText = TooltipStrings.LOG_CMD_COMBO
        mLogCmdCombo.isEditable = true
        mLogCmdCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mLogCmdCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mLogCmdCombo.addItemListener(mItemHandler)
        mLogCmdCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mLogCmdCombo.addPopupMenuListener(mPopupMenuHandler)
        
        val deviceComboPanel = JPanel(BorderLayout())
        mDeviceCombo = ColorComboBox(true, 100)
        mDeviceCombo.toolTipText = TooltipStrings.DEVICES_COMBO
        mDeviceCombo.isEditable = true
        mDeviceCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mDeviceCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mDeviceCombo.addItemListener(mItemHandler)
        mDeviceCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        deviceComboPanel.add(mDeviceCombo, BorderLayout.CENTER)
        mAdbConnectBtn = ColorButton(Strings.CONNECT)
        mAdbConnectBtn.margin = btnMargin
        mAdbConnectBtn.toolTipText = TooltipStrings.CONNECT_BTN
        mAdbConnectBtn.addActionListener(mActionHandler)
//        deviceComboPanel.add(mAdbConnectBtn, BorderLayout.EAST)
        mAdbRefreshBtn = ColorButton(Strings.REFRESH)
        mAdbRefreshBtn.margin = btnMargin
        mAdbRefreshBtn.addActionListener(mActionHandler)
        mAdbRefreshBtn.toolTipText = TooltipStrings.REFRESH_BTN
        mAdbDisconnectBtn = ColorButton(Strings.DISCONNECT)
        mAdbDisconnectBtn.margin = btnMargin
        mAdbDisconnectBtn.addActionListener(mActionHandler)
        mAdbDisconnectBtn.toolTipText = TooltipStrings.DISCONNECT_BTN

        mMatchCaseToggle = FilterToggleButton("Aa")
        mMatchCaseToggle.toolTipText = TooltipStrings.CASE_TOGGLE
        mMatchCaseToggle.margin = Insets(0, 0, 0, 0)
        mMatchCaseToggle.preferredSize = Dimension(mMatchCaseToggle.preferredSize.width, mBoldLogCombo.preferredSize.height)
        mMatchCaseToggle.addItemListener(mItemHandler)
        mMatchCaseTogglePanel = JPanel(GridLayout(1, 1))
        mMatchCaseTogglePanel.add(mMatchCaseToggle)
        mMatchCaseTogglePanel.border = BorderFactory.createEmptyBorder(0,3,0,3)

        mShowLogPanel.layout = BorderLayout()
        mShowLogPanel.add(mShowLogTogglePanel, BorderLayout.WEST)
        mShowLogPanel.add(mShowLogCombo, BorderLayout.CENTER)

        mBoldLogPanel.layout = BorderLayout()
        mBoldLogPanel.add(mBoldLogTogglePanel, BorderLayout.WEST)
        mBoldLogCombo.preferredSize = Dimension(170, mBoldLogCombo.preferredSize.height)
        mBoldLogPanel.add(mBoldLogCombo, BorderLayout.CENTER)


        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            mTokenPanel[idx].layout = BorderLayout()
            mTokenPanel[idx].add(mTokenTogglePanel[idx], BorderLayout.WEST)
            mTokenCombo[idx].preferredSize = Dimension(mFormatManager.mCurrFormat.mTokenFilters[idx].mUiWidth, mTokenCombo[idx].preferredSize.height)
            mTokenPanel[idx].add(mTokenCombo[idx], BorderLayout.CENTER)
        }

        mLogCmdCombo.preferredSize = Dimension(200 * mUIFontPercent / 100, mLogCmdCombo.preferredSize.height)

        mDeviceCombo.preferredSize = Dimension(200 * mUIFontPercent / 100, mDeviceCombo.preferredSize.height)

        mScrollbackApplyBtn = ColorButton(Strings.APPLY)
        mScrollbackApplyBtn.margin = btnMargin
        mScrollbackApplyBtn.toolTipText = TooltipStrings.SCROLLBACK_APPLY_BTN
        mScrollbackApplyBtn.addActionListener(mActionHandler)
        mScrollbackKeepToggle = ColorToggleButton(Strings.KEEP)
        mScrollbackKeepToggle.toolTipText = TooltipStrings.SCROLLBACK_KEEP_TOGGLE
        mScrollbackKeepToggle.mSelectedBg = Color.RED
        mScrollbackKeepToggle.mSelectedFg = Color.BLACK
        val imgIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on_warn.png"))
        mScrollbackKeepToggle.selectedIcon = imgIcon

        mScrollbackKeepToggle.margin = btnMargin
        mScrollbackKeepToggle.addItemListener(mItemHandler)

        mScrollbackLabel = JLabel(Strings.SCROLLBACK_LINES)

        mScrollbackTF = JTextField()
        mScrollbackTF.toolTipText = TooltipStrings.SCROLLBACK_TF
        mScrollbackTF.preferredSize = Dimension(80, mScrollbackTF.preferredSize.height)
        mScrollbackTF.addKeyListener(mKeyHandler)
        mScrollbackSplitFileToggle = ColorToggleButton(Strings.SPLIT_FILE)
        mScrollbackSplitFileToggle.toolTipText = TooltipStrings.SCROLLBACK_SPLIT_CHK
        mScrollbackSplitFileToggle.margin = btnMargin
        mScrollbackSplitFileToggle.addItemListener(mItemHandler)

        val itemFilterPanel = JPanel(FlowLayout(FlowLayout.LEADING, 0, 0))
        itemFilterPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            itemFilterPanel.add(mTokenPanel[idx])
            if (mTokenToggle[idx].text.isNullOrEmpty()) {
                mTokenPanel[idx].isVisible = false
            }
        }
        itemFilterPanel.add(mBoldLogPanel)
        itemFilterPanel.add(mMatchCaseTogglePanel)

        mLogPanel.layout = BorderLayout()
        mLogPanel.add(mShowLogPanel, BorderLayout.CENTER)
        mLogPanel.add(itemFilterPanel, BorderLayout.EAST)

        mFilterLeftPanel.layout = BorderLayout()
        mFilterLeftPanel.add(mLogPanel, BorderLayout.NORTH)
        mFilterLeftPanel.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)

        mFilterPanel.layout = BoxLayout(mFilterPanel, BoxLayout.Y_AXIS)
        mFilterPanel.addMouseListener(mMouseHandler)

        changeToolBtnColor()

        mLogToolBar.add(mStartBtn)
        mLogToolBar.add(mRetryAdbToggle)
        addVSeparator2(mLogToolBar)
        mLogToolBar.add(mPauseToggle)
        mLogToolBar.add(mStopBtn)
        mLogToolBar.add(mSaveBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(mLogCmdCombo)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(deviceComboPanel)
        mLogToolBar.add(mAdbConnectBtn)
        mLogToolBar.add(mAdbDisconnectBtn)
        mLogToolBar.add(mAdbRefreshBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(mClearViewsBtn)


        val scrollbackPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        scrollbackPanel.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        addVSeparator(scrollbackPanel)
        scrollbackPanel.add(mScrollbackLabel)
        scrollbackPanel.add(mScrollbackTF)
        scrollbackPanel.add(mScrollbackSplitFileToggle)
        scrollbackPanel.add(mScrollbackApplyBtn)
        scrollbackPanel.add(mScrollbackKeepToggle)

        val toolBarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        toolBarPanel.layout = BorderLayout()
        toolBarPanel.addMouseListener(mMouseHandler)
        toolBarPanel.add(mLogToolBar, BorderLayout.CENTER)
        toolBarPanel.add(scrollbackPanel, BorderLayout.EAST)

        mFilterPanel.add(toolBarPanel)
        mFilterPanel.add(mFilterLeftPanel)
        mFilterPanel.add(mFindPanel)
        mFilterPanel.add(mAgingTestManager.mTriggerPanel)

        mFindPanel.isVisible = false
        mItemFind.state = mFindPanel.isVisible

        mAgingTestManager.mTriggerPanel.isVisible = false
        mItemFind.state = mAgingTestManager.mTriggerPanel.isVisible

        layout = BorderLayout()

        mFullLogPanel = LogPanel(this, null, FocusHandler(false), mItemColumnMode.state)
        mFilteredLogPanel = LogPanel(this, mFullLogPanel, FocusHandler(true), mItemColumnMode.state)

        FilterComboBox.IsFilterIncremental = { mItemFilterIncremental.state }
        mShowLogCombo.setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterLog = filter }
        mBoldLogCombo.setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterHighlightLog = filter }
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            mTokenCombo[idx].setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, filter) }
        }

        mFiltersManager = FiltersManager(this, mFilteredLogPanel)
        mCmdManager = CmdManager(this, mFullLogPanel)

        when (mRotationStatus) {
            ROTATION_LEFT_RIGHT -> {
                mLogSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mFilteredLogPanel, mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_RIGHT_LEFT -> {
                mLogSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mFullLogPanel, mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
            ROTATION_TOP_BOTTOM -> {
                mLogSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mFilteredLogPanel, mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_BOTTOM_TOP -> {
                mLogSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mFullLogPanel, mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
        }

        val dividerSize = mConfigManager.getItem(ConfigManager.ITEM_APPEARANCE_DIVIDER_SIZE)
        if (!dividerSize.isNullOrEmpty()) {
            mLogSplitPane.dividerSize = dividerSize.toInt()
        }
        mLogSplitPane.isOneTouchExpandable = false

        val logWidth = mConfigManager.getItem(ConfigManager.ITEM_LOG_VIEW_WIDTH)
        if (!logWidth.isNullOrEmpty()) {
            LogTable.LogWidth = logWidth.toInt()
            if (LogTable.LogWidth < LogTable.MIN_LOG_WIDTH) {
                LogTable.LogWidth = LogTable.MIN_LOG_WIDTH
            }
        }

        mStatusBar = JPanel(BorderLayout())
        mStatusBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mStatusMethod = JLabel("")
        mStatusMethod.isOpaque = true
        mStatusMethod.background = Color.DARK_GRAY
        mStatusMethod.addPropertyChangeListener(mStatusChangeListener)
        mStatusReloadBtn = ColorButton(Strings.RELOAD)
        mStatusReloadBtn.margin = Insets(mStatusReloadBtn.margin.top, 2, mStatusReloadBtn.margin.bottom, 2)
        mStatusReloadBtn.isVisible = false
        mStatusReloadBtn.addActionListener(mActionHandler)
        mStatusTF = StatusTextField(Strings.NONE)
        mStatusTF.document.addDocumentListener(mStatusChangeListener)
        mStatusTF.toolTipText = TooltipStrings.SAVED_FILE_TF
        mStatusTF.isEditable = false
        mStatusTF.border = BorderFactory.createEmptyBorder()

        mStartFollowBtn = ColorButton(Strings.START)
        mStartFollowBtn.margin = btnMargin
        mStartFollowBtn.toolTipText = TooltipStrings.START_FOLLOW_BTN
        mStartFollowBtn.addActionListener(mActionHandler)
        mStartFollowBtn.addMouseListener(mMouseHandler)

        mPauseFollowToggle = ColorToggleButton(Strings.PAUSE)
        mPauseFollowToggle.margin = Insets(mPauseFollowToggle.margin.top, 0, mPauseFollowToggle.margin.bottom, 0)
        mPauseFollowToggle.addItemListener(mItemHandler)

        mStopFollowBtn = ColorButton(Strings.STOP)
        mStopFollowBtn.margin = btnMargin
        mStopFollowBtn.toolTipText = TooltipStrings.STOP_FOLLOW_BTN
        mStopFollowBtn.addActionListener(mActionHandler)
        mStopFollowBtn.addMouseListener(mMouseHandler)

        val followPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        followPanel.border = BorderFactory.createEmptyBorder(0, 3, 0, 3)
        mFollowLabel = JLabel(" ${Strings.FOLLOW} ")
        mFollowLabel.border = BorderFactory.createDashedBorder(null, 1.0f, 2.0f)
        mFollowLabel.toolTipText = TooltipStrings.FOLLOW_LABEL
        mFollowLabel.addMouseListener(mMouseHandler)
        followPanel.add(mFollowLabel)
        followPanel.add(mStartFollowBtn)
        followPanel.add(mPauseFollowToggle)
        followPanel.add(mStopFollowBtn)

        setVisibleFollowBtn(false)

        val logFormatPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        logFormatPanel.border = BorderFactory.createEmptyBorder(0, 3, 0, 3)
        mLogFormatCombo = ColorComboBox(true, 60)
        mLogFormatCombo.toolTipText = TooltipStrings.LOG_FORMAT_COMBO
        mLogFormatCombo.isEditable = false
        mLogFormatCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mLogFormatCombo.addPopupMenuListener(mPopupMenuHandler)
        mLogLevelCombo = ColorComboBox(true, 60)
        mLogLevelCombo.toolTipText = TooltipStrings.LOG_LEVEL_COMBO
        mLogLevelCombo.isEditable = false
        mLogLevelCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mLogLevelCombo.addPopupMenuListener(mPopupMenuHandler)

        logFormatPanel.add(mLogFormatCombo)
        logFormatPanel.add(mLogLevelCombo)

        val statusRightPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        statusRightPanel.border = BorderFactory.createEmptyBorder(0, 3, 0, 3)
        statusRightPanel.add(followPanel)
        statusRightPanel.add(logFormatPanel)

        val statusLeftPanel = JPanel(BorderLayout())
        statusLeftPanel.border = BorderFactory.createEmptyBorder(0, 2, 0, 2)
        statusLeftPanel.add(mStatusMethod, BorderLayout.CENTER)
        statusLeftPanel.add(mStatusReloadBtn, BorderLayout.EAST)

        mStatusBar.add(statusLeftPanel, BorderLayout.WEST)
        mStatusBar.add(mStatusTF, BorderLayout.CENTER)
        mStatusBar.add(statusRightPanel, BorderLayout.EAST)

        for (format in mFormatManager.mFormatList) {
            mLogFormatCombo.addItem(format.mName)
        }

        mLogFormatCombo.selectedItem = mFormatManager.mCurrFormat.mName

        for (item in FormatManager.TEXT_LEVEL) {
            mLogLevelCombo.addItem(item)
        }

        val logLevel = mConfigManager.getItem(ConfigManager.ITEM_LOG_LEVEL)
        if (!logLevel.isNullOrEmpty()) {
            mLogLevelCombo.selectedIndex = logLevel.toInt()
            mFilteredLogPanel.mTableModel.mFilterLevel = mLogLevelCombo.selectedIndex
        }

        var item: String?
        for (i in 0 until ConfigManager.COUNT_SHOW_LOG) {
            item = mConfigManager.getItem(ConfigManager.ITEM_SHOW_LOG + i)
            if (item == null) {
                break
            }

            if (mShowLogCombo.getItemIdx(item) < 0) {
                mShowLogCombo.addItem(item)
            }
        }

        mShowLogCombo.updateTooltip()

        if (mShowLogCombo.itemCount > 0) {
            mShowLogCombo.selectedIndex = 0
        }

        var check = mConfigManager.getItem(ConfigManager.ITEM_SHOW_LOG_CHECK)
        if (!check.isNullOrEmpty()) {
            mShowLogToggle.isSelected = check.toBoolean()
        } else {
            mShowLogToggle.isSelected = true
        }
        mShowLogCombo.isEnabled = mShowLogToggle.isSelected

        val formatName = mFormatManager.mCurrFormat.mName
        val tokens = mFormatManager.mCurrFormat.mTokenFilters
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (tokens[idx].mIsSaveFilter) {
                for (i in 0 until ConfigManager.COUNT_TOKEN_FILTER) {
                    item = mConfigManager.getItem("${ConfigManager.ITEM_TOKEN_FILTER}${formatName}_${tokens[idx].mToken}_$i")
                    if (item == null) {
                        break
                    }
                    mTokenCombo[idx].insertItemAt(item, i)
                    if (i == 0) {
                        mTokenCombo[idx].selectedIndex = 0
                    }
                }

                mTokenCombo[idx].updateTooltip()
            }
            
            check = mConfigManager.getItem("${ConfigManager.ITEM_TOKEN_CHECK}${formatName}_${tokens[idx].mToken}")
            if (!check.isNullOrEmpty()) {
                mTokenToggle[idx].isSelected = check.toBoolean()
            } else {
                mTokenToggle[idx].isSelected = false
            }
            mTokenCombo[idx].setEnabledFilter(mTokenToggle[idx].isSelected)
        }

        for (i in 0 until ConfigManager.COUNT_HIGHLIGHT_LOG) {
            item = mConfigManager.getItem(ConfigManager.ITEM_HIGHLIGHT_LOG + i)
            if (item == null) {
                break
            }
            mBoldLogCombo.insertItemAt(item, i)
            if (i == 0) {
                mBoldLogCombo.selectedIndex = 0
            }
        }

        mBoldLogCombo.updateTooltip()

        check = mConfigManager.getItem(ConfigManager.ITEM_HIGHLIGHT_LOG_CHECK)
        if (!check.isNullOrEmpty()) {
            mBoldLogToggle.isSelected = check.toBoolean()
        } else {
            mBoldLogToggle.isSelected = false
        }
        mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)

        for (i in 0 until ConfigManager.COUNT_FIND_LOG) {
            item = mConfigManager.getItem(ConfigManager.ITEM_FIND_LOG + i)
            if (item == null) {
                break
            }
            mFindPanel.mFindCombo.insertItemAt(item, i)
            if (i == 0) {
                mFindPanel.mFindCombo.selectedIndex = 0
            }
        }

        mFindPanel.mFindCombo.updateTooltip()

        updateLogCmdCombo(true)

        val targetDevice = mConfigManager.getItem(ConfigManager.ITEM_ADB_DEVICE)
        mDeviceCombo.insertItemAt(targetDevice, 0)
        mDeviceCombo.selectedIndex = 0

        if (mLogCmdManager.mDevices.contains(targetDevice)) {
            setDeviceComboColor(true)
        } else {
            setDeviceComboColor(false)
        }

        var fontName = mConfigManager.getItem(ConfigManager.ITEM_FONT_NAME)
        if (fontName.isNullOrEmpty()) {
            fontName = DEFAULT_FONT_NAME
        }

        var fontSize = 12
        check = mConfigManager.getItem(ConfigManager.ITEM_FONT_SIZE)
        if (!check.isNullOrEmpty()) {
            fontSize = check.toInt()
        }

        mFont = Font(fontName, Font.PLAIN, fontSize)
        mFilteredLogPanel.mFont = mFont
        mFullLogPanel.mFont = mFont

        var divider = mConfigManager.getItem(ConfigManager.ITEM_LAST_DIVIDER_LOCATION)
        if (!divider.isNullOrEmpty()) {
            mLogSplitPane.lastDividerLocation = divider.toInt()
        }

        divider = mConfigManager.getItem(ConfigManager.ITEM_DIVIDER_LOCATION)
        if (!divider.isNullOrEmpty() && mLogSplitPane.lastDividerLocation != -1) {
            mLogSplitPane.dividerLocation = divider.toInt()
        }

        if (mLogLevelCombo.selectedIndex >= 0) {
            mFilteredLogPanel.mTableModel.mFilterLevel = mLogLevelCombo.selectedIndex
        }

        if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterLog = ""
        }
        if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterHighlightLog = ""
        }
        if (mFindPanel.isVisible && mFindPanel.mFindCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterFindLog = mFindPanel.mFindCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterFindLog = ""
        }
        
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mTokenToggle[idx].isSelected && mTokenCombo[idx].selectedItem != null) {
                mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, mTokenCombo[idx].selectedItem!!.toString())
            } else {
                mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, "")
            }
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_VIEW_FULL)
        if (!check.isNullOrEmpty()) {
            mItemFull.state = check.toBoolean()
        } else {
            mItemFull.state = true
        }
        if (!mItemFull.state) {
            windowedModeLogPanel(mFullLogPanel)
            mItemFullLogToNewWindow.state = true
        }

        updateLogPanelTableBar()

        mToolsPane = ToolsPane.getInstance()

        check = mConfigManager.getItem(ConfigManager.ITEM_TOOL_SELECTION)
        if (!check.isNullOrEmpty()) {
            mItemToolSelection.state = check.toBoolean()
        } else {
            mItemToolSelection.state = false
        }

        if (mItemToolSelection.state) {
            mToolsPane.addTab(ToolsPane.Companion.ToolId.TOOL_ID_SELECTION)
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_TOOL_SELECTION_RANGE_PREVIOUS)
        if (!check.isNullOrEmpty()) {
            mToolsPane.mToolSelection.mPrevLines = check.toInt()
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_TOOL_SELECTION_RANGE_NEXT)
        if (!check.isNullOrEmpty()) {
            mToolsPane.mToolSelection.mNextLines = check.toInt()
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_TOOL_TEST)
        if (mToolTestEnable && !check.isNullOrEmpty()) {
            mItemToolTest.state = check.toBoolean()
        } else {
            mItemToolTest.state = false
        }

        if (mItemToolTest.state) {
            mToolsPane.addTab(ToolsPane.Companion.ToolId.TOOL_ID_TEST)
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_TOOL_PANEL)
        if (!check.isNullOrEmpty()) {
            mToolsPane.updateVisible(check.toBoolean())
        } else {
            mToolsPane.isVisible = false
        }

        mItemToolPanel.state = mToolsPane.isVisible

        when (mToolRotationStatus) {
            ROTATION_TOP_BOTTOM -> {
                mToolSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mToolsPane, mLogSplitPane)
                mToolSplitPane.resizeWeight = SPLIT_WEIGHT
            }

            ROTATION_BOTTOM_TOP -> {
                mToolSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mLogSplitPane, mToolsPane)
                mToolSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
        }

        if (mToolsPane.isVisible) {
            mToolsPane.mToolSelection.setBgColor(mFilteredLogPanel.mTable.mTableColor.mLogBG)
            mToolSplitPane.dividerSize = mLogSplitPane.dividerSize
        }
        else {
            mToolSplitPane.dividerSize = 0
        }

        divider = mConfigManager.getItem(ConfigManager.ITEM_TOOL_LAST_DIVIDER_LOCATION)
        if (!divider.isNullOrEmpty()) {
            mToolSplitLastDividerLocation = divider.toInt()
            mToolSplitPane.lastDividerLocation = mToolSplitLastDividerLocation
        }

        divider = mConfigManager.getItem(ConfigManager.ITEM_TOOL_DIVIDER_LOCATION)
        if (!divider.isNullOrEmpty() && mToolSplitLastDividerLocation != -1) {
            mToolSplitDividerLocation = divider.toInt()
            mToolSplitPane.dividerLocation = mToolSplitDividerLocation
        }

        mToolSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY) { evt ->
            if (evt.propertyName == JSplitPane.DIVIDER_LOCATION_PROPERTY && mToolsPane.isVisible) {
                Utils.printlnLog("Tool divider location = ${mToolSplitPane.dividerLocation}")
                mToolSplitDividerLocation = mToolSplitPane.dividerLocation
                mToolSplitLastDividerLocation = mToolSplitPane.lastDividerLocation
            }
        }

        mToolSplitPane.isOneTouchExpandable = false

        check = mConfigManager.getItem(ConfigManager.ITEM_FILTER_INCREMENTAL)
        if (!check.isNullOrEmpty()) {
            mItemFilterIncremental.state = check.toBoolean()
        } else {
            mItemFilterIncremental.state = false
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_FILTER_BY_FILE)
        if (!check.isNullOrEmpty()) {
            mItemFilterByFile.state = check.toBoolean()
        } else {
            mItemFilterByFile.state = true
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_SCROLLBACK)
        if (!check.isNullOrEmpty()) {
            mScrollbackTF.text = check
        } else {
            mScrollbackTF.text = "100000"
        }
        mFilteredLogPanel.mTableModel.mScrollback = mScrollbackTF.text.toInt()

        check = mConfigManager.getItem(ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE)
        if (!check.isNullOrEmpty()) {
            mScrollbackSplitFileToggle.isSelected = check.toBoolean()
        } else {
            mScrollbackSplitFileToggle.isSelected = true
        }
        mFilteredLogPanel.mTableModel.mScrollbackSplitFile = mScrollbackSplitFileToggle.isSelected

        check = mConfigManager.getItem(ConfigManager.ITEM_MATCH_CASE)
        if (!check.isNullOrEmpty()) {
            mMatchCaseToggle.isSelected = check.toBoolean()
        } else {
            mMatchCaseToggle.isSelected = false
        }
        mFilteredLogPanel.mTableModel.mMatchCase = mMatchCaseToggle.isSelected

        check = mConfigManager.getItem(ConfigManager.ITEM_FIND_MATCH_CASE)
        if (!check.isNullOrEmpty()) {
            mFindPanel.mFindMatchCaseToggle.isSelected = check.toBoolean()
        } else {
            mFindPanel.mFindMatchCaseToggle.isSelected = false
        }
        mFilteredLogPanel.mTableModel.mFindMatchCase = mFindPanel.mFindMatchCaseToggle.isSelected

        check = mConfigManager.getItem(ConfigManager.ITEM_COLOR_TAG_REGEX)
        if (!check.isNullOrEmpty()) {
            LogTableModel.IsColorTagRegex = check.toBoolean()
        }
        mItemColorTagRegex.state = LogTableModel.IsColorTagRegex

        check = mConfigManager.getItem(ConfigManager.ITEM_RETRY_ADB)
        if (!check.isNullOrEmpty()) {
            mRetryAdbToggle.isSelected = check.toBoolean()
        } else {
            mRetryAdbToggle.isSelected = false
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_ICON_TEXT)
        if (!check.isNullOrEmpty()) {
            when (check) {
                ConfigManager.VALUE_ICON_TEXT_I -> {
                    setBtnIconsTexts(true, false)
                }
                ConfigManager.VALUE_ICON_TEXT_T -> {
                    setBtnIconsTexts(false, true)
                }
                else -> {
                    setBtnIconsTexts(true, true)
                }
            }
        } else {
            setBtnIconsTexts(true, true)
        }

        add(mFilterPanel, BorderLayout.NORTH)
        add(mToolSplitPane, BorderLayout.CENTER)
        add(mStatusBar, BorderLayout.SOUTH)

        registerKeyStroke()
        registerFindKeyStroke()
//        registerTriggerKeyStroke()

        IsCreatingUI = false
    }

    private fun changeToolBtnColor() {
        mStartBtn.background = mLogToolBar.background
        mRetryAdbToggle.background = mLogToolBar.background
        mPauseToggle.background = mLogToolBar.background
        mStopBtn.background = mLogToolBar.background
        mSaveBtn.background = mLogToolBar.background
        mAdbConnectBtn.background = mLogToolBar.background
        mAdbDisconnectBtn.background = mLogToolBar.background
        mAdbRefreshBtn.background = mLogToolBar.background
        mClearViewsBtn.background = mLogToolBar.background
        mScrollbackSplitFileToggle.background = mLogToolBar.background
        mScrollbackApplyBtn.background = mLogToolBar.background
        mScrollbackKeepToggle.background = mLogToolBar.background

        mShowLogToggle.background = mLogPanel.background
        mBoldLogToggle.background = mLogPanel.background
        mMatchCaseToggle.background = mLogPanel.background

        for (item in mTokenToggle) {
            item.background = mLogPanel.background
        }

        mStartBtn.border = ColorButtonBorder(mLogToolBar.background)
        mStartBtn.background = mLogToolBar.background
        mRetryAdbToggle.border = ColorButtonBorder(mLogToolBar.background)
        mPauseToggle.border = ColorButtonBorder(mLogToolBar.background)
        mStopBtn.border = ColorButtonBorder(mLogToolBar.background)
        mSaveBtn.border = ColorButtonBorder(mLogToolBar.background)
        mAdbConnectBtn.border = ColorButtonBorder(mLogToolBar.background)
        mAdbDisconnectBtn.border = ColorButtonBorder(mLogToolBar.background)
        mAdbRefreshBtn.border = ColorButtonBorder(mLogToolBar.background)
        mClearViewsBtn.border = ColorButtonBorder(mLogToolBar.background)
        mScrollbackSplitFileToggle.border = ColorButtonBorder(mLogToolBar.background)
        mScrollbackApplyBtn.border = ColorButtonBorder(mLogToolBar.background)
        mScrollbackKeepToggle.border = ColorButtonBorder(mLogToolBar.background)

        mShowLogToggle.border = ColorButtonBorder(mLogPanel.background)
        mBoldLogToggle.border = ColorButtonBorder(mLogPanel.background)
        mMatchCaseToggle.border = ColorButtonBorder(mLogPanel.background)

        for (item in mTokenToggle) {
            item.border = ColorButtonBorder(mLogPanel.background)
        }
    }

    private fun resetLogPanel(keepCurrentMethod: Boolean) {
        val method = CurrentMethod
        val isRunning = when (method) {
            METHOD_ADB, METHOD_CMD -> {
                mFilteredLogPanel.mTableModel.isScanning()
            }
            METHOD_FOLLOW -> {
                mFilteredLogPanel.mTableModel.isFollowing()
            }
            else -> {
                false
            }
        }


        mFilteredLogPanel.mTableModel.stopScan()
        mFilteredLogPanel.mTableModel.stopFollow()
        mFullLogPanel.mTableModel.stopScan()
        mFullLogPanel.mTableModel.stopFollow()
        mLogCmdManager.stop()

        val scrollback = mFilteredLogPanel.mTableModel.mScrollback

        mLogSplitPane.remove(mFilteredLogPanel)
        mLogSplitPane.remove(mFullLogPanel)

        mFullLogPanel = LogPanel(this, null, FocusHandler(false), mItemColumnMode.state)
        mFilteredLogPanel = LogPanel(this, mFullLogPanel, FocusHandler(true), mItemColumnMode.state)

        mShowLogCombo.setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterLog = filter }
        mBoldLogCombo.setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterHighlightLog = filter }
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            mTokenCombo[idx].setApplyFilter { filter -> mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, filter) }
        }

        updateLogPanelTableBar()

        mFiltersManager = FiltersManager(this, mFilteredLogPanel)
        mCmdManager = CmdManager(this, mFullLogPanel)

        mFilteredLogPanel.mFont = mFont
        mFullLogPanel.mFont = mFont

        if (mLogLevelCombo.selectedIndex >= 0) {
            mFilteredLogPanel.mTableModel.mFilterLevel = mLogLevelCombo.selectedIndex
        }

        if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterLog = ""
        }
        if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterHighlightLog = ""
        }
        if (mFindPanel.isVisible && mFindPanel.mFindCombo.selectedItem != null) {
            mFilteredLogPanel.mTableModel.mFilterFindLog = mFindPanel.mFindCombo.selectedItem!!.toString()
        } else {
            mFilteredLogPanel.mTableModel.mFilterFindLog = ""
        }

        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mTokenToggle[idx].isSelected && mTokenCombo[idx].selectedItem != null) {
                mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, mTokenCombo[idx].selectedItem!!.toString())
            } else {
                mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, "")
            }
        }

        if (!mItemFull.state) {
            windowedModeLogPanel(mFullLogPanel)
            mItemFullLogToNewWindow.state = true
        }

        mFilteredLogPanel.mTableModel.mScrollback = scrollback
        mFilteredLogPanel.mTableModel.mScrollbackSplitFile = mScrollbackSplitFileToggle.isSelected
        mFilteredLogPanel.mTableModel.mMatchCase = mMatchCaseToggle.isSelected
        mFilteredLogPanel.mTableModel.mFindMatchCase = mFindPanel.mFindMatchCaseToggle.isSelected

        rotateLogSplitPane(false)

        mStatusTF.text = Strings.NONE
        mStatusMethod.text = ""
        updateTablePNameColumn(false)
        title = Main.NAME

        if (keepCurrentMethod) {
            when (method) {
                METHOD_OPEN -> {
                    for ((idx, path) in mOpenFileList.withIndex()) {
                        openFile(path, idx != 0, true)
                    }
                }
                METHOD_CMD, METHOD_ADB -> {
                    startAdbScan(true)
                }
                METHOD_FOLLOW -> {
                    startFileFollow(mOpenFileList.first())
                }
                else -> {
                }
            }
        }
    }

    private fun rotateLogSplitPane(isNeedRemove: Boolean) {
        if (isNeedRemove) {
            mLogSplitPane.remove(mFilteredLogPanel)
            mLogSplitPane.remove(mFullLogPanel)
        }
        when (mRotationStatus) {
            ROTATION_LEFT_RIGHT -> {
                mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                mLogSplitPane.add(mFilteredLogPanel)
                mLogSplitPane.add(mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_RIGHT_LEFT -> {
                mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                mLogSplitPane.add(mFullLogPanel)
                mLogSplitPane.add(mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
            ROTATION_TOP_BOTTOM -> {
                mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                mLogSplitPane.add(mFilteredLogPanel)
                mLogSplitPane.add(mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_BOTTOM_TOP -> {
                mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                mLogSplitPane.add(mFullLogPanel)
                mLogSplitPane.add(mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
        }
    }

    fun rotateToolSplitPane(rotation: Int) {
        mToolRotationStatus = rotation
        mToolSplitPane.remove(mLogSplitPane)
        mToolSplitPane.remove(mToolsPane)

        when (mToolRotationStatus) {
            ROTATION_TOP_BOTTOM -> {
                mToolSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                mToolSplitPane.add(mToolsPane)
                mToolSplitPane.add(mLogSplitPane)
                mToolSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_BOTTOM_TOP -> {
                mToolSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                mToolSplitPane.add(mLogSplitPane)
                mToolSplitPane.add(mToolsPane)
                mToolSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
        }

        mConfigManager.saveItem(ConfigManager.ITEM_TOOL_ROTATION, mToolRotationStatus.toString())
    }

    private fun setBtnIconsTexts(isShowIcons: Boolean, isShowTexts: Boolean) {
        setBtnPreferredHeight(0)
        setBtnIcons(isShowIcons)
        setBtnTexts(isShowTexts)
        setBtnPreferredHeight(mLogCmdCombo.preferredSize.height)
    }

    private fun setBtnPreferredHeight(preferredHeight: Int) {
        if (preferredHeight == 0) {
            mStartBtn.preferredSize = null
            mStopBtn.preferredSize = null
            mClearViewsBtn.preferredSize = null
            mSaveBtn.preferredSize = null
            mAdbConnectBtn.preferredSize = null
            mAdbRefreshBtn.preferredSize = null
            mAdbDisconnectBtn.preferredSize = null
            mScrollbackApplyBtn.preferredSize = null
            mRetryAdbToggle.preferredSize = null
            mPauseToggle.preferredSize = null
            mScrollbackKeepToggle.preferredSize = null
            mScrollbackSplitFileToggle.preferredSize = null
            mScrollbackLabel.preferredSize = null
        }
        else {
            mStartBtn.preferredSize = Dimension(mStartBtn.preferredSize.width, preferredHeight)
            mStopBtn.preferredSize = Dimension(mStopBtn.preferredSize.width, preferredHeight)
            mClearViewsBtn.preferredSize = Dimension(mClearViewsBtn.preferredSize.width, preferredHeight)
            mSaveBtn.preferredSize = Dimension(mSaveBtn.preferredSize.width, preferredHeight)
            mAdbConnectBtn.preferredSize = Dimension(mAdbConnectBtn.preferredSize.width, preferredHeight)
            mAdbRefreshBtn.preferredSize = Dimension(mAdbRefreshBtn.preferredSize.width, preferredHeight)
            mAdbDisconnectBtn.preferredSize = Dimension(mAdbDisconnectBtn.preferredSize.width, preferredHeight)
            mScrollbackApplyBtn.preferredSize = Dimension(mScrollbackApplyBtn.preferredSize.width, preferredHeight)
            mRetryAdbToggle.preferredSize = Dimension(mRetryAdbToggle.preferredSize.width, preferredHeight)
            mPauseToggle.preferredSize = Dimension(mPauseToggle.preferredSize.width, preferredHeight)
            mScrollbackKeepToggle.preferredSize = Dimension(mScrollbackKeepToggle.preferredSize.width, preferredHeight)
            mScrollbackSplitFileToggle.preferredSize = Dimension(mScrollbackSplitFileToggle.preferredSize.width, preferredHeight)
            mScrollbackLabel.preferredSize = Dimension(mScrollbackLabel.preferredSize.width, preferredHeight)
        }
    }

    private fun setBtnIcons(isShow:Boolean) {
        if (isShow) {
            mStartBtn.icon = ImageIcon(this.javaClass.getResource("/images/start.png"))
            mStopBtn.icon = ImageIcon(this.javaClass.getResource("/images/stop.png"))
            mClearViewsBtn.icon = ImageIcon(this.javaClass.getResource("/images/clear.png"))
            mSaveBtn.icon = ImageIcon(this.javaClass.getResource("/images/save.png"))
            mAdbConnectBtn.icon = ImageIcon(this.javaClass.getResource("/images/connect.png"))
            mAdbRefreshBtn.icon = ImageIcon(this.javaClass.getResource("/images/refresh.png"))
            mAdbDisconnectBtn.icon = ImageIcon(this.javaClass.getResource("/images/disconnect.png"))
            mScrollbackApplyBtn.icon = ImageIcon(this.javaClass.getResource("/images/apply.png"))

            mRetryAdbToggle.icon = ImageIcon(this.javaClass.getResource("/images/retry_off.png"))
            mPauseToggle.icon = ImageIcon(this.javaClass.getResource("/images/pause_off.png"))
            mScrollbackKeepToggle.icon = ImageIcon(this.javaClass.getResource("/images/keeplog_off.png"))
            mScrollbackSplitFileToggle.icon = ImageIcon(this.javaClass.getResource("/images/splitfile_off.png"))

            if (IsFlatLaf && !IsFlatLightLaf) {
                mRetryAdbToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/retry_on_dark.png"))
                mPauseToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/pause_on_dark.png"))
                mScrollbackKeepToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/keeplog_on_dark.png"))
                mScrollbackSplitFileToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/splitfile_on_dark.png"))
            }
            else {
                mRetryAdbToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/retry_on.png"))
                mPauseToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/pause_on.png"))
                mScrollbackKeepToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/keeplog_on.png"))
                mScrollbackSplitFileToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/splitfile_on.png"))
            }

            mScrollbackLabel.icon = ImageIcon(this.javaClass.getResource("/images/scrollback.png"))
        }
        else {
            mStartBtn.icon = null
            mStopBtn.icon = null
            mClearViewsBtn.icon = null
            mSaveBtn.icon = null
            mAdbConnectBtn.icon = null
            mAdbRefreshBtn.icon = null
            mAdbDisconnectBtn.icon = null
            mScrollbackApplyBtn.icon = null

            mRetryAdbToggle.icon = ImageIcon(this.javaClass.getResource("/images/toggle_off.png"))
            mRetryAdbToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on.png"))
            mPauseToggle.icon = ImageIcon(this.javaClass.getResource("/images/toggle_off.png"))
            mPauseToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on.png"))
            mScrollbackKeepToggle.icon = ImageIcon(this.javaClass.getResource("/images/toggle_off.png"))
            mScrollbackKeepToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on_warn.png"))
            mScrollbackSplitFileToggle.icon = ImageIcon(this.javaClass.getResource("/images/toggle_off.png"))
            mScrollbackSplitFileToggle.selectedIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on.png"))

            mScrollbackLabel.icon = null
        }
    }

    private fun setBtnTexts(isShow:Boolean) {
        if (isShow) {
            mStartBtn.text = Strings.START
            mRetryAdbToggle.text = Strings.RETRY_ADB
            mPauseToggle.text = Strings.PAUSE
            mStopBtn.text = Strings.STOP
            mClearViewsBtn.text = Strings.CLEAR_VIEWS
            mSaveBtn.text = Strings.SAVE
            mAdbConnectBtn.text = Strings.CONNECT
            mAdbRefreshBtn.text = Strings.REFRESH
            mAdbDisconnectBtn.text = Strings.DISCONNECT
            mScrollbackApplyBtn.text = Strings.APPLY
            mScrollbackKeepToggle.text = Strings.KEEP
            mScrollbackSplitFileToggle.text = Strings.SPLIT_FILE
            mScrollbackLabel.text = Strings.SCROLLBACK_LINES
        }
        else {
            mStartBtn.text = null
            mStopBtn.text = null
            mClearViewsBtn.text = null
            mSaveBtn.text = null
            mAdbConnectBtn.text = null
            mAdbRefreshBtn.text = null
            mAdbDisconnectBtn.text = null
            mScrollbackApplyBtn.text = null
            mRetryAdbToggle.text = null
            mPauseToggle.text = null
            mScrollbackKeepToggle.text = null
            mScrollbackSplitFileToggle.text = null
            mScrollbackLabel.text = null
        }
    }

    inner class StatusChangeListener : PropertyChangeListener, DocumentListener {
        private var mMethod = ""
        override fun propertyChange(evt: PropertyChangeEvent?) {
            if (evt?.source == mStatusMethod && evt.propertyName == "text") {
                mMethod = evt.newValue.toString().trim()
            }
        }

        override fun insertUpdate(evt: DocumentEvent?) {
            updateTitleBar(mMethod)
        }

        override fun removeUpdate(e: DocumentEvent?) {
        }

        override fun changedUpdate(evt: DocumentEvent?) {
        }
    }

    private fun updateTitleBar(statusMethod: String) {
        title = when (statusMethod) {
            Strings.OPEN, Strings.FOLLOW, "${Strings.FOLLOW} ${Strings.STOP}" -> {
                val statusText = mStatusTF.text
                val files = statusText.split("|")
                val lastFile = files.last().trim()
                val path: Path = Paths.get(lastFile)
                Utils.printlnLog("Paths.get $lastFile, ${path.fileName}")
                "${path.fileName} - $statusMethod"
            }
            Strings.ADB, Strings.CMD, "${Strings.ADB} ${Strings.STOP}", "${Strings.CMD} ${Strings.STOP}" -> {
                "${mLogCmdManager.mTargetDevice.ifEmpty { Main.NAME }} - $statusMethod"
            }
            else -> {
                Main.NAME
            }
        }
    }

    fun setLaF() {
        when (ConfigManager.LaF) {
            SYSTEM_LAF->{
                IsFlatLaf = false
                IsFlatLightLaf = true
            }
            FLAT_LIGHT_LAF, FLAT_INTELLIJ_LAF, FLAT_MACOS_LIGHT_LAF->{
                IsFlatLaf = true
                IsFlatLightLaf = true
            }
            FLAT_DARK_LAF, FLAT_DARCULA_LAF, FLAT_MACOS_DARK_LAF->{
                IsFlatLaf = true
                IsFlatLightLaf = false
            }
            else-> {
                ConfigManager.LaF = FLAT_LIGHT_LAF
                IsFlatLaf = true
                IsFlatLightLaf = true
            }
        }

        val uiFontSize = mConfigManager.getItem(ConfigManager.ITEM_UI_FONT_SIZE)
        if (!uiFontSize.isNullOrEmpty()) {
            mUIFontPercent = uiFontSize.toInt()
        }

        if (IsFlatLaf) {
            System.setProperty("flatlaf.uiScale", "$mUIFontPercent%")
        }
        else {
            initFontSize(mUIFontPercent)
        }

        when (ConfigManager.LaF) {
            SYSTEM_LAF->{
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize SystemLaf")
                }
            }
            FLAT_LIGHT_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatLightLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatLightLaf")
                }
            }
            FLAT_DARK_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatDarkLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatDarkLaf")
                }
            }
            FLAT_INTELLIJ_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatIntelliJLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatDarkLaf")
                }
            }
            FLAT_DARCULA_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatDarculaLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatDarkLaf")
                }
            }
            FLAT_MACOS_LIGHT_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatMacLightLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatDarkLaf")
                }
            }
            FLAT_MACOS_DARK_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatMacDarkLaf())
                } catch (ex: Exception) {
                    Utils.printlnLog("Failed to initialize FlatDarkLaf")
                }
            }
        }
        FlatLaf.updateUI()
        SwingUtilities.updateComponentTreeUI(this)
    }

    private fun addVSeparator(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width, 20)
        if (IsFlatLaf && !IsFlatLightLaf) {
            separator1.foreground = Color.GRAY
            separator1.background = Color.GRAY
        }
        else {
            separator1.foreground = Color.DARK_GRAY
            separator1.background = Color.DARK_GRAY
        }
        val separator2 = JSeparator(SwingConstants.VERTICAL)
        separator2.preferredSize = Dimension(separator2.preferredSize.width, 20)
        if (IsFlatLaf && !IsFlatLightLaf) {
            separator2.foreground = Color.GRAY
            separator2.background = Color.GRAY
        }
        else {
            separator2.background = Color.DARK_GRAY
            separator2.foreground = Color.DARK_GRAY
        }
        panel.add(Box.createHorizontalStrut(5))
        panel.add(separator1)
        panel.add(Box.createHorizontalStrut(5))
    }

    private fun addVSeparator2(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width / 2, 20)
        if (IsFlatLaf && !IsFlatLightLaf) {
            separator1.foreground = Color.GRAY
            separator1.background = Color.GRAY
        }
        else {
            separator1.foreground = Color.DARK_GRAY
            separator1.background = Color.DARK_GRAY
        }
        panel.add(Box.createHorizontalStrut(2))
        panel.add(separator1)
        panel.add(Box.createHorizontalStrut(2))
    }

    private fun initFontSize(fontSize: Int) {
        val multiplier = fontSize / 100.0f
        val defaults = UIManager.getDefaults()
        val e: Enumeration<*> = defaults.keys()
        while (e.hasMoreElements()) {
            val key = e.nextElement()
            val value = defaults[key]
            if (value is Font) {
                val newSize = (value.size * multiplier).roundToInt()
                if (value is FontUIResource) {
                    defaults[key] = FontUIResource(value.name, value.style, newSize)
                } else {
                    defaults[key] = Font(value.name, value.style, newSize)
                }
            }
        }
    }

    fun windowedModeLogPanel(logPanel: LogPanel) {
        if (logPanel.parent == mLogSplitPane) {
            mItemRotation.isEnabled = false
            mLogSplitPane.remove(logPanel)
            if (mItemFull.state) {
                mLogTableDialog = LogTableDialog(this@MainUI, logPanel)
                mLogTableDialog?.isVisible = true
            }
            else {
                mLogTableDialog?.isVisible = false
            }
        }
    }

    fun attachLogPanel(logPanel: LogPanel) {
        if (logPanel.parent != mLogSplitPane) {
            mLogTableDialog?.isVisible = false
            mItemFullLogToNewWindow.state = false
            mItemRotation.isEnabled = true
            rotateLogSplitPane(true)
        }
    }

    private fun setCurrentMethod(method: Int) {
        mStatusReloadBtn.isVisible = false
        when (method) {
            METHOD_OPEN -> {
                mStatusMethod.text = " ${Strings.OPEN} "
                mStatusReloadBtn.isVisible = true
            }
            METHOD_CMD -> {
                mStatusMethod.text = " ${Strings.CMD} "
                mOpenFileList.clear()
            }
            METHOD_ADB -> {
                mStatusMethod.text = " ${Strings.ADB} "
                mOpenFileList.clear()
            }
            METHOD_FOLLOW -> {
                mStatusMethod.text = " ${Strings.FOLLOW} "
                mOpenFileList.clear()
            }
            else -> {
                mStatusMethod.text = ""
                mOpenFileList.clear()
            }
        }

        CurrentMethod = method
        mFilteredLogPanel.mTableModel.stopScan()
        mFilteredLogPanel.mTableModel.stopFollow()
    }

    fun openFile(path: String, isAppend: Boolean, isReload: Boolean) {
        Utils.printlnLog("Opening: $path, $isAppend")
        saveRecentFile()
        setCurrentMethod(METHOD_OPEN)
        updateTablePNameColumn(false)
        if (isAppend) {
            mStatusTF.text += "| $path"
            mOpenFileList.add(path)
        } else {
            mStatusTF.text = path
            mRecentFileManager.mOpenList.clear()
            mOpenFileList.clear()
            mOpenFileList.add(path)
        }

        val openItem = RecentFileManager.OpenItem(path.trim(), 0, 0)
        mFullLogPanel.mTableModel.setLogFile(path)
        mFilteredLogPanel.mTableModel.setLogFile(path)

        openItem.mStartLine = if (isAppend) mFullLogPanel.mTableModel.rowCount + 1 else 0
        mFullLogPanel.mTableModel.loadItems(isAppend)
        openItem.mEndLine = mFullLogPanel.mTableModel.rowCount - 1
        mRecentFileManager.addOpenFile(openItem)
        mFilteredLogPanel.mTableModel.loadItems(isAppend)

        if (IsFlatLaf && !IsFlatLightLaf) {
            mStatusMethod.background = Color(0x50, 0x50, 0x00)
        }
        else {
            mStatusMethod.background = Color(0xF0, 0xF0, 0x30)
        }

        setVisibleFollowBtn(false)

        if (!isReload) {
            applyRecentOpen(path, openItem.mStartLine)
        }
        repaint()

        return
    }

    private fun saveRecentFile() {
        if (!mItemFilterByFile.state || CurrentMethod != METHOD_OPEN) {
            return
        }

        mRecentFileManager.loadList()

        for (openItem in mRecentFileManager.mOpenList) {
            mRecentFileManager.mRecentList.removeIf { item: RecentFileManager.RecentItem -> openItem.mPath == item.mPath }
        }

        for (openItem in mRecentFileManager.mOpenList) {
            var isExist = false
            for (item in mRecentFileManager.mRecentList) {
                if (openItem.mPath == item.mPath) {
                    for (bookmark in mBookmarkManager.mBookmarks) {
                        if (bookmark >= openItem.mStartLine && bookmark <= openItem.mEndLine) {
                            item.mBookmarks += "${ bookmark - openItem.mStartLine },"
                        }
                    }
                    isExist = true
                }
            }

            if (isExist) {
                continue
            }

            val item = RecentFileManager.RecentItem()
            item.mPath = openItem.mPath

            item.mShowLogCheck = mShowLogToggle.isSelected
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                item.mTokenCheck[idx] = mTokenToggle[idx].isSelected
            }
            item.mHighlightLogCheck = mBoldLogToggle.isSelected
            item.mFindMatchCase = mFindPanel.mFindMatchCaseToggle.isSelected

            for (bookmark in mBookmarkManager.mBookmarks) {
                if (bookmark >= openItem.mStartLine && bookmark <= openItem.mEndLine) {
                    item.mBookmarks += "${ bookmark - openItem.mStartLine },"
                }
            }

            item.mShowLog = mShowLogCombo.selectedItem?.toString() ?: ""
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                item.mTokenFilter[idx] = mTokenCombo[idx].selectedItem?.toString() ?: ""
            }
            item.mHighlightLog = mBoldLogCombo.selectedItem?.toString() ?: ""
            item.mFindLog = mFindPanel.mFindCombo.selectedItem?.toString() ?: ""

            mRecentFileManager.mRecentList.add(0, item)
        }
        mRecentFileManager.saveList()
    }

    private fun saveRecentFileNew(path: String) {
        mRecentFileManager.loadList()

        mRecentFileManager.mRecentList.removeIf { item: RecentFileManager.RecentItem -> path == item.mPath }

        val item = RecentFileManager.RecentItem()
        item.mPath = path

        item.mShowLogCheck = mShowLogToggle.isSelected
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            item.mTokenCheck[idx] = mTokenToggle[idx].isSelected
        }
        item.mHighlightLogCheck = mBoldLogToggle.isSelected
        item.mFindMatchCase = mFindPanel.mFindMatchCaseToggle.isSelected

        item.mShowLog = mShowLogCombo.selectedItem?.toString() ?: ""
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            item.mTokenFilter[idx] = mTokenCombo[idx].selectedItem?.toString() ?: ""
        }
        item.mHighlightLog = mBoldLogCombo.selectedItem?.toString() ?: ""
        item.mFindLog = mFindPanel.mFindCombo.selectedItem?.toString() ?: ""

        mRecentFileManager.mRecentList.add(0, item)
        mRecentFileManager.saveList()
    }

    private fun sanitizeFileName(fileName: String): String {
        val pattern = Pattern.compile("[<>:\"/\\\\|?*]")
        val matcher = pattern.matcher(fileName)

        var newName = matcher.replaceAll("_")
        newName = newName.trim()

        if (fileName != newName) {
            Utils.printlnLog("filename is incorrect, changed from $fileName to $newName")
        }
        return newName
    }

    fun setSaveLogFile() {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HH.mm.ss")
        var device = mDeviceCombo.selectedItem!!.toString()
        device = device.substringBefore(":")
        if (mLogCmdManager.mPrefix.isEmpty()) {
            mLogCmdManager.mPrefix = LogCmdManager.DEFAULT_PREFIX
        }

        val fileName = sanitizeFileName("${mLogCmdManager.mPrefix}_${device}_${dtf.format(LocalDateTime.now())}.txt")
        val filePath = "${mLogCmdManager.mLogSavePath}/$fileName"
        var file = File(filePath)
        var idx = 1
        var filePathSaved = filePath
        while (file.isFile) {
            filePathSaved = "${filePath}-$idx.txt"
            file = File(filePathSaved)
            idx++
        }

        mFullLogPanel.mTableModel.setLogFile(filePathSaved)
        mFilteredLogPanel.mTableModel.setLogFile(filePathSaved)
        mStatusTF.text = filePathSaved
    }

    fun startAdbScan(reconnect: Boolean) {
        saveRecentFile()

        if (reconnect) {
            mLogCmdManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
        }

        if (mLogCmdManager.getType() == LogCmdManager.TYPE_CMD) {
            setCurrentMethod(METHOD_CMD)
            updateTablePNameColumn(false)
        }
        else {
            setCurrentMethod(METHOD_ADB)
            updateTablePNameColumn(LogTableModel.TypeShowProcessName != LogTableModel.SHOW_PROCESS_NONE)
        }

        mPauseToggle.isSelected = false
        setSaveLogFile()
        if (reconnect) {
            PackageManager.getInstance().updateUids(mFullLogPanel.mPackageBtns)
            mLogCmdManager.startLogcat()
        }
        mFilteredLogPanel.mTableModel.startScan()
        if (IsFlatLaf && !IsFlatLightLaf) {
            mStatusMethod.background = Color(0x00, 0x50, 0x00)
        }
        else {
            mStatusMethod.background = Color(0x90, 0xE0, 0x90)
        }

        setVisibleFollowBtn(false)
    }

    fun stopAdbScan() {
        if (mLogCmdManager.getType() == LogCmdManager.TYPE_CMD) {
            mStatusMethod.text = " ${Strings.CMD} ${Strings.STOP} "
        }
        else {
            mStatusMethod.text = " ${Strings.ADB} ${Strings.STOP} "
        }
        mStatusTF.text = mStatusTF.text

        if (!mFilteredLogPanel.mTableModel.isScanning()) {
            Utils.printlnLog("stopAdbScan : not adb scanning mode")
            return
        }
        mFilteredLogPanel.mTableModel.stopScan()
        if (IsFlatLaf && !IsFlatLightLaf) {
            mStatusMethod.background = Color(0x50, 0x50, 0x50)
        }
        else {
            mStatusMethod.background = Color.LIGHT_GRAY
        }
    }

    fun isRestartAdbLogcat(): Boolean {
        return mRetryAdbToggle.isSelected
    }

    fun restartAdbLogcat() {
        Utils.printlnLog("Restart Adb Logcat")
        mLogCmdManager.stop()
        mLogCmdManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
        mLogCmdManager.startLogcat()
    }

    fun pauseAdbScan(pause: Boolean) {
        if (!mFilteredLogPanel.mTableModel.isScanning()) {
            Utils.printlnLog("pauseAdbScan : not adb scanning mode")
            return
        }
        mFilteredLogPanel.mTableModel.pauseScan(pause)
    }

    fun startFileFollow(filePath: String) {
        saveRecentFile()

        if (filePath.isNotEmpty()) {
            mFullLogPanel.mTableModel.setLogFile(filePath)
            mFilteredLogPanel.mTableModel.setLogFile(filePath)
            updateTablePNameColumn(false)
            mStatusTF.text = filePath
        }

        setCurrentMethod(METHOD_FOLLOW)
        mOpenFileList.add(filePath)
        mPauseFollowToggle.isSelected = false
        mFilteredLogPanel.mTableModel.startFollow()

        if (IsFlatLaf && !IsFlatLightLaf) {
            mStatusMethod.background = Color(0x00, 0x00, 0x50)
        }
        else {
            mStatusMethod.background = Color(0xA0, 0xA0, 0xF0)
        }

        setVisibleFollowBtn(true)
    }

    fun stopFileFollow() {
        if (!mFilteredLogPanel.mTableModel.isFollowing()) {
            Utils.printlnLog("stopFileFollow : not file follow mode")
            return
        }
        mStatusMethod.text = " ${Strings.FOLLOW} ${Strings.STOP} "
        mFilteredLogPanel.mTableModel.stopFollow()
        if (IsFlatLaf && !IsFlatLightLaf) {
            mStatusMethod.background = Color(0x50, 0x50, 0x50)
        }
        else {
            mStatusMethod.background = Color.LIGHT_GRAY
        }
    }

    fun pauseFileFollow(pause: Boolean) {
        if (!mFilteredLogPanel.mTableModel.isFollowing()) {
            Utils.printlnLog("pauseFileFollow : not file follow mode")
            return
        }
        mFilteredLogPanel.mTableModel.pauseFollow(pause)
    }

    fun performToolsMenu(visible: Boolean, toolId: ToolsPane.Companion.ToolId) {
        val prevVisible = mToolsPane.isVisible
        if (visible) {
            mToolsPane.showTab(toolId)
        } else {
            mToolsPane.hideTab(toolId)
        }

        if (prevVisible != mToolsPane.isVisible) {
            if (mToolsPane.isVisible) {
                rotateToolSplitPane(mToolRotationStatus)
                mToolSplitPane.dividerSize = mLogSplitPane.dividerSize
                mToolSplitPane.dividerLocation = mToolSplitDividerLocation
                mToolSplitPane.lastDividerLocation = mToolSplitLastDividerLocation
            }
            else {
                mToolSplitPane.dividerSize = 0
                mToolSplitDividerLocation = mToolSplitPane.dividerLocation
                mToolSplitLastDividerLocation = mToolSplitPane.lastDividerLocation
            }
        }
        mToolSplitPane.revalidate()
        mToolSplitPane.repaint()

        if (prevVisible != mToolsPane.isVisible || mItemToolPanel.state != mToolsPane.isVisible) {
            mItemToolPanel.state = mToolsPane.isVisible
            mConfigManager.saveItem(ConfigManager.ITEM_TOOL_PANEL, mItemToolPanel.state.toString())
        }
    }

    internal inner class ActionHandler : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            when (p0?.source) {
                mItemFileOpen -> {
                    val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN, FileDialog.LOAD)
                    fileDialog.isMultipleMode = false
                    fileDialog.directory = mFullLogPanel.mTableModel.mLogFile?.parent
                    fileDialog.isVisible = true
                    if (fileDialog.file != null) {
                        val file = File(fileDialog.directory + fileDialog.file)
                        openFile(file.absolutePath, false, false)
                    } else {
                        Utils.printlnLog("Cancel Open")
                    }
                }
                mItemFileFollow -> {
                    val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.FOLLOW, FileDialog.LOAD)
                    fileDialog.isMultipleMode = false
                    fileDialog.directory = mFullLogPanel.mTableModel.mLogFile?.parent
                    fileDialog.isVisible = true
                    if (fileDialog.file != null) {
                        setVisibleFollowBtn(true)
                        val file = File(fileDialog.directory + fileDialog.file)
                        startFileFollow(file.absolutePath)
                    } else {
                        Utils.printlnLog("Cancel Open")
                    }
                }
                mItemFileOpenFiles -> {
                    val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN_FILES, FileDialog.LOAD)
                    fileDialog.isMultipleMode = true
                    fileDialog.directory = mFullLogPanel.mTableModel.mLogFile?.parent
                    fileDialog.isVisible = true
                    val fileList = fileDialog.files
                    if (fileList != null) {
                        var isFirst = true
                        for (file in fileList) {
                            if (isFirst) {
                                openFile(file.absolutePath, false, false)
                                isFirst = false
                            } else {
                                openFile(file.absolutePath, true, false)
                            }
                        }
                    } else {
                        Utils.printlnLog("Cancel Open")
                    }
                }
                mItemFileAppendFiles -> {
                    val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.APPEND_FILES, FileDialog.LOAD)
                    fileDialog.isMultipleMode = true
                    fileDialog.directory = mFullLogPanel.mTableModel.mLogFile?.parent
                    fileDialog.isVisible = true
                    val fileList = fileDialog.files
                    if (fileList != null) {
                        for (file in fileList) {
                            openFile(file.absolutePath, true, false)
                        }
                    } else {
                        Utils.printlnLog("Cancel Open")
                    }
                }
                mItemFileSaveFull, mItemFileSaveFiltered-> {
                    val title: String
                    val tableModel: LogTableModel
                    if (p0.source == mItemFileSaveFull) {
                        title = Strings.SAVE_FULL
                        tableModel = mFullLogPanel.mTableModel
                    } else {
                        title = Strings.SAVE_FILTERED
                        tableModel = mFilteredLogPanel.mTableModel
                    }

                    val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + title, FileDialog.SAVE)
                    fileDialog.isMultipleMode = false
                    if (mFileSaveDir.isEmpty()) {
                        mFileSaveDir = mLogCmdManager.mLogSavePath
                    }
                    fileDialog.directory = mFileSaveDir
                    fileDialog.isVisible = true
                    if (fileDialog.files.isNotEmpty() && fileDialog.files[0] != null) {
                        Utils.printlnLog("$title ${fileDialog.files[0].absoluteFile}")
                        mFileSaveDir = fileDialog.files[0].parent
                        tableModel.saveFile(fileDialog.files[0].absolutePath)
                        saveRecentFileNew(fileDialog.files[0].absolutePath)
                    } else {
                        Utils.printlnLog("Cancel $title")
                    }
                }
                mItemLogCmd, mItemLogFile -> {
                    val settingsDialog = LogCmdSettingsDialog(this@MainUI)
                    settingsDialog.setLocationRelativeTo(this@MainUI)
                    settingsDialog.isVisible = true
                }
                mItemLogFormat -> {
                    mFormatManager.showFormatListDialog(this@MainUI)
                }
                mItemFull -> {
                    if (mItemFull.state) {
                        attachLogPanel(mFullLogPanel)
                    } else {
                        windowedModeLogPanel(mFullLogPanel)
                    }

                    updateLogPanelTableBar()
                    mItemFullLogToNewWindow.state = !mItemFull.state
                    mItemFullLogToNewWindow.isEnabled = mItemFull.state

                    mConfigManager.saveItem(ConfigManager.ITEM_VIEW_FULL, mItemFull.state.toString())
                }

                mItemToolPanel -> {
                    performToolsMenu(mItemToolPanel.state, ToolsPane.Companion.ToolId.TOOL_ID_PANEL)
                }

                mItemToolSelection -> {
                    performToolsMenu(mItemToolSelection.state, ToolsPane.Companion.ToolId.TOOL_ID_SELECTION)
                    mConfigManager.saveItem(ConfigManager.ITEM_TOOL_SELECTION, mItemToolSelection.state.toString())
                }

                mItemToolTest -> {
                    performToolsMenu(mItemToolTest.state, ToolsPane.Companion.ToolId.TOOL_ID_TEST)
                    mConfigManager.saveItem(ConfigManager.ITEM_TOOL_TEST, mItemToolTest.state.toString())
                }

                mItemFullLogToNewWindow -> {
                    if (mItemFull.state) {
                        if (mItemFullLogToNewWindow.state) {
                            windowedModeLogPanel(mFullLogPanel)
                        } else {
                            attachLogPanel(mFullLogPanel)
                        }
                    }
                }

                mItemColumnMode -> {
                    mConfigManager.saveItem(ConfigManager.ITEM_VIEW_COLUMN_MODE, mItemColumnMode.state.toString())
                    resetLogPanel(true)
                }

                mItemFind -> {
                    mFindPanel.isVisible = !mFindPanel.isVisible
                    mItemFind.state = mFindPanel.isVisible
                }

                mItemTrigger -> {
                    if (!mAgingTestManager.mTriggerPanel.isVisible) {
                        mAgingTestManager.mTriggerPanel.isVisible = true
                    }
                    else {
                        if (mAgingTestManager.mTriggerPanel.canHide()) {
                            mAgingTestManager.mTriggerPanel.isVisible = false
                        }
                        else {
                            JOptionPane.showMessageDialog(this@MainUI, Strings.TRIGGER_CANNOT_HIDE, Strings.WARNING, JOptionPane.WARNING_MESSAGE)
                        }
                    }
                }

                mItemFilterIncremental -> {
                    mConfigManager.saveItem(ConfigManager.ITEM_FILTER_INCREMENTAL, mItemFilterIncremental.state.toString())
                }

                mItemFilterByFile -> {
                    mConfigManager.saveItem(ConfigManager.ITEM_FILTER_BY_FILE, mItemFilterByFile.state.toString())
                }

                mItemColorTagRegex -> {
                    LogTableModel.IsColorTagRegex = mItemColorTagRegex.state
                    mConfigManager.saveItem(ConfigManager.ITEM_COLOR_TAG_REGEX, mItemColorTagRegex.state.toString())
                    repaint()
                }

                mItemAppearance -> {
                    val appearanceSettingsDialog = AppearanceSettingsDialog(this@MainUI)
                    appearanceSettingsDialog.setLocationRelativeTo(this@MainUI)
                    appearanceSettingsDialog.isVisible = true
                }
                mItemTool -> {
                    val toolSettingsDialog = ToolSettingsDialog(this@MainUI)
                    toolSettingsDialog.setLocationRelativeTo(this@MainUI)
                    toolSettingsDialog.isVisible = true
                }
                mItemAbout -> {
                    val aboutDialog = AboutDialog(this@MainUI)
                    aboutDialog.setLocationRelativeTo(this@MainUI)
                    aboutDialog.isVisible = true
                }
                mItemHelp -> {
                    val helpDialog = HelpGotoDialog(this@MainUI)
                    helpDialog.setLocationRelativeTo(this@MainUI)
                    helpDialog.isVisible = true
                }
                mItemCheckUpdate -> {
                    val checkUpdateDialog = CheckUpdateDialog(this@MainUI)
                    checkUpdateDialog.setLocationRelativeTo(this@MainUI)
                    checkUpdateDialog.isVisible = true
                }
                mAdbConnectBtn -> {
                    stopAdbScan()
                    mLogCmdManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
                    mLogCmdManager.connect()
                }
                mAdbRefreshBtn -> {
                    mLogCmdManager.getDevices()
                }
                mAdbDisconnectBtn -> {
                    stopAdbScan()
                    mLogCmdManager.disconnect()
                }
                mScrollbackApplyBtn -> {
                    try {
                        mFilteredLogPanel.mTableModel.mScrollback = mScrollbackTF.text.toString().trim().toInt()
                    } catch (e: java.lang.NumberFormatException) {
                        mFilteredLogPanel.mTableModel.mScrollback = 100000
                        mScrollbackTF.text = "100000"
                    }
                    mFilteredLogPanel.mTableModel.mScrollbackSplitFile = mScrollbackSplitFileToggle.isSelected

                    mConfigManager.saveItem(ConfigManager.ITEM_SCROLLBACK, mScrollbackTF.text)
                    mConfigManager.saveItem(ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE, mScrollbackSplitFileToggle.isSelected.toString())
                }
                mStartBtn -> {
                    startAdbScan(true)
                }
                mStopBtn -> {
                    stopAdbScan()
                    mLogCmdManager.stop()
    //            } else if (p0?.source == mPauseBtn) {
                }
                mClearViewsBtn -> {
                    mFilteredLogPanel.mTableModel.clearItems()
                    repaint()
                }
                mSaveBtn -> {
    //                mFilteredLogPanel.mTableModel.clearItems()
                    if (mFilteredLogPanel.mTableModel.isScanning()) {
                        setSaveLogFile()
                    }
                    else {
                        Utils.printlnLog("SaveBtn : not adb scanning mode")
                    }
    //                repaint()
                }
                mItemRotation -> {
                    mRotationStatus++

                    if (mRotationStatus > ROTATION_MAX) {
                        mRotationStatus = Companion.ROTATION_LEFT_RIGHT
                    }

                    mConfigManager.saveItem(ConfigManager.ITEM_ROTATION, mRotationStatus.toString())
                    rotateLogSplitPane(true)
                }
//                mFiltersBtn -> {
//                    mFiltersManager.showDialog()
//                }
//                mCmdsBtn -> {
//                    mCmdsManager.showDialog()
//                }
                mStartFollowBtn -> {
                    startFileFollow("")
                }
                mStopFollowBtn -> {
                    stopFileFollow()
                }
                mStatusReloadBtn -> {
                    if (mOpenFileList.isNotEmpty()) {
                        for ((idx, path) in mOpenFileList.withIndex()) {
                            openFile(path, idx != 0, true)
                        }
                    }
                }
            }
        }
    }

    internal inner class FramePopUp : JPopupMenu() {
        var mItemIconText: JMenuItem = JMenuItem("IconText")
        var mItemIcon: JMenuItem = JMenuItem("Icon")
        var mItemText: JMenuItem = JMenuItem("Text")
        private val mActionHandler = ActionHandler()

        init {
            mItemIconText.addActionListener(mActionHandler)
            add(mItemIconText)
            mItemIcon.addActionListener(mActionHandler)
            add(mItemIcon)
            mItemText.addActionListener(mActionHandler)
            add(mItemText)
        }
        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mItemIconText -> {
                        setBtnIconsTexts(true, true)
                        mConfigManager.saveItem(ConfigManager.ITEM_ICON_TEXT, ConfigManager.VALUE_ICON_TEXT_I_T)
                    }
                    mItemIcon -> {
                        setBtnIconsTexts(true, false)
                        mConfigManager.saveItem(ConfigManager.ITEM_ICON_TEXT, ConfigManager.VALUE_ICON_TEXT_I)
                    }
                    mItemText -> {
                        setBtnIconsTexts(false, true)
                        mConfigManager.saveItem(ConfigManager.ITEM_ICON_TEXT, ConfigManager.VALUE_ICON_TEXT_I)
                    }
                }
            }
        }
    }
    internal inner class FrameMouseListener(frame: JFrame) : MouseAdapter() {
        private val mFrame = frame
        private var mDownPoint: Point? = null

        private var mPopupMenu: JPopupMenu? = null
        override fun mouseReleased(e: MouseEvent) {
            mDownPoint = null

            if (SwingUtilities.isRightMouseButton(e)) {
                if (e.source == this@MainUI.contentPane) {
                    mPopupMenu = FramePopUp()
                    mPopupMenu?.show(e.component, e.x, e.y)
                }
            }
            else {
                mPopupMenu?.isVisible = false
            }
        }

        override fun mousePressed(e: MouseEvent) {
            mDownPoint = e.point
        }

        override fun mouseDragged(e: MouseEvent) {
            val currPoint = e.locationOnScreen
            mFrame.setLocation(currPoint.x - mDownPoint!!.x, currPoint.y - mDownPoint!!.y)
        }
    }

    internal inner class PopUpCombobox(combo: JComboBox<String>?) : JPopupMenu() {
        var mSelectAllItem: JMenuItem
        var mCopyItem: JMenuItem
        var mPasteItem: JMenuItem
        var mReconnectItem: JMenuItem
        var mCombo: JComboBox<String>?
        private val mActionHandler = ActionHandler()

        init {
            mSelectAllItem = JMenuItem(Strings.SELECT_ALL)
            mSelectAllItem.addActionListener(mActionHandler)
            add(mSelectAllItem)
            mCopyItem = JMenuItem(Strings.COPY)
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mPasteItem = JMenuItem(Strings.PASTE)
            mPasteItem.addActionListener(mActionHandler)
            add(mPasteItem)
            mReconnectItem = JMenuItem("${Strings.RECONNECT} " + mDeviceCombo.selectedItem?.toString())
            mReconnectItem.addActionListener(mActionHandler)
            add(mReconnectItem)
            mCombo = combo
        }
        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mSelectAllItem -> {
                        mCombo?.editor?.selectAll()
                    }
                    mCopyItem -> {
                        val editorCom = mCombo?.editor?.editorComponent as JTextComponent
                        val stringSelection = StringSelection(editorCom.selectedText)
                        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(stringSelection, null)
                    }
                    mPasteItem -> {
                        val editorCom = mCombo?.editor?.editorComponent as JTextComponent
                        editorCom.paste()
                    }
                    mReconnectItem -> {
                        reconnectAdb()
                    }
                }
            }
        }
    }

    internal inner class PopUpFilterCombobox(combo: FilterComboBox) : JPopupMenu() {
        var mSelectAllItem: JMenuItem
        var mCopyItem: JMenuItem
        var mPasteItem: JMenuItem
        var mRemoveItem: JMenuItem
        var mRemoveOthersItem: JMenuItem
        var mRemoveColorTagsItem: JMenuItem
        lateinit var mRemoveOneColorTagItem: JMenuItem
        lateinit var mAddColorTagItems: ArrayList<JMenuItem>
        var mCombo: FilterComboBox
        private val mActionHandler = ActionHandler()
        private val mAddColorTagAction: Action
        private val mAddColorTagKey = "add_color_tag"

        init {
            mCombo = combo
            mAddColorTagAction = object : AbstractAction(mAddColorTagKey) {
                override fun actionPerformed(evt: ActionEvent?) {
                    if (evt != null) {
                        val textSplit = evt.actionCommand.split(":")
                        if (textSplit.size == 2) {
                            mCombo.addColorTag(textSplit[1].trim())
                            if (mCombo == mShowLogCombo) {
                                applyShowLogComboEditor()
                            }
                        }
                    }
                }
            }
            mSelectAllItem = JMenuItem(Strings.SELECT_ALL)
            mSelectAllItem.addActionListener(mActionHandler)
            add(mSelectAllItem)
            mCopyItem = JMenuItem(Strings.COPY)
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mPasteItem = JMenuItem(Strings.PASTE)
            mPasteItem.addActionListener(mActionHandler)
            add(mPasteItem)
            addSeparator()
            mRemoveItem = JMenuItem(Strings.REMOVE)
            mRemoveItem.addActionListener(mActionHandler)
            add(mRemoveItem)
            mRemoveOthersItem = JMenuItem(Strings.REMOVE_OTHERS)
            mRemoveOthersItem.addActionListener(mActionHandler)
            add(mRemoveOthersItem)
            addSeparator()
            mRemoveColorTagsItem = JMenuItem(Strings.REMOVE_ALL_COLOR_TAGS)
            mRemoveColorTagsItem.isOpaque = true
            mRemoveColorTagsItem.foreground = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredFGs[0])
            mRemoveColorTagsItem.background = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredBGs[0])
            mRemoveColorTagsItem.addActionListener(mActionHandler)
            add(mRemoveColorTagsItem)

            if (mCombo.mUseColorTag) {
                mRemoveOneColorTagItem = JMenuItem(Strings.REMOVE_COLOR_TAG)
                mRemoveOneColorTagItem.isOpaque = true
                mRemoveOneColorTagItem.foreground = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredFGs[0])
                mRemoveOneColorTagItem.background = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredBGs[0])
                mRemoveOneColorTagItem.addActionListener(mActionHandler)
                add(mRemoveOneColorTagItem)
                mAddColorTagItems = arrayListOf()
                for (idx in 0..8) {
                    val num = idx + 1
                    val item = JMenuItem("${Strings.ADD_COLOR_TAG} : #$num")
                    item.isOpaque = true
                    item.foreground = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredFGs[num])
                    item.background = Color.decode(ColorManager.getInstance().mFilterTableColor.mStrFilteredBGs[num])
                    item.addActionListener(mAddColorTagAction)
                    mAddColorTagItems.add(item)
                    add(item)
                }
            }
        }

        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mSelectAllItem -> {
                        mCombo.editor?.selectAll()
                    }
                    mCopyItem -> {
                        val editorCom = mCombo.editor?.editorComponent as JTextComponent
                        val stringSelection = StringSelection(editorCom.selectedText)
                        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(stringSelection, null)
                    }
                    mPasteItem -> {
                        val editorCom = mCombo.editor?.editorComponent as JTextComponent
                        editorCom.paste()
                    }
                    mRemoveItem -> {
                        val editorCom = mCombo.editor?.editorComponent as JTextComponent
                        editorCom.replaceSelection("")
                        if (mCombo == mShowLogCombo) {
                            applyShowLogComboEditor()
                        }
                    }
                    mRemoveOthersItem -> {
                        val editorCom = mCombo.editor?.editorComponent as JTextComponent
                        editorCom.text = editorCom.selectedText
                        if (mCombo == mShowLogCombo) {
                            applyShowLogComboEditor()
                        }
                    }
                    mRemoveColorTagsItem -> {
                        mCombo.removeAllColorTags()
                        if (mCombo == mShowLogCombo) {
                            applyShowLogComboEditor()
                        }
                    }
                    mRemoveOneColorTagItem -> {
                        mCombo.removeColorTag()
                        if (mCombo == mShowLogCombo) {
                            applyShowLogComboEditor()
                        }
                    }
                }
            }
        }
    }

    fun setVisibleFollowBtn(visible: Boolean) {
        if (CurrentMethod == METHOD_FOLLOW && !visible) {
            Utils.printlnLog("Follow ctrl btns :  cannot be hidden in \"follow mode\"")
            return
        }
        mFollowLabel.isVisible = visible
        mStartFollowBtn.isVisible = visible
        mPauseFollowToggle.isVisible = visible
        mStopFollowBtn.isVisible = visible
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            if (p0?.source == mFollowLabel) {
                setVisibleFollowBtn(!mStartFollowBtn.isVisible)
            }
            super.mouseClicked(p0)
        }

        private var popupMenu: JPopupMenu? = null
        override fun mouseReleased(p0: MouseEvent?) {
            if (p0 == null) {
                super.mouseReleased(p0)
                return
            }

            if (SwingUtilities.isRightMouseButton(p0)) {
                var isNeedCheck = true
                for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                    if (p0.source == mTokenCombo[idx].editor.editorComponent) {
                        popupMenu = PopUpFilterCombobox(mTokenCombo[idx])
                        popupMenu?.show(p0.component, p0.x, p0.y)
                        isNeedCheck = false
                        break
                    }
                }
                if (isNeedCheck) {
                    when (p0.source) {
                        mDeviceCombo.editor.editorComponent -> {
                            popupMenu = PopUpCombobox(mDeviceCombo)
                            popupMenu?.show(p0.component, p0.x, p0.y)
                        }

                        mShowLogCombo.editor.editorComponent, mBoldLogCombo.editor.editorComponent -> {
                            mShowLogCombo.requestFocus()
                            lateinit var combo: FilterComboBox
                            when (p0.source) {
                                mShowLogCombo.editor.editorComponent -> {
                                    if (mShowLogCombo.editor.editorComponent is JTextArea) {
                                        val textArea = mShowLogCombo.editor.editorComponent as JTextArea
                                        val offset = textArea.viewToModel(p0.point)
                                        var needSelect = true
                                        if (!textArea.selectedText.isNullOrEmpty()) {
                                            if (offset >= textArea.selectionStart && offset <= textArea.selectionEnd) {
                                                needSelect = false
                                            }
                                        }
                                        if (needSelect) {
                                            val text = textArea.text
                                            if (offset >= 0) {
                                                var start = offset
                                                var end = offset
                                                while (start > 0 && text[start - 1] != '|') {
                                                    start--
                                                }

                                                while (end < text.length && text[end] != '|') {
                                                    end++
                                                }
                                                textArea.select(start, end)
                                            }
                                        }
                                    }
                                    combo = mShowLogCombo
                                }

                                mBoldLogCombo.editor.editorComponent -> {
                                    combo = mBoldLogCombo
                                }
                            }
                            popupMenu = PopUpFilterCombobox(combo)
                            popupMenu?.show(p0.component, p0.x, p0.y)
                        }
                        else -> {
                            val compo = p0.source as JComponent
                            val event = MouseEvent(
                                compo.parent,
                                p0.id,
                                p0.`when`,
                                p0.modifiers,
                                p0.x + compo.x,
                                p0.y + compo.y,
                                p0.clickCount,
                                p0.isPopupTrigger
                            )

                            compo.parent.dispatchEvent(event)
                        }
                    }
                }
            }
            else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }
    }

    fun reconnectAdb() {
        Utils.printlnLog("Reconnect ADB")
        mStopBtn.doClick()
        Thread.sleep(200)

        if (mDeviceCombo.selectedItem!!.toString().isNotBlank()) {
            mAdbConnectBtn.doClick()
            Thread.sleep(200)
        }

        Thread {
            run {
                Thread.sleep(200)
                mClearViewsBtn.doClick()
                Thread.sleep(200)
                mStartBtn.doClick()
            }
        }.start()
    }

    fun startAdbLog() {
        Thread {
            run {
                mStartBtn.doClick()
            }
        }.start()
    }

    fun stopAdbLog() {
        mStopBtn.doClick()
    }

    fun clearAdbLog() {
        Thread {
            run {
                mClearViewsBtn.doClick()
            }
        }.start()
    }

//    fun clearSaveAdbLog() {
//        Thread(Runnable {
//            run {
//                mSaveBtn.doClick()
//            }
//        }).start()
//    }

    fun getTextShowLogCombo() : String {
        if (mShowLogCombo.selectedItem == null) {
           return ""
        }
        return mShowLogCombo.selectedItem!!.toString()
    }

    fun setTextShowLogCombo(text : String) {
        mShowLogCombo.setFilterText(text)
    }

    fun applyShowLogCombo(isCheck: Boolean) {
        mShowLogCombo.applyFilterText(isCheck)
    }

    fun applyShowLogComboEditor() {
        mShowLogCombo.applyFilterTextEditor()
    }

    fun removeIncludeFilterShowLogCombo(filterRemove: String) {
        if (filterRemove.isNotEmpty()) {
            val text = getTextShowLogCombo()
            if (text.isNotEmpty()) {
                val filterList = mutableListOf<String>()
                val filterRegex = if (mMatchCaseToggle.isSelected) {
                    Regex("(#[0-9])?$filterRemove")
                } else {
                    Regex("(#[0-9])?$filterRemove", RegexOption.IGNORE_CASE)
                }
                val filterSplit = text.split("|")
                var isChanged = false
                for (str in filterSplit) {
                    if (!filterRegex.matches(str)) {
                        filterList.add(str)
                    }
                    else {
                        isChanged = true
                    }
                }

                if (isChanged) {
                    val newFilter = StringBuilder()
                    for (filter in filterList) {
                        if (newFilter.isNotEmpty()) {
                            newFilter.append("|")
                        }
                        newFilter.append(filter)
                    }
                    setTextShowLogCombo(newFilter.toString())
                    applyShowLogCombo(true)
                }
                else {
                    JOptionPane.showMessageDialog(this, "${Strings.NO_FILTER_MATCHING} '$filterRemove'\n(${Strings.NO_FILTER_MATCHING_2})", Strings.REMOVE_INCLUDE, JOptionPane.INFORMATION_MESSAGE)
                }
            }
            else {
                JOptionPane.showMessageDialog(this, Strings.FILTERS_ARE_EMPTY, Strings.REMOVE_INCLUDE, JOptionPane.INFORMATION_MESSAGE)
            }
        }
    }

    fun getTextFindCombo() : String {
        if (mFindPanel.mFindCombo.selectedItem == null) {
            return ""
        }
        return mFindPanel.mFindCombo.selectedItem!!.toString()
    }

    fun setTextFindCombo(text : String) {
        mFindPanel.mFindCombo.selectedItem = text
        mFilteredLogPanel.mTableModel.mFilterFindLog = mFindPanel.mFindCombo.selectedItem!!.toString()
        mFindPanel.isVisible = true
        mItemFind.state = mFindPanel.isVisible
    }

    fun setDeviceComboColor(isConnected: Boolean) {
        if (isConnected) {
            if (IsFlatLaf && !IsFlatLightLaf) {
                mDeviceCombo.editor.editorComponent.foreground = Color(0x7070C0)
            }
            else {
                mDeviceCombo.editor.editorComponent.foreground = Color.BLUE
            }
        } else {
            if (IsFlatLaf && !IsFlatLightLaf) {
                mDeviceCombo.editor.editorComponent.foreground = Color(0xC07070)
            }
            else {
                mDeviceCombo.editor.editorComponent.foreground = Color.RED
            }
        }
    }

    fun updateLogCmdCombo(isReload: Boolean) {
        if (isReload) {
            var logCmd: String?
            val currLogCmd = mLogCmdCombo.editor.item.toString()
            mLogCmdCombo.removeAllItems()
            for (i in 0 until LogCmdManager.LOG_CMD_MAX) {
                logCmd = mConfigManager.getItem("${ConfigManager.ITEM_ADB_LOG_CMD}_$i")
                if (logCmd.isNullOrBlank()) {
                    continue
                }

                mLogCmdCombo.addItem(logCmd)
            }
            mLogCmdCombo.selectedIndex = -1
            if (currLogCmd.isBlank()) {
                mLogCmdCombo.editor.item = mLogCmdManager.mLogCmd
            }
            else {
                mLogCmdCombo.editor.item = currLogCmd
            }
            mLogCmdCombo.selectedItem = mLogCmdCombo.editor.item
        }

        mLogCmdCombo.toolTipText = "\"${mLogCmdManager.mLogCmd}\"\n\n${TooltipStrings.LOG_CMD_COMBO}"

        if (mLogCmdManager.mLogCmd == mLogCmdCombo.editor.item.toString()) {
            if (IsFlatLaf && !IsFlatLightLaf) {
                mLogCmdCombo.editor.editorComponent.foreground = Color(0x7070C0)
            }
            else {
                mLogCmdCombo.editor.editorComponent.foreground = Color.BLUE
            }
        } else {
            if (IsFlatLaf && !IsFlatLightLaf) {
                mLogCmdCombo.editor.editorComponent.foreground = Color(0xC07070)
            }
            else {
                mLogCmdCombo.editor.editorComponent.foreground = Color.RED
            }
        }
    }

    internal inner class KeyHandler : KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (KeyEvent.VK_ENTER != p0?.keyCode && p0?.source == mLogCmdCombo.editor.editorComponent) {
                updateLogCmdCombo(false)
            }

            if (KeyEvent.VK_ENTER == p0?.keyCode) {
                when (p0.source) {
                    mLogCmdCombo.editor.editorComponent -> {
                        if (mLogCmdManager.mLogCmd == mLogCmdCombo.editor.item.toString()) {
                            reconnectAdb()
                        } else {
                            val item = mLogCmdCombo.editor.item.toString().trim()

                            if (item.isEmpty()) {
                                mLogCmdCombo.editor.item = LogCmdManager.DEFAULT_LOGCAT
                            }
                            mLogCmdManager.mLogCmd = mLogCmdCombo.editor.item.toString()
                            updateLogCmdCombo(false)
                        }
                    }
                    mDeviceCombo.editor.editorComponent -> {
                        reconnectAdb()
                    }
                    mScrollbackTF -> {
                        mScrollbackApplyBtn.doClick()
                    }
                }
            }
            super.keyReleased(p0)
        }
    }

    internal inner class ItemHandler : ItemListener {
        override fun itemStateChanged(p0: ItemEvent?) {
            var isNeedCheck = true
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                if (p0?.source == mTokenToggle[idx]) {
                    mTokenCombo[idx].setEnabledFilter(mTokenToggle[idx].isSelected)
                    isNeedCheck = false
                    break
                }
            }
            if (isNeedCheck) {
                when (p0?.source) {
                    mShowLogToggle -> {
                        mShowLogCombo.setEnabledFilter(mShowLogToggle.isSelected)
                    }

                    mBoldLogToggle -> {
                        mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)
                    }

                }
            }

            if (IsCreatingUI) {
                return
            }

            isNeedCheck = true
            val formatName = mFormatManager.mCurrFormat.mName
            val tokens = mFormatManager.mCurrFormat.mTokenFilters
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                if (p0?.source == mTokenToggle[idx]) {
                    if (mTokenToggle[idx].isSelected && mTokenCombo[idx].selectedItem != null) {
                        mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, mTokenCombo[idx].selectedItem!!.toString())
                    } else {
                        mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, "")
                    }
                    mConfigManager.saveItem("${ConfigManager.ITEM_TOKEN_CHECK}${formatName}_${tokens[idx].mToken}", mTokenToggle[idx].isSelected.toString())
                    isNeedCheck = false
                    break
                }
            }
            if (isNeedCheck) {
                when (p0?.source) {
                    mShowLogToggle -> {
                        if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
                            mFilteredLogPanel.mTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
                        } else {
                            mFilteredLogPanel.mTableModel.mFilterLog = ""
                        }
                        mConfigManager.saveItem(ConfigManager.ITEM_SHOW_LOG_CHECK, mShowLogToggle.isSelected.toString())
                    }

                    mBoldLogToggle -> {
                        if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
                            mFilteredLogPanel.mTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
                        } else {
                            mFilteredLogPanel.mTableModel.mFilterHighlightLog = ""
                        }
                        mConfigManager.saveItem(
                            ConfigManager.ITEM_HIGHLIGHT_LOG_CHECK,
                            mBoldLogToggle.isSelected.toString()
                        )
                    }

                    mMatchCaseToggle -> {
                        mFilteredLogPanel.mTableModel.mMatchCase = mMatchCaseToggle.isSelected
                        mConfigManager.saveItem(ConfigManager.ITEM_MATCH_CASE, mMatchCaseToggle.isSelected.toString())
                    }

                    mScrollbackKeepToggle -> {
                        mFilteredLogPanel.mTableModel.mScrollbackKeep = mScrollbackKeepToggle.isSelected
                    }

                    mRetryAdbToggle -> {
                        mConfigManager.saveItem(ConfigManager.ITEM_RETRY_ADB, mRetryAdbToggle.isSelected.toString())
                    }

                    mPauseToggle -> {
                        pauseAdbScan(mPauseToggle.isSelected)
                    }

                    mPauseFollowToggle -> {
                        pauseFileFollow(mPauseFollowToggle.isSelected)
                    }
                    mItemProcessNameNone -> {
                        if (mItemProcessNameNone.isSelected) {
                            mConfigManager.saveItem(ConfigManager.ITEM_VIEW_PROCESS_NAME, LogTableModel.SHOW_PROCESS_NONE.toString())
                            LogTableModel.TypeShowProcessName = LogTableModel.SHOW_PROCESS_NONE
                            updateTablePNameColumn(false)
                        }
                    }
                    mItemProcessNameShow -> {
                        if (mItemProcessNameShow.isSelected) {
                            mConfigManager.saveItem(ConfigManager.ITEM_VIEW_PROCESS_NAME, LogTableModel.SHOW_PROCESS_SHOW.toString())
                            LogTableModel.TypeShowProcessName = LogTableModel.SHOW_PROCESS_SHOW
                            updateTablePNameColumn(false)
                            updateTablePNameColumn(CurrentMethod == METHOD_ADB)
                        }
                    }
                    mItemProcessNameColor -> {
                        if (mItemProcessNameColor.isSelected) {
                            mConfigManager.saveItem(ConfigManager.ITEM_VIEW_PROCESS_NAME, LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR.toString())
                            LogTableModel.TypeShowProcessName = LogTableModel.SHOW_PROCESS_SHOW_WITH_BGCOLOR
                            updateTablePNameColumn(false)
                            updateTablePNameColumn(CurrentMethod == METHOD_ADB)
                        }
                    }
                }
            }
        }
    }

    internal inner class AdbHandler : LogCmdManager.AdbEventListener {
        override fun changedStatus(event: LogCmdManager.AdbEvent) {
            when (event.cmd) {
                LogCmdManager.CMD_CONNECT -> {
                    mLogCmdManager.getDevices()
                }
                LogCmdManager.CMD_GET_DEVICES -> {
                    if (IsCreatingUI) {
                        return
                    }
                    var selectedItem = mDeviceCombo.selectedItem
                    mDeviceCombo.removeAllItems()
                    for (item in mLogCmdManager.mDevices) {
                        mDeviceCombo.addItem(item)
                    }
                    if (selectedItem == null) {
                        selectedItem = ""
                    }

                    if (mLogCmdManager.mDevices.contains(selectedItem.toString())) {
                        setDeviceComboColor(true)
                    } else {
                        var isExist = false
                        val deviceChk = "$selectedItem:"
                        for (device in mLogCmdManager.mDevices) {
                            if (device.contains(deviceChk)) {
                                isExist = true
                                selectedItem = device
                                break
                            }
                        }
                        if (isExist) {
                            setDeviceComboColor(true)
                        } else {
                            setDeviceComboColor(false)
                        }
                    }
                    mDeviceCombo.selectedItem = selectedItem
                }
                LogCmdManager.CMD_DISCONNECT -> {
                    mLogCmdManager.getDevices()
                }
            }
        }
    }

    internal inner class PopupMenuHandler : PopupMenuListener {
        private var mIsCanceled = false
        override fun popupMenuWillBecomeInvisible(p0: PopupMenuEvent?) {
            if (mIsCanceled) {
                mIsCanceled = false
                return
            }
            var isNeedCheck = true
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                if (p0?.source == mTokenCombo[idx]) {
                    if (mTokenCombo[idx].selectedIndex < 0) {
                        return
                    }
                    val combo = mTokenCombo[idx]
                    val item = combo.selectedItem!!.toString()
                    combo.resetComboItem(item)
                    mFilteredLogPanel.mTableModel.mFilterTokenMgr.set(idx, item)
                    combo.updateTooltip()
                    isNeedCheck = false
                    break
                }
            }
            if (isNeedCheck) {
                when (p0?.source) {
                    mShowLogCombo -> {
                        if (mShowLogCombo.selectedIndex < 0) {
                            return
                        }
                        val combo = mShowLogCombo
                        val item = combo.selectedItem!!.toString()
                        if (combo.editor.item.toString() != item) {
                            return
                        }
                        combo.resetComboItem(item)
                        mFilteredLogPanel.mTableModel.mFilterLog = item
                        combo.updateTooltip()
                    }

                    mBoldLogCombo -> {
                        if (mBoldLogCombo.selectedIndex < 0) {
                            return
                        }
                        val combo = mBoldLogCombo
                        val item = combo.selectedItem!!.toString()
                        combo.resetComboItem(item)
                        mFilteredLogPanel.mTableModel.mFilterHighlightLog = item
                        combo.updateTooltip()
                    }

                    mLogCmdCombo -> {
                        if (mLogCmdCombo.selectedIndex < 0) {
                            return
                        }
                        val combo = mLogCmdCombo
                        val item = combo.selectedItem!!.toString()
                        mLogCmdManager.mLogCmd = item
                        updateLogCmdCombo(false)
                    }

                    mLogLevelCombo -> {
                        if (mLogLevelCombo.selectedIndex < 0) {
                            return
                        }
                        mFilteredLogPanel.mTableModel.mFilterLevel = mLogLevelCombo.selectedIndex
                        mConfigManager.saveItem(ConfigManager.ITEM_LOG_LEVEL, mLogLevelCombo.selectedIndex.toString())
                    }

                    mLogFormatCombo -> {
                        if (mLogFormatCombo.selectedIndex < 0) {
                            return
                        }
                        mLogFormatCombo.selectedItem?.let { mFormatManager.setCurrFormat(it.toString()) }
                        mConfigManager.saveItem(ConfigManager.ITEM_LOG_FORMAT, mFormatManager.mCurrFormat.mName)
                    }
                }
            }
        }

        override fun popupMenuCanceled(p0: PopupMenuEvent?) {
            mIsCanceled = true
        }

        override fun popupMenuWillBecomeVisible(p0: PopupMenuEvent?) {
            val box = p0?.source as JComboBox<*>
            val comp = box.ui.getAccessibleChild(box, 0) as? JPopupMenu ?: return
            val scrollPane = comp.getComponent(0) as JScrollPane
            scrollPane.verticalScrollBar?.setUI(BasicScrollBarUI())
            scrollPane.horizontalScrollBar?.setUI(BasicScrollBarUI())
            mIsCanceled = false
        }
    }

    internal inner class ComponentHandler : ComponentAdapter() {
        override fun componentResized(p0: ComponentEvent?) {
            revalidate()
            super.componentResized(p0)
        }
    }

    internal inner class MenuHandler : MenuListener {
        override fun menuSelected(e: MenuEvent?) {
            if (e?.source == mItemFileOpenRecents) {
                updateRecentFiles()
            }
            else if (e?.source == mMenuView) {
                mItemTrigger.state = mAgingTestManager.mTriggerPanel.isVisible
            }
        }

        override fun menuDeselected(e: MenuEvent?) {
        }

        override fun menuCanceled(e: MenuEvent?) {
        }
    }

    fun goToLine(line: Int) {
        Utils.printlnLog("Line : $line")
        if (line < 0) {
            return
        }
        var num = 0
        for (idx in 0 until mFilteredLogPanel.mTableModel.rowCount) {
            num = mFilteredLogPanel.mTableModel.getValueAt(idx, 0).toString().trim().toInt()
            if (line <= num) {
                mFilteredLogPanel.goToRow(idx, 0)
                break
            }
        }

        if (line != num) {
            for (idx in 0 until mFullLogPanel.mTableModel.rowCount) {
                num = mFullLogPanel.mTableModel.getValueAt(idx, 0).toString().trim().toInt()
                if (line <= num) {
                    mFullLogPanel.goToRow(idx, 0)
                    break
                }
            }
        }
    }

    fun markLine() {
        if (IsCreatingUI) {
            return
        }
        mSelectedLine = mFilteredLogPanel.getSelectedLine()
    }

    fun getMarkLine(): Int {
        return mSelectedLine
    }

    fun goToMarkedLine() {
        if (IsCreatingUI) {
            return
        }
        goToLine(mSelectedLine)
    }

    fun updateUIAfterVisible(args: Array<String>) {
        if (mShowLogCombo.selectedIndex >= 0 && (mShowLogComboStyle == FilterComboBox.Mode.MULTI_LINE || mShowLogComboStyle == FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT)) {
            val selectedItem = mShowLogCombo.selectedItem
            mShowLogCombo.selectedItem = ""
            mShowLogCombo.selectedItem = selectedItem
            mShowLogCombo.parent.revalidate()
            mShowLogCombo.parent.repaint()
        }
        
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (mFormatManager.mCurrFormat.mTokenFilters[idx].mIsSaveFilter
                && mTokenCombo[idx].selectedIndex >= 0
                && (mTokenComboStyle[idx] == FilterComboBox.Mode.MULTI_LINE || mTokenComboStyle[idx] == FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT)) {
                val selectedItem = mTokenCombo[idx].selectedItem
                mTokenCombo[idx].selectedItem = ""
                mTokenCombo[idx].selectedItem = selectedItem
                mTokenCombo[idx].parent.revalidate()
                mTokenCombo[idx].parent.repaint()
            }
        }
        if (mBoldLogCombo.selectedIndex >= 0 && (mBoldLogComboStyle == FilterComboBox.Mode.MULTI_LINE || mBoldLogComboStyle == FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT)) {
            val selectedItem = mBoldLogCombo.selectedItem
            mBoldLogCombo.selectedItem = ""
            mBoldLogCombo.selectedItem = selectedItem
            mBoldLogCombo.parent.revalidate()
            mBoldLogCombo.parent.repaint()
        }
        mColorManager.applyFilterStyle()

        mShowLogCombo.mEnabledTfTooltip = true
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            mTokenCombo[idx].mEnabledTfTooltip = true
        }

        var isFirst = true
        for (fileName in args) {
            val file = File(fileName)
            if (file.isFile) {
                if (isFirst) {
                    openFile(file.absolutePath, false, false)
                    isFirst = false
                } else {
                    openFile(file.absolutePath, true, false)
                }
            }
        }
    }

    fun repaintUI() {
    }

    fun updateLogViewWidth() {
        mFullLogPanel.updateTableWidth()
        mFilteredLogPanel.updateTableWidth()
    }

    internal inner class StatusTextField(text: String?) : JTextField(text) {
        private var mPrevText = ""
        override fun getToolTipText(event: MouseEvent?): String? {
            val textTrimmed = text.trim()
            if (mPrevText != textTrimmed && textTrimmed.isNotEmpty()) {
                mPrevText = textTrimmed
                val splitData = textTrimmed.split("|")

                var tooltip = "<html>"
                for (item in splitData) {
                    val itemTrimmed = item.trim()
                    if (itemTrimmed.isNotEmpty()) {
                        tooltip += "$itemTrimmed<br>"
                    }
                }
                tooltip += "</html>"
                toolTipText = tooltip
            }
            return super.getToolTipText(event)
        }
    }

    inner class FindPanel : JPanel() {
        var mFindCombo: FilterComboBox
        var mFindMatchCaseToggle: FilterToggleButton
        private var mFindLabel: JLabel
        private var mTargetLabel: JLabel
        private var mUpBtn: ColorButton
        private var mDownBtn: ColorButton
        var mCloseBtn: ColorButton

        var mTargetView = true  // true : filter view, false : full view

        private val mFindActionHandler = FindActionHandler()
        private val mFindKeyHandler = FindKeyHandler()
        private val mFindPopupMenuHandler = FindPopupMenuHandler()

        init {
            mFindLabel = JLabel("${Strings.FIND} ")
            mFindCombo = FilterComboBox(FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT, false)
            mFindCombo.preferredSize = Dimension(700, mFindCombo.preferredSize.height)

            mFindCombo.toolTipText = TooltipStrings.FIND_COMBO
            mFindCombo.mEnabledTfTooltip = false
            mFindCombo.isEditable = true
            mFindCombo.renderer = FilterComboBox.ComboBoxRenderer()
            val keyListeners = mFindCombo.editor.editorComponent.keyListeners
            for (listener in keyListeners) {
                if (listener is FilterComboBox.KeyHandler) {
                    mFindCombo.editor.editorComponent.removeKeyListener(listener)
                    break
                }
            }
            mFindCombo.editor.editorComponent.addKeyListener(mFindKeyHandler)
            mFindCombo.addPopupMenuListener(mFindPopupMenuHandler)

            mFindMatchCaseToggle = FilterToggleButton("Aa")
            mFindMatchCaseToggle.toolTipText = TooltipStrings.FIND_CASE_TOGGLE
            mFindMatchCaseToggle.margin = Insets(0, 0, 0, 0)
            mFindMatchCaseToggle.addItemListener(FindItemHandler())

            mUpBtn = ColorButton("▲") //△ ▲ ▽ ▼
            mUpBtn.toolTipText = TooltipStrings.FIND_PREV_BTN
            mUpBtn.margin = Insets(0, 7, 0, 7)
            mUpBtn.addActionListener(mFindActionHandler)

            mDownBtn = ColorButton("▼") //△ ▲ ▽ ▼
            mDownBtn.toolTipText = TooltipStrings.FIND_NEXT_BTN
            mDownBtn.margin = Insets(0, 7, 0, 7)
            mDownBtn.addActionListener(mFindActionHandler)

            mTargetLabel = if (mTargetView) {
                JLabel("  # ${Strings.FILTER} ${Strings.LOG} View")
            } else {
                JLabel("  # ${Strings.FULL} ${Strings.LOG} View")
            }
            mTargetLabel.toolTipText = TooltipStrings.FIND_TARGET_LABEL

            mCloseBtn = ColorButton(Strings.HIDE)
            mCloseBtn.toolTipText = TooltipStrings.FIND_CLOSE_BTN
            mCloseBtn.margin = Insets(0, 3, 0, 3)
            mCloseBtn.addActionListener(mFindActionHandler)

            val findPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 2))
            findPanel.add(mFindLabel)
            findPanel.add(mFindCombo)
            findPanel.add(mFindMatchCaseToggle)
            findPanel.add(mUpBtn)
            findPanel.add(mDownBtn)

            findPanel.add(mTargetLabel)
            findPanel.add(mCloseBtn)

            layout = BorderLayout()
            add(findPanel, BorderLayout.WEST)

            updateColor()
        }

        fun updateColor() {
            mFindMatchCaseToggle.background = background
            mUpBtn.background = background
            mDownBtn.background = background

            mFindMatchCaseToggle.border = ColorButtonBorder(background)
            mUpBtn.border = ColorButtonBorder(background)
            mDownBtn.border = ColorButtonBorder(background)
        }


        override fun setVisible(aFlag: Boolean) {
            super.setVisible(aFlag)

            if (!IsCreatingUI) {
                if (aFlag) {
                    mFindCombo.requestFocus()
                    mFindCombo.editor.selectAll()
                    if (mFindCombo.selectedItem != null) {
                        mFilteredLogPanel.mTableModel.mFilterFindLog = mFindCombo.selectedItem!!.toString()
                    }
                    else {
                        mFilteredLogPanel.mTableModel.mFilterFindLog = ""
                    }
                } else {
                    mFilteredLogPanel.mTableModel.mFilterFindLog = ""
                }
            }
        }

        fun setTargetView(aFlag: Boolean) {
            mTargetView = aFlag
            if (mTargetView) {
                mTargetLabel.text = "  # ${Strings.FILTER} ${Strings.LOG} View"
            } else {
                mTargetLabel.text = "  # ${Strings.FULL} ${Strings.LOG} View"
            }
        }

        fun moveToNext() {
            if (mTargetView) {
                mFilteredLogPanel.mTableModel.moveToNextFind()
            }
            else {
                mFullLogPanel.mTableModel.moveToNextFind()
            }
        }

        fun moveToPrev() {
            if (mTargetView) {
                mFilteredLogPanel.mTableModel.moveToPrevFind()
            }
            else {
                mFullLogPanel.mTableModel.moveToPrevFind()
            }
        }

        internal inner class FindActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                when (p0?.source) {
                    mUpBtn -> {
                        moveToPrev()
                    }
                    mDownBtn -> {
                        moveToNext()
                    }
                    mCloseBtn -> {
                        mFindPanel.isVisible = false
                        mItemFind.state = mFindPanel.isVisible
                    }
                }
            }
        }

        internal inner class FindKeyHandler : KeyAdapter() {
            override fun keyReleased(p0: KeyEvent?) {
                if (KeyEvent.VK_ENTER == p0?.keyCode) {
                    when (p0.source) {
                        mFindCombo.editor.editorComponent -> {
                            val item = mFindCombo.selectedItem!!.toString()
                            mFindCombo.resetComboItem(item)
                            mFilteredLogPanel.mTableModel.mFilterFindLog = item
                            if (KeyEvent.SHIFT_MASK == p0.modifiers) {
                                moveToPrev()
                            }
                            else {
                                moveToNext()
                            }
                        }
                    }
                }
                super.keyReleased(p0)
            }
        }
        internal inner class FindPopupMenuHandler : PopupMenuListener {
            private var mIsCanceled = false
            override fun popupMenuWillBecomeInvisible(p0: PopupMenuEvent?) {
                if (mIsCanceled) {
                    mIsCanceled = false
                    return
                }
                when (p0?.source) {
                    mFindCombo -> {
                        if (mFindCombo.selectedIndex < 0) {
                            return
                        }
                        val item = mFindCombo.selectedItem!!.toString()
                        mFindCombo.resetComboItem(item)
                        mFilteredLogPanel.mTableModel.mFilterFindLog = item
                        mFindCombo.updateTooltip()
                    }
                }
            }

            override fun popupMenuCanceled(p0: PopupMenuEvent?) {
                mIsCanceled = true
            }

            override fun popupMenuWillBecomeVisible(p0: PopupMenuEvent?) {
                val box = p0?.source as JComboBox<*>
                val comp = box.ui.getAccessibleChild(box, 0) as? JPopupMenu ?: return
                val scrollPane = comp.getComponent(0) as JScrollPane
                scrollPane.verticalScrollBar?.setUI(BasicScrollBarUI())
                scrollPane.horizontalScrollBar?.setUI(BasicScrollBarUI())
                mIsCanceled = false
            }
        }

        internal inner class FindItemHandler : ItemListener {
            override fun itemStateChanged(p0: ItemEvent?) {
                if (IsCreatingUI) {
                    return
                }
                when (p0?.source) {
                    mFindMatchCaseToggle -> {
                        mFilteredLogPanel.mTableModel.mFindMatchCase = mFindMatchCaseToggle.isSelected
                        mConfigManager.saveItem(ConfigManager.ITEM_FIND_MATCH_CASE, mFindMatchCaseToggle.isSelected.toString())
                    }
                }
            }
        }
    }

    private fun registerFindKeyStroke() {
        var stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
        var actionMapKey = javaClass.name + ":FIND_CLOSING"
        var action: Action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                mFindPanel.isVisible = false
                mItemFind.state = mFindPanel.isVisible
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
        actionMapKey = javaClass.name + ":FIND_MOVE_PREV"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                if (mFindPanel.isVisible) {
                    mFindPanel.moveToPrev()
                }
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)
        actionMapKey = javaClass.name + ":FIND_MOVE_NEXT"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                if (mFindPanel.isVisible) {
                    mFindPanel.moveToNext()
                }
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)
    }

    private fun registerTriggerKeyStroke() {
        val stroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK)
        val actionMapKey = javaClass.name + ":TRIGGER_OPENING_CLOSING"
        val action: Action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                if (!mAgingTestManager.mTriggerPanel.isVisible) {
                    mAgingTestManager.mTriggerPanel.isVisible = true
                }
                else {
                    if (mAgingTestManager.mTriggerPanel.canHide()) {
                        mAgingTestManager.mTriggerPanel.isVisible = false
                    }
                    else {
                        JOptionPane.showMessageDialog(this@MainUI, Strings.TRIGGER_CANNOT_HIDE, Strings.WARNING, JOptionPane.WARNING_MESSAGE)
                    }
                }
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)
    }

    private fun registerKeyStroke() {
        mScrollbackTF.getInputMap(JTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK), "none")

        var stroke = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK)
        var actionMapKey = javaClass.name + ":GO_TO_LAST"
        var action: Action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                mFilteredLogPanel.goToLast()
                mFullLogPanel.goToLast()
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK)
        actionMapKey = javaClass.name + ":GO_TO_FIRST"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                mFilteredLogPanel.goToFirst()
                mFullLogPanel.goToFirst()
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK)
        actionMapKey = javaClass.name + ":RECONNECT"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                reconnectAdb()
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK)
        actionMapKey = javaClass.name + ":GO_TO_LINE"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val goToDialog = GoToDialog(this@MainUI)
                goToDialog.setLocationRelativeTo(this@MainUI)
                goToDialog.isVisible = true
                if (goToDialog.mLine != -1) {
                    goToLine(goToDialog.mLine)
                } else {
                    Utils.printlnLog("Cancel Goto Line")
                }
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, KeyEvent.CTRL_MASK)
        actionMapKey = javaClass.name + ":FOCUS_LOG_COMBO"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                mShowLogCombo.requestFocus()
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK)
        actionMapKey = javaClass.name + ":CLEAR_VIEWS"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                mFilteredLogPanel.mTableModel.clearItems()
                repaint()
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
        actionMapKey = javaClass.name + ":PREVIOUS_FILTER"
        action = object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                if (mShowLogCombo.itemCount > 1) {
                    val filter = mShowLogCombo.getItemAt(1)
                    setTextShowLogCombo(filter)
                    applyShowLogCombo(true)
                }
            }
        }
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, actionMapKey)
        rootPane.actionMap.put(actionMapKey, action)
    }

    fun showFindResultTooltip(isNext: Boolean, result: String) {
        val targetPanel = if (mFindPanel.mTargetView) {
            mFilteredLogPanel
        }
        else {
            mFullLogPanel
        }

        targetPanel.toolTipText = result
        if (isNext) {
            ToolTipManager.sharedInstance().mouseMoved(MouseEvent(targetPanel, 0, 0, 0, targetPanel.width / 3, targetPanel.height - 50, 0, false))
        }
        else {
            ToolTipManager.sharedInstance().mouseMoved(MouseEvent(targetPanel, 0, 0, 0, targetPanel.width / 3, 0, 0, false))
        }

        val clearThread = Thread {
            run {
                Thread.sleep(1000)
                SwingUtilities.invokeAndWait {
                    targetPanel.toolTipText = ""
                }
            }
        }

        clearThread.start()
    }

    inner class FocusHandler(isFilter: Boolean) : FocusAdapter() {
        private val mIsFilter = isFilter
        override fun focusGained(e: FocusEvent?) {
            super.focusGained(e)
            mFindPanel.setTargetView(mIsFilter)
        }
    }

    override fun formatChanged(format: FormatManager.FormatItem) {
        val formatName = mFormatManager.mCurrFormat.mName
        val tokenFilters = mFormatManager.mCurrFormat.mTokenFilters
        var item: String?
        var check: String?
        for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
            if (tokenFilters[idx].mIsSaveFilter) {
                for (i in 0 until ConfigManager.COUNT_TOKEN_FILTER) {
                    item = mConfigManager.getItem("${ConfigManager.ITEM_TOKEN_FILTER}${formatName}_${tokenFilters[idx].mToken}_$i")
                    if (item == null) {
                        break
                    }
                    mTokenCombo[idx].insertItemAt(item, i)
                    if (i == 0) {
                        mTokenCombo[idx].selectedIndex = 0
                    }
                }

                mTokenCombo[idx].updateTooltip()
            }

            check = mConfigManager.getItem("${ConfigManager.ITEM_TOKEN_CHECK}${formatName}_${tokenFilters[idx].mToken}")
            if (!check.isNullOrEmpty()) {
                mTokenToggle[idx].isSelected = check.toBoolean()
            } else {
                mTokenToggle[idx].isSelected = false
            }
            mTokenCombo[idx].setEnabledFilter(mTokenToggle[idx].isSelected)
            mTokenCombo[idx].preferredSize = Dimension(tokenFilters[idx].mUiWidth, mTokenCombo[idx].preferredSize.height)

            mTokenToggle[idx].text = tokenFilters[idx].mToken
            mTokenPanel[idx].isVisible = (tokenFilters[idx].mToken.isNotEmpty() && tokenFilters[idx].mUiWidth > 0)
        }

        resetLogPanel(true)
    }

    override fun formatListChanged() {
        mLogFormatCombo.removeAllItems()
        for (format in mFormatManager.mFormatList) {
            mLogFormatCombo.addItem(format.mName)
        }

        mLogFormatCombo.selectedItem = mFormatManager.mCurrFormat.mName
    }

    private fun updateTablePNameColumn(isShow: Boolean) {
        mFullLogPanel.updateTablePNameColumn(isShow)
        mFilteredLogPanel.updateTablePNameColumn(isShow)
    }

    fun updateLogPanelTableBar() {
        updateLogPanelTableBar(mConfigManager.loadFilters(), mConfigManager.loadCmds())
    }

    private fun updateLogPanelTableBar(filters: ArrayList<CustomListManager.CustomElement>?, cmds: ArrayList<CustomListManager.CustomElement>?) {
        mFullLogPanel.updateTableBar(filters, cmds)
        mFilteredLogPanel.updateTableBar(filters, cmds)
    }

    fun updateUI() {
        mLogCmdCombo.toolTipText = ""
        mLogCmdCombo.toolTipText = TooltipStrings.LOG_CMD_COMBO
        mLogCmdCombo.renderer = ColorComboBox.ComboBoxRenderer()

        mLogCmdCombo.editor.editorComponent.removeKeyListener(mKeyHandler)
        mLogCmdCombo.removeItemListener(mItemHandler)
        mLogCmdCombo.editor.editorComponent.removeMouseListener(mMouseHandler)
        mLogCmdCombo.removePopupMenuListener(mPopupMenuHandler)

        mLogCmdCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mLogCmdCombo.addItemListener(mItemHandler)
        mLogCmdCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mLogCmdCombo.addPopupMenuListener(mPopupMenuHandler)
        updateLogCmdCombo(false)

        mDeviceCombo.toolTipText = ""
        mDeviceCombo.toolTipText = TooltipStrings.DEVICES_COMBO
        mDeviceCombo.renderer = ColorComboBox.ComboBoxRenderer()

        mDeviceCombo.editor.editorComponent.removeKeyListener(mKeyHandler)
        mDeviceCombo.removeItemListener(mItemHandler)
        mDeviceCombo.editor.editorComponent.removeMouseListener(mMouseHandler)

        mDeviceCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mDeviceCombo.addItemListener(mItemHandler)
        mDeviceCombo.editor.editorComponent.addMouseListener(mMouseHandler)

        setDeviceComboColor(mLogCmdManager.mDevices.contains(mDeviceCombo.selectedItem?.toString() ?: ""))

        mFindPanel.updateColor()

        changeToolBtnColor()
        updateLogPanelTableBar(mConfigManager.loadFilters(), mConfigManager.loadCmds())
    }
}

