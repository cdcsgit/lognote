package com.blogspot.cdcsutils.lognote

import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.ITEM_TRIGGER_ACTION
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.ITEM_TRIGGER_ACTION_PARAMETER
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.ITEM_TRIGGER_FILTER
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.ITEM_TRIGGER_NAME
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.ITEM_TRIGGER_ONCE
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.MAX_TRIGGER_COUNT
import com.blogspot.cdcsutils.lognote.AgingTestManager.Companion.TriggerAction
import com.blogspot.cdcsutils.lognote.ColorManager.TableColorType
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_CMDS_CMD
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_CMDS_TABLEBAR
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_CMDS_TITLE
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_FILTERS_FILTER
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_FILTERS_TABLEBAR
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_FILTERS_TITLE
import com.blogspot.cdcsutils.lognote.ConfigManager.Companion.ITEM_PACKAGES_ITEM
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_COLUMN_NAMES
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_LEVEL
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_LEVEL_POSITION
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_LOG_POSITION
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_NAME
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_PID_TOK_IDX
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_SAMPLE_TEXT
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_SEPARATOR
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_TOKEN_COUNT
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_TOKEN_FILTER_NAME
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_TOKEN_FILTER_POSITION
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_TOKEN_SAVE_FILTER
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.ITEM_TOKEN_UI_WIDTH
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.MAX_TOKEN_FILTER_COUNT
import com.blogspot.cdcsutils.lognote.FormatManager.Companion.TEXT_LEVEL
import com.blogspot.cdcsutils.lognote.FormatManager.FormatItem
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_BOOKMARKS
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_FIND_LOG
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_FIND_MATCH_CASE
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_HIGHLIGHT_LOG
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_HIGHLIGHT_LOG_CHECK
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_PATH
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_SHOW_LOG
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_SHOW_LOG_CHECK
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_TOKEN_CHECK
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.ITEM_TOKEN_FILTER
import com.blogspot.cdcsutils.lognote.RecentFileManager.Companion.MAX_RECENT_FILE
import com.blogspot.cdcsutils.lognote.RecentFileManager.RecentItem
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

class ListCompactAdapter : TypeAdapter<List<Any>?>() {
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
    override fun write(out: JsonWriter, value: List<Any>?) {
        Utils.printlnLog("ListCompactAdapter write")
        // Handle null values safely
        if (value == null) {
            out.nullValue()
            return
        }

        val originalIndent = getIndent(out)

        out.beginArray() // Open outer array (pretty-printed)
        for (item in value) {
            out.setIndent("")
            when (item) {
                is Number -> out.value(item)
                is Boolean -> out.value(item)
                is String -> out.value(item)
                else -> out.value(item.toString())
            }
        }
        out.endArray() // Close outer array
        out.setIndent(originalIndent)
    }

    // Deserialize JSON format back to object
    override fun read(reader: JsonReader): List<Any>? {
        // Return null if JSON token is NULL
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val result = mutableListOf<Any>()
        reader.beginArray()
        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonToken.NUMBER -> {
                    val numberStr = reader.nextString()
                    // Parse as Double if decimal point exists, otherwise Long
                    if (numberStr.contains(".")) {
                        result.add(numberStr.toDouble())
                    } else {
                        result.add(numberStr.toLong())
                    }
                }
                JsonToken.BOOLEAN -> result.add(reader.nextBoolean())
                JsonToken.STRING -> result.add(reader.nextString())
                else -> reader.skipValue() // Skip unknown value types
            }
        }
        reader.endArray()
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
    val colorFullView: List<List<Any>>? = null,
    @JsonAdapter(InnerListCompactAdapter::class)
    val colorFilterView: List<List<Any>>? = null,
    @JsonAdapter(InnerListCompactAdapter::class)
    val colorFilterStyle: List<List<Any>>? = null,
)

data class LogCmdSettings (
    val targetDevice: String? = null,
    val adbPath: String? = null,
    val logCmd: String? = null,
    val logSavePath: String? = null,
    val savePrefix: String? = null,
    val adbOptionUpdatePidTimeout: Int? = null,
    val retryLogCmd: Boolean? = null,
)

