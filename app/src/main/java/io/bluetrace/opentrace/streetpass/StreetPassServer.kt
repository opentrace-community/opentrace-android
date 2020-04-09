package io.bluetrace.opentrace.streetpass

import android.content.Context
import io.bluetrace.opentrace.bluetooth.gatt.GattServer
import io.bluetrace.opentrace.bluetooth.gatt.GattService

class StreetPassServer constructor(val context: Context, val serviceUUIDString: String) {

    private val TAG = "StreetPassServer"
    private var gattServer: GattServer? = null

    init {
        gattServer = setupGattServer(context, serviceUUIDString)
    }

    private fun setupGattServer(context: Context, serviceUUIDString: String): GattServer? {
        val gattServer = GattServer(context, serviceUUIDString)
        var started = gattServer.startServer()

        if (started) {
            val readService = GattService(context, serviceUUIDString)
            gattServer.addService(readService)
            return gattServer
        }
        return null
    }

    fun tearDown() {
        gattServer?.stop()
    }

    fun checkServiceAvailable(): Boolean {
        return gattServer?.bluetoothGattServer?.services?.filter {
            it.uuid.toString().equals(serviceUUIDString)
        }?.isNotEmpty() ?: false
    }

}
