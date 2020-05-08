package au.gov.health.covidsafe

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import au.gov.health.covidsafe.bluetooth.gatt.*
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.scheduler.Scheduler
import au.gov.health.covidsafe.services.BluetoothMonitoringService
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_ADVERTISE_REQ_CODE
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_BM_UPDATE
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_HEALTH_CHECK_CODE
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_SCAN_REQ_CODE
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_START
import au.gov.health.covidsafe.status.Status
import au.gov.health.covidsafe.streetpass.ACTION_DEVICE_SCANNED
import au.gov.health.covidsafe.streetpass.ConnectablePeripheral
import au.gov.health.covidsafe.streetpass.ConnectionRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    private const val TAG = "Utils"

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getBatteryOptimizerExemptionIntent(packageName: String): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        return intent
    }

    fun canHandleIntent(batteryExemptionIntent: Intent, packageManager: PackageManager?): Boolean {
        packageManager?.let {
            return batteryExemptionIntent.resolveActivity(packageManager) != null
        }
        return false
    }

    fun getDate(milliSeconds: Long): String {
        val dateFormat = "dd/MM/yyyy HH:mm:ss.SSS"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun startBluetoothMonitoringService(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_START.index
        )

        context.startService(intent)
    }

    fun scheduleStartMonitoringService(context: Context, timeInMillis: Long) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_START.index
        )

        Scheduler.scheduleServiceIntent(
                PENDING_START,
                context,
                intent,
                timeInMillis
        )
    }

    fun scheduleBMUpdateCheck(context: Context, bmCheckInterval: Long) {

        cancelBMUpdateCheck(context)

        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.scheduleServiceIntent(
                PENDING_BM_UPDATE,
                context,
                intent,
                bmCheckInterval
        )
    }

    fun cancelBMUpdateCheck(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.cancelServiceIntent(PENDING_BM_UPDATE, context, intent)
    }

    fun stopBluetoothMonitoringService(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_STOP.index
        )
        cancelNextScan(context)
        cancelNextHealthCheck(context)
        context.stopService(intent)
    }

    fun cancelNextScan(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_SCAN.index
        )
        Scheduler.cancelServiceIntent(PENDING_SCAN_REQ_CODE, context, nextIntent)
    }

    fun cancelNextAdvertise(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_ADVERTISE.index
        )
        Scheduler.cancelServiceIntent(PENDING_ADVERTISE_REQ_CODE, context, nextIntent)
    }

    fun scheduleNextHealthCheck(context: Context, timeInMillis: Long) {
        //cancels any outstanding check schedules.
        cancelNextHealthCheck(context)

        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        Scheduler.scheduleServiceIntent(
                PENDING_HEALTH_CHECK_CODE,
                context,
                nextIntent,
                timeInMillis
        )
    }

    private fun cancelNextHealthCheck(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
                BluetoothMonitoringService.COMMAND_KEY,
                BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        Scheduler.cancelServiceIntent(PENDING_HEALTH_CHECK_CODE, context, nextIntent)
    }

    fun broadcastDeviceScanned(
            context: Context,
            device: BluetoothDevice,
            connectableBleDevice: ConnectablePeripheral
    ) {
        val intent = Intent(ACTION_DEVICE_SCANNED)
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        intent.putExtra(CONNECTION_DATA, connectableBleDevice)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastDeviceProcessed(context: Context, deviceAddress: String) {
        val intent = Intent(ACTION_DEVICE_PROCESSED)
        intent.putExtra(DEVICE_ADDRESS, deviceAddress)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }


    fun broadcastStreetPassReceived(context: Context, streetpass: ConnectionRecord) {
        val intent = Intent(ACTION_RECEIVED_STREETPASS)
        intent.putExtra(STREET_PASS, streetpass)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastStatusReceived(context: Context, statusRecord: Status) {
        val intent = Intent(ACTION_RECEIVED_STATUS)
        intent.putExtra(STATUS, statusRecord)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastDeviceDisconnected(context: Context, device: BluetoothDevice) {
        val intent = Intent(ACTION_GATT_DISCONNECTED)
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun isBluetoothAvailable(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null &&
                bluetoothAdapter.isEnabled && bluetoothAdapter.state == BluetoothAdapter.STATE_ON
    }

    fun storeBroadcastMessage(context: Context?, packet: String) {
        CentralLog.d(TAG, "Storing packet into internal storage...")
        val file = File(context?.filesDir, "packet")
        file.writeText(packet)
    }

    fun retrieveBroadcastMessage(context: Context): String? {
        val file = File(context.filesDir, "packet")
        if (file.exists()) {
            val readback = file.readText()
            CentralLog.d(TAG, "fetched broadcastmessage from file:  $readback")
            return readback
        }
        return null
    }

    fun needToUpdate(context: Context): Boolean {
        val nextFetchTime = Preference.getNextFetchTimeInMillis(context)

        val currentTime = System.currentTimeMillis()

        val update = currentTime >= nextFetchTime
        CentralLog.i(TAG, "Need to update BM? $nextFetchTime vs $currentTime: $update")
        return update
    }

    fun bmValid(context: Context): Boolean {
        val expiryTime = Preference.getExpiryTimeInMillis(context)
        val currentTime = System.currentTimeMillis()

        val update = currentTime < expiryTime
        CentralLog.i(TAG, "Is BM Valid? $expiryTime vs $currentTime: $update")

        return true
    }
}
