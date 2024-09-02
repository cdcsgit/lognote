package com.blogspot.cdcsutils.lognote

import java.io.IOException
import java.util.*
import javax.swing.JOptionPane


class LogCmdManager private constructor(){
    var mPrefix: String = DEFAULT_PREFIX
    var mAdbCmd = "adb"
    var mLogSavePath:String = "."
    var mTargetDevice: String = ""
    var mLogCmd: String = ""
    var mDevices = ArrayList<String>()
    private val mEventListeners = ArrayList<AdbEventListener>()
    private val mProcessList: ProcessList = ProcessList.getInstance()

    companion object {
        const val DEFAULT_PREFIX = Main.NAME

        const val EVENT_NONE = 0
        const val EVENT_SUCCESS = 1
        const val EVENT_FAIL = 2

        const val CMD_CONNECT = 1
        const val CMD_GET_DEVICES = 2
        const val CMD_LOGCAT = 3
        const val CMD_DISCONNECT = 4
        const val CMD_GET_PROCESSES = 5

        const val DEFAULT_LOGCAT = "logcat -v threadtime"
        const val LOG_CMD_MAX = 10

        const val TYPE_CMD_PREFIX = "CMD:"
        const val TYPE_CMD_PREFIX_LEN = 4
        const val TYPE_LOGCAT = 0
        const val TYPE_CMD = 1

        private val mInstance: LogCmdManager = LogCmdManager()

        fun getInstance(): LogCmdManager {
            return mInstance
        }
    }

    fun getDevices() {
        execute(makeExecuter(CMD_GET_DEVICES))
    }

    fun getType(): Int {
        return if (mLogCmd.startsWith(TYPE_CMD_PREFIX)) {
            TYPE_CMD
        }
        else {
            TYPE_LOGCAT
        }
    }

    fun connect() {
        if (mTargetDevice.isEmpty()) {
            Utils.printlnLog("Target device is not selected")
            return
        }

        execute(makeExecuter(CMD_CONNECT))
    }

    fun disconnect() {
        execute(makeExecuter(CMD_DISCONNECT))
    }

    fun startLogcat() {
        execute(makeExecuter(CMD_LOGCAT))
    }

    fun stop() {
        Utils.printlnLog("Stop all processes ++")
        mProcessLogcat?.destroy()
        mProcessLogcat = null
        mCurrentExecuter?.interrupt()
        mCurrentExecuter = null
        Utils.printlnLog("Stop all processes --")
    }

    fun addEventListener(eventListener:AdbEventListener) {
        mEventListeners.add(eventListener)
    }

    private fun sendEvent(event: AdbEvent) {
        for (listener in mEventListeners) {
            listener.changedStatus(event)
        }
    }

    private var mCurrentExecuter:Thread? = null
    var mProcessLogcat:Process? = null
    private fun execute(cmd:Runnable?) {
        cmd?.run()
    }

