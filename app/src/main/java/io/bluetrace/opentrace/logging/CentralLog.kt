package io.bluetrace.opentrace.logging

import android.os.Build
import android.os.PowerManager
import android.util.Log
import io.bluetrace.opentrace.BuildConfig


class CentralLog {

    companion object {

        var pm: PowerManager? = null

        fun setPowerManager(powerManager: PowerManager) {
            pm = powerManager
        }

        private fun shouldLog(): Boolean {
            return BuildConfig.DEBUG
        }

        private fun getIdleStatus(): String {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return if (true == pm?.isDeviceIdleMode) {
                    " IDLE "
                } else {
                    " NOT-IDLE "
                }
            }
            return " NO-DOZE-FEATURE "
        }

        fun d(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.d(tag, getIdleStatus() + message)
            SDLog.d(tag, getIdleStatus() + message)
        }

        fun d(tag: String, message: String, e: Throwable?) {
            if (!shouldLog()) {
                return
            }

            Log.d(tag, getIdleStatus() + message, e)
            SDLog.d(tag, getIdleStatus() + message)
        }


        fun w(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.w(tag, getIdleStatus() + message)
            SDLog.w(tag, getIdleStatus() + message)
        }

        fun i(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.i(tag, getIdleStatus() + message)
            SDLog.i(tag, getIdleStatus() + message)
        }

        fun e(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.e(tag, getIdleStatus() + message)
            SDLog.e(tag, getIdleStatus() + message)
        }

    }

}