data class FilterOptionSettings (
    val filterIncremental: Boolean? = null,
    val matchCase: Boolean? = null,
    val filterByRecentFile: Boolean? = null,
    val colorTagRegex: Boolean? = null,
    val showLogStyle: Int? = null,
    val boldLogStyle: Int? = null,
    val tokenComboStyles: List<Int>? = null,
)

data class TokenCheckStatus(
    val key: String,
    val checkUse: Boolean,
)

data class FilterSettings (
    val findMatchCase: Boolean? = null,
    val showLogCheck: Boolean? = null,
    val tokenCheckStatuses: List<TokenCheckStatus>? = null,
    val highlightLogCheck: Boolean? = null,
)

data class TokenLogFilter(
    val key: String,
    val filters: List<String>
)

data class PresetElement (
    val name: String? = null,
    val value: String? = null,
    val tableBar: Boolean? = null,
)

data class ToolSettings (
    val toolPanel: Boolean? = null,
    val toolSelection: Boolean? = null,
    val toolSelectionRangePrevious: Int? = null,
    val toolSelectionRangeNext: Int? = null,
    val toolTestEnable: Boolean? = null,
    val toolTest: Boolean? = null,
)

data class LogFormat (
    val name: String? = null,
    val separator: String? = null,
    val tokenCount: Int? = null,
    val logPosition: Int? = null,
    val columnNames: String? = null,
    @JsonAdapter(ListCompactAdapter::class)
    val level: List<String>? = null,
    val levelPosition: Int? = null,
    @JsonAdapter(InnerListCompactAdapter::class)
    val tokenFilters: List<List<Any>>? = null,
    val pidTokIdx: Int? = null,
    val sampleText: String? = null,
)

data class TestTrigger (
    val name: String? = null,
    val filter: String? = null,
    val action: Int? = null,
    val actionParameter: String? = null,
    val once: Boolean? = null,
)

data class AppData(
    val version: String = "",
    val appearance: AppearanceSettings = AppearanceSettings(),
    val color: ColorSettings = ColorSettings(),
    val logCmd: LogCmdSettings = LogCmdSettings(),
    val filterOption: FilterOptionSettings = FilterOptionSettings(),
    val filter: FilterSettings = FilterSettings(),
    val filterSnippet: List<PresetElement>? = null,
    val cmdAlias: List<PresetElement>? = null,
    val targetPackage: List<String>? = null,
    val tool: ToolSettings = ToolSettings(),
    val logFormats: List<LogFormat>? = null,
    val testTriggers: List<TestTrigger>? = null,
)

data class RecentFilters (
    val showLogFilters: List<String>? = null,
    val tokenLogFilters: List<TokenLogFilter>? = null,
    val highlightLogs: List<String>? = null,
    val findLogs: List<String>? = null,
)

data class RecentFileTokenFilter(
    val key: String,
    val filter: String
)

data class RecentFileItem (
    val path: String? = null,
    val showLog: String? = null,
    val tokenFilter: List<RecentFileTokenFilter>? = null,
    val highlightLog: String? = null,
    val findLog: String? = null,
    val bookmarks: String? = null,

    val showLogCheck: Boolean? = null,
    val tokenCheck: List<Boolean>? = null,
    val highlightLogCheck: Boolean? = null,
    val findMatchCase: Boolean? = null,
)

data class RecentFiles (
    val fileItems: List<RecentFileItem>? = null,
)

data class AppHistory(
    val version: String = "",
    val recentFilter: RecentFilters = RecentFilters(),
    val recentFiles: RecentFiles = RecentFiles(),
)

object AppConstants {
    const val COUNT_SHOW_LOG = 20
    const val COUNT_TOKEN_FILTER = 10
    const val COUNT_SAVE_FILTER = 4
    const val COUNT_HIGHLIGHT_LOG = 10
    const val COUNT_FIND_LOG = 10
}

