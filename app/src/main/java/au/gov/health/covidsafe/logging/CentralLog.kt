package au.gov.health.covidsafe.logging

import android.os.Build
import android.os.PowerManager
import android.util.Log
import au.gov.health.covidsafe.BuildConfig

class CentralLog {

    companion object {

        private var pm: PowerManager? = null

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
        }

        fun d(tag: String, message: String, e: Throwable?) {
            if (!shouldLog()) {
                return
            }

            Log.d(tag, getIdleStatus() + message, e)
        }


        fun w(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.w(tag, getIdleStatus() + message)
        }

        fun i(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.i(tag, getIdleStatus() + message)
        }

        fun e(tag: String, message: String) {
            if (!shouldLog()) {
                return
            }

            Log.e(tag, getIdleStatus() + message)
        }

        fun e(tag: String, message: String, exception: Exception) {
            if (!shouldLog()) {
                return
            }

            Log.e(tag, getIdleStatus() + message, exception)
        }

    }

}