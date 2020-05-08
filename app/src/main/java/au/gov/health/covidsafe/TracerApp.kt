package au.gov.health.covidsafe

import android.app.Application
import android.content.Context
import android.os.Build
import com.atlassian.mobilekit.module.feedback.FeedbackModule

import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.services.BluetoothMonitoringService
import au.gov.health.covidsafe.streetpass.CentralDevice
import au.gov.health.covidsafe.streetpass.PeripheralDevice

class TracerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext = applicationContext
        FeedbackModule.init(this)
    }

    companion object {

        private const val TAG = "TracerApp"
        const val ORG = BuildConfig.ORG
        const val protocolVersion = BuildConfig.PROTOCOL_VERSION

        lateinit var AppContext: Context

        fun thisDeviceMsg(): String {
            BluetoothMonitoringService.broadcastMessage?.let {
                CentralLog.i(TAG, "Retrieved BM for storage: $it")
                return it
            }

            CentralLog.e(TAG, "No local Broadcast Message")
            return BluetoothMonitoringService.broadcastMessage!!
        }

        fun asPeripheralDevice(): PeripheralDevice {
            return PeripheralDevice(Build.MODEL, "SELF")
        }

        fun asCentralDevice(): CentralDevice {
            return CentralDevice(Build.MODEL, "SELF")
        }

    }
}