class AppDataManager private constructor() {
    companion object {
        private const val CONFIG_FILE = "lognote.json"
        private const val HISTORY_FILE = "lognote-history.json"
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

    var mAppHistory = AppHistory()
        private set

    private var mConfigPath = CONFIG_FILE
    private var mHistoryPath = HISTORY_FILE

    init {
        mConfigPath = getHomePath(CONFIG_FILE)
        mHistoryPath = getHomePath(HISTORY_FILE)
        Utils.printlnLog("Config Path : $mConfigPath, History Path : $mHistoryPath")
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

    fun loadHistory() {
        val file = File(mHistoryPath)
        if (!file.exists()) {
            mAppHistory = AppHistory()
        }
        else {
            val jsonString = file.readText()
            mAppHistory = Gson().fromJson(jsonString, AppHistory::class.java)
        }
    }

    fun saveHistory(appHistory: AppHistory) {
        val gson = GsonBuilder().setPrettyPrinting().create()

        mAppHistory = appHistory
        val jsonString = gson.toJson(mAppHistory)
        File(mHistoryPath).writeText(jsonString)
    }

    fun saveFont(family: String, size: Int) {
        loadConfig()

        mAppData = mAppData.copy(appearance = mAppData.appearance.copy(fontName = family, fontSize = size))

        saveConfig(mAppData)
    }

    fun saveLogViewColors(fullColors: Array<ColorManager.ColorItem>, filterColors: Array<ColorManager.ColorItem>) {
        loadConfig()

        val fullList: List<List<Any>> = fullColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }
        val filterList: List<List<Any>> = filterColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }

        mAppData = mAppData.copy(color = mAppData.color.copy(colorFullView = fullList, colorFilterView = filterList))

        saveConfig(mAppData)
    }

    fun saveFilterStyle(logStyle: Int, boldStyle: Int, tokenStyles: List<Int>, filterStyle: Array<ColorManager.ColorItem>) {
        loadConfig()

        val colorFilterStyleList: List<List<Any>> = filterStyle.map { listOf(it.mName, it.mStrColor, it.mOrder) }

        mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(showLogStyle = logStyle, boldLogStyle = boldStyle, tokenComboStyles = tokenStyles),
            color = mAppData.color.copy(colorFilterStyle = colorFilterStyleList))

