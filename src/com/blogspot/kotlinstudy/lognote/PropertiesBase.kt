package com.blogspot.kotlinstudy.lognote

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

abstract class PropertiesBase(fileName: String) {
    companion object {
        val LOGNOTE_HOME: String = System.getenv("LOGNOTE_HOME") ?: ""
    }

    protected val mProperties = Properties()
    private var mXmlPath = fileName

    init {
        if (LOGNOTE_HOME.isNotEmpty()) {
            mXmlPath = "$LOGNOTE_HOME${File.separator}$mXmlPath"
        }
        println("Xml File Path : $mXmlPath")
    }

    protected fun loadXml(): Boolean {
        var ret = true
        var fileInput: FileInputStream? = null

        try {
            fileInput = FileInputStream(mXmlPath)
            mProperties.loadFromXML(fileInput)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ret = false
        } finally {
            if (null != fileInput) {
                try {
                    fileInput.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

        return ret
    }

    protected fun saveXml(): Boolean {
        var ret = true
        var fileOutput: FileOutputStream? = null
        try {
            fileOutput = FileOutputStream(mXmlPath)
            mProperties.storeToXML(fileOutput, "")
        } catch (ex: Exception) {
            ex.printStackTrace()
            ret = false
        } finally {
            if (null != fileOutput) {
                try {
                    fileOutput.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
        return ret
    }

    fun getItem(key: String): String? {
        return mProperties[key] as String?
    }

    fun setItem(key: String, value: String) {
        mProperties[key] = value
    }

    fun setItems(keys: Array<String>, values: Array<String>) {
        if (keys.size != values.size) {
            println("saveItem : size not match ${keys.size}, ${values.size}")
            return
        }
        for (idx in keys.indices) {
            mProperties[keys[idx]] = values[idx]
        }
    }

    fun removeItem(key: String) {
        mProperties.remove(key)
    }

    protected abstract fun manageVersion()
}

