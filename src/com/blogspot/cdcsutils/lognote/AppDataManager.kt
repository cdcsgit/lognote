package com.blogspot.cdcsutils.lognote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.File
import java.util.*

class InnerListCompactAdapter : TypeAdapter<List<List<Any>>?>() {
    // Safely extract private indent field using reflection
    private fun getIndent(out: JsonWriter): String {
        return try {
            val field = JsonWriter::class.java.getDeclaredField("indent")
            field.isAccessible = true
            (field.get(out) as? String) ?: ""
        } catch (e: Exception) {
            "  " // Default fallback indent
        }
    }

    // Serialize object to JSON format
    override fun write(out: JsonWriter, value: List<List<Any>>?) {
        Utils.printlnLog("InnerListCompactAdapter write")
        // Handle null values safely
        if (value == null) {
            out.nullValue()
            return
        }

        val originalIndent = getIndent(out)

        out.beginArray() // Open outer array (pretty-printed)
        for (innerList in value) {
            out.beginArray() // Open inner array (written inline)
            out.setIndent("")
            for (item in innerList) {
                when (item) {
                    is Number -> out.value(item)
                    is Boolean -> out.value(item)
                    is String -> out.value(item)
                    else -> out.value(item.toString())
                }
            }
            out.endArray() // Close inner array
            out.setIndent(originalIndent)
        }
        out.endArray() // Close outer array
    }

    // Deserialize JSON format back to object
    override fun read(reader: JsonReader): List<List<Any>>? {
        // Return null if JSON token is NULL
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val result = mutableListOf<List<Any>>()
        reader.beginArray() // Read outer array start
        while (reader.hasNext()) {
            val innerList = mutableListOf<Any>()
            reader.beginArray() // Read inner array start
            while (reader.hasNext()) {
                when (reader.peek()) {
                    JsonToken.NUMBER -> {
                        val numberStr = reader.nextString()
                        // Parse as Double if decimal point exists, otherwise Long
                        if (numberStr.contains(".")) {
                            innerList.add(numberStr.toDouble())
                        } else {
                            innerList.add(numberStr.toLong())
                        }
                    }
                    JsonToken.BOOLEAN -> innerList.add(reader.nextBoolean())
                    JsonToken.STRING -> innerList.add(reader.nextString())
                    else -> reader.skipValue() // Skip unknown value types
                }
            }
            reader.endArray() // Read inner array end
            result.add(innerList)
        }
        reader.endArray() // Read outer array end
        return result
    }
}

data class AppearanceSettings(
    val frameX: Int? = null,
    val frameY: Int? = null,
    val frameWidth: Int? = null,
    val frameHeight: Int? = null,
    val frameExtendedState: Int? = null,
    val rotation: Int? = null,
    val dividerLocation: Int? = null,
    val lastDividerLocation: Int? = null,

    val toolRotation: Int? = null,
    val toolDividerLocation: Int? = null,
    val toolLastDividerLocation: Int? = null,

    val language: String? = null,

    val logFormat: String? = null,
    val logLevel: String? = null,

    val lookAndFeel: String? = null,
    val lafAccentColor: String? = null,
    val uiFontSize: Int? = null,
    val appearanceDividerSize: Int? = null,
    val logViewWidth: Int? = null,

    val fontName: String? = null,
    val fontSize: Int? = null,
    val viewFull: Boolean? = null,
    val viewColumnMode: Boolean? = null,
    val viewProcessName: String? = null,

    val scrollback: Int? = null,
    val scrollbackSplitFile: Boolean? = null,

    val iconText: String? = null,

    val cmdToolbar: Boolean? = null,
)