        saveConfig(mAppData)
    }

    fun loadFilters() : ArrayList<PresetElement> {
        return (if (mAppData.filterSnippet == null) {
            ArrayList<PresetElement>()
        } else {
            mAppData.filterSnippet
        }) as ArrayList<PresetElement>
    }

    fun saveFilters(filters : ArrayList<PresetElement>) {
        loadConfig()

        mAppData = mAppData.copy(filterSnippet = filters.take(FiltersManager.MAX_FILTERS))

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
        return (if (mAppData.targetPackage == null) {
            ArrayList<String>()
        } else {
            mAppData.targetPackage
        }) as ArrayList<String>
    }

    fun savePackages(packagess : ArrayList<String>) {
        loadConfig()

        mAppData = mAppData.copy(targetPackage = packagess.take(PackageManager.MAX_PACKAGE_COUNT))

        saveConfig(mAppData)
        return
    }

    private fun manageVersion() {
        loadConfig()
        loadHistory()

        if (mAppData.version.isEmpty()) {
            updateAppDataFromV0ToV1()
            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
        }

//        if (mAppData.version == "1") {
//            updateAppDataFromVToV2()
//            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
//        }

        saveConfig(mAppData)
        saveHistory(mAppHistory)
    }

    private fun getFromProperties(src: PropertiesBase, key: String): String? {
        val prop = src.getItem(key)
        return prop
    }

    private fun getIntFromProperties(src: PropertiesBase, key: String): Int? {
        val prop = src.getItem(key)
        return if (!prop.isNullOrEmpty()) {
            prop.toInt()
        } else {
            null
        }
    }

    private fun getBooleanFromProperties(src: PropertiesBase, key: String): Boolean? {
        val prop = src.getItem(key)
        return if (!prop.isNullOrEmpty()) {
            prop.toBoolean()
        } else {
            null
        }
    }

    private fun updateAppDataFromV0ToV1() {
        Utils.printlnLog("updateAppDataFromV0ToV1 : copy from config.xml ++")

        class PropertiesReader(fileName: String) : PropertiesBase(fileName) {
            init {
                mXmlPath = fileName
                Utils.printlnLog("Xml File Path : $mXmlPath")
                loadXml()
            }
            
            override fun manageVersion() {
                // do nothing
            }
        }
        
        val oldConfigPath = getHomePath("lognote.xml")
        Utils.printlnLog("Config Path : $oldConfigPath")
        val configFile = File(oldConfigPath)
        val configReader = if (configFile.exists()) PropertiesReader(oldConfigPath) else PropertiesReader("")

        val oldFormatPath = getHomePath("lognote_formats.xml")
        Utils.printlnLog("Format Path : $oldFormatPath")
        val formatFile = File(oldFormatPath)
        val formatReader = if (formatFile.exists()) PropertiesReader(oldFormatPath) else PropertiesReader("")

        val oldRecentFilePath = getHomePath("lognote_recents.xml")
        Utils.printlnLog("RecentFile Path : $oldRecentFilePath")
        val recentFileFile = File(oldRecentFilePath)
        val recentFileReader = if (recentFileFile.exists()) PropertiesReader(oldRecentFilePath) else PropertiesReader("")

        val oldAgingTestPath = getHomePath("lognote_agingtests.xml")
        Utils.printlnLog("AgingTest Path : $oldAgingTestPath")
        val agingTestFile = File(oldAgingTestPath)
        val agingTestReader = if (agingTestFile.exists()) PropertiesReader(oldAgingTestPath) else PropertiesReader("")
        
        if (formatFile.exists()) {
            val logFormats = mutableListOf<LogFormat>()
            for (idx in 0 until FormatManager.MAX_FORMAT_COUNT) {
                val name = getFromProperties(formatReader, "$idx$ITEM_NAME") ?: ""
                if (name.trim().isEmpty()) {
                    break
                }
                val separator = getFromProperties(formatReader, "$idx$ITEM_SEPARATOR") ?: ""
                val tokenCount = try {
                    (getFromProperties(formatReader, "$idx$ITEM_TOKEN_COUNT") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    1
                }
                val logPosition = try {
                    (getFromProperties(formatReader, "$idx$ITEM_LOG_POSITION") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    0
                }
                val columnNames = getFromProperties(formatReader, "$idx$ITEM_COLUMN_NAMES") ?: ""
                val level = mutableListOf<String>()
                for (lvlIdx in TEXT_LEVEL.indices) {
                    level.add(lvlIdx, (getFromProperties(formatReader, "$idx$ITEM_LEVEL$lvlIdx") ?: "").trim())
                }

                val levelPosition = try {
                    (getFromProperties(formatReader, "$idx$ITEM_LEVEL_POSITION") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    -1
                }

                var tokenFilters: List<MutableList<Any>> = List(MAX_TOKEN_FILTER_COUNT) { mutableListOf("", 0, false, 120) }
                for (tokIdx in 0 until MAX_TOKEN_FILTER_COUNT) {
                    tokenFilters[tokIdx][0] = (getFromProperties(formatReader, "$idx$ITEM_TOKEN_FILTER_NAME$tokIdx") ?: "").trim()
                    tokenFilters[tokIdx][1] = try {
                        (getFromProperties(formatReader, "$idx$ITEM_TOKEN_FILTER_POSITION$tokIdx") ?: "").toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                    val check = getFromProperties(formatReader, "$idx$ITEM_TOKEN_SAVE_FILTER$tokIdx") ?: ""
                    tokenFilters[tokIdx][2] = if (check.isNotEmpty()) {
                        check.toBoolean()
                    } else {
                        false
                    }
                    tokenFilters[tokIdx][3] = try {
                        (getFromProperties(formatReader, "$idx$ITEM_TOKEN_UI_WIDTH$tokIdx") ?: "").toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                }

                val pidTokIdx = try {
                    (getFromProperties(formatReader, "$idx$ITEM_PID_TOK_IDX") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    -1
                }

                val sampleText = getFromProperties(formatReader, "$idx$ITEM_SAMPLE_TEXT") ?: ""
                
                logFormats.add(LogFormat(name, separator, tokenCount, logPosition, columnNames, level, levelPosition, tokenFilters, pidTokIdx, sampleText))
            }

            mAppData = mAppData.copy(logFormats = logFormats)
        }

        if (configFile.exists()) {
            val frameX = getIntFromProperties(configReader, ConfigManager.ITEM_FRAME_X)
            val frameY = getIntFromProperties(configReader, ConfigManager.ITEM_FRAME_Y)
            val frameWidth = getIntFromProperties(configReader, ConfigManager.ITEM_FRAME_WIDTH)
            val frameHeight = getIntFromProperties(configReader, ConfigManager.ITEM_FRAME_HEIGHT)
            val frameExtendedState = getIntFromProperties(configReader, ConfigManager.ITEM_FRAME_EXTENDED_STATE)
            val rotation = getIntFromProperties(configReader, ConfigManager.ITEM_ROTATION)
            val lastDividerLocation = getIntFromProperties(configReader, ConfigManager.ITEM_LAST_DIVIDER_LOCATION)
            val dividerLocation = getIntFromProperties(configReader, ConfigManager.ITEM_DIVIDER_LOCATION)

            val toolRotation = getIntFromProperties(configReader, ConfigManager.ITEM_TOOL_ROTATION)
            val toolLastDividerLocation = getIntFromProperties(configReader, ConfigManager.ITEM_TOOL_LAST_DIVIDER_LOCATION)
            val toolDividerLocation = getIntFromProperties(configReader, ConfigManager.ITEM_TOOL_DIVIDER_LOCATION)

            val language = getFromProperties(configReader, ConfigManager.ITEM_LANG)

            val logFormat = getFromProperties(configReader, ConfigManager.ITEM_LOG_FORMAT)
            val logLevel = getFromProperties(configReader, ConfigManager.ITEM_LOG_LEVEL)

            val lookAndFeel = getFromProperties(configReader, ConfigManager.ITEM_LOOK_AND_FEEL)
            val lafAccentColor = getFromProperties(configReader, ConfigManager.ITEM_LAF_ACCENT_COLOR)
            val uiFontSize = getIntFromProperties(configReader, ConfigManager.ITEM_UI_FONT_SIZE)
            val appearanceDividerSize = getIntFromProperties(configReader, ConfigManager.ITEM_APPEARANCE_DIVIDER_SIZE)
            val logViewWidth = getIntFromProperties(configReader, ConfigManager.ITEM_LOG_VIEW_WIDTH)

            val fontName = getFromProperties(configReader, ConfigManager.ITEM_FONT_NAME)
            val fontSize = getIntFromProperties(configReader, ConfigManager.ITEM_FONT_SIZE)
            val viewFull = getBooleanFromProperties(configReader, ConfigManager.ITEM_VIEW_FULL)
            val viewColumnMode = getBooleanFromProperties(configReader, ConfigManager.ITEM_VIEW_COLUMN_MODE)
            val viewProcessName = getFromProperties(configReader, ConfigManager.ITEM_VIEW_PROCESS_NAME)

            val scrollback = getIntFromProperties(configReader, ConfigManager.ITEM_SCROLLBACK)
            val scrollbackSplitFile = getBooleanFromProperties(configReader, ConfigManager.ITEM_SCROLLBACK_SPLIT_FILE)

            val iconText = getFromProperties(configReader, ConfigManager.ITEM_ICON_TEXT)
            val cmdToolbar = getBooleanFromProperties(configReader, ConfigManager.ITEM_CMD_TOOLBAR)

            mAppData = mAppData.copy(appearance = mAppData.appearance.copy(frameX = frameX, frameY = frameY,
                frameWidth = frameWidth, frameHeight = frameHeight, frameExtendedState = frameExtendedState,
                rotation = rotation, lastDividerLocation = lastDividerLocation, dividerLocation = dividerLocation,
                toolRotation = toolRotation, toolLastDividerLocation = toolLastDividerLocation, toolDividerLocation = toolDividerLocation,
                language = language, logFormat = logFormat, logLevel = logLevel, lookAndFeel = lookAndFeel, lafAccentColor = lafAccentColor,
                uiFontSize = uiFontSize, appearanceDividerSize = appearanceDividerSize, logViewWidth = logViewWidth,
                fontName = fontName, fontSize = fontSize, viewFull = viewFull, viewColumnMode = viewColumnMode, viewProcessName = viewProcessName,
                scrollback = scrollback, scrollbackSplitFile = scrollbackSplitFile, iconText = iconText, cmdToolbar = cmdToolbar))

            val colorManager = ColorManager.getInstance()
            for (idx in colorManager.mFullTableColor.mColorArray.indices) {
                getFromProperties(configReader, "${ConfigManager.ITEM_COLOR_MANAGER}${TableColorType.FULL_LOG_TABLE}_$idx")?.let {
                    colorManager.mFullTableColor.mColorArray[idx].mStrColor = it
                }
            }
            val colorFullView: List<List<Any>> = colorManager.mFullTableColor.mColorArray.map { listOf(it.mName, it.mStrColor, it.mOrder) }
            for (idx in colorManager.mFilterTableColor.mColorArray.indices) {
                getFromProperties(configReader, "${ConfigManager.ITEM_COLOR_MANAGER}${TableColorType.FILTER_LOG_TABLE}_$idx")?.let {
                    colorManager.mFilterTableColor.mColorArray[idx].mStrColor = it
                }
            }
            val colorFilterView: List<List<Any>> = colorManager.mFilterTableColor.mColorArray.map { listOf(it.mName, it.mStrColor, it.mOrder) }
            for (idx in colorManager.mFilterStyle.indices) {
                getFromProperties(configReader, ConfigManager.ITEM_COLOR_FILTER_STYLE + idx)?.let {
                    colorManager.mFilterStyle[idx].mStrColor = it
                }
            }
            val colorFilterStyle: List<List<Any>> = colorManager.mFilterStyle.map { listOf(it.mName, it.mStrColor, it.mOrder) }

            mAppData = mAppData.copy(color = mAppData.color.copy(colorFullView = colorFullView, colorFilterView = colorFilterView, colorFilterStyle = colorFilterStyle))

            val targetDevice = getFromProperties(configReader, ConfigManager.ITEM_ADB_DEVICE)
            val adbPath = getFromProperties(configReader, ConfigManager.ITEM_ADB_CMD)
            val logCmd = getFromProperties(configReader, ConfigManager.ITEM_ADB_LOG_CMD)
            val logSavePath = getFromProperties(configReader, ConfigManager.ITEM_ADB_LOG_SAVE_PATH)
            val savePrefix = getFromProperties(configReader, ConfigManager.ITEM_ADB_PREFIX)
            val adbOptionUpdatePidTimeout = getIntFromProperties(configReader, ConfigManager.ITEM_ADB_OPTION_1)
            val retryLogCmd = getBooleanFromProperties(configReader, ConfigManager.ITEM_RETRY_ADB)

            mAppData = mAppData.copy(logCmd = mAppData.logCmd.copy(targetDevice = targetDevice, adbPath = adbPath, logCmd = logCmd,
                logSavePath = logSavePath, savePrefix = savePrefix, adbOptionUpdatePidTimeout = adbOptionUpdatePidTimeout, retryLogCmd = retryLogCmd))

            val filterIncremental = getBooleanFromProperties(configReader, ConfigManager.ITEM_FILTER_INCREMENTAL)
            val matchCase = getBooleanFromProperties(configReader, ConfigManager.ITEM_MATCH_CASE)
            val filterByRecentFile = getBooleanFromProperties(configReader, ConfigManager.ITEM_FILTER_BY_FILE)
            val colorTagRegex = getBooleanFromProperties(configReader, ConfigManager.ITEM_COLOR_TAG_REGEX)
            val showLogStyle = getIntFromProperties(configReader, ConfigManager.ITEM_SHOW_LOG_STYLE)
            val boldLogStyle = getIntFromProperties(configReader, ConfigManager.ITEM_BOLD_LOG_STYLE)
            val tokenComboStyles: MutableList<Int> = List(FormatManager.MAX_TOKEN_FILTER_COUNT) { FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT.value }.toMutableList()
            for (idx in 0 until FormatManager.MAX_TOKEN_FILTER_COUNT) {
                getIntFromProperties(configReader, ConfigManager.ITEM_TOKEN_COMBO_STYLE + idx)?.let { tokenComboStyles[idx] = it }
            }

            mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(filterIncremental = filterIncremental, matchCase = matchCase,
                filterByRecentFile = filterByRecentFile, colorTagRegex = colorTagRegex, showLogStyle = showLogStyle, boldLogStyle = boldLogStyle,
                tokenComboStyles = tokenComboStyles.toList()
            ))

            var filter: String?
            val showLogFilters = mutableListOf<String>()
            for (idx in 0 until ConfigManager.COUNT_SHOW_LOG) {
                filter = getFromProperties(configReader, ConfigManager.ITEM_SHOW_LOG + idx)
                if (filter == null) {
                    break
                }
                showLogFilters.add(filter)
            }

            val tokenLogFilters = mutableListOf<TokenLogFilter>()
            val tokenCheckStatuses = mutableListOf<TokenCheckStatus>()
            for (idx in 0 until (mAppData.logFormats?.size ?: 0)) {
                val formatName = mAppData.logFormats!![idx].name
                for (tokIdx in 0 until MAX_TOKEN_FILTER_COUNT) {
                    val tokenFilters = mAppData.logFormats!![idx].tokenFilters!!
                    val isSaveFilter = tokenFilters[tokIdx][2] as Boolean
                    val tokenName = tokenFilters[tokIdx][0] as String
                    if (tokenName.isEmpty()) {
                        continue
                    }
                    val key = "${formatName}_${tokenName}"
                    if (isSaveFilter) {
                        val filters = mutableListOf<String>()
                        for (i in 0 until ConfigManager.COUNT_TOKEN_FILTER) {
                            val item = getFromProperties(configReader, "${ConfigManager.ITEM_TOKEN_FILTER}${key}_$i")
                            if (item == null) {
                                break
                            }
                            filters.add(item)
                        }
                        tokenLogFilters.add(TokenLogFilter(key, filters))
                    }

                    val checkStatus = getBooleanFromProperties(configReader, "${ConfigManager.ITEM_TOKEN_CHECK}${key}")
                    checkStatus?.let {
                        tokenCheckStatuses.add(TokenCheckStatus(key, it))
                    }
                }
            }

            val highlightLogs = mutableListOf<String>()
            for (idx in 0 until ConfigManager.COUNT_HIGHLIGHT_LOG) {
                filter = getFromProperties(configReader, ConfigManager.ITEM_HIGHLIGHT_LOG + idx)
                if (filter == null) {
                    break
                }
                highlightLogs.add(filter)
            }

            val findLogs = mutableListOf<String>()
            for (idx in 0 until ConfigManager.COUNT_FIND_LOG) {
                filter = getFromProperties(configReader, ConfigManager.ITEM_FIND_LOG + idx)
                if (filter == null) {
                    break
                }
                findLogs.add(filter)
            }
            mAppHistory = mAppHistory.copy(recentFilter = mAppHistory.recentFilter.copy(showLogFilters = showLogFilters, tokenLogFilters = tokenLogFilters,
                    highlightLogs = highlightLogs, findLogs = findLogs))

            val findMatchCase = getBooleanFromProperties(configReader, ConfigManager.ITEM_FIND_MATCH_CASE)
            val showLogCheck = getBooleanFromProperties(configReader, ConfigManager.ITEM_SHOW_LOG_CHECK)
            val highlightLogCheck = getBooleanFromProperties(configReader, ConfigManager.ITEM_HIGHLIGHT_LOG_CHECK)

            mAppData = mAppData.copy(filter = mAppData.filter.copy(findMatchCase = findMatchCase, showLogCheck = showLogCheck,
                tokenCheckStatuses = tokenCheckStatuses, highlightLogCheck = highlightLogCheck))

            val filterSnippet = mutableListOf<PresetElement>()
            for (i in 0 until FiltersManager.MAX_FILTERS) {
                val name = getFromProperties(configReader, ITEM_FILTERS_TITLE + i)
                if (name.isNullOrEmpty()) {
                    break
                }
                val value = getFromProperties(configReader, ITEM_FILTERS_FILTER + i) ?: "null"
                val tableBar = getBooleanFromProperties(configReader, ITEM_FILTERS_TABLEBAR + i) ?: false
                filterSnippet.add(PresetElement(name, value, tableBar))
            }

            val cmdAlias = mutableListOf<PresetElement>()
            for (i in 0 until CmdManager.MAX_CMD_COUNT) {
                val name = getFromProperties(configReader, ITEM_CMDS_TITLE + i)
                if (name.isNullOrEmpty()) {
                    break
                }
                val value = getFromProperties(configReader, ITEM_CMDS_CMD + i) ?: "null"
                val tableBar = getBooleanFromProperties(configReader, ITEM_CMDS_TABLEBAR + i) ?: false
                cmdAlias.add(PresetElement(name, value, tableBar))
            }

            val targetPackage = mutableListOf<String>()
            for (i in 0 until PackageManager.MAX_PACKAGE_COUNT) {
                val packageName = getFromProperties(configReader, ITEM_PACKAGES_ITEM + i)
                if (packageName.isNullOrEmpty()) {
                    break
                }
                targetPackage.add(packageName)
            }

            mAppData = mAppData.copy(filterSnippet = filterSnippet, cmdAlias = cmdAlias, targetPackage = targetPackage)

            val toolPanel = getBooleanFromProperties(configReader, ConfigManager.ITEM_TOOL_PANEL)
            val toolSelection = getBooleanFromProperties(configReader, ConfigManager.ITEM_TOOL_SELECTION)
            val toolSelectionRangePrevious = getIntFromProperties(configReader, ConfigManager.ITEM_TOOL_SELECTION_RANGE_PREVIOUS)
            val toolSelectionRangeNext = getIntFromProperties(configReader, ConfigManager.ITEM_TOOL_SELECTION_RANGE_NEXT)
            val toolTestEnable = getBooleanFromProperties(configReader, ConfigManager.ITEM_TOOL_TEST_ENABLE)
            val toolTest = getBooleanFromProperties(configReader, ConfigManager.ITEM_TOOL_TEST)

            mAppData = mAppData.copy(tool = mAppData.tool.copy(toolPanel = toolPanel, toolSelection = toolSelection, toolSelectionRangePrevious = toolSelectionRangePrevious,
                toolSelectionRangeNext = toolSelectionRangeNext, toolTestEnable = toolTestEnable, toolTest = toolTest))

            val testTriggers = mutableListOf<TestTrigger>()
            for (i in 0 until MAX_TRIGGER_COUNT) {
                val name = getFromProperties(agingTestReader, "$i$ITEM_TRIGGER_NAME")
                if (name.isNullOrEmpty()) {
                    break
                }
                val filter = getFromProperties(agingTestReader, "$i$ITEM_TRIGGER_FILTER")
                val action = getIntFromProperties(agingTestReader, "$i$ITEM_TRIGGER_ACTION")
                val actionParameter = getFromProperties(agingTestReader, "$i$ITEM_TRIGGER_ACTION_PARAMETER")
                val once = getBooleanFromProperties(agingTestReader, "$i$ITEM_TRIGGER_ONCE")

                testTriggers.add(TestTrigger(name, filter, action, actionParameter, once))
            }

            mAppData = mAppData.copy(testTriggers = testTriggers)

            val fileItems = mutableListOf<RecentFileItem>()
            for (i in 0 until MAX_RECENT_FILE) {
                val  path = getFromProperties(recentFileReader, "$i$ITEM_PATH")
                if (path.isNullOrEmpty()) {
                    break
                }
                val showLog = getFromProperties(recentFileReader, "$i$ITEM_SHOW_LOG")

                val tokenFilter = mutableListOf<RecentFileTokenFilter>()
                val highlightLog = getFromProperties(recentFileReader, "$i$ITEM_HIGHLIGHT_LOG")
                val findLog = getFromProperties(recentFileReader, "$i$ITEM_FIND_LOG")
                val bookmarks = getFromProperties(recentFileReader, "$i$ITEM_BOOKMARKS")

                val showLogCheck = getBooleanFromProperties(recentFileReader, "$i$ITEM_SHOW_LOG_CHECK")
                val tokenCheck = mutableListOf<Boolean>()

                val highlightLogCheck = getBooleanFromProperties(recentFileReader, "$i$ITEM_HIGHLIGHT_LOG_CHECK")
                val findMatchCase = getBooleanFromProperties(recentFileReader, "$i$ITEM_FIND_MATCH_CASE")

                fileItems.add(RecentFileItem(path, showLog, tokenFilter, highlightLog, findLog, bookmarks, showLogCheck, tokenCheck, highlightLogCheck, findMatchCase))
            }

            mAppHistory = mAppHistory.copy(recentFiles = mAppHistory.recentFiles.copy(fileItems = fileItems))
        }

//        mAppData = mAppData.copy(version = "1")
        Utils.printlnLog("updateAppDataFromV0ToV1 : --")
    }
}

