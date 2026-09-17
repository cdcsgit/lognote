package com.blogspot.cdcsutils.lognote

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
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

    val lookAndFeel: String? = null,
    val lafAccentColor: String? = null,
    val uiFontSize: Int? = null,
    val dividerSize: Int? = null,
    val logViewWidth: Int? = null,

    val fontName: String? = null,
    val fontSize: Int? = null,
    val viewFull: Boolean? = null,
    val viewColumnMode: Boolean? = null,
    val viewProcessName: Int? = null,

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
    val lastLogCmd: String? = null,
    val logCmds: List<String>? = null,
    val logSavePath: String? = null,
    val logFilePrefix: String? = null,
    val adbOptionUpdatePidTimeout: Int? = null,
    val retryLogCmd: Boolean? = null,
    val logFormat: String? = null,
    val logLevel: Int? = null,
)

data class FilterOptionSettings (
    val filterIncremental: Boolean? = null,
    val matchCase: Boolean? = null,
    val filterByRecentFile: Boolean? = null,
    val colorTagRegex: Boolean? = null,
    val showLogStyle: Int? = null,
    val boldLogStyle: Int? = null,
    val tokenComboStyles: List<Int>? = null,
    val findMatchCase: Boolean? = null,
)

data class FilterSettings (
    val showLogCheck: Boolean? = null,
    val tokenCheckStatusMap: Map<String, Boolean>? = null,
    val boldLogCheck: Boolean? = null,
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
    val name: String,
    val separator: String,
    val tokenCount: Int,
    val logPosition: Int,
    val columnNames: String,
    @JsonAdapter(ListCompactAdapter::class)
    val level: List<String>,
    val levelPosition: Int,
    @JsonAdapter(InnerListCompactAdapter::class)
    val tokenFilters: List<List<Any>>,
    val pidTokIdx: Int,
    val sampleText: String,
)

data class TestTrigger (
    val name: String,
    val filter: String,
    val action: Int,
    val actionParameter: String,
    val once: Boolean,
)

data class AppData(
    val version: String = "",
    val appearance: AppearanceSettings = AppearanceSettings(),
    val color: ColorSettings = ColorSettings(),
    val logCmd: LogCmdSettings = LogCmdSettings(),
    val filterOption: FilterOptionSettings = FilterOptionSettings(),
    val filter: FilterSettings = FilterSettings(),
    val filterSnippet: List<PresetManager.PresetElement>? = null,
    val cmdAlias: List<PresetManager.PresetElement>? = null,
    val targetPackage: List<String>? = null,
    val tool: ToolSettings = ToolSettings(),
    val logFormats: List<LogFormat>? = null,
    val testTriggers: List<TestTrigger>? = null,
)

data class RecentFilters (
    val showLogFilters: List<String>? = null,
    val tokenLogFilterMap: Map<String, List<String>>? = null,
    val boldLogs: List<String>? = null,
    val findLogs: List<String>? = null,
)

data class RecentFileItem (
    val path: String,
    val showLog: String,
    val tokenFilterMap: Map<String, String>,
    val boldLog: String,
    val findLog: String,
    val bookmarks: String,

    val showLogCheck: Boolean,
    val tokenCheckMap: Map<String, Boolean>,
    val boldLogCheck: Boolean,
    val findMatchCase: Boolean,
)

data class RecentFiles (
    val fileItems: List<RecentFileItem>? = null,
)

data class AppHistory(
    val version: String = "",
    val recentFilters: RecentFilters = RecentFilters(),
    val recentFiles: RecentFiles = RecentFiles(),
)

object AppConstants {
    const val MAX_SHOW_LOG = 20
    const val MAX_TOKEN_FILTER = 10
    const val MAX_SAVE_FILTER = 4
    const val MAX_BOLD_LOG = 10
    const val MAX_FIND_LOG = 10
    const val MAX_LOG_CMD = 10
    const val MAX_TRIGGER_COUNT = 30
    const val MAX_RECENT_FILE = 30
    const val MAX_FORMAT_COUNT = 50
    const val MAX_TOKEN_COUNT = 3
}

