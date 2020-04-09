package io.bluetrace.opentrace.streetpass

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Handler
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.BLEScanner
import io.bluetrace.opentrace.logging.CentralLog
import io.bluetrace.opentrace.services.BluetoothMonitoringService.Companion.infiniteScanning
import io.bluetrace.opentrace.status.Status
import kotlin.properties.Delegates

class StreetPassScanner constructor(
    context: Context,
    serviceUUIDString: String,
    private val scanDurationInMillis: Long
) {

    private var scanner: BLEScanner by Delegates.notNull()

    private var context: Context by Delegates.notNull()
    private val TAG = "StreetPassScanner"

    private var handler: Handler = Handler()

    var scannerCount = 0

    val scanCallback = BleScanCallback()

//    var discoverer: BLEDiscoverer

    init {
        scanner = BLEScanner(context, serviceUUIDString, 0)
        this.context = context
//        discoverer = BLEDiscoverer(context, serviceUUIDString)
    }

    fun startScan() {

        var statusRecord = Status("Scanning Started")
        Utils.broadcastStatusReceived(context, statusRecord)

        scanner.startScan(scanCallback)
        scannerCount++

        if (!infiniteScanning) {
            handler.postDelayed(
                { stopScan() }
                , scanDurationInMillis)
        }

        CentralLog.d(TAG, "scanning started")
//        discoverer.startDiscovery()
    }

    fun stopScan() {
        //only stop if scanning was successful - kinda.
        if (scannerCount > 0) {
            var statusRecord = Status("Scanning Stopped")
            Utils.broadcastStatusReceived(context, statusRecord)
            scannerCount--
            scanner.stopScan()
//        discoverer.cancelDiscovery()
        }
    }

    fun isScanning(): Boolean {
        return scannerCount > 0
    }

    inner class BleScanCallback : ScanCallback() {

        private val TAG = "BleScanCallback"

        private fun processScanResult(scanResult: ScanResult?) {

            scanResult?.let { result ->
                val device = result.device
                var rssi = result.rssi // get RSSI value

                var txPower: Int? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    txPower = result.txPower
                    if (txPower == 127) {
                        txPower = null
                    }
                }

                var manuData: ByteArray =
                    scanResult.scanRecord?.getManufacturerSpecificData(1023) ?: "N.A".toByteArray()
                var manuString = String(manuData, Charsets.UTF_8)

                var connectable = ConnectablePeripheral(manuString, txPower, rssi)

                CentralLog.i(TAG, "Scanned: ${manuString} - ${device.address}")

                Utils.broadcastDeviceScanned(context, device, connectable)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            processScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            val reason = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "$errorCode - SCAN_FAILED_ALREADY_STARTED"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "$errorCode - SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "$errorCode - SCAN_FAILED_FEATURE_UNSUPPORTED"
                SCAN_FAILED_INTERNAL_ERROR -> "$errorCode - SCAN_FAILED_INTERNAL_ERROR"
                else -> {
                    "$errorCode - UNDOCUMENTED"
                }
            }
            CentralLog.e(TAG, "BT Scan failed: $reason")
            if (scannerCount > 0) {
                scannerCount--
            }
        }
    }


}

