package com.blogspot.kotlinstudy.lognote

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.*
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.FontUIResource
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.text.JTextComponent
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class MainUI(title: String) : JFrame() {
    companion object {
        private const val SPLIT_WEIGHT = 0.7

        private const val ROTATION_LEFT_RIGHT = 0
        private const val ROTATION_TOP_BOTTOM = 1
        private const val ROTATION_RIGHT_LEFT = 2
        private const val ROTATION_BOTTOM_TOP = 3
        private const val ROTATION_MAX = ROTATION_BOTTOM_TOP

        const val VERBOSE = "Verbose"
        const val DEBUG = "Debug"
        const val INFO = "Info"
        const val WARNING = "Warning"
        const val ERROR = "Error"
        const val FATAL = "Fatal"

        const val CROSS_PLATFORM_LAF = "Cross Platform"
        const val SYSTEM_LAF = "System"
        const val FLAT_LIGHT_LAF = "Flat Light"
        const val FLAT_DARK_LAF = "Flat Dark"
    }

    private lateinit var mMenuBar: JMenuBar
    private lateinit var mMenuFile: JMenu
    private lateinit var mItemFileOpen: JMenuItem
    private lateinit var mItemFileOpenFiles: JMenuItem
    private lateinit var mItemFileAppendFiles: JMenuItem
//    private lateinit var mItemFileOpenRecents: JMenu
    private lateinit var mItemFileExit: JMenuItem
    private lateinit var mMenuView: JMenu
    private lateinit var mItemFull: JCheckBoxMenuItem
    private lateinit var mMenuSettings: JMenu
    private lateinit var mItemAdb: JMenuItem
    private lateinit var mItemLogFile: JMenuItem
    private lateinit var mItemFont: JMenuItem
    private lateinit var mItemFilterIncremental: JCheckBoxMenuItem
    private lateinit var mItemFilterStyle: JMenuItem
    private lateinit var mMenuLogLevel: JMenu
    private lateinit var mLogLevelGroup: ButtonGroup
    private lateinit var mItemLaF: JMenuItem
    private lateinit var mMenuHelp: JMenu
    private lateinit var mItemHelp: JMenuItem
    private lateinit var mItemAbout: JMenuItem

    private lateinit var mFilterPanel: JPanel
    private lateinit var mFilterLeftPanel: JPanel

    private lateinit var mLogToolBar: JPanel
    private lateinit var mStartBtn: ColorButton
    private lateinit var mRetryAdbToggle: ColorToggleButton
    private lateinit var mStopBtn: ColorButton
    private lateinit var mPauseToggle: ColorToggleButton
    private lateinit var mClearBtn: ColorButton
    private lateinit var mSaveBtn: ColorButton
    private lateinit var mRotationBtn: ColorButton
    lateinit var mFiltersBtn: ColorButton
    lateinit var mCmdsBtn: ColorButton

    private lateinit var mLogPanel: JPanel
    private lateinit var mShowLogPanel: JPanel
    private lateinit var mMatchCaseToggle: ColorToggleButton
    private lateinit var mMatchCaseTogglePanel: JPanel
    private lateinit var mShowLogCombo: FilterComboBox
    var mShowLogComboStyle: FilterComboBox.Mode
    private lateinit var mShowLogToggle: ColorToggleButton
    private lateinit var mShowLogTogglePanel: JPanel

    private lateinit var mBoldLogPanel: JPanel
    private lateinit var mBoldLogCombo: FilterComboBox
    var mBoldLogComboStyle: FilterComboBox.Mode
    private lateinit var mBoldLogToggle: ColorToggleButton
    private lateinit var mBoldLogTogglePanel: JPanel

    private lateinit var mShowTagPanel: JPanel
    private lateinit var mShowTagCombo: FilterComboBox
    var mShowTagComboStyle: FilterComboBox.Mode
    private lateinit var mShowTagToggle: ColorToggleButton
    private lateinit var mShowTagTogglePanel: JPanel

    private lateinit var mShowPidPanel: JPanel
    private lateinit var mShowPidCombo: FilterComboBox
    var mShowPidComboStyle: FilterComboBox.Mode
    private lateinit var mShowPidToggle: ColorToggleButton
    private lateinit var mShowPidTogglePanel: JPanel

    private lateinit var mShowTidPanel: JPanel
    private lateinit var mShowTidCombo: FilterComboBox
    var mShowTidComboStyle: FilterComboBox.Mode
    private lateinit var mShowTidToggle: ColorToggleButton
    private lateinit var mShowTidTogglePanel: JPanel

    private lateinit var mDeviceCombo: ColorComboBox<String>
    private lateinit var mDeviceStatus: JLabel
    private lateinit var mAdbConnectBtn: ColorButton
    private lateinit var mAdbRefreshBtn: ColorButton
    private lateinit var mAdbDisconnectBtn: ColorButton

    private lateinit var mScrollbackLabel: JLabel
    private lateinit var mScrollbackTF: JTextField
    private lateinit var mScrollbackSplitFileCheck: JCheckBox
    private lateinit var mScrollbackApplyBtn: ColorButton
    private lateinit var mScrollbackKeepToggle: ColorToggleButton

    lateinit var mFilteredTableModel: LogTableModel
        private set

    private lateinit var mFullTableModel: LogTableModel

    private lateinit var mLogSplitPane: JSplitPane

    lateinit var mFilteredLogPanel: LogPanel
    lateinit var mFullLogPanel: LogPanel
    private var mSelectedLine = 0

    private lateinit var mStatusBar: JPanel
    private lateinit var mStatusTF: JTextField

    private val mFrameMouseListener = FrameMouseListener(this)
    private val mKeyHandler = KeyHandler()
    private val mItemHandler = ItemHandler()
    private val mLevelItemHandler = LevelItemHandler()
    private val mActionHandler = ActionHandler()
    private val mPopupMenuHandler = PopupMenuHandler()
    private val mMouseHandler = MouseHandler()
    private val mComponentHandler = ComponentHandler()

    val mConfigManager = ConfigManager.getInstance()
    private val mColorManager = ColorManager.getInstance()
    private var mIsCreatingUI = true

    private val mAdbManager = AdbManager.getInstance()
    private lateinit var mFiltersManager:FiltersManager
    private lateinit var mCmdsManager:CmdsManager

    private var mFrameX = 0
    private var mFrameY = 0
    private var mFrameWidth = 1280
    private var mFrameHeight = 720
    private var mFrameExtendedState = Frame.MAXIMIZED_BOTH

    private var mRotationStatus = ROTATION_LEFT_RIGHT

    var mFont: Font = Font("Dialog", Font.PLAIN, 12)
        set(value) {
            field = value
            if (!mIsCreatingUI) {
                mFilteredLogPanel.mFont = value
                mFullLogPanel.mFont = value
            }
        }

    var mUIFontPercent = 100

    init {
        loadConfigOnCreate()

        val laf = mConfigManager.getItem(ConfigManager.ITEM_LOOK_AND_FEEL)

        if (laf == null) {
            ConfigManager.LaF = FLAT_LIGHT_LAF
        }
        else {
            ConfigManager.LaF = laf
        }

        val uiFontSize = mConfigManager.getItem(ConfigManager.ITEM_UI_FONT_SIZE)
        if (!uiFontSize.isNullOrEmpty()) {
            mUIFontPercent = uiFontSize.toInt()
        }

        if (ConfigManager.LaF == FLAT_LIGHT_LAF || ConfigManager.LaF == FLAT_DARK_LAF) {
            System.setProperty("flatlaf.uiScale", "$mUIFontPercent%");
        }
        else {
            initFontSize(mUIFontPercent)
        }

        setLaF(ConfigManager.LaF)

        val cmd = mConfigManager.getItem(ConfigManager.ITEM_ADB_CMD)
        if (!cmd.isNullOrEmpty()) {
            mAdbManager.mAdbCmd = cmd
        } else {
            val os = System.getProperty("os.name")
            println("OS : $os")
            if (os.lowercase().contains("windows")) {
                mAdbManager.mAdbCmd = "adb.exe"
            } else {
                mAdbManager.mAdbCmd = "adb"
            }
        }
        mAdbManager.addEventListener(AdbHandler())
        val logSavePath = mConfigManager.getItem(ConfigManager.ITEM_ADB_LOG_SAVE_PATH)
        if (logSavePath.isNullOrEmpty()) {
            mAdbManager.mLogSavePath = "."
        } else {
            mAdbManager.mLogSavePath = logSavePath
        }

        val prefix = mConfigManager.getItem(ConfigManager.ITEM_ADB_PREFIX)
        if (prefix.isNullOrEmpty()) {
            mAdbManager.mPrefix = ""
        } else {
            mAdbManager.mPrefix = prefix
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

        prop = mConfigManager.getItem(ConfigManager.ITEM_LANG)
        if (!prop.isNullOrEmpty()) {
            Strings.lang = prop.toInt()
        }
        else {
            Strings.lang = Strings.EN
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_SHOW_LOG_STYLE)
        if (!prop.isNullOrEmpty()) {
            mShowLogComboStyle = FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            mShowLogComboStyle = FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_BOLD_LOG_STYLE)
        if (!prop.isNullOrEmpty()) {
            mBoldLogComboStyle = FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            mBoldLogComboStyle = FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_SHOW_TAG_STYLE)
        if (!prop.isNullOrEmpty()) {
            mShowTagComboStyle = FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            mShowTagComboStyle = FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_SHOW_PID_STYLE)
        if (!prop.isNullOrEmpty()) {
            mShowPidComboStyle = FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            mShowPidComboStyle = FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT
        }

        prop = mConfigManager.getItem(ConfigManager.ITEM_SHOW_TID_STYLE)
        if (!prop.isNullOrEmpty()) {
            mShowTidComboStyle = FilterComboBox.Mode.fromInt(prop.toInt())
        }
        else {
            mShowTidComboStyle = FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT
        }

        createUI(title)

        mAdbManager.getDevices()
    }

    private fun exit() {
        saveConfigOnDestroy()
        mFilteredTableModel.stopScan()
        mFullTableModel.stopScan()
        mAdbManager.stop()
        exitProcess(0)
    }

    private fun loadConfigOnCreate() {
        mConfigManager.loadConfig()
        mColorManager.getConfig()
        mColorManager.applyColor()
        mColorManager.getConfigFilterStyle()
        mConfigManager.saveConfig()
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

        var nCount = mShowLogCombo.itemCount
        if (nCount > ConfigManager.COUNT_SHOW_LOG) {
            nCount = ConfigManager.COUNT_SHOW_LOG
        }
        for (i in 0 until nCount) {
            mConfigManager.setItem(ConfigManager.ITEM_SHOW_LOG + i, mShowLogCombo.getItemAt(i).toString())
        }

        for (i in nCount until ConfigManager.COUNT_SHOW_LOG) {
            mConfigManager.removeConfigItem(ConfigManager.ITEM_SHOW_LOG + i)
        }

        nCount = mShowTagCombo.itemCount
        if (nCount > ConfigManager.COUNT_SHOW_TAG) {
            nCount = ConfigManager.COUNT_SHOW_TAG
        }
        for (i in 0 until nCount) {
            mConfigManager.setItem(ConfigManager.ITEM_SHOW_TAG + i, mShowTagCombo.getItemAt(i).toString())
        }
        for (i in nCount until ConfigManager.COUNT_SHOW_TAG) {
            mConfigManager.removeConfigItem(ConfigManager.ITEM_SHOW_TAG + i)
        }

        nCount = mBoldLogCombo.itemCount
        if (nCount > ConfigManager.COUNT_HIGHLIGHT_LOG) {
            nCount = ConfigManager.COUNT_HIGHLIGHT_LOG
        }
        for (i in 0 until nCount) {
            mConfigManager.setItem(ConfigManager.ITEM_HIGHLIGHT_LOG + i, mBoldLogCombo.getItemAt(i).toString())
        }
        for (i in nCount until ConfigManager.COUNT_HIGHLIGHT_LOG) {
            mConfigManager.removeConfigItem(ConfigManager.ITEM_HIGHLIGHT_LOG + i)
        }
        try {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_DEVICE, mDeviceCombo.getItemAt(0).toString())
        } catch (e: NullPointerException) {
            mConfigManager.setItem(ConfigManager.ITEM_ADB_DEVICE, "0.0.0.0")
        }

        mConfigManager.setItem(ConfigManager.ITEM_DIVIDER_LOCATION, mLogSplitPane.dividerLocation.toString())
        if (mLogSplitPane.lastDividerLocation != -1) {
            mConfigManager.setItem(ConfigManager.ITEM_LAST_DIVIDER_LOCATION, mLogSplitPane.lastDividerLocation.toString())
        }

//            mProperties.put(ITEM_LANG, Strings.lang.toString())

        mConfigManager.saveConfig()
    }

    private fun createUI(title: String) {
        setTitle(title)

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

        mItemFileOpenFiles = JMenuItem(Strings.OPEN_FILES)
        mItemFileOpenFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpenFiles)

        mItemFileAppendFiles = JMenuItem(Strings.APPEND_FILES)
        mItemFileAppendFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileAppendFiles)