enum class SaveFileType(val value: Int) {
    APP_DATA(0),
    APP_HISTORY(1);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

class AppDataManager private constructor() {
    companion object {
        private const val APP_DATA_FILE = "lognote.json"
        private const val APP_HISTORY_FILE = "lognote-history.json"
        val LOGNOTE_HOME: String = System.getenv("LOGNOTE_HOME") ?: ""

        const val VALUE_ICON_TEXT_I_T = "IconText"
        const val VALUE_ICON_TEXT_I = "Icon"
        const val VALUE_ICON_TEXT_T = "Text"

        var LaF = ""
        var LaFAccentColor = ""

        private val mInstance: AppDataManager = AppDataManager()
        fun getInstance(caller: String): AppDataManager {
            Utils.printlnLog("TEST TEST AppDataManager.getInstance(), caller: $caller")
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

    val mGson = GsonBuilder().setPrettyPrinting().create()
    val mAppDataFile: File
    val mAppHistoryFile: File
    var mAppData = AppData()
        private set

    var mAppHistory = AppHistory()
        private set

    private var mAppDataPath = APP_DATA_FILE
    private var mAppHistoryPath = APP_HISTORY_FILE

    init {
        mAppDataPath = getHomePath(APP_DATA_FILE)
        mAppDataFile = File(mAppDataPath)
        mAppHistoryPath = getHomePath(APP_HISTORY_FILE)
        mAppHistoryFile = File(mAppHistoryPath)
        Utils.printlnLog("Config Path : $mAppDataPath, History Path : $mAppHistoryPath")
        manageVersion()
    }

    fun loadJson(saveFileType: SaveFileType) {
        val file = when (saveFileType) {
            SaveFileType.APP_DATA -> {
                mAppDataFile
            }
            SaveFileType.APP_HISTORY -> {
                mAppHistoryFile
            }
        }

        if (!file.exists()) {
            save(saveFileType)
            return
        }
        try {
            RandomAccessFile(file, "r").use { raf ->
                val bytes = ByteArray(raf.length().toInt())
                raf.readFully(bytes)
                val json = String(bytes, StandardCharsets.UTF_8)
                if (saveFileType == SaveFileType.APP_DATA) {
                    mAppData = mGson.fromJson(json, AppData::class.java) ?: AppData()
                }
                else if (saveFileType == SaveFileType.APP_HISTORY) {
                    mAppHistory = mGson.fromJson(json, AppHistory::class.java) ?: AppHistory()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun updateAppData(transform: (AppData) -> AppData) {
        mAppData = transform(mAppData)
    }

    @Synchronized
    fun updateAppHistory(transform: (AppHistory) -> AppHistory) {
        mAppHistory = transform(mAppHistory)
    }

    @Synchronized
    fun updateAndSaveAppData(transform: (AppData) -> AppData) {
        val file = mAppDataFile

        try {
            RandomAccessFile(file, "rw").use { raf ->
                raf.channel.use { channel ->
                    channel.lock().use {
                        val latestData = if (raf.length() > 0) {
                            val bytes = ByteArray(raf.length().toInt())
                            raf.readFully(bytes)
                            val json = String(bytes, StandardCharsets.UTF_8)
                            mGson.fromJson(json, AppData::class.java) ?: AppData()
                        } else {
                            AppData()
                        }

                        val updatedData = transform(latestData)

                        raf.setLength(0)
                        raf.seek(0)
                        val updatedJson = mGson.toJson(updatedData)
                        raf.write(updatedJson.toByteArray(StandardCharsets.UTF_8))

                        mAppData = updatedData
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun updateAndSaveAppHistory(transform: (AppHistory) -> AppHistory) {
        val file = mAppHistoryFile

        try {
            RandomAccessFile(file, "rw").use { raf ->
                raf.channel.use { channel ->
                    channel.lock().use {
                        val latestData = if (raf.length() > 0) {
                            val bytes = ByteArray(raf.length().toInt())
                            raf.readFully(bytes)
                            val json = String(bytes, StandardCharsets.UTF_8)
                            mGson.fromJson(json, AppHistory::class.java) ?: AppHistory()
                        } else {
                            AppHistory()
                        }

                        val updatedData = transform(latestData)

                        raf.setLength(0)
                        raf.seek(0)
                        val updatedJson = mGson.toJson(updatedData)
                        raf.write(updatedJson.toByteArray(StandardCharsets.UTF_8))

                        mAppHistory = updatedData
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save(saveFileType: SaveFileType) {
        if (saveFileType == SaveFileType.APP_DATA) {
            updateAndSaveAppData { mAppData }
        }
        else if (saveFileType == SaveFileType.APP_HISTORY) {
            updateAndSaveAppHistory { mAppHistory }
        }
    }

    fun loadAppData() {
        loadJson(SaveFileType.APP_DATA)
    }

    fun saveAppData() {
        save(SaveFileType.APP_DATA)
    }

    fun loadAppHistory() {
        loadJson(SaveFileType.APP_HISTORY)
    }

    fun saveAppHistory() {
        save(SaveFileType.APP_HISTORY)
    }

    fun saveFont(family: String, size: Int) {
        loadAppData()

        mAppData = mAppData.copy(appearance = mAppData.appearance.copy(fontName = family, fontSize = size))

        saveAppData()
    }

    fun saveLogViewColors(fullColors: Array<ColorManager.ColorItem>, filterColors: Array<ColorManager.ColorItem>) {
        loadAppData()

        val fullList: List<List<Any>> = fullColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }
        val filterList: List<List<Any>> = filterColors.map { listOf(it.mName, it.mStrColor, it.mOrder) }

        mAppData = mAppData.copy(color = mAppData.color.copy(colorFullView = fullList, colorFilterView = filterList))

        saveAppData()
    }

    fun saveFilterStyle(logStyle: Int, boldStyle: Int, tokenStyles: List<Int>, filterStyle: Array<ColorManager.ColorItem>) {
        loadAppData()

        val colorFilterStyleList: List<List<Any>> = filterStyle.map { listOf(it.mName, it.mStrColor, it.mOrder) }

        mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(showLogStyle = logStyle, boldLogStyle = boldStyle, tokenComboStyles = tokenStyles),
            color = mAppData.color.copy(colorFilterStyle = colorFilterStyleList))

        saveAppData()
    }

    fun loadFilters() : ArrayList<PresetManager.PresetElement> {
        return (if (mAppData.filterSnippet == null) {
            ArrayList<PresetManager.PresetElement>()
        } else {
            mAppData.filterSnippet
        }) as ArrayList<PresetManager.PresetElement>
    }

    fun saveFilters(filters : ArrayList<PresetManager.PresetElement>) {
        loadAppData()

        mAppData = mAppData.copy(filterSnippet = filters.take(FiltersManager.MAX_FILTERS))

        saveAppData()
        return
    }

    fun loadCmds() : ArrayList<PresetManager.PresetElement> {
        return (if (mAppData.cmdAlias == null) {
            ArrayList<PresetManager.PresetElement>()
        } else {
            mAppData.cmdAlias
        }) as ArrayList<PresetManager.PresetElement>
    }

    fun saveCmds(cmds : ArrayList<PresetManager.PresetElement>) {
        loadAppData()

        mAppData = mAppData.copy(cmdAlias = cmds.take(CmdManager.MAX_CMD_COUNT))

        saveAppData()
        return
    }

    fun loadPackages() : ArrayList<String> {
        return (if (mAppData.targetPackage == null) {
            ArrayList<String>()
        } else {
            mAppData.targetPackage
        }) as ArrayList<String>
    }

    fun savePackages(packages : ArrayList<String>) {
        loadAppData()

        mAppData = mAppData.copy(targetPackage = packages.take(PackageManager.MAX_PACKAGE_COUNT))

        saveAppData()
        return
    }

    private fun manageVersion() {
        loadAppData()
        loadAppHistory()

        if (mAppData.version.isEmpty()) {
            updateAppDataFromV0ToV1()
            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
        }

//        if (mAppData.version == "1") {
//            updateAppDataFromVToV2()
//            Utils.printlnLog("manageVersion : ${mAppData.version} applied")
//        }

        saveAppData()
        saveAppHistory()
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
        val ITEM_FRAME_X = "FRAME_X"
        val ITEM_FRAME_Y = "FRAME_Y"
        val ITEM_FRAME_WIDTH = "FRAME_WIDTH"
        val ITEM_FRAME_HEIGHT = "FRAME_HEIGHT"
        val ITEM_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE"
        val ITEM_ROTATION = "ROTATION"
        val ITEM_DIVIDER_LOCATION = "DIVIDER_LOCATION"
        val ITEM_LAST_DIVIDER_LOCATION = "LAST_DIVIDER_LOCATION"

        val ITEM_TOOL_ROTATION = "TOOL_ROTATION"
        val ITEM_TOOL_DIVIDER_LOCATION = "TOOL_DIVIDER_LOCATION"
        val ITEM_TOOL_LAST_DIVIDER_LOCATION = "TOOL_LAST_DIVIDER_LOCATION"

        val ITEM_LANG = "LANG"

        val ITEM_SHOW_LOG = "SHOW_LOG_"
        val ITEM_TOKEN_FILTER = "TOKEN_FILTER_"

        val ITEM_HIGHLIGHT_LOG = "HIGHLIGHT_LOG_"

        val ITEM_FIND_LOG = "SEARCH_LOG_"
        val ITEM_FIND_MATCH_CASE = "SEARCH_MATCH_CASE"

        val ITEM_SHOW_LOG_CHECK = "SHOW_LOG_CHECK"
        val ITEM_TOKEN_CHECK = "TOKEN_CHECK_"

        val ITEM_HIGHLIGHT_LOG_CHECK = "HIGHLIGHT_LOG_CHECK"

        val ITEM_LOG_LEVEL = "LOG_LEVEL"
        val ITEM_LOG_FORMAT = "LOG_FORMAT"

        val ITEM_LOOK_AND_FEEL = "LOOK_AND_FEEL"
        val ITEM_LAF_ACCENT_COLOR = "LAF_ACCENT_COLOR"
        val ITEM_UI_FONT_SIZE = "UI_FONT_SIZE"
        val ITEM_APPEARANCE_DIVIDER_SIZE = "APPEARANCE_DIVIDER_SIZE"
        val ITEM_LOG_VIEW_WIDTH = "LOG_VIEW_WIDTH"

        val ITEM_ADB_DEVICE = "ADB_DEVICE"
        val ITEM_ADB_CMD = "ADB_CMD"
        val ITEM_ADB_LOG_CMD = "ADB_LOG_CMD"
        val ITEM_ADB_LOG_SAVE_PATH = "ADB_LOG_SAVE_PATH"
        val ITEM_ADB_PREFIX = "ADB_PREFIX"
        val ITEM_ADB_OPTION_1 = "ADB_OPTION_1"

        val ITEM_FONT_NAME = "FONT_NAME"
        val ITEM_FONT_SIZE = "FONT_SIZE"
        val ITEM_VIEW_FULL = "VIEW_FULL"
        val ITEM_VIEW_COLUMN_MODE = "VIEW_COLUMN_MODE"
        val ITEM_VIEW_PROCESS_NAME = "VIEW_PROCESS_NAME"
        val ITEM_FILTER_INCREMENTAL = "FILTER_INCREMENTAL"
        val ITEM_FILTER_BY_FILE = "FILTER_BY_FILE"
        val ITEM_COLOR_TAG_REGEX = "COLOR_TAG_REGEX"

        val ITEM_SCROLLBACK = "SCROLLBACK"
        val ITEM_SCROLLBACK_SPLIT_FILE = "SCROLLBACK_SPLIT_FILE"
        val ITEM_MATCH_CASE = "MATCH_CASE"

        val ITEM_FILTERS_TITLE = "FILTERS_TITLE_"
        val ITEM_FILTERS_FILTER = "FILTERS_FILTER_"
        val ITEM_FILTERS_TABLEBAR = "FILTERS_TABLEBAR_"

        val ITEM_CMDS_TITLE = "CMDS_TITLE_"
        val ITEM_CMDS_CMD = "CMDS_CMD_"
        val ITEM_CMDS_TABLEBAR = "CMDS_TABLEBAR_"

        val ITEM_PACKAGES_ITEM = "PACKAGES_ITEM_"

        val ITEM_COLOR_MANAGER = "COLOR_MANAGER_"
        val ITEM_COLOR_FILTER_STYLE = "COLOR_FILTER_STYLE_"

        val ITEM_RETRY_ADB = "RETRY_ADB"

        val ITEM_SHOW_LOG_STYLE = "SHOW_LOG_STYLE"
        val ITEM_BOLD_LOG_STYLE = "BOLD_LOG_STYLE"
        val ITEM_TOKEN_COMBO_STYLE = "TOKEN_COMBO_STYLE_"

        val ITEM_ICON_TEXT = "ICON_TEXT"
        val ITEM_CMD_TOOLBAR = "CMD_TOOLBAR"

        val ITEM_TOOL_PANEL = "TOOL_PANEL"
        val ITEM_TOOL_SELECTION = "TOOL_SELECTION"
        val ITEM_TOOL_SELECTION_RANGE_PREVIOUS = "TOOL_SELECTION_RANGE_PREVIOUS"
        val ITEM_TOOL_SELECTION_RANGE_NEXT = "TOOL_SELECTION_RANGE_NEXT"
        val ITEM_TOOL_TEST_ENABLE = "TOOL_TEST_ENABLE"
        val ITEM_TOOL_TEST = "TOOL_TEST"

        val TRIGGER_ITEM_TRIGGER_NAME = "_TRIGGER_NAME"
        val TRIGGER_ITEM_TRIGGER_FILTER = "_TRIGGER_FILTER"
        val TRIGGER_ITEM_TRIGGER_ACTION = "_TRIGGER_ACTION"
        val TRIGGER_ITEM_TRIGGER_ACTION_PARAMETER = "_TRIGGER_ACTION_PARAMETER"
        val TRIGGER_ITEM_TRIGGER_ONCE = "_TRIGGER_ONCE"

        val RECENT_ITEM_PATH = "_PATH"
        val RECENT_ITEM_SHOW_LOG = "_SHOW_LOG"
        val RECENT_ITEM_TOKEN_FILTER = "_TOKEN_FILTER_"
        val RECENT_ITEM_HIGHLIGHT_LOG = "_HIGHLIGHT_LOG"
        val RECENT_ITEM_FIND_LOG = "_SEARCH_LOG"
        val RECENT_ITEM_BOOKMARKS = "_BOOKMARKS"

        val RECENT_ITEM_SHOW_LOG_CHECK = "_SHOW_LOG_CHECK"
        val RECENT_ITEM_TOKEN_CHECK = "_TOKEN_CHECK_"
        val RECENT_ITEM_HIGHLIGHT_LOG_CHECK = "_HIGHLIGHT_LOG_CHECK"
        val RECENT_ITEM_FIND_MATCH_CASE = "_SEARCH_MATCH_CASE"

        val FORMAT_ITEM_NAME = "_NAME"
        val FORMAT_ITEM_SEPARATOR = "_SEPARATOR"
        val FORMAT_ITEM_TOKEN_COUNT = "_TOKEN_COUNT"
        val FORMAT_ITEM_LOG_POSITION = "_LOG_NTH"
        val FORMAT_ITEM_COLUMN_NAMES = "_COLUMN_NAMES"
        val FORMAT_ITEM_LEVEL = "_LEVEL_"
        val FORMAT_ITEM_LEVEL_POSITION = "_LEVEL_NTH"
        val FORMAT_ITEM_TOKEN_FILTER_NAME = "_TOKEN_NAME_"
        val FORMAT_ITEM_TOKEN_FILTER_POSITION = "_TOKEN_NTH_"
        val FORMAT_ITEM_TOKEN_SAVE_FILTER = "_TOKEN_SAVE_FILTER_"
        val FORMAT_ITEM_TOKEN_UI_WIDTH = "_TOKEN_UI_WIDTH_"
        val FORMAT_ITEM_PID_TOK_IDX = "_PID_TOK_IDX"
        val FORMAT_ITEM_SAMPLE_TEXT = "_SAMPLE_TEXT"

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
            for (idx in 0 until AppConstants.MAX_FORMAT_COUNT) {
                val name = getFromProperties(formatReader, "$idx$FORMAT_ITEM_NAME") ?: ""
                if (name.trim().isEmpty()) {
                    break
                }
                val separator = getFromProperties(formatReader, "$idx$FORMAT_ITEM_SEPARATOR") ?: ""
                val tokenCount = try {
                    (getFromProperties(formatReader, "$idx$FORMAT_ITEM_TOKEN_COUNT") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    1
                }
                val logPosition = try {
                    (getFromProperties(formatReader, "$idx$FORMAT_ITEM_LOG_POSITION") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    0
                }
                val columnNames = getFromProperties(formatReader, "$idx$FORMAT_ITEM_COLUMN_NAMES") ?: ""
                val level = mutableListOf<String>()
                for (lvlIdx in FormatConstants.TEXT_LEVEL.indices) {
                    level.add(lvlIdx, (getFromProperties(formatReader, "$idx$FORMAT_ITEM_LEVEL$lvlIdx") ?: "").trim())
                }

                val levelPosition = try {
                    (getFromProperties(formatReader, "$idx$FORMAT_ITEM_LEVEL_POSITION") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    -1
                }

                var tokenFilters: List<MutableList<Any>> = List(AppConstants.MAX_TOKEN_COUNT) { mutableListOf("", 0, false, 120) }
                for (tokIdx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                    tokenFilters[tokIdx][0] = (getFromProperties(formatReader, "$idx$FORMAT_ITEM_TOKEN_FILTER_NAME$tokIdx") ?: "").trim()
                    tokenFilters[tokIdx][1] = try {
                        (getFromProperties(formatReader, "$idx$FORMAT_ITEM_TOKEN_FILTER_POSITION$tokIdx") ?: "").toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                    val check = getFromProperties(formatReader, "$idx$FORMAT_ITEM_TOKEN_SAVE_FILTER$tokIdx") ?: ""
                    tokenFilters[tokIdx][2] = if (check.isNotEmpty()) {
                        check.toBoolean()
                    } else {
                        false
                    }
                    tokenFilters[tokIdx][3] = try {
                        (getFromProperties(formatReader, "$idx$FORMAT_ITEM_TOKEN_UI_WIDTH$tokIdx") ?: "").toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                }

                val pidTokIdx = try {
                    (getFromProperties(formatReader, "$idx$FORMAT_ITEM_PID_TOK_IDX") ?: "").toInt()
                } catch (ex: NumberFormatException) {
                    -1
                }

                val sampleText = getFromProperties(formatReader, "$idx$FORMAT_ITEM_SAMPLE_TEXT") ?: ""
                
                logFormats.add(LogFormat(name, separator, tokenCount, logPosition, columnNames, level, levelPosition, tokenFilters, pidTokIdx, sampleText))
            }

            mAppData = mAppData.copy(logFormats = logFormats)
        }

        if (configFile.exists()) {
            val frameX = getIntFromProperties(configReader, ITEM_FRAME_X)
            val frameY = getIntFromProperties(configReader, ITEM_FRAME_Y)
            val frameWidth = getIntFromProperties(configReader, ITEM_FRAME_WIDTH)
            val frameHeight = getIntFromProperties(configReader, ITEM_FRAME_HEIGHT)
            val frameExtendedState = getIntFromProperties(configReader, ITEM_FRAME_EXTENDED_STATE)
            val rotation = getIntFromProperties(configReader, ITEM_ROTATION)
            val lastDividerLocation = getIntFromProperties(configReader, ITEM_LAST_DIVIDER_LOCATION)
            val dividerLocation = getIntFromProperties(configReader, ITEM_DIVIDER_LOCATION)

            val toolRotation = getIntFromProperties(configReader, ITEM_TOOL_ROTATION)
            val toolLastDividerLocation = getIntFromProperties(configReader, ITEM_TOOL_LAST_DIVIDER_LOCATION)
            val toolDividerLocation = getIntFromProperties(configReader, ITEM_TOOL_DIVIDER_LOCATION)

            val language = getFromProperties(configReader, ITEM_LANG)

            val lookAndFeel = getFromProperties(configReader, ITEM_LOOK_AND_FEEL)
            val lafAccentColor = getFromProperties(configReader, ITEM_LAF_ACCENT_COLOR)
            val uiFontSize = getIntFromProperties(configReader, ITEM_UI_FONT_SIZE)
            val appearanceDividerSize = getIntFromProperties(configReader, ITEM_APPEARANCE_DIVIDER_SIZE)
            val logViewWidth = getIntFromProperties(configReader, ITEM_LOG_VIEW_WIDTH)

            val fontName = getFromProperties(configReader, ITEM_FONT_NAME)
            val fontSize = getIntFromProperties(configReader, ITEM_FONT_SIZE)
            val viewFull = getBooleanFromProperties(configReader, ITEM_VIEW_FULL)
            val viewColumnMode = getBooleanFromProperties(configReader, ITEM_VIEW_COLUMN_MODE)
            val viewProcessName = getIntFromProperties(configReader, ITEM_VIEW_PROCESS_NAME)

            val scrollback = getIntFromProperties(configReader, ITEM_SCROLLBACK)
            val scrollbackSplitFile = getBooleanFromProperties(configReader, ITEM_SCROLLBACK_SPLIT_FILE)

            val iconText = getFromProperties(configReader, ITEM_ICON_TEXT)
            val cmdToolbar = getBooleanFromProperties(configReader, ITEM_CMD_TOOLBAR)

            mAppData = mAppData.copy(appearance = mAppData.appearance.copy(frameX = frameX, frameY = frameY,
                frameWidth = frameWidth, frameHeight = frameHeight, frameExtendedState = frameExtendedState,
                rotation = rotation, lastDividerLocation = lastDividerLocation, dividerLocation = dividerLocation,
                toolRotation = toolRotation, toolLastDividerLocation = toolLastDividerLocation, toolDividerLocation = toolDividerLocation,
                language = language, lookAndFeel = lookAndFeel, lafAccentColor = lafAccentColor,
                uiFontSize = uiFontSize, dividerSize = appearanceDividerSize, logViewWidth = logViewWidth,
                fontName = fontName, fontSize = fontSize, viewFull = viewFull, viewColumnMode = viewColumnMode, viewProcessName = viewProcessName,
                scrollback = scrollback, scrollbackSplitFile = scrollbackSplitFile, iconText = iconText, cmdToolbar = cmdToolbar))

            val colorManager = ColorManager.getInstance()
            for (idx in colorManager.mFullTableColor.mColorArray.indices) {
                getFromProperties(configReader, "${ITEM_COLOR_MANAGER}${ColorManager.TableColorType.FULL_LOG_TABLE}_$idx")?.let {
                    colorManager.mFullTableColor.mColorArray[idx].mStrColor = it
                }
            }
            val colorFullView: List<List<Any>> = colorManager.mFullTableColor.mColorArray.map { listOf(it.mName, it.mStrColor, it.mOrder) }

            for (idx in colorManager.mFilterTableColor.mColorArray.indices) {
                getFromProperties(configReader, "${ITEM_COLOR_MANAGER}${ColorManager.TableColorType.FILTER_LOG_TABLE}_$idx")?.let {
                    colorManager.mFilterTableColor.mColorArray[idx].mStrColor = it
                }
            }
            val colorFilterView: List<List<Any>> = colorManager.mFilterTableColor.mColorArray.map { listOf(it.mName, it.mStrColor, it.mOrder) }

            for (idx in colorManager.mFilterStyle.indices) {
                getFromProperties(configReader, ITEM_COLOR_FILTER_STYLE + idx)?.let {
                    colorManager.mFilterStyle[idx].mStrColor = it
                }
            }
            val colorFilterStyle: List<List<Any>> = colorManager.mFilterStyle.map { listOf(it.mName, it.mStrColor, it.mOrder) }

            mAppData = mAppData.copy(color = mAppData.color.copy(colorFullView = colorFullView, colorFilterView = colorFilterView, colorFilterStyle = colorFilterStyle))

            val targetDevice = getFromProperties(configReader, ITEM_ADB_DEVICE)
            val adbPath = getFromProperties(configReader, ITEM_ADB_CMD)
            val lastLogCmd = getFromProperties(configReader, ITEM_ADB_LOG_CMD)
            val logCmds = mutableListOf<String>()
            for (idx in 0 until AppConstants.MAX_LOG_CMD) {
                val logCmd = if (idx == 0) {
                    LogCmdManager.DEFAULT_LOGCAT
                }
                else {
                    getFromProperties(configReader, "${ITEM_ADB_LOG_CMD}_$idx")
                }
                if (logCmd == null) {
                    break
                }
                logCmds.add(logCmd)
            }
            val logSavePath = getFromProperties(configReader, ITEM_ADB_LOG_SAVE_PATH)
            val savePrefix = getFromProperties(configReader, ITEM_ADB_PREFIX)
            val adbOptionUpdatePidTimeout = getIntFromProperties(configReader, ITEM_ADB_OPTION_1)
            val retryLogCmd = getBooleanFromProperties(configReader, ITEM_RETRY_ADB)

            val logFormat = getFromProperties(configReader, ITEM_LOG_FORMAT)
            val logLevel = getIntFromProperties(configReader, ITEM_LOG_LEVEL)

            mAppData = mAppData.copy(logCmd = mAppData.logCmd.copy(targetDevice = targetDevice, adbPath = adbPath, lastLogCmd = lastLogCmd,
                logSavePath = logSavePath, logFilePrefix = savePrefix, adbOptionUpdatePidTimeout = adbOptionUpdatePidTimeout, retryLogCmd = retryLogCmd,
                logFormat = logFormat, logLevel = logLevel))

            val filterIncremental = getBooleanFromProperties(configReader, ITEM_FILTER_INCREMENTAL)
            val matchCase = getBooleanFromProperties(configReader, ITEM_MATCH_CASE)
            val filterByRecentFile = getBooleanFromProperties(configReader, ITEM_FILTER_BY_FILE)
            val colorTagRegex = getBooleanFromProperties(configReader, ITEM_COLOR_TAG_REGEX)
            val showLogStyle = getIntFromProperties(configReader, ITEM_SHOW_LOG_STYLE)
            val boldLogStyle = getIntFromProperties(configReader, ITEM_BOLD_LOG_STYLE)
            val tokenComboStyles: MutableList<Int> = List(AppConstants.MAX_TOKEN_COUNT) { FilterComboBox.Mode.SINGLE_LINE_HIGHLIGHT.value }.toMutableList()
            for (idx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                getIntFromProperties(configReader, ITEM_TOKEN_COMBO_STYLE + idx)?.let { tokenComboStyles[idx] = it }
            }
            val findMatchCase = getBooleanFromProperties(configReader, ITEM_FIND_MATCH_CASE)

            mAppData = mAppData.copy(filterOption = mAppData.filterOption.copy(filterIncremental = filterIncremental, matchCase = matchCase,
                filterByRecentFile = filterByRecentFile, colorTagRegex = colorTagRegex, showLogStyle = showLogStyle, boldLogStyle = boldLogStyle,
                tokenComboStyles = tokenComboStyles.toList(), findMatchCase = findMatchCase
            ))

            var filter: String?
            val showLogFilters = mutableListOf<String>()
            for (idx in 0 until AppConstants.MAX_SHOW_LOG) {
                filter = getFromProperties(configReader, ITEM_SHOW_LOG + idx)
                if (filter == null) {
                    break
                }
                if (filter.isEmpty()) {
                    continue
                }
                showLogFilters.add(filter)
            }

            val tokenLogFilters = mutableMapOf<String, List<String>>()
            val tokenCheckStatuses = mutableMapOf<String, Boolean>()
            for (idx in 0 until (mAppData.logFormats?.size ?: 0)) {
                val formatName = mAppData.logFormats!![idx].name
                for (tokIdx in 0 until AppConstants.MAX_TOKEN_COUNT) {
                    val tokenFilters = mAppData.logFormats!![idx].tokenFilters!!
                    val isSaveFilter = tokenFilters[tokIdx][2] as Boolean
                    val tokenName = tokenFilters[tokIdx][0] as String
                    if (tokenName.isEmpty()) {
                        continue
                    }
                    val key = "${formatName}_${tokenName}"
                    if (isSaveFilter) {
                        val filters = mutableListOf<String>()
                        for (i in 0 until AppConstants.MAX_TOKEN_FILTER) {
                            val item = getFromProperties(configReader, "${ITEM_TOKEN_FILTER}${key}_$i")
                            if (item == null) {
                                break
                            }
                            filters.add(item)
                        }
                        tokenLogFilters[key] = filters
                    }

                    val checkStatus = getBooleanFromProperties(configReader, "${ITEM_TOKEN_CHECK}${key}")
                    checkStatus?.let {
                        tokenCheckStatuses[key] = it
                    }
                }
            }

            val highlightLogs = mutableListOf<String>()
            for (idx in 0 until AppConstants.MAX_BOLD_LOG) {
                filter = getFromProperties(configReader, ITEM_HIGHLIGHT_LOG + idx)
                if (filter == null) {
                    break
                }
                highlightLogs.add(filter)
            }

            val findLogs = mutableListOf<String>()
            for (idx in 0 until AppConstants.MAX_FIND_LOG) {
                filter = getFromProperties(configReader, ITEM_FIND_LOG + idx)
                if (filter == null) {
                    break
                }
                findLogs.add(filter)
            }
            mAppHistory = mAppHistory.copy(recentFilters = mAppHistory.recentFilters.copy(showLogFilters = showLogFilters, tokenLogFilterMap = tokenLogFilters,
                    boldLogs = highlightLogs, findLogs = findLogs))

            val showLogCheck = getBooleanFromProperties(configReader, ITEM_SHOW_LOG_CHECK)
            val highlightLogCheck = getBooleanFromProperties(configReader, ITEM_HIGHLIGHT_LOG_CHECK)

            mAppData = mAppData.copy(filter = mAppData.filter.copy(showLogCheck = showLogCheck,
                tokenCheckStatusMap = tokenCheckStatuses, boldLogCheck = highlightLogCheck))

            val filterSnippet = mutableListOf<PresetManager.PresetElement>()
            for (i in 0 until FiltersManager.MAX_FILTERS) {
                val name = getFromProperties(configReader, ITEM_FILTERS_TITLE + i)
                if (name.isNullOrEmpty()) {
                    break
                }
                val value = getFromProperties(configReader, ITEM_FILTERS_FILTER + i) ?: "null"
                val tableBar = getBooleanFromProperties(configReader, ITEM_FILTERS_TABLEBAR + i) ?: false
                filterSnippet.add(PresetManager.PresetElement(name, value, tableBar))
            }

            val cmdAlias = mutableListOf<PresetManager.PresetElement>()
            for (i in 0 until CmdManager.MAX_CMD_COUNT) {
                val name = getFromProperties(configReader, ITEM_CMDS_TITLE + i)
                if (name.isNullOrEmpty()) {
                    break
                }
                val value = getFromProperties(configReader, ITEM_CMDS_CMD + i) ?: "null"
                val tableBar = getBooleanFromProperties(configReader, ITEM_CMDS_TABLEBAR + i) ?: false
                cmdAlias.add(PresetManager.PresetElement(name, value, tableBar))
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

            val toolPanel = getBooleanFromProperties(configReader, ITEM_TOOL_PANEL)
            val toolSelection = getBooleanFromProperties(configReader, ITEM_TOOL_SELECTION)
            val toolSelectionRangePrevious = getIntFromProperties(configReader, ITEM_TOOL_SELECTION_RANGE_PREVIOUS)
            val toolSelectionRangeNext = getIntFromProperties(configReader, ITEM_TOOL_SELECTION_RANGE_NEXT)
            val toolTestEnable = getBooleanFromProperties(configReader, ITEM_TOOL_TEST_ENABLE)
            val toolTest = getBooleanFromProperties(configReader, ITEM_TOOL_TEST)

            mAppData = mAppData.copy(tool = mAppData.tool.copy(toolPanel = toolPanel, toolSelection = toolSelection, toolSelectionRangePrevious = toolSelectionRangePrevious,
                toolSelectionRangeNext = toolSelectionRangeNext, toolTestEnable = toolTestEnable, toolTest = toolTest))

            val testTriggers = mutableListOf<TestTrigger>()
            for (i in 0 until AppConstants.MAX_TRIGGER_COUNT) {
                val name = getFromProperties(agingTestReader, "$i$TRIGGER_ITEM_TRIGGER_NAME") ?: ""
                if (name.isEmpty()) {
                    break
                }
                val filter = getFromProperties(agingTestReader, "$i$TRIGGER_ITEM_TRIGGER_FILTER") ?: ""
                val action = getIntFromProperties(agingTestReader, "$i$TRIGGER_ITEM_TRIGGER_ACTION") ?: 0
                val actionParameter = getFromProperties(agingTestReader, "$i$TRIGGER_ITEM_TRIGGER_ACTION_PARAMETER") ?: ""
                val once = getBooleanFromProperties(agingTestReader, "$i$TRIGGER_ITEM_TRIGGER_ONCE") ?: true

                testTriggers.add(TestTrigger(name, filter, action, actionParameter, once))
            }

            mAppData = mAppData.copy(testTriggers = testTriggers)

            val fileItems = mutableListOf<RecentFileItem>()
            for (i in 0 until AppConstants.MAX_RECENT_FILE) {
                val  path = getFromProperties(recentFileReader, "$i$RECENT_ITEM_PATH")
                if (path.isNullOrEmpty()) {
                    break
                }
                val showLog = getFromProperties(recentFileReader, "$i$RECENT_ITEM_SHOW_LOG") ?: ""

                val tokenFilter = mutableMapOf<String, String>()
                val highlightLog = getFromProperties(recentFileReader, "$i$RECENT_ITEM_HIGHLIGHT_LOG") ?: ""
                val findLog = getFromProperties(recentFileReader, "$i$RECENT_ITEM_FIND_LOG") ?: ""
                val bookmarks = getFromProperties(recentFileReader, "$i$RECENT_ITEM_BOOKMARKS") ?: ""

                val showLogCheck = getBooleanFromProperties(recentFileReader, "$i$RECENT_ITEM_SHOW_LOG_CHECK") ?: false
                val tokenCheck = mutableMapOf<String, Boolean>()

                val highlightLogCheck = getBooleanFromProperties(recentFileReader, "$i$RECENT_ITEM_HIGHLIGHT_LOG_CHECK") ?: false
                val findMatchCase = getBooleanFromProperties(recentFileReader, "$i$RECENT_ITEM_FIND_MATCH_CASE") ?: false

                fileItems.add(RecentFileItem(path, showLog, tokenFilter, highlightLog, findLog, bookmarks, showLogCheck, tokenCheck, highlightLogCheck, findMatchCase))
            }

            mAppHistory = mAppHistory.copy(recentFiles = mAppHistory.recentFiles.copy(fileItems = fileItems))
        }

        mAppData = mAppData.copy(version = "1")
        mAppHistory = mAppHistory.copy(version = "1")
        Utils.printlnLog("updateAppDataFromV0ToV1 : --")
    }
}