data class ColorSettings (
    @JsonAdapter(InnerListCompactAdapter::class)
    val colorFullTable: List<List<Any>>? = null,
    val colorFullTableTag: List<String>? = null,
    @JsonAdapter(InnerListCompactAdapter::class)
    val colorFilterTable: List<List<Any>>? = null,
    val colorFilterTableTag: List<String>? = null,
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
    val matchCase: Boolean? = null,
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

data class PresetElement (
    val title: String? = null,
    val value: String? = null,
    val tableBar: String? = null,
)

data class AppData(
    val version: String = "",
    val appearance: AppearanceSettings = AppearanceSettings(),
    val color: ColorSettings = ColorSettings(),
    val tool: ToolSettings = ToolSettings(),
    val logCmd: LogCmdSettings = LogCmdSettings(),
    val filterOption: FilterOptionSettings = FilterOptionSettings(),
    val filter: FilterValues = FilterValues(),
    val filterSnippets: List<PresetElement>? = null,
    val cmdAlias: List<PresetElement>? = null,
    val targetPackages: List<String>? = null,
)

class AppDataManager private constructor() {
    companion object {
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

        val fullList: List<List<Any>> = fullColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }
        val filterList: List<List<Any>> = filterColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }

        mAppData = mAppData.copy(appearance = mAppData.appearance.copy(fontName = family, fontSize = size),
            color = mAppData.color.copy(colorFullTable = fullList, colorFilterTable = filterList))

        saveConfig(mAppData)
    }

    fun saveFilterStyle(logStyle: Int, boldStyle: Int, tokenStyles: List<Int>) {
        loadConfig()

        mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(showLogStyle = logStyle, boldLogStyle = boldStyle, tokenComboStyles = tokenStyles))

        saveConfig(mAppData)
    }

    fun loadFilters() : ArrayList<PresetElement> {
        return (if (mAppData.filterSnippets == null) {
            ArrayList<PresetElement>()
        } else {
            mAppData.filterSnippets
        }) as ArrayList<PresetElement>
    }

    fun saveFilters(filters : ArrayList<PresetElement>) {
        loadConfig()

        mAppData = mAppData.copy(filterSnippets = filters.take(FiltersManager.MAX_FILTERS))

        saveConfig(mAppData)
        return
    }

    fun loadCmds() : ArrayList<PresetElement> {
        return (if (mAppData.cmdAlias == null) {
            ArrayList<PresetElement>()
        } else {
            mAppData.cmdAlias
        }) as ArrayList<PresetElement>
    }

    fun saveCmds(cmds : ArrayList<PresetElement>) {
        loadConfig()

        mAppData = mAppData.copy(cmdAlias = cmds.take(CmdManager.MAX_CMD_COUNT))

        saveConfig(mAppData)
        return
    }

    fun loadPackages() : ArrayList<String> {
        return (if (mAppData.targetPackages == null) {
            ArrayList<String>()
        } else {
            mAppData.targetPackages
        }) as ArrayList<String>
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

    private fun getFromConfig(config: ConfigManager, key: String): String? {
        val prop = config.getItem(key)
        return prop
    }

    private fun getIntFromConfig(config: ConfigManager, key: String): Int? {
        val prop = config.getItem(key)
        return if (!prop.isNullOrEmpty()) {
            prop.toInt()
        } else {
            null
        }
    }

    private fun getBooleanFromConfig(config: ConfigManager, key: String): Boolean? {
        val prop = config.getItem(key)
        return if (!prop.isNullOrEmpty()) {
            prop.toBoolean()
        } else {
            null
        }
    }

    private fun updateAppDataFromV0ToV1() {
        Utils.printlnLog("updateAppDataFromV0ToV1 : copy from config.xml ++")

        val oldConfigPath = getHomePath("lognote.xml")
        Utils.printlnLog("Config Path : $oldConfigPath")
        val file = File(oldConfigPath)

        if (file.exists()) {
            val configManager = ConfigManager.getInstance()

            val frameX = getIntFromConfig(configManager, ConfigManager.ITEM_FRAME_X)
            val frameY = getIntFromConfig(configManager, ConfigManager.ITEM_FRAME_Y)
            val frameWidth = getIntFromConfig(configManager, ConfigManager.ITEM_FRAME_WIDTH)
            val frameHeight = getIntFromConfig(configManager, ConfigManager.ITEM_FRAME_HEIGHT)
            val frameExtendedState = getIntFromConfig(configManager, ConfigManager.ITEM_FRAME_EXTENDED_STATE)
            val rotation = getIntFromConfig(configManager, ConfigManager.ITEM_ROTATION)
            val lastDividerLocation = getIntFromConfig(configManager, ConfigManager.ITEM_LAST_DIVIDER_LOCATION)
            val dividerLocation = getIntFromConfig(configManager, ConfigManager.ITEM_DIVIDER_LOCATION)

            val toolRotation = getIntFromConfig(configManager, ConfigManager.ITEM_TOOL_ROTATION)
            val toolLastDividerLocation = getIntFromConfig(configManager, ConfigManager.ITEM_TOOL_LAST_DIVIDER_LOCATION)
            val toolDividerLocation = getIntFromConfig(configManager, ConfigManager.ITEM_TOOL_DIVIDER_LOCATION)

            val language = getFromConfig(configManager, ConfigManager.ITEM_LANG)

            val logFormat = getFromConfig(configManager, ConfigManager.ITEM_LOG_FORMAT)
            val logLevel = getFromConfig(configManager, ConfigManager.ITEM_LOG_LEVEL)

            val lookAndFeel = getFromConfig(configManager, ConfigManager.ITEM_LOOK_AND_FEEL)
            val lafAccentColor = getFromConfig(configManager, ConfigManager.ITEM_LAF_ACCENT_COLOR)
            val uiFontSize = getIntFromConfig(configManager, ConfigManager.ITEM_UI_FONT_SIZE)
            val appearanceDividerSize = getIntFromConfig(configManager, ConfigManager.ITEM_APPEARANCE_DIVIDER_SIZE)
            val logViewWidth = getIntFromConfig(configManager, ConfigManager.ITEM_LOG_VIEW_WIDTH)

            val fontName = getFromConfig(configManager, ConfigManager.ITEM_FONT_NAME)
            val fontSize = getIntFromConfig(configManager, ConfigManager.ITEM_FONT_SIZE)
            val viewFull = getBooleanFromConfig(configManager, ConfigManager.ITEM_VIEW_FULL)
            val viewColumnMode = getBooleanFromConfig(configManager, ConfigManager.ITEM_VIEW_COLUMN_MODE)
            val viewProcessName = getFromConfig(configManager, ConfigManager.ITEM_VIEW_PROCESS_NAME)

            val scrollback = getIntFromConfig(configManager, ConfigManager.ITEM_SCROLLBACK)
            val scrollbackSplitFile = getBooleanFromConfig(configManager, ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE)

            val iconText = getFromConfig(configManager, ConfigManager.ITEM_ICON_TEXT)
            val cmdToolbar = getBooleanFromConfig(configManager, ConfigManager.ITEM_CMD_TOOLBAR)

            mAppData = mAppData.copy(appearance = mAppData.appearance.copy(frameX = frameX, frameY = frameY,
                frameWidth = frameWidth, frameHeight = frameHeight, frameExtendedState = frameExtendedState,
                rotation = rotation, lastDividerLocation = lastDividerLocation, dividerLocation = dividerLocation,
                toolRotation = toolRotation, toolLastDividerLocation = toolLastDividerLocation, toolDividerLocation = toolDividerLocation,
                language = language, logFormat = logFormat, logLevel = logLevel, lookAndFeel = lookAndFeel, lafAccentColor = lafAccentColor,
                uiFontSize = uiFontSize, appearanceDividerSize = appearanceDividerSize, logViewWidth = logViewWidth,
                fontName = fontName, fontSize = fontSize, viewFull = viewFull, viewColumnMode = viewColumnMode, viewProcessName = viewProcessName,
                scrollback = scrollback, scrollbackSplitFile = scrollbackSplitFile, iconText = iconText, cmdToolbar = cmdToolbar))


        }

//        mAppData = mAppData.copy(version = "1")
        Utils.printlnLog("updateAppDataFromV0ToV1 : --")
    }
}