//        mItemFileOpenRecents = JMenu(Strings.OPEN_RECENTS)
//        mItemFileOpenRecents.addActionListener(mActionHandler)
//        mMenuFile.add(mItemFileOpenRecents)
        mMenuFile.addSeparator()

        mItemFileExit = JMenuItem(Strings.EXIT)
        mItemFileExit.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileExit)
        mMenuBar.add(mMenuFile)

        mMenuView = JMenu(Strings.VIEW)
        mMenuView.mnemonic = KeyEvent.VK_V

        mItemFull = JCheckBoxMenuItem(Strings.VIEW_FULL)
        mItemFull.addActionListener(mActionHandler)
        mMenuView.add(mItemFull)
        mMenuBar.add(mMenuView)

        mMenuSettings = JMenu(Strings.SETTING)
        mMenuSettings.mnemonic = KeyEvent.VK_S

        mItemAdb = JMenuItem(Strings.ADB)
        mItemAdb.addActionListener(mActionHandler)
        mMenuSettings.add(mItemAdb)
        mItemLogFile = JMenuItem(Strings.LOGFILE)
        mItemLogFile.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLogFile)

        mMenuSettings.addSeparator()

        mItemFont = JMenuItem(Strings.FONT + " & " + Strings.COLOR)
//        mItemFont = JMenuItem(Strings.FONT)
        mItemFont.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFont)

        mMenuSettings.addSeparator()

        mItemFilterIncremental = JCheckBoxMenuItem(Strings.FILTER + "-" + Strings.INCREMENTAL)
        mItemFilterIncremental.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFilterIncremental)

        mItemFilterStyle = JMenuItem(Strings.FILTER_STYLE)
        mItemFilterStyle.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFilterStyle)

        mMenuSettings.addSeparator()

        mMenuLogLevel = JMenu(Strings.LOGLEVEL)
        mMenuLogLevel.addActionListener(mActionHandler)
        mMenuSettings.add(mMenuLogLevel)

        mLogLevelGroup = ButtonGroup()

        var menuItem = JRadioButtonMenuItem(VERBOSE)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.isSelected = true
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(DEBUG)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(INFO)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(WARNING)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(ERROR)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(FATAL)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        mMenuSettings.addSeparator()

        mItemLaF = JMenuItem(Strings.LOOK_AND_FEEL)
        mItemLaF.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLaF)

        mMenuBar.add(mMenuSettings)

        mMenuHelp = JMenu(Strings.HELP)
        mMenuHelp.mnemonic = KeyEvent.VK_H

        mItemHelp = JMenuItem(Strings.HELP)
        mItemHelp.addActionListener(mActionHandler)
        mMenuHelp.add(mItemHelp)

        mMenuHelp.addSeparator()

        mItemAbout = JMenuItem(Strings.ABOUT)
        mItemAbout.addActionListener(mActionHandler)
        mMenuHelp.add(mItemAbout)
        mMenuBar.add(mMenuHelp)

        jMenuBar = mMenuBar

        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            UIManager.put("ScrollBar.thumb", ColorUIResource(Color(0xE0, 0xE0, 0xE0)))
            UIManager.put("ScrollBar.thumbHighlight", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
            UIManager.put("ScrollBar.thumbShadow", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
            UIManager.put("ComboBox.buttonDarkShadow", ColorUIResource(Color.black))
        }

        addMouseListener(mFrameMouseListener)
        addMouseMotionListener(mFrameMouseListener)
        contentPane.addMouseListener(mFrameMouseListener)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(object : KeyEventDispatcher {
            override fun dispatchKeyEvent(p0: KeyEvent?): Boolean {
                if (p0?.keyCode == KeyEvent.VK_PAGE_DOWN && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mFilteredLogPanel.goToLast()
                    mFullLogPanel.goToLast()
                } else if (p0?.keyCode == KeyEvent.VK_PAGE_UP && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mFilteredLogPanel.goToFirst()
                    mFullLogPanel.goToFirst()
//                } else if (p0?.keyCode == KeyEvent.VK_N && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {

                } else if (p0?.keyCode == KeyEvent.VK_L && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mDeviceCombo.requestFocus()
                } else if (p0?.keyCode == KeyEvent.VK_R && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    reconnectAdb()
                } else if (p0?.keyCode == KeyEvent.VK_G && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    val goToDialog = GoToDialog(this@MainUI)
                    goToDialog.setLocationRelativeTo(this@MainUI)
                    goToDialog.isVisible = true
                    if (goToDialog.line != -1) {
                        goToLine(goToDialog.line)
                    } else {
                        println("Cancel Goto Line")
                    }
                }

                return false
            }
        })

        mFilterPanel = JPanel()
        mFilterLeftPanel = JPanel()

        mLogToolBar = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0))
        mLogToolBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mLogToolBar.addMouseListener(mMouseHandler)

        val btnMargin = Insets(2, 5, 2, 5)
