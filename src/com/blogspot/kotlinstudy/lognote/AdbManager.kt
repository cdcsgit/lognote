package com.blogspot.kotlinstudy.lognote

import java.io.IOException
import java.util.*

interface AdbEventListener {
    fun changedStatus(event:AdbEvent)
}

class AdbEvent(c:Int, e:Int) {
    val cmd = c
    val event = e
}

class AdbManager private constructor(){
    var mPrefix: String =""
    var mAdbCmd = "adb"
    var mLogSavePath:String? = "."
    var mTargetDevice: String = ""
    var mDevices = ArrayList<String>()
    val mEventListeners = ArrayList<AdbEventListener>()

    companion object {
        val EVENT_NONE = 0
        val EVENT_SUCCESS = 1
        val EVENT_FAIL = 2

        val CMD_CONNECT = 1
        val CMD_GET_DEVICES = 2
        val CMD_LOGCAT = 3
        val CMD_DISCONNECT = 4

        private val mInstance: AdbManager = AdbManager()

        fun getInstance(): AdbManager {
            return mInstance
        }
    }

    fun getDevices() {
        execute(makeExecuter(CMD_GET_DEVICES))
    }

    fun connect() {
        if (mTargetDevice.isEmpty()) {
            System.err.println("Target device is not selected")
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
        System.err.println("Stop all processes")
        mProcessLogcat?.destroy()
        mProcessLogcat = null
        mCurrentExecuter?.interrupt()
        mCurrentExecuter = null
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
                    val cmd = mAdbCmd + " connect " + mTargetDevice
                    val runtime = Runtime.getRuntime()
                    val scanner = try {
                        val process = runtime.exec(cmd)
                        Scanner(process.inputStream)
                    } catch (e:IOException) {
                        System.out.println("Failed run " + cmd)
                        val adbEvent = AdbEvent(CMD_CONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    var line:String
                    var isSuccess = false
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine()
                        if (line.contains("connected to")) {
                            System.out.println("Success connect to " + mTargetDevice)
                            val adbEvent = AdbEvent(CMD_CONNECT, EVENT_SUCCESS)
                            sendEvent(adbEvent)
                            isSuccess = true
                            break
                        }
                    }

                    if (!isSuccess) {
                        System.out.println("Failed connect to " + mTargetDevice)
                        val adbEvent = AdbEvent(CMD_CONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                    }
                }
            }

            CMD_GET_DEVICES -> executer = Runnable {
                run {
                    mDevices.clear()

                    val cmd = mAdbCmd + " devices"
                    val runtime = Runtime.getRuntime()
                    val scanner = try {
                        val process = runtime.exec(cmd)
                        Scanner(process.inputStream)
                    } catch (e:IOException) {
                        System.out.println("Failed run " + cmd)
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
                            System.out.println("device : " + textSplited[0])
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
                    val cmd = mAdbCmd + " -s " + mTargetDevice + " logcat -v threadtime"
                    System.out.println("Run : " + cmd)
                    val runtime = Runtime.getRuntime()
                    try {
                        mProcessLogcat = runtime.exec(cmd)
                    } catch (e:IOException) {
                        System.out.println("Failed run " + cmd)
                        mProcessLogcat = null
                        return@run
                    }
                }
            }
            CMD_DISCONNECT -> executer = Runnable {
                run {
                    val cmd = mAdbCmd + " disconnect"
                    val runtime = Runtime.getRuntime()
                    try {
                        runtime.exec(cmd)
                    } catch (e: IOException) {
                        System.out.println("Failed run " + cmd)
                        val adbEvent = AdbEvent(CMD_DISCONNECT, EVENT_FAIL)
                        sendEvent(adbEvent)
                        return@run
                    }

                    val adbEvent = AdbEvent(CMD_DISCONNECT, EVENT_SUCCESS)
                    sendEvent(adbEvent)
                }
            }
        }

        return executer
    }


}