    private fun makeExecuter(cmdNum:Int) :Runnable? {
        var executer:Runnable? = null
        when (cmdNum) {
            CMD_CONNECT -> executer = Runnable {
                run {
                    val cmd = "$mAdbCmd connect $mTargetDevice"
                    val runtime = Runtime.getRuntime()
                    val scanner = try {
                        val process = runtime.exec(cmd)
                        Scanner(process.inputStream)
                    } catch (e:IOException) {
                        Utils.printlnLog("Failed run $cmd")
                        e.printStackTrace()
                        val mainUI = MainUI.getInstance()
                        JOptionPane.showMessageDialog(mainUI, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                        val adbEvent = AdbEvent(CMD_CONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    var line:String
                    var isSuccess = false
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine()
                        if (line.contains("connected to")) {
                            Utils.printlnLog("Success connect to $mTargetDevice")
                            val adbEvent = AdbEvent(CMD_CONNECT, EVENT_SUCCESS)
                            sendEvent(adbEvent)
                            isSuccess = true
                            break
                        }
                    }

                    if (!isSuccess) {
                        Utils.printlnLog("Failed connect to $mTargetDevice")
                        val adbEvent = AdbEvent(CMD_CONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                    }
                }
            }

            CMD_GET_DEVICES -> executer = Runnable {
                run {
                    mDevices.clear()

                    val cmd = "$mAdbCmd devices"
                    val runtime = Runtime.getRuntime()
                    val scanner = try {
                        val process = runtime.exec(cmd)
                        Scanner(process.inputStream)
                    } catch (e:IOException) {
                        Utils.printlnLog("Failed run $cmd")
                        e.printStackTrace()
                        val adbEvent = AdbEvent(CMD_GET_DEVICES, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    var line:String
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine()
                        if (line.contains("List of devices")) {
                            continue
                        }
                        val textSplited = line.trim().split(Regex("\\s+"))
                        if (textSplited.size >= 2) {
                            Utils.printlnLog("device : ${textSplited[0]}")
                            mDevices.add(textSplited[0])
                        }
                    }
                    val adbEvent = AdbEvent(CMD_GET_DEVICES, EVENT_SUCCESS)
                    sendEvent(adbEvent)
                }
            }

            CMD_LOGCAT -> executer = Runnable {
                run {
                    mProcessLogcat?.destroy()

                    val cmd = if (mTargetDevice.isNotBlank()) {
                        if (getType() == TYPE_CMD) {
                            "${mLogCmd.substring(TYPE_CMD_PREFIX_LEN)} $mTargetDevice"
                        }
                        else {
                            "$mAdbCmd -s $mTargetDevice $mLogCmd"
                        }
                    }
                    else {
                        if (getType() == TYPE_CMD) {
                            mLogCmd.substring(TYPE_CMD_PREFIX_LEN)
                        }
                        else {
                            "$mAdbCmd $mLogCmd"
                        }
                    }
                    Utils.printlnLog("Start : $cmd")
                    val runtime = Runtime.getRuntime()
                    try {
                        mProcessLogcat = runtime.exec(cmd)
                        val processExitDetector = ProcessExitDetector(mProcessLogcat!!)
                        processExitDetector.addProcessListener(object : ProcessListener {
                            override fun processFinished(process: Process?) {
                                Utils.printlnLog("The subprocess has finished")
                            }
                        })
                        processExitDetector.start()
                    } catch (e:IOException) {
                        Utils.printlnLog("Failed run $cmd")
                        e.printStackTrace()
                        val mainUI = MainUI.getInstance()
                        JOptionPane.showMessageDialog(mainUI, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                        mProcessLogcat = null
                        return@run
                    }
                    Utils.printlnLog("End : $cmd")
                }
            }

            CMD_DISCONNECT -> executer = Runnable {
                run {
                    val cmd = "$mAdbCmd disconnect"
                    val runtime = Runtime.getRuntime()
                    try {
                        runtime.exec(cmd)
                    } catch (e: IOException) {
                        Utils.printlnLog("Failed run $cmd")
                        e.printStackTrace()
                        val mainUI = MainUI.getInstance()
                        JOptionPane.showMessageDialog(mainUI, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                        val adbEvent = AdbEvent(CMD_DISCONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    val adbEvent = AdbEvent(CMD_DISCONNECT, EVENT_SUCCESS)
                    sendEvent(adbEvent)
                }
            }

            CMD_GET_PROCESSES -> executer = Runnable {
                run {
                    mProcessList.clear()

                    val cmd = if (mTargetDevice.isNotBlank()) {
                        "$mAdbCmd -s $mTargetDevice shell ps"
                    }
                    else {
                        "$mAdbCmd shell ps"
                    }

                    val runtime = Runtime.getRuntime()
                    val scanner = try {
                        val process = runtime.exec(cmd)
                        Scanner(process.inputStream)
                    } catch (e:IOException) {
                        Utils.printlnLog("Failed run $cmd")
                        e.printStackTrace()
                        val adbEvent = AdbEvent(CMD_GET_PROCESSES, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    val thread = Thread {
                        try {
                            var line:String
                            while (scanner.hasNextLine()) {
                                line = scanner.nextLine()
                                if (line.contains("USER") && line.contains("PID")) {
                                    continue
                                }
                                val textSplit = line.trim().split(Regex("\\s+"))
                                if (textSplit.size >= 9) {
                                    mProcessList.add(ProcessItem(textSplit[1], textSplit[8], textSplit[0]))
                                }
                            }
                        } catch (e: InterruptedException) {
                            Utils.printlnLog("Failed get process list")
                            mProcessList.clear()
                        }
                    }
                    thread.start()
                    try {
                        thread.join(1000)
                        if (thread.isAlive) {
                            thread.interrupt()
                        }
                    } catch (ex: InterruptedException) {
                        Utils.printlnLog("get process join : InterruptedException, throw exception")
                        throw ex
                    }

                    for (i in 0 until 10) {
                        if (!thread.isAlive) {
                            break
                        }
                        Thread.sleep(100)
                    }

                    val adbEvent = AdbEvent(CMD_GET_PROCESSES, EVENT_SUCCESS)
                    sendEvent(adbEvent)
                }
            }
        }

        return executer
    }

    fun getProcesses() {
        execute(makeExecuter(CMD_GET_PROCESSES))
    }

    interface AdbEventListener {
        fun changedStatus(event:AdbEvent)
    }

    class AdbEvent(val cmd: Int, val event: Int)

    interface ProcessListener : EventListener {
        fun processFinished(process: Process?)
    }

    class ProcessExitDetector(process: Process) : Thread() {
        var process: Process
        private val listeners: MutableList<ProcessListener> = ArrayList<ProcessListener>()
        override fun run() {
            try {
                process.waitFor()
                for (listener in listeners) {
                    listener.processFinished(process)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun addProcessListener(listener: ProcessListener) {
            listeners.add(listener)
        }

        fun removeProcessListener(listener: ProcessListener) {
            listeners.remove(listener)
        }

        init {
//            try {
//                process.exitValue()
//                throw IllegalArgumentException("The process is already ended")
//            } catch (exc: IllegalThreadStateException) {
                this.process = process
//            }
        }
    }
}
