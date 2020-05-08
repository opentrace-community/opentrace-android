package au.gov.health.covidsafe.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_PRIVACY_CLEANER_CODE
import au.gov.health.covidsafe.services.SensorMonitoringService.Companion.TAG
import au.gov.health.covidsafe.status.persistence.StatusRecordStorage
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecordStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class PrivacyCleanerReceiver : BroadcastReceiver(), CoroutineScope {

    private val TAG = this.javaClass.simpleName

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    companion object {

        private fun getIntent(context: Context, requestCode: Int): PendingIntent? {
            val intent = Intent(context, PrivacyCleanerReceiver::class.java)
            return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        fun startAlarm(context: Context) {
            val pendingIntent = getIntent(context, PENDING_PRIVACY_CLEANER_CODE)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent)
        }

        suspend fun cleanDb(context: Context) {
            val twentyOneDaysAgo = Calendar.getInstance()
            twentyOneDaysAgo.set(Calendar.HOUR_OF_DAY, 23)
            twentyOneDaysAgo.set(Calendar.MINUTE, 59)
            twentyOneDaysAgo.set(Calendar.SECOND, 59)
            twentyOneDaysAgo.add(Calendar.DATE, -21)

            val countStreetDeleted = StreetPassRecordStorage(context).deleteDataOlderThan(twentyOneDaysAgo.timeInMillis)
            val countStatusDeleted = StatusRecordStorage(context).deleteDataOlderThan(twentyOneDaysAgo.timeInMillis)

            CentralLog.i(TAG, "Street info deleted count : $countStreetDeleted")
            CentralLog.i(TAG, "Status info deleted count : $countStatusDeleted")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        launch {
            cleanDb(context)
        }
    }

}