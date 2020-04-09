package io.bluetrace.opentrace.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Handler
import android.os.ParcelUuid
import io.bluetrace.opentrace.logging.CentralLog
import io.bluetrace.opentrace.services.BluetoothMonitoringService.Companion.infiniteAdvertising
import java.util.*


class BLEAdvertiser constructor(val serviceUUID: String) {

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

            var reason: String

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
    val pUuid = ParcelUuid(UUID.fromString(serviceUUID))

    val settings = AdvertiseSettings.Builder()
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTimeout(0)
        .build()

    var data: AdvertiseData? = null

    var handler = Handler()

    var stopRunnable: Runnable = Runnable {
        CentralLog.i(TAG, "Advertising stopping as scheduled.")
        stopAdvertising()
    }

    var isAdvertising = false
    var shouldBeAdvertising = false

    //reference
    //https://code.tutsplus.com/tutorials/how-to-advertise-android-as-a-bluetooth-le-peripheral--cms-25426
    fun startAdvertisingLegacy(timeoutInMillis: Long) {

        val randomUUID = UUID.randomUUID().toString()
        val finalString = randomUUID.substring(randomUUID.length - charLength, randomUUID.length)
        CentralLog.d(TAG, "Unique string: $finalString")
        val serviceDataByteArray = finalString.toByteArray()

        data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(pUuid)
            .addManufacturerData(1023, serviceDataByteArray)
            .build()

        try {
            CentralLog.d(TAG, "Start advertising")
            advertiser = advertiser ?: BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
            advertiser?.startAdvertising(settings, data, callback)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Failed to start advertising legacy: ${e.message}")
        }

        if (!infiniteAdvertising) {
            handler.removeCallbacksAndMessages(stopRunnable)
            handler.postDelayed(stopRunnable, timeoutInMillis)
        }
    }

    fun startAdvertising(timeoutInMillis: Long) {
        startAdvertisingLegacy(timeoutInMillis)
        shouldBeAdvertising = true
    }

    fun stopAdvertising() {
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
