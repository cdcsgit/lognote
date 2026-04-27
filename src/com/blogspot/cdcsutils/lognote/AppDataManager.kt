package com.blogspot.cdcsutils.lognote

import com.blogspot.cdcsutils.lognote.MainUI.Companion.FLAT_LIGHT_LAF
import com.blogspot.cdcsutils.lognote.MainUI.Companion.LAF_ACCENT_COLORS
import com.blogspot.cdcsutils.lognote.MainUI.Companion.SYSTEM_LAF
import com.formdev.flatlaf.FlatLaf
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.awt.Color
import java.io.File
import java.util.*

data class AppearanceSettings(
    val frameX: String? = null,
    val frameY: String? = null,
    val frameWidth: String? = null,
    val frameHeight: String? = null,
    val frameExtendedState: String? = null,
    val rotation: String? = null,
    val dividerLocation: String? = null,
    val lastDividerLocation: String? = null,

    val toolRotation: String? = null,
    val toolDividerLocation: String? = null,
    val toolLastDividerLocation: String? = null,

    val lang: String? = null,

    val logLevel: String? = null,
    val logFormat: String? = null,

    val lookAndFeel: String? = null,
    val lafAccentColor: String? = null,
    val uiFontSize: String? = null,
    val appearanceDividerSize: String? = null,
    val logViewWidth: String? = null,

    val fontName: String? = null,
    val fontSize: Int? = null,
    val viewFull: String? = null,
    val viewColumnMode: String? = null,
    val viewProcessName: String? = null,

    val scrollback: String? = null,
    val scrollbackSplitFile: String? = null,
    val matchCase: String? = null,

    val iconText: String? = null,
    val valueIconTextIT: String? = null,
    val valueIconTextI: String? = null,
    val valueIconTextT: String? = null,

    val cmdToolbar: String? = null,
)

data class ColorSettings (
    val colorFullTable: List<String>? = null,
    val colorFilterTable: List<String>? = null,
    val colorFilterStyle: List<String>? = null,
)

data class ToolSettings (
    val toolPanel: String? = null,
    val toolSelection: String? = null,
    val toolSelectionRangePrevious: String? = null,
    val toolSelectionRangeNext: String? = null,
    val toolTestEnable: String? = null,
    val toolTest: String? = null,
)

data class LogCmdSettings (
    val adbDevice: String? = null,
    val adbCmd: String? = null,
    val adbLogCmd: String? = null,
    val adbLogSavePath: String? = null,
    val adbPrefix: String? = null,
    val adbOption_1: String? = null,
    val retryAdb: String? = null,
)

data class FilterOptionSettings (
    val filterIncremental: String? = null,
    val filterByFile: String? = null,
    val colorTagRegex: String? = null,
    val showLogStyle: Int? = null,
    val boldLogStyle: Int? = null,
    val tokenComboStyles: List<Int>? = null,
)

data class FilterValues (
    val itemShowLog: String? = null,
    val countShowLog: Int? = null,
    val itemTokenFilter: String? = null,
    val countTokenFilter: Int? = null,
    val saveFilterCount: Int? = null,

    val itemHighlightLog: String? = null,
    val countHighlightLog: Int? = null,

    val itemFindLog: String? = null,
    val countFindLog: Int? = null,
    val itemFindMatchCase: String? = null,

    val itemShowLogCheck: String? = null,
    val itemTokenCheck: String? = null,

    val itemHighlightLogCheck: String? = null,
)

data class PresetSettings (
    val itemFiltersTitle: String? = null,
    val itemFiltersFilter: String? = null,
    val itemFiltersTablebar: String? = null,

    val itemCmdsTitle: String? = null,
    val itemCmdsCmd: String? = null,
    val itemCmdsTablebar: String? = null,

    val itemPackagesItem: String? = null,
)

data class AppData(
    val version: String = "1",
    val appearance: AppearanceSettings = AppearanceSettings(),
    val color: ColorSettings = ColorSettings(),
    val tool: ToolSettings = ToolSettings(),
    val logCmd: LogCmdSettings = LogCmdSettings(),
    val filterOption: FilterOptionSettings = FilterOptionSettings(),
    val filter: FilterValues = FilterValues(),
    val preset: PresetSettings = PresetSettings(),
    val filterSnippets: List<CustomListManager.CustomElement>? = null,
    val cmdAlias: List<CustomListManager.CustomElement>? = null,
    val targetPackages: List<String>? = null,
)

