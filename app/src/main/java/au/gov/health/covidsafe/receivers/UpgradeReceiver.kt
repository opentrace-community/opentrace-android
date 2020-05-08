package au.gov.health.covidsafe.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import au.gov.health.covidsafe.Utils
import au.gov.health.covidsafe.logging.CentralLog

class UpgradeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        try {
            if (Intent.ACTION_MY_PACKAGE_REPLACED != intent!!.action) return
            context?.let {
                CentralLog.i("UpgradeReceiver", "Starting service from upgrade receiver")
                Utils.startBluetoothMonitoringService(context)
            }
        } catch (e: Exception) {
            CentralLog.e("UpgradeReceiver", "Unable to handle upgrade: ${e.localizedMessage}")
        }
    }
}