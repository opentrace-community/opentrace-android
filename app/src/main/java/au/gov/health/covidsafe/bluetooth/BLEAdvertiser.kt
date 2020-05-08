package au.gov.health.covidsafe.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Handler
import android.os.ParcelUuid
import au.gov.health.covidsafe.logging.CentralLog
import java.util.*


class BLEAdvertiser constructor(serviceUUID: String) {

    private var advertiser: BluetoothLeAdvertiser? =
            BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    private val TAG = "BLEAdvertiser"
    private var charLength = 3
    private var callback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            CentralLog.i(TAG, "Advertising onStartSuccess")
            CentralLog.i(TAG, settingsInEffect.toString())
            isAdvertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            val reason: String

            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> {
                    reason = "ADVERTISE_FAILED_ALREADY_STARTED"
                    isAdvertising = true
                }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
                    reason = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_INTERNAL_ERROR -> {
                    reason = "ADVERTISE_FAILED_INTERNAL_ERROR"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
                    reason = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_DATA_TOO_LARGE -> {
                    reason = "ADVERTISE_FAILED_DATA_TOO_LARGE"
                    isAdvertising = false
                    charLength--
                }

                else -> {
                    reason = "UNDOCUMENTED"
                }
            }

            CentralLog.d(TAG, "Advertising onStartFailure: $errorCode - $reason")
        }
    }
    private val pUuid = ParcelUuid(UUID.fromString(serviceUUID))

    private val settings: AdvertiseSettings? = AdvertiseSettings.Builder()
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()

    var data: AdvertiseData? = null

    private var handler = Handler()

    private var stopRunnable: Runnable = Runnable {
        CentralLog.i(TAG, "Advertising stopping as scheduled.")
        stopAdvertising()
    }

    var isAdvertising = false
    var shouldBeAdvertising = false

    private fun startAdvertisingLegacy(timeoutInMillis: Long) {

        val randomUUID = UUID.randomUUID().toString()
        val finalString = randomUUID.substring(randomUUID.length - charLength, randomUUID.length)
        CentralLog.d(TAG, "Unique string: $finalString")
        val serviceDataByteArray = finalString.toByteArray()

        if (data == null) {
            data = AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(true)
                    .addServiceUuid(pUuid)
                    .addManufacturerData(1023, serviceDataByteArray)
                    .build()
        }

        try {
            CentralLog.d(TAG, "Start advertising")
            advertiser = advertiser ?: BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
            advertiser?.startAdvertising(settings, data, callback)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Failed to start advertising legacy: ${e.message}")
        }

        handler.removeCallbacksAndMessages(stopRunnable)
        handler.postDelayed(stopRunnable, timeoutInMillis)
    }

    fun startAdvertising(timeoutInMillis: Long) {
        startAdvertisingLegacy(timeoutInMillis)
        shouldBeAdvertising = true
    }

    private fun stopAdvertising() {
        try {
            CentralLog.d(TAG, "stop advertising")
            advertiser?.stopAdvertising(callback)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Failed to stop advertising: ${e.message}")
        }
        shouldBeAdvertising = false
        handler.removeCallbacksAndMessages(null)
    }
}
