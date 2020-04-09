package io.bluetrace.opentrace.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bluetrace.opentrace.logging.CentralLog
import java.util.*
import kotlin.properties.Delegates


class BLEDiscoverer constructor(context: Context, serviceUUIDString: String) {

    private var serviceUUID: ParcelUuid by Delegates.notNull()
    private var context: Context by Delegates.notNull()
    private val TAG = "BLEDiscoverer"

    private var localBroadcastManager: LocalBroadcastManager =
        LocalBroadcastManager.getInstance(context)

    init {
        this.context = context
        serviceUUID = ParcelUuid(UUID.fromString(serviceUUIDString))
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    fun startDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        localBroadcastManager.registerReceiver(mDiscoveryReceiver, filter)

        bluetoothAdapter!!.startDiscovery()
    }

    fun cancelDiscovery() {
        bluetoothAdapter!!.cancelDiscovery()
        try {
            localBroadcastManager.unregisterReceiver(mDiscoveryReceiver)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Already unregistered workReceiver? ${e.message}")
        }
    }

    private val mDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var action = intent.action
            // When discovery finds a device
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    CentralLog.i(TAG, "Discovery started")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, java.lang.Short.MIN_VALUE)

                    CentralLog.i(TAG, "Scanned Device address: ${device.address} @ $rssi")

                    if (device.uuids == null) {
                        CentralLog.w(TAG, "Nope. No uuids cached for address: " + device.address)
                    }

//                    if(device.uuids.contains(serviceUUID)){
//                        Utils.broadcastDeviceAvailable(context, device)
//                    }

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    CentralLog.i(TAG, "Discovery ended")
                }

                else -> {

                }
            }

        }


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

//                var connectable = ConnectablePeripheral(txPower, rssi)
//                Utils.broadcastDeviceScanned(context, device, connectable)

            }

        }
    }
}
