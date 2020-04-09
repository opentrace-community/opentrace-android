package io.bluetrace.opentrace.logging

import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object SDLog {

    private const val APP_NAME = "OpenTrace"
    private const val FOLDER = "SDLogging"
    private val dateFormat = SimpleDateFormat("yyyyMMdd")
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

    private var buffer = StringBuffer()
    private var lastWrite = 0L
    private var cachedDateStamp = ""
    private lateinit var cachedFileWriter: BufferedWriter

    private val isWritable: Boolean
        get() {
            val states = checkSDState()
            return states[0] and states[1]
        }

    private fun checkSDState(): BooleanArray {
        val state = Environment.getExternalStorageState()
        var writeable: Boolean
        var sdcard: Boolean
        when (state) {
            Environment.MEDIA_MOUNTED -> {
                writeable = true
                sdcard = true
            }
            Environment.MEDIA_MOUNTED_READ_ONLY -> {
                writeable = false
                sdcard = true
            }
            else -> {
                writeable = false
                sdcard = false
            }
        }
        return booleanArrayOf(sdcard, writeable)
    }

    fun i(vararg message: String) {
        log("INFO", message)
    }

    fun w(vararg message: String) {
        log("WARN", message)
    }

    fun d(vararg message: String) {
        log("DEBUG", message)
    }

    fun e(vararg message: String) {
        log("ERROR", message)
    }

    private fun createFileWriter(dateStamp: String): BufferedWriter {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + "/" + FOLDER)
        dir.mkdirs()
        val file = File(dir, APP_NAME + "_" + dateStamp + ".txt")
        val fw = FileWriter(file, true)
        return fw.buffered()
    }

    private fun getFileWriter(): BufferedWriter {
        //date stamp for filename
        val dateStamp = dateFormat.format(Date())

        return if (dateStamp == cachedDateStamp) {
            cachedFileWriter
        } else {
            //make sure all the logs from previous day is written to the previous file
            if (::cachedFileWriter.isInitialized) {
                cachedFileWriter.flush()
                cachedFileWriter.close()
            }

            //create a new fileWriter for the day
            cachedFileWriter = createFileWriter(dateStamp)
            cachedDateStamp = dateStamp
            cachedFileWriter
        }
    }

    private fun log(label: String, message: Array<out String>) {
        if (!isWritable) {
            return
        }

        if (message == null) {
            return
        }

        val timeStamp = timestampFormat.format(Date())
        val line = message.joinToString(" ")
        buffer.append("$timeStamp $label $line\n")

        try {
            val fw = getFileWriter()
            fw.write(buffer.toString())
            buffer = StringBuffer()
            if (System.currentTimeMillis() - lastWrite > 10000) {
                fw.flush()
                lastWrite = System.currentTimeMillis()
            }
        } catch (e: IOException) {
            buffer.append("$timeStamp ERROR SDLog ??? IOException while writing to SDLog: ${e.message}\n")
        }
    }
}