class AppDataManager private constructor() {
    companion object {
        private const val CONFIG_FILE_OLD = "lognote.xml"
        private const val CONFIG_FILE = "lognote.json"
        val LOGNOTE_HOME: String = System.getenv("LOGNOTE_HOME") ?: ""

        private val mInstance: AppDataManager = AppDataManager()
        fun getInstance(): AppDataManager {
            return mInstance
        }

        fun getHomePath(fileName: String): String {
            if(LOGNOTE_HOME.isEmpty()) {
                return fileName
            }

            val firstPathExists = LOGNOTE_HOME
                .split(File.pathSeparator)
                .filter(String::isNotEmpty)
                .firstOrNull(Utils::pathExists)

            return if(firstPathExists != null) {
                "$firstPathExists${File.separator}$fileName"
            } else { // No valid path found same as empty LOGNOTE_HOME
                fileName
            }
        }
    }

    var mAppData = AppData()
        private set

    private var mConfigPath = CONFIG_FILE

    init {
        mConfigPath = getHomePath(CONFIG_FILE)
        Utils.printlnLog("Config Path : $mConfigPath")
        manageVersion()
    }

    fun loadConfig() {
        val file = File(mConfigPath)
        if (!file.exists()) {
            mAppData = AppData()
        }
        else {
            val jsonString = file.readText()
            mAppData = Gson().fromJson(jsonString, AppData::class.java)
        }
    }

    fun saveConfig(appData: AppData) {
        val gson = GsonBuilder().setPrettyPrinting().create()

        mAppData = appData
        val jsonString = gson.toJson(mAppData)
        File(mConfigPath).writeText(jsonString)
    }

    fun saveFontColors(family: String, size: Int, fullColors: Array<ColorManager.ColorItem>, filterColors: Array<ColorManager.ColorItem>) {
        loadConfig()

        mAppData = mAppData.copy(appearance = mAppData.appearance.copy(fontName = family, fontSize = size))
        val fullList: List<String> = fullColors.map { it.mStrColor }
        val filterList: List<String> = filterColors.map { it.mStrColor }
        mAppData = mAppData.copy(color = mAppData.color.copy(colorFullTable = fullList, colorFilterTable = filterList))

        saveConfig(mAppData)
    }

    fun saveFilterStyle(logStyle: Int, boldStyle: Int, tokenStyles: List<Int>) {
        loadConfig()

        mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(showLogStyle = logStyle, boldLogStyle = boldStyle, tokenComboStyles = tokenStyles))

        saveConfig(mAppData)
    }

    fun loadFilters() : ArrayList<CustomListManager.CustomElement> {
        return if (mAppData.filterSnippets == null) {
            ArrayList<CustomListManager.CustomElement>()
        } else {
            ArrayList(mAppData.filterSnippets)
        }
    }

    fun saveFilters(filters : ArrayList<CustomListManager.CustomElement>) {
        loadConfig()

        mAppData = mAppData.copy(filterSnippets = filters.take(FiltersManager.MAX_FILTERS))

        saveConfig(mAppData)
        return
    }

    fun loadCmds() : ArrayList<CustomListManager.CustomElement> {
        return if (mAppData.cmdAlias == null) {
            ArrayList<CustomListManager.CustomElement>()
        } else {
            ArrayList(mAppData.cmdAlias)
        }
    }

    fun saveCmds(cmds : ArrayList<CustomListManager.CustomElement>) {
        loadConfig()

        mAppData = mAppData.copy(cmdAlias = cmds.take(CmdManager.MAX_CMD_COUNT))

        saveConfig(mAppData)
        return
    }

    fun loadPackages() : ArrayList<String> {
        return if (mAppData.targetPackages == null) {
            ArrayList<String>()
        } else {
            ArrayList(mAppData.targetPackages)
        }
    }

    fun savePackages(packagess : ArrayList<String>) {
        loadConfig()

        mAppData = mAppData.copy(targetPackages = packagess.take(PackageManager.MAX_PACKAGE_COUNT))

        saveConfig(mAppData)
        return
    }

    private fun manageVersion() {
        loadConfig()

        if (mAppData.version.isEmpty()) {
            updateAppDataFromV0ToV1()
            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
        }

//        if (mAppData.version == "1") {
//            updateAppDataFromVToV2()
//            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
//        }

        saveConfig(mAppData)
    }

    private fun updateAppDataFromV0ToV1() {
        Utils.printlnLog("updateAppDataFromV0ToV1 : copy from config.xml ++")


        mAppData = mAppData.copy(version = "1")
        Utils.printlnLog("updateAppDataFromV0ToV1 : --")
    }
}