//        mLogToolBar = JPanel()
//        mLogToolBar.background = Color(0xE5, 0xE5, 0xE5)
        mStartBtn = ColorButton(Strings.START)
        mStartBtn.margin = btnMargin
        mStartBtn.toolTipText = TooltipStrings.START_BTN
        mStartBtn.addActionListener(mActionHandler)
        mStartBtn.addMouseListener(mMouseHandler)
        mRetryAdbToggle = ColorToggleButton(Strings.RETRY_ADB)
        mRetryAdbToggle.toolTipText = TooltipStrings.RETRY_ADB_TOGGLE
        mRetryAdbToggle.margin = Insets(mRetryAdbToggle.margin.top, 0, mRetryAdbToggle.margin.bottom, 0)
        mRetryAdbToggle.addItemListener(mItemHandler)

        mPauseToggle = ColorToggleButton(Strings.PAUSE)
//        mPauseToggle.toolTipText = TooltipStrings.BOLD_TOGGLE
        mPauseToggle.margin = Insets(mPauseToggle.margin.top, 0, mPauseToggle.margin.bottom, 0)
        mPauseToggle.addItemListener(mItemHandler)


        mStopBtn = ColorButton(Strings.STOP)
        mStopBtn.margin = btnMargin
        mStopBtn.toolTipText = TooltipStrings.STOP_BTN
        mStopBtn.addActionListener(mActionHandler)
        mStopBtn.addMouseListener(mMouseHandler)
        mClearBtn = ColorButton(Strings.CLEAR)
        mClearBtn.margin = btnMargin
        mClearBtn.toolTipText = TooltipStrings.CLEAR_BTN
        mClearBtn.addActionListener(mActionHandler)
        mClearBtn.addMouseListener(mMouseHandler)
        mSaveBtn = ColorButton(Strings.SAVE)
        mSaveBtn.margin = btnMargin
        mSaveBtn.toolTipText = TooltipStrings.SAVE_BTN
        mSaveBtn.addActionListener(mActionHandler)
        mSaveBtn.addMouseListener(mMouseHandler)
        mRotationBtn = ColorButton(Strings.ROTATION)
        mRotationBtn.margin = btnMargin
        mRotationBtn.toolTipText = TooltipStrings.ROTATION_BTN
        mRotationBtn.addActionListener(mActionHandler)
        mRotationBtn.addMouseListener(mMouseHandler)
        mFiltersBtn = ColorButton(Strings.FILTERS)
        mFiltersBtn.margin = btnMargin
        mFiltersBtn.toolTipText = TooltipStrings.FILTER_LIST_BTN
        mFiltersBtn.addActionListener(mActionHandler)
        mFiltersBtn.addMouseListener(mMouseHandler)
        mCmdsBtn = ColorButton(Strings.CMDS)
        mCmdsBtn.margin = btnMargin
        mCmdsBtn.toolTipText = TooltipStrings.CMD_LIST_BTN
        mCmdsBtn.addActionListener(mActionHandler)
        mCmdsBtn.addMouseListener(mMouseHandler)

        mLogPanel = JPanel()
        mShowLogPanel = JPanel()
        mShowLogCombo = FilterComboBox(mShowLogComboStyle)
        mShowLogCombo.toolTipText = TooltipStrings.LOG_COMBO
        mShowLogCombo.isEditable = true
        mShowLogCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mShowLogCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowLogCombo.addItemListener(mItemHandler)
        mShowLogCombo.addPopupMenuListener(mPopupMenuHandler)
        mShowLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowLogToggle = ColorToggleButton(Strings.LOG)
        mShowLogToggle.toolTipText = TooltipStrings.LOG_TOGGLE
        mShowLogToggle.margin = Insets(0, 0, 0, 0)
        mShowLogTogglePanel = JPanel(GridLayout(1, 1))
        mShowLogTogglePanel.add(mShowLogToggle)
        mShowLogTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowLogToggle.addItemListener(mItemHandler)

        mBoldLogPanel = JPanel()
        mBoldLogCombo = FilterComboBox(mBoldLogComboStyle)
        mBoldLogCombo.toolTipText = TooltipStrings.BOLD_COMBO
        mBoldLogCombo.mEnabledTfTooltip = false
        mBoldLogCombo.isEditable = true
        mBoldLogCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mBoldLogCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mBoldLogCombo.addItemListener(mItemHandler)
        mBoldLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mBoldLogToggle = ColorToggleButton(Strings.BOLD)
        mBoldLogToggle.toolTipText = TooltipStrings.BOLD_TOGGLE
        mBoldLogToggle.margin = Insets(0, 0, 0, 0)
        mBoldLogTogglePanel = JPanel(GridLayout(1, 1))
        mBoldLogTogglePanel.add(mBoldLogToggle)
        mBoldLogTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mBoldLogToggle.addItemListener(mItemHandler)

        mShowTagPanel = JPanel()
        mShowTagCombo = FilterComboBox(mShowTagComboStyle)
        mShowTagCombo.toolTipText = TooltipStrings.TAG_COMBO
        mShowTagCombo.isEditable = true
        mShowTagCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mShowTagCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowTagCombo.addItemListener(mItemHandler)
        mShowTagCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowTagToggle = ColorToggleButton(Strings.TAG)
        mShowTagToggle.toolTipText = TooltipStrings.TAG_TOGGLE
        mShowTagToggle.margin = Insets(0, 0, 0, 0)
        mShowTagTogglePanel = JPanel(GridLayout(1, 1))
        mShowTagTogglePanel.add(mShowTagToggle)
        mShowTagTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowTagToggle.addItemListener(mItemHandler)

        mShowPidPanel = JPanel()
        mShowPidCombo = FilterComboBox(mShowPidComboStyle)
        mShowPidCombo.toolTipText = TooltipStrings.PID_COMBO
        mShowPidCombo.isEditable = true
        mShowPidCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mShowPidCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowPidCombo.addItemListener(mItemHandler)
        mShowPidCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowPidToggle = ColorToggleButton(Strings.PID)
        mShowPidToggle.toolTipText = TooltipStrings.PID_TOGGLE
        mShowPidToggle.margin = Insets(0, 0, 0, 0)
        mShowPidTogglePanel = JPanel(GridLayout(1, 1))
        mShowPidTogglePanel.add(mShowPidToggle)
        mShowPidTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowPidToggle.addItemListener(mItemHandler)

        mShowTidPanel = JPanel()
        mShowTidCombo = FilterComboBox(mShowTidComboStyle)
        mShowTidCombo.toolTipText = TooltipStrings.TID_COMBO
        mShowTidCombo.isEditable = true
        mShowTidCombo.renderer = FilterComboBox.ComboBoxRenderer()
        mShowTidCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowTidCombo.addItemListener(mItemHandler)
        mShowTidCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowTidToggle = ColorToggleButton(Strings.TID)
        mShowTidToggle.toolTipText = TooltipStrings.TID_TOGGLE
        mShowTidToggle.margin = Insets(0, 0, 0, 0)
        mShowTidTogglePanel = JPanel(GridLayout(1, 1))
        mShowTidTogglePanel.add(mShowTidToggle)
        mShowTidTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowTidToggle.addItemListener(mItemHandler)

        mDeviceStatus = JLabel("None", JLabel.LEFT)
        mDeviceStatus.isEnabled = false
        val deviceComboPanel = JPanel(BorderLayout())
        mDeviceCombo = ColorComboBox()
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
        deviceComboPanel.add(mAdbConnectBtn, BorderLayout.EAST)
        mAdbRefreshBtn = ColorButton(Strings.REFRESH)
        mAdbRefreshBtn.margin = btnMargin
        mAdbRefreshBtn.addActionListener(mActionHandler)
        mAdbRefreshBtn.toolTipText = TooltipStrings.REFRESH_BTN
        mAdbDisconnectBtn = ColorButton(Strings.DISCONNECT)
        mAdbDisconnectBtn.margin = btnMargin
        mAdbDisconnectBtn.addActionListener(mActionHandler)
        mAdbDisconnectBtn.toolTipText = TooltipStrings.DISCONNECT_BTN

        mMatchCaseToggle = ColorToggleButton("Aa")
        mMatchCaseToggle.toolTipText = TooltipStrings.CASE_TOGGLE
        mMatchCaseToggle.margin = Insets(0, 0, 0, 0)
        mMatchCaseToggle.addItemListener(mItemHandler)
        mMatchCaseTogglePanel = JPanel(GridLayout(1, 1))
        mMatchCaseTogglePanel.add(mMatchCaseToggle)
        mMatchCaseTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)

        mShowLogPanel.layout = BorderLayout()
        mShowLogPanel.add(mShowLogTogglePanel, BorderLayout.WEST)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mShowLogCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        }
        mShowLogPanel.add(mShowLogCombo, BorderLayout.CENTER)

        mBoldLogPanel.layout = BorderLayout()
        mBoldLogPanel.add(mBoldLogTogglePanel, BorderLayout.WEST)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mBoldLogCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        }
        mBoldLogCombo.preferredSize = Dimension(170, mBoldLogCombo.preferredSize.height)
        mBoldLogPanel.add(mBoldLogCombo, BorderLayout.CENTER)
//        mBoldPanel.add(mBoldLogPanel)

        mShowTagPanel.layout = BorderLayout()
        mShowTagPanel.add(mShowTagTogglePanel, BorderLayout.WEST)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mShowTagCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        }
        mShowTagCombo.preferredSize = Dimension(250, mShowTagCombo.preferredSize.height)
        mShowTagPanel.add(mShowTagCombo, BorderLayout.CENTER)
//        mTagPanel.add(mShowTagPanel)

        mShowPidPanel.layout = BorderLayout()
        mShowPidPanel.add(mShowPidTogglePanel, BorderLayout.WEST)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mShowPidCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        }
        mShowPidCombo.preferredSize = Dimension(120, mShowPidCombo.preferredSize.height)
        mShowPidPanel.add(mShowPidCombo, BorderLayout.CENTER)
//        mPidPanel.add(mShowPidPanel)

        mShowTidPanel.layout = BorderLayout()
        mShowTidPanel.add(mShowTidTogglePanel, BorderLayout.WEST)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mShowTidCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        }
        mShowTidCombo.preferredSize = Dimension(120, mShowTidCombo.preferredSize.height)
        mShowTidPanel.add(mShowTidCombo, BorderLayout.CENTER)
//        mTidPanel.add(mShowTidPanel)

        mDeviceCombo.preferredSize = Dimension(200, mDeviceCombo.preferredSize.height)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            mDeviceCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 5)
        }
        mDeviceStatus.preferredSize = Dimension(100, 30)
        mDeviceStatus.border = BorderFactory.createEmptyBorder(3, 0, 3, 0)
        mDeviceStatus.horizontalAlignment = JLabel.CENTER

        mScrollbackApplyBtn = ColorButton(Strings.APPLY)
        mScrollbackApplyBtn.margin = btnMargin
        mScrollbackApplyBtn.toolTipText = TooltipStrings.SCROLLBACK_APPLY_BTN
        mScrollbackApplyBtn.addActionListener(mActionHandler)
        mScrollbackKeepToggle = ColorToggleButton(Strings.KEEP)
        mScrollbackKeepToggle.toolTipText = TooltipStrings.SCROLLBACK_KEEP_TOGGLE
        mScrollbackKeepToggle.mSelectedBg = Color.RED
        mScrollbackKeepToggle.mSelectedFg = Color.BLACK
        if (ConfigManager.LaF != CROSS_PLATFORM_LAF) {
            val imgIcon = ImageIcon(this.javaClass.getResource("/images/toggle_on_warn.png"))
            mScrollbackKeepToggle.selectedIcon = imgIcon
        }

        mScrollbackKeepToggle.margin = Insets(mScrollbackKeepToggle.margin.top, 0, mScrollbackKeepToggle.margin.bottom, 0)
        mScrollbackKeepToggle.addItemListener(mItemHandler)

        mScrollbackLabel = JLabel(Strings.SCROLLBACK_LINES)

        mScrollbackTF = JTextField()
        mScrollbackTF.toolTipText = TooltipStrings.SCROLLBACK_TF
        mScrollbackTF.preferredSize = Dimension(80, mScrollbackTF.preferredSize.height)
        mScrollbackTF.addKeyListener(mKeyHandler)
        mScrollbackSplitFileCheck = JCheckBox(Strings.SPLIT_FILE, false)
        mScrollbackSplitFileCheck.toolTipText = TooltipStrings.SCROLLBACK_SPLIT_CHK

        val itemFilterPanel = JPanel(FlowLayout(FlowLayout.LEADING, 0, 0))
        itemFilterPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        itemFilterPanel.add(mShowTagPanel)
        itemFilterPanel.add(mShowPidPanel)
        itemFilterPanel.add(mShowTidPanel)
        itemFilterPanel.add(mBoldLogPanel)
        itemFilterPanel.add(mMatchCaseTogglePanel)

        mLogPanel.layout = BorderLayout()
        mLogPanel.add(mShowLogPanel, BorderLayout.CENTER)
        mLogPanel.add(itemFilterPanel, BorderLayout.EAST)

        mFilterLeftPanel.layout = BorderLayout()
        mFilterLeftPanel.add(mLogPanel, BorderLayout.NORTH)
        mFilterLeftPanel.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)

        mFilterPanel.layout = BorderLayout()
        mFilterPanel.add(mFilterLeftPanel, BorderLayout.CENTER)
        mFilterPanel.addMouseListener(mMouseHandler)

        mLogToolBar.add(mStartBtn)
        mLogToolBar.add(mRetryAdbToggle)
        addVSeparator2(mLogToolBar)
        mLogToolBar.add(mPauseToggle)
        mLogToolBar.add(mStopBtn)
        mLogToolBar.add(mClearBtn)
        mLogToolBar.add(mSaveBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(deviceComboPanel)
        mLogToolBar.add(mAdbRefreshBtn)
        mLogToolBar.add(mAdbDisconnectBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(mScrollbackLabel)
        mLogToolBar.add(mScrollbackTF)
        mLogToolBar.add(mScrollbackSplitFileCheck)
        mLogToolBar.add(mScrollbackApplyBtn)
        mLogToolBar.add(mScrollbackKeepToggle)

        addVSeparator(mLogToolBar)
        mLogToolBar.add(mRotationBtn)

        addVSeparator(mLogToolBar)
        mLogToolBar.add(mFiltersBtn)
        mLogToolBar.add(mCmdsBtn)

        val toolBarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        toolBarPanel.addMouseListener(mMouseHandler)
        toolBarPanel.add(mLogToolBar)

        mFilterPanel.add(toolBarPanel, BorderLayout.NORTH)

        layout = BorderLayout()

        mFullTableModel = LogTableModel(null)

        mFilteredTableModel = LogTableModel(mFullTableModel)
        mFilteredTableModel.mMainUI = this@MainUI

        mFullLogPanel = LogPanel(mFullTableModel, null)
        mFilteredLogPanel = LogPanel(mFilteredTableModel, mFullLogPanel)
        mFullLogPanel.updateTableBar(mConfigManager.loadCmds())
        mFilteredLogPanel.updateTableBar(mConfigManager.loadFilters())

        mFiltersManager = FiltersManager(this, mFilteredLogPanel)
        mCmdsManager = CmdsManager(this, mFullLogPanel)

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
        mLogSplitPane.dividerSize = 2
        mLogSplitPane.isOneTouchExpandable = false

        mStatusBar = JPanel(BorderLayout())
        mStatusBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mStatusTF = StatusTextField(Strings.NONE)
        mStatusTF.toolTipText = TooltipStrings.SAVED_FILE_TF
        mStatusTF.isEditable = false
        mStatusTF.border = BorderFactory.createEmptyBorder()
        mStatusBar.add(mStatusTF)

        val logLevel = mConfigManager.getItem(ConfigManager.ITEM_LOG_LEVEL)
        if (logLevel != null) {
            for (item in mLogLevelGroup.elements) {
                if (logLevel == item.text) {
                    item.isSelected = true
                    break
                }
            }
        }

        var item: String?
        for (i in 0 until ConfigManager.COUNT_SHOW_LOG) {
            item = mConfigManager.getItem(ConfigManager.ITEM_SHOW_LOG + i)
            if (item == null) {
                break
            }

            if (!mShowLogCombo.isExistItem(item)) {
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

        for (i in 0 until ConfigManager.COUNT_SHOW_TAG) {
            item = mConfigManager.getItem(ConfigManager.ITEM_SHOW_TAG + i)
            if (item == null) {
                break
            }
            mShowTagCombo.insertItemAt(item, i)
            if (i == 0) {
                mShowTagCombo.selectedIndex = 0
            }
        }

        mShowTagCombo.updateTooltip()

        check = mConfigManager.getItem(ConfigManager.ITEM_SHOW_TAG_CHECK)
        if (!check.isNullOrEmpty()) {
            mShowTagToggle.isSelected = check.toBoolean()
        } else {
            mShowTagToggle.isSelected = true
        }
        mShowTagCombo.setEnabledFilter(mShowTagToggle.isSelected)

        check = mConfigManager.getItem(ConfigManager.ITEM_SHOW_PID_CHECK)
        if (!check.isNullOrEmpty()) {
            mShowPidToggle.isSelected = check.toBoolean()
        } else {
            mShowPidToggle.isSelected = true
        }
        mShowPidCombo.setEnabledFilter(mShowPidToggle.isSelected)

        check = mConfigManager.getItem(ConfigManager.ITEM_SHOW_TID_CHECK)
        if (!check.isNullOrEmpty()) {
            mShowTidToggle.isSelected = check.toBoolean()
        } else {
            mShowTidToggle.isSelected = true
        }
        mShowTidCombo.setEnabledFilter(mShowTidToggle.isSelected)

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
            mBoldLogToggle.isSelected = true
        }
        mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)

        val targetDevice = mConfigManager.getItem(ConfigManager.ITEM_ADB_DEVICE)
        mDeviceCombo.insertItemAt(targetDevice, 0)
        mDeviceCombo.selectedIndex = 0

        if (mAdbManager.mDevices.contains(targetDevice)) {
            mDeviceStatus.text = Strings.CONNECTED
            setDeviceComboColor(true)
        } else {
            mDeviceStatus.text = Strings.NOT_CONNECTED
            setDeviceComboColor(false)
        }

        var fontName = mConfigManager.getItem(ConfigManager.ITEM_FONT_NAME)
        if (fontName.isNullOrEmpty()) {
            fontName = "Dialog"
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

        when (logLevel) {
            VERBOSE->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_VERBOSE
            DEBUG->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_DEBUG
            INFO->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_INFO
            WARNING->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_WARNING
            ERROR->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_ERROR
            FATAL->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_FATAL
        }

        if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
            mFilteredTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterLog = ""
        }
        if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
            mFilteredTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterHighlightLog = ""
        }
        if (mShowTagToggle.isSelected && mShowTagCombo.selectedItem != null) {
            mFilteredTableModel.mFilterTag = mShowTagCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterTag = ""
        }
        if (mShowPidToggle.isSelected && mShowPidCombo.selectedItem != null) {
            mFilteredTableModel.mFilterPid = mShowPidCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterPid = ""
        }
        if (mShowTidToggle.isSelected && mShowTidCombo.selectedItem != null) {
            mFilteredTableModel.mFilterTid = mShowTidCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterTid = ""
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_VIEW_FULL)
        if (!check.isNullOrEmpty()) {
            mItemFull.state = check.toBoolean()
        } else {
            mItemFull.state = true
        }
        if (!mItemFull.state) {
            windowedModeLogPanel(mFullLogPanel)
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_FILTER_INCREMENTAL)
        if (!check.isNullOrEmpty()) {
            mItemFilterIncremental.state = check.toBoolean()
        } else {
            mItemFilterIncremental.state = false
        }

        check = mConfigManager.getItem(ConfigManager.ITEM_SCROLLBACK)
        if (!check.isNullOrEmpty()) {
            mScrollbackTF.text = check
        } else {
            mScrollbackTF.text = "0"
        }
        mFilteredTableModel.mScrollback = mScrollbackTF.text.toInt()

        check = mConfigManager.getItem(ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE)
        if (!check.isNullOrEmpty()) {
            mScrollbackSplitFileCheck.isSelected = check.toBoolean()
        } else {
            mScrollbackSplitFileCheck.isSelected = false
        }
        mFilteredTableModel.mScrollbackSplitFile = mScrollbackSplitFileCheck.isSelected

        check = mConfigManager.getItem(ConfigManager.ITEM_MATCH_CASE)
        if (!check.isNullOrEmpty()) {
            mMatchCaseToggle.isSelected = check.toBoolean()
        } else {
            mMatchCaseToggle.isSelected = false
        }
        mFilteredTableModel.mMatchCase = mMatchCaseToggle.isSelected

        check = mConfigManager.getItem(ConfigManager.ITEM_RETRY_ADB)
        if (!check.isNullOrEmpty()) {
            mRetryAdbToggle.isSelected = check.toBoolean()
        } else {
            mRetryAdbToggle.isSelected = false
        }

        add(mFilterPanel, BorderLayout.NORTH)
        add(mLogSplitPane, BorderLayout.CENTER)
        add(mStatusBar, BorderLayout.SOUTH)

        mIsCreatingUI = false
    }

    private fun setLaF(laf:String) {
        ConfigManager.LaF = laf
        when (laf) {
            CROSS_PLATFORM_LAF->{
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
                } catch (ex: Exception) {
                    println("Failed to initialize CrossPlatformLaf")
                }
            }
            SYSTEM_LAF->{
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (ex: Exception) {
                    println("Failed to initialize SystemLaf")
                }
            }
            FLAT_LIGHT_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatLightLaf())
                } catch (ex: Exception) {
                    println("Failed to initialize FlatLightLaf")
                }
            }
            FLAT_DARK_LAF->{
                try {
                    UIManager.setLookAndFeel(FlatDarkLaf())
                } catch (ex: Exception) {
                    println("Failed to initialize FlatDarkLaf")
                }
            }
            else->{
                try {
                    UIManager.setLookAndFeel(FlatLightLaf())
                } catch (ex: Exception) {
                    println("Failed to initialize FlatLightLaf")
                }
            }
        }
        SwingUtilities.updateComponentTreeUI(this)
    }

    private fun addVSeparator(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width, 20)
        if (ConfigManager.LaF == FLAT_DARK_LAF) {
            separator1.foreground = Color.GRAY
            separator1.background = Color.GRAY
        }
        else {
            separator1.foreground = Color.DARK_GRAY
            separator1.background = Color.DARK_GRAY
        }
        val separator2 = JSeparator(SwingConstants.VERTICAL)
        separator2.preferredSize = Dimension(separator2.preferredSize.width, 20)
        if (ConfigManager.LaF == FLAT_DARK_LAF) {
            separator2.foreground = Color.GRAY
            separator2.background = Color.GRAY
        }
        else {
            separator2.background = Color.DARK_GRAY
            separator2.foreground = Color.DARK_GRAY
        }
        panel.add(Box.createHorizontalStrut(5))
        panel.add(separator1)
        if (ConfigManager.LaF == CROSS_PLATFORM_LAF) {
            panel.add(separator2)
        }
        panel.add(Box.createHorizontalStrut(5))
    }

    private fun addVSeparator2(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width / 2, 20)
        if (ConfigManager.LaF == FLAT_DARK_LAF) {
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
            logPanel.mIsWindowedMode = true
            mRotationBtn.isEnabled = false
            mLogSplitPane.remove(logPanel)
            if (mItemFull.state) {
                val logTableDialog = LogTableDialog(this@MainUI, logPanel)
                logTableDialog.isVisible = true
            }
        }
    }

    fun attachLogPanel(logPanel: LogPanel) {
        if (logPanel.parent != mLogSplitPane) {
            logPanel.mIsWindowedMode = false
            mRotationBtn.isEnabled = true
            mLogSplitPane.remove(mFilteredLogPanel)
            mLogSplitPane.remove(mFullLogPanel)
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
    }

    fun openFile(path: String, isAppend: Boolean) {
        println("Opening: $path, $isAppend")
        mFilteredTableModel.stopScan()

        if (isAppend) {
            mStatusTF.text += "| $path"
        } else {
            mStatusTF.text = path
        }
        mFullTableModel.setLogFile(path)
        mFullTableModel.loadItems(isAppend)
        mFilteredTableModel.loadItems(isAppend)
        repaint()

        return
    }

    fun setSaveLogFile() {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HH.mm.ss")
        var prefix = mDeviceCombo.selectedItem!!.toString()
        prefix = prefix.substringBefore(":")
        if (mAdbManager.mPrefix.isNotEmpty()) {
            prefix = mAdbManager.mPrefix
        }

        var filePath = mAdbManager.mLogSavePath + "/" + prefix + "_" + dtf.format(LocalDateTime.now()) + ".txt"
        var file = File(filePath)
        var idx = 1
        while (file.isFile) {
            filePath = mAdbManager.mLogSavePath + "/" + prefix + "_" + dtf.format(LocalDateTime.now()) + "-" + idx + ".txt"
            file = File(filePath)
            idx++
        }

        mFilteredTableModel.setLogFile(filePath)
        mStatusTF.text = filePath
    }

    fun startAdbScan(reconnect: Boolean) {
        mFilteredTableModel.stopScan()
        mPauseToggle.isSelected = false
        setSaveLogFile()
        if (reconnect) {
            mAdbManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
            mAdbManager.startLogcat()
        }
        mFilteredTableModel.startScan()
    }

    fun isRestartAdbLogcat(): Boolean {
        return mRetryAdbToggle.isSelected
    }

    fun restartAdbLogcat() {
        println("Restart Adb Logcat")
        mAdbManager.stop()
        mAdbManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
        mAdbManager.startLogcat()
    }

    fun pauseAdbScan(pause: Boolean) {
        mFilteredTableModel.pauseScan(pause)
    }

    internal inner class ActionHandler : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            if (p0?.source == mItemFileOpen) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN, FileDialog.LOAD)
                fileDialog.isMultipleMode = false
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.isVisible = true
                if (fileDialog.file != null) {
                    val file = File(fileDialog.directory + fileDialog.file)
                    openFile(file.absolutePath, false)
                } else {
                    println("Cancel Open")
                }
            } else if (p0?.source == mItemFileOpenFiles) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN_FILES, FileDialog.LOAD)
                fileDialog.isMultipleMode = true
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.isVisible = true
                val fileList = fileDialog.files
                if (fileList != null) {
                    var isFirst = true
                    for (file in fileList) {
                        if (isFirst) {
                            openFile(file.absolutePath, false)
                            isFirst = false
                        } else {
                            openFile(file.absolutePath, true)
                        }
                    }
                } else {
                    println("Cancel Open")
                }
            } else if (p0?.source == mItemFileAppendFiles) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.APPEND_FILES, FileDialog.LOAD)
                fileDialog.isMultipleMode = true
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.isVisible = true
                val fileList = fileDialog.files
                if (fileList != null) {
                    for (file in fileList) {
                        openFile(file.absolutePath, true)
                    }
                } else {
                    println("Cancel Open")
                }
            } else if (p0?.source == mItemFileExit) {
                exit()
            } else if (p0?.source == mItemAdb || p0?.source == mItemLogFile) {
                val settingsDialog = AdbSettingsDialog(this@MainUI)
                settingsDialog.setLocationRelativeTo(this@MainUI)
                settingsDialog.isVisible = true
            } else if (p0?.source == mItemFont) {
                val fontDialog = FontDialog(this@MainUI)
                fontDialog.setLocationRelativeTo(this@MainUI)
                fontDialog.isVisible = true
            } else if (p0?.source == mItemFull) {
                if (mItemFull.state) {
                    attachLogPanel(mFullLogPanel)
                } else {
                    windowedModeLogPanel(mFullLogPanel)
                }

                mConfigManager.saveItem(ConfigManager.ITEM_VIEW_FULL, mItemFull.state.toString())
            } else if (p0?.source == mItemFilterIncremental) {
                mConfigManager.saveItem(ConfigManager.ITEM_FILTER_INCREMENTAL, mItemFilterIncremental.state.toString())
            } else if (p0?.source == mItemFilterStyle) {
                val filterStyleDialog = FilterStyleDialog(this@MainUI)
                filterStyleDialog.setLocationRelativeTo(this@MainUI)
                filterStyleDialog.isVisible = true
            } else if (p0?.source == mItemLaF) {
                val laFDialog = LaFDialog(this@MainUI)
                laFDialog.setLocationRelativeTo(this@MainUI)
                laFDialog.isVisible = true
            } else if (p0?.source == mItemAbout) {
                val aboutDialog = AboutDialog(this@MainUI)
                aboutDialog.setLocationRelativeTo(this@MainUI)
                aboutDialog.isVisible = true
            } else if (p0?.source == mItemHelp) {
                val helpDialog = HelpDialog(this@MainUI)
                helpDialog.setLocationRelativeTo(this@MainUI)
                helpDialog.isVisible = true
            } else if (p0?.source == mAdbConnectBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
                mAdbManager.connect()
            } else if (p0?.source == mAdbRefreshBtn) {
                mAdbManager.getDevices()
            } else if (p0?.source == mAdbDisconnectBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.disconnect()
            } else if (p0?.source == mScrollbackApplyBtn) {
                try {
                    mFilteredTableModel.mScrollback = mScrollbackTF.text.toString().trim().toInt()
                } catch (e: java.lang.NumberFormatException) {
                    mFilteredTableModel.mScrollback = 0
                    mScrollbackTF.text = "0"
                }
                mFilteredTableModel.mScrollbackSplitFile = mScrollbackSplitFileCheck.isSelected

                mConfigManager.saveItem(ConfigManager.ITEM_SCROLLBACK, mScrollbackTF.text)
                mConfigManager.saveItem(ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE, mScrollbackSplitFileCheck.isSelected.toString())
            } else if (p0?.source == mStartBtn) {
                startAdbScan(true)
            } else if (p0?.source == mStopBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.stop()
//            } else if (p0?.source == mPauseBtn) {
            } else if (p0?.source == mClearBtn) {
                mFilteredTableModel.clearItems()
                repaint()
            } else if (p0?.source == mSaveBtn) {
//                mFilteredTableModel.clearItems()
                setSaveLogFile()
//                repaint()
            } else if (p0?.source == mRotationBtn) {
                mRotationStatus++

                if (mRotationStatus > ROTATION_MAX) {
                    mRotationStatus = Companion.ROTATION_LEFT_RIGHT
                }

                mConfigManager.saveItem(ConfigManager.ITEM_ROTATION, mRotationStatus.toString())

                mLogSplitPane.remove(mFilteredLogPanel)
                mLogSplitPane.remove(mFullLogPanel)
                when (mRotationStatus) {
                    Companion.ROTATION_LEFT_RIGHT -> {
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
            } else if (p0?.source == mFiltersBtn) {
                mFiltersManager.showDialog()
            } else if (p0?.source == mCmdsBtn) {
                mCmdsManager.showDialog()
            }
        }
    }

    internal inner class FramePopUp : JPopupMenu() {
        var mReconnectItem: JMenuItem
        private val mActionHandler = ActionHandler()

        init {
            mReconnectItem = JMenuItem("Reconnect " + mDeviceCombo.selectedItem?.toString())
            mReconnectItem.addActionListener(mActionHandler)
            add(mReconnectItem)
        }
        internal inner class ActionHandler : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                if (p0?.source == mReconnectItem) {
                    reconnectAdb()
                }
            }
        }
    }
    internal inner class FrameMouseListener(private val frame: JFrame) : MouseAdapter() {
        private var mouseDownCompCoords: Point? = null

        private var popupMenu: JPopupMenu? = null
        override fun mouseReleased(e: MouseEvent) {
            mouseDownCompCoords = null

            if (SwingUtilities.isRightMouseButton(e)) {
                if (e.source == this@MainUI.contentPane) {
                    popupMenu = FramePopUp()
                    popupMenu?.show(e.component, e.x, e.y)
                }
            }
            else {
                popupMenu?.isVisible = false
            }
        }

        override fun mousePressed(e: MouseEvent) {
            mouseDownCompCoords = e.point
        }

        override fun mouseDragged(e: MouseEvent) {
            val currCoords = e.locationOnScreen
            frame.setLocation(currCoords.x - mouseDownCompCoords!!.x, currCoords.y - mouseDownCompCoords!!.y)
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
            mSelectAllItem = JMenuItem("Select All")
            mSelectAllItem.addActionListener(mActionHandler)
            add(mSelectAllItem)
            mCopyItem = JMenuItem("Copy")
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mPasteItem = JMenuItem("Paste")
            mPasteItem.addActionListener(mActionHandler)
            add(mPasteItem)
            mReconnectItem = JMenuItem("Reconnect " + mDeviceCombo.selectedItem?.toString())
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
                        val editorCom: JTextComponent = mCombo?.editor?.editorComponent as JTextField
                        editorCom.selectedText
                        val stringSelection = StringSelection(editorCom.selectedText)
                        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(stringSelection, null)
                    }
                    mPasteItem -> {
                        val editorCom: JTextComponent = mCombo?.editor?.editorComponent as JTextField
                        editorCom.paste()
                    }
                    mReconnectItem -> {
                        reconnectAdb()
                    }
                }
            }
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
        }

        private var popupMenu: JPopupMenu? = null
        override fun mouseReleased(p0: MouseEvent?) {
            if (p0 == null) {
                super.mouseReleased(p0)
                return
            }

            if (SwingUtilities.isRightMouseButton(p0)) {
                if (p0.source == mDeviceCombo.editor.editorComponent
                        || p0.source == mShowLogCombo.editor.editorComponent
                        || p0.source == mBoldLogCombo.editor.editorComponent
                        || p0.source == mShowTagCombo.editor.editorComponent
                        || p0.source == mShowPidCombo.editor.editorComponent
                        || p0.source == mShowTidCombo.editor.editorComponent) {
                    var combo: JComboBox<String>? = null
                    when (p0.source) {
                        mDeviceCombo.editor.editorComponent -> {
                            combo = mDeviceCombo
                        }
                        mShowLogCombo.editor.editorComponent -> {
                            combo = mShowLogCombo
                        }
                        mBoldLogCombo.editor.editorComponent -> {
                            combo = mBoldLogCombo
                        }
                        mShowTagCombo.editor.editorComponent -> {
                            combo = mShowTagCombo
                        }
                        mShowPidCombo.editor.editorComponent -> {
                            combo = mShowPidCombo
                        }
                        mShowTidCombo.editor.editorComponent -> {
                            combo = mShowTidCombo
                        }
                    }
                    popupMenu = PopUpCombobox(combo)
                    popupMenu?.show(p0.component, p0.x, p0.y)
                }
                else {
                    val compo = p0.source as JComponent
                    val event = MouseEvent(compo.parent, p0.id, p0.`when`, p0.modifiers, p0.x + compo.x, p0.y + compo.y, p0.clickCount, p0.isPopupTrigger)

                    compo.parent.dispatchEvent(event)
                }
            }
            else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }
    }

    fun reconnectAdb() {
        println("Reconnect ADB")
        if (mDeviceCombo.selectedItem!!.toString().isNotEmpty()) {
            mStopBtn.doClick()
            Thread.sleep(200)
            mAdbConnectBtn.doClick()
            Thread.sleep(200)
            Thread(Runnable {
                run {
                    Thread.sleep(200)
                    mClearBtn.doClick()
                    Thread.sleep(200)
                    mStartBtn.doClick()
                }
            }).start()
        }
    }

    fun startAdbLog() {
        Thread(Runnable {
            run {
                mStartBtn.doClick()
            }
        }).start()
    }

    fun stopAdbLog() {
        mStopBtn.doClick()
    }

    fun clearAdbLog() {
        Thread(Runnable {
            run {
                mClearBtn.doClick()
            }
        }).start()
    }

    fun clearSaveAdbLog() {
        Thread(Runnable {
            run {
                mSaveBtn.doClick()
            }
        }).start()
    }

    fun getTextShowLogCombo() : String {
        if (mShowLogCombo.selectedItem == null) {
           return ""
        }
        return mShowLogCombo.selectedItem!!.toString()
    }

    fun setTextShowLogCombo(text : String) {
        mShowLogCombo.selectedItem = text
        mShowLogCombo.updateTooltip()
    }

    fun applyShowLogCombo() {
        val item = mShowLogCombo.selectedItem!!.toString()
        resetComboItem(mShowLogCombo, item)
        mFilteredTableModel.mFilterLog = item
    }

    fun setDeviceComboColor(isConnected: Boolean) {
        if (isConnected) {
            if (ConfigManager.LaF == FLAT_DARK_LAF) {
                mDeviceCombo.editor.editorComponent.foreground = Color(0x7070C0)
            }
            else {
                mDeviceCombo.editor.editorComponent.foreground = Color.BLUE
            }
        } else {
            if (ConfigManager.LaF == FLAT_DARK_LAF) {
                mDeviceCombo.editor.editorComponent.foreground = Color(0xC07070)
            }
            else {
                mDeviceCombo.editor.editorComponent.foreground = Color.RED
            }
        }
    }

    internal inner class KeyHandler : KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (KeyEvent.VK_ENTER == p0?.keyCode) {
                if (p0.source == mShowLogCombo.editor.editorComponent && mShowLogToggle.isSelected) {
                    val combo = mShowLogCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterLog = item
                } else if (p0.source == mBoldLogCombo.editor.editorComponent && mBoldLogToggle.isSelected) {
                    val combo = mBoldLogCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterHighlightLog = item
                } else if (p0.source == mShowTagCombo.editor.editorComponent && mShowTagToggle.isSelected) {
                    val combo = mShowTagCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTag = item
                } else if (p0.source == mShowPidCombo.editor.editorComponent && mShowPidToggle.isSelected) {
                    val combo = mShowPidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterPid = item
                } else if (p0.source == mShowTidCombo.editor.editorComponent && mShowTidToggle.isSelected) {
                    val combo = mShowTidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTid = item
                } else if (p0.source == mDeviceCombo.editor.editorComponent) {
                    reconnectAdb()
                } else if (p0.source == mScrollbackTF) {
                    mScrollbackApplyBtn.doClick()
                }
            } else if (p0 != null && mItemFilterIncremental.state) {
                if (p0.source == mShowLogCombo.editor.editorComponent && mShowLogToggle.isSelected) {
                    val item = mShowLogCombo.editor.item.toString()
                    mFilteredTableModel.mFilterLog = item
                } else if (p0.source == mBoldLogCombo.editor.editorComponent && mBoldLogToggle.isSelected) {
                    val item = mBoldLogCombo.editor.item.toString()
                    mFilteredTableModel.mFilterHighlightLog = item
                } else if (p0.source == mShowTagCombo.editor.editorComponent && mShowTagToggle.isSelected) {
                    val item = mShowTagCombo.editor.item.toString()
                    mFilteredTableModel.mFilterTag = item
                } else if (p0.source == mShowPidCombo.editor.editorComponent && mShowPidToggle.isSelected) {
                    val item = mShowPidCombo.editor.item.toString()
                    mFilteredTableModel.mFilterPid = item
                } else if (p0.source == mShowTidCombo.editor.editorComponent && mShowTidToggle.isSelected) {
                    val item = mShowTidCombo.editor.item.toString()
                    mFilteredTableModel.mFilterTid = item
                }
            }
            super.keyReleased(p0)
        }
    }

    internal inner class ItemHandler : ItemListener {
        override fun itemStateChanged(p0: ItemEvent?) {
            when (p0?.source) {
                mShowLogToggle -> {
                    mShowLogCombo.setEnabledFilter(mShowLogToggle.isSelected)
                }
                mBoldLogToggle -> {
                    mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)
                }
                mShowTagToggle -> {
                    mShowTagCombo.setEnabledFilter(mShowTagToggle.isSelected)
                }
                mShowPidToggle -> {
                    mShowPidCombo.setEnabledFilter(mShowPidToggle.isSelected)
                }
                mShowTidToggle -> {
                    mShowTidCombo.setEnabledFilter(mShowTidToggle.isSelected)
                }
            }

            if (mIsCreatingUI) {
                return
            }
            when (p0?.source) {
                mShowLogToggle -> {
                    if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
                        mFilteredTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
                    } else {
                        mFilteredTableModel.mFilterLog = ""
                    }
                    mConfigManager.saveItem(ConfigManager.ITEM_SHOW_LOG_CHECK, mShowLogToggle.isSelected.toString())
                }
                mBoldLogToggle -> {
                    if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
                        mFilteredTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
                    } else {
                        mFilteredTableModel.mFilterHighlightLog = ""
                    }
                    mConfigManager.saveItem(ConfigManager.ITEM_HIGHLIGHT_LOG_CHECK, mBoldLogToggle.isSelected.toString())
                }
                mShowTagToggle -> {
                    if (mShowTagToggle.isSelected && mShowTagCombo.selectedItem != null) {
                        mFilteredTableModel.mFilterTag = mShowTagCombo.selectedItem!!.toString()
                    } else {
                        mFilteredTableModel.mFilterTag = ""
                    }
                    mConfigManager.saveItem(ConfigManager.ITEM_SHOW_TAG_CHECK, mShowTagToggle.isSelected.toString())
                }
                mShowPidToggle -> {
                    if (mShowPidToggle.isSelected && mShowPidCombo.selectedItem != null) {
                        mFilteredTableModel.mFilterPid = mShowPidCombo.selectedItem!!.toString()
                    } else {
                        mFilteredTableModel.mFilterPid = ""
                    }
                    mConfigManager.saveItem(ConfigManager.ITEM_SHOW_PID_CHECK, mShowPidToggle.isSelected.toString())
                }
                mShowTidToggle -> {
                    if (mShowTidToggle.isSelected && mShowTidCombo.selectedItem != null) {
                        mFilteredTableModel.mFilterTid = mShowTidCombo.selectedItem!!.toString()
                    } else {
                        mFilteredTableModel.mFilterTid = ""
                    }
                    mConfigManager.saveItem(ConfigManager.ITEM_SHOW_TID_CHECK, mShowTidToggle.isSelected.toString())
                }
                mMatchCaseToggle -> {
                    mFilteredTableModel.mMatchCase = mMatchCaseToggle.isSelected
                    mConfigManager.saveItem(ConfigManager.ITEM_MATCH_CASE, mMatchCaseToggle.isSelected.toString())
                }
                mScrollbackKeepToggle -> {
                    mFilteredTableModel.mScrollbackKeep = mScrollbackKeepToggle.isSelected
                }
                mRetryAdbToggle -> {
                    mConfigManager.saveItem(ConfigManager.ITEM_RETRY_ADB, mRetryAdbToggle.isSelected.toString())
                }
                mPauseToggle -> {
                    if (mPauseToggle.isSelected) {
                        pauseAdbScan(true)
                    }
                    else {
                        pauseAdbScan(false)
                    }
                }
            }
        }
    }

    internal inner class LevelItemHandler : ItemListener {
        override fun itemStateChanged(p0: ItemEvent?) {
            val item = p0?.source as JRadioButtonMenuItem
            when (item.text) {
                VERBOSE->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_VERBOSE
                DEBUG->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_DEBUG
                INFO->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_INFO
                WARNING->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_WARNING
                ERROR->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_ERROR
                FATAL->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_FATAL
            }
            mConfigManager.saveItem(ConfigManager.ITEM_LOG_LEVEL, item.text)
        }
    }

    internal inner class AdbHandler : AdbManager.AdbEventListener {
        override fun changedStatus(event: AdbManager.AdbEvent) {
            when (event.cmd) {
                AdbManager.CMD_CONNECT -> {
                    mAdbManager.getDevices()
                }
                AdbManager.CMD_GET_DEVICES -> {
                    if (mIsCreatingUI) {
                        return
                    }
                    var selectedItem = mDeviceCombo.selectedItem
                    mDeviceCombo.removeAllItems()
                    for (item in mAdbManager.mDevices) {
                        mDeviceCombo.addItem(item)
                    }
                    if (selectedItem == null) {
                        selectedItem = ""
                    }

                    if (mAdbManager.mDevices.contains(selectedItem.toString())) {
                        mDeviceStatus.text = Strings.CONNECTED
                        setDeviceComboColor(true)
                    } else {
                        var isExist = false
                        val deviceChk = "$selectedItem:"
                        for (device in mAdbManager.mDevices) {
                            if (device.contains(deviceChk)) {
                                isExist = true
                                selectedItem = device
                                break
                            }
                        }
                        if (isExist) {
                            mDeviceStatus.text = Strings.CONNECTED
                            setDeviceComboColor(true)
                        } else {
                            mDeviceStatus.text = Strings.NOT_CONNECTED
                            setDeviceComboColor(false)
                        }
                    }
                    mDeviceCombo.selectedItem = selectedItem
                }
                AdbManager.CMD_DISCONNECT -> {
                    mAdbManager.getDevices()
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
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterLog = item
                    combo.updateTooltip()
                }
                mBoldLogCombo -> {
                    if (mBoldLogCombo.selectedIndex < 0) {
                        return
                    }
                    val combo = mBoldLogCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterHighlightLog = item
                    combo.updateTooltip()
                }
                mShowTagCombo -> {
                    if (mShowTagCombo.selectedIndex < 0) {
                        return
                    }
                    val combo = mShowTagCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTag = item
                    combo.updateTooltip()
                }
                mShowPidCombo -> {
                    if (mShowPidCombo.selectedIndex < 0) {
                        return
                    }
                    val combo = mShowPidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterPid = item
                    combo.updateTooltip()
                }
                mShowTidCombo -> {
                    if (mShowTidCombo.selectedIndex < 0) {
                        return
                    }
                    val combo = mShowTidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTid = item
                    combo.updateTooltip()
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
            scrollPane.verticalScrollBar?.ui = BasicScrollBarUI()
            scrollPane.horizontalScrollBar?.ui = BasicScrollBarUI()
            mIsCanceled = false
        }
    }

    internal inner class ComponentHandler : ComponentAdapter() {
        override fun componentResized(p0: ComponentEvent?) {
            revalidate()
            super.componentResized(p0)
        }
    }

    fun resetComboItem(combo: FilterComboBox, item: String) {
        if (combo.isExistItem(item)) {
            if (combo.selectedIndex == 0) {
                return
            }
            combo.removeItem(item)
        }
        combo.insertItemAt(item, 0)
        combo.selectedIndex = 0
        return
    }

    fun goToLine(line: Int) {
        println("Line : $line")
        if (line < 0) {
            return
        }
        var num = 0
        for (idx in 0 until mFilteredTableModel.rowCount) {
            num = mFilteredTableModel.getValueAt(idx, 0).toString().trim().toInt()
            if (line <= num) {
                mFilteredLogPanel.goToRow(idx, 0)
                break
            }
        }

        if (line != num) {
            for (idx in 0 until mFullTableModel.rowCount) {
                num = mFullTableModel.getValueAt(idx, 0).toString().trim().toInt()
                if (line <= num) {
                    mFullLogPanel.goToRow(idx, 0)
                    break
                }
            }
        }
    }

    fun markLine() {
        if (mIsCreatingUI) {
            return
        }
        mSelectedLine = mFilteredLogPanel.getSelectedLine()
    }

    fun getMarkLine(): Int {
        return mSelectedLine
    }

    fun goToMarkedLine() {
        if (mIsCreatingUI) {
            return
        }
        goToLine(mSelectedLine)
    }

    fun updateUIAfterVisible() {
        if (mShowLogCombo.selectedIndex >= 0 && (mShowLogComboStyle == FilterComboBox.Mode.MULTI_LINE || mShowLogComboStyle == FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT)) {
            val selectedItem = mShowLogCombo.selectedItem
            mShowLogCombo.selectedItem = ""
            mShowLogCombo.selectedItem = selectedItem
            mShowLogCombo.parent.revalidate()
            mShowLogCombo.parent.repaint()
        }
        if (mShowTagCombo.selectedIndex >= 0 && (mShowTagComboStyle == FilterComboBox.Mode.MULTI_LINE || mShowTagComboStyle == FilterComboBox.Mode.MULTI_LINE_HIGHLIGHT)) {
            val selectedItem = mShowTagCombo.selectedItem
            mShowTagCombo.selectedItem = ""
            mShowTagCombo.selectedItem = selectedItem
            mShowTagCombo.parent.revalidate()
            mShowTagCombo.parent.repaint()
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
        mShowTagCombo.mEnabledTfTooltip = true
        mShowPidCombo.mEnabledTfTooltip = true
        mShowTidCombo.mEnabledTfTooltip = true
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
}

