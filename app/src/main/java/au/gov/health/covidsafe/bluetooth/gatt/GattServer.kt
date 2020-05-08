package au.gov.health.covidsafe.bluetooth.gatt

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.content.Context
import au.gov.health.covidsafe.TracerApp
import au.gov.health.covidsafe.Utils
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.streetpass.CentralDevice
import au.gov.health.covidsafe.streetpass.ConnectionRecord
import java.util.*
import kotlin.properties.Delegates

class GattServer constructor(val context: Context, serviceUUIDString: String) {

    private val TAG = "GattServer"
    private var bluetoothManager: BluetoothManager by Delegates.notNull()

    private var serviceUUID: UUID by Delegates.notNull()
    var bluetoothGattServer: BluetoothGattServer? = null

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.serviceUUID = UUID.fromString(serviceUUIDString)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        val writeDataPayload: MutableMap<String, ByteArray> = HashMap()
        val readPayloadMap: MutableMap<String, ByteArray> = HashMap()

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    CentralLog.i(TAG, "${device?.address} Connected to local GATT server")
                    device?.let {
                        val b = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
                            .contains(device)
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    CentralLog.i(TAG, "${device?.address} Disconnected from local GATT server.")
                    device?.let {
                        Utils.broadcastDeviceDisconnected(context, device)
                    }

                }

                else -> {
                    CentralLog.i(TAG, "Connection status: $newState - ${device?.address}")
                }
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {

            device?.let {

                CentralLog.i(TAG, "onCharacteristicReadRequest from ${device.address}")

                if (serviceUUID == characteristic?.uuid) {

                    if (Utils.bmValid(context)) {
                        val base = readPayloadMap.getOrPut(device.address, {
                            ReadRequestPayload(
                                v = TracerApp.protocolVersion,
                                msg = TracerApp.thisDeviceMsg(),
                                org = TracerApp.ORG,
                                peripheral = TracerApp.asPeripheralDevice()
                            ).getPayload()
                        })

                        val value = base.copyOfRange(offset, base.size)

                        CentralLog.i(
                            TAG,
                            "onCharacteristicReadRequest from ${device.address} - $requestId- $offset - ${String(
                                value,
                                Charsets.UTF_8
                            )}"
                        )

                        bluetoothGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)
                    } else {
                        CentralLog.i(
                            TAG,
                            "onCharacteristicReadRequest from ${device.address} - $requestId- $offset - BM Expired"
                        )
                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            GATT_FAILURE,
                            0,
                            ByteArray(0)
                        )
                    }
                } else {
                    CentralLog.i(TAG, "incorrect serviceUUID from ${device.address}")
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, null)
                }
            }

            if (device == null) {
                CentralLog.i(TAG, "No device")
            }

        }


        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {


            device?.let {
                CentralLog.i(
                    TAG,
                    "onCharacteristicWriteRequest - ${device.address} - preparedWrite: $preparedWrite"
                )

                CentralLog.i(
                    TAG,
                    "onCharacteristicWriteRequest from ${device.address} - $requestId - $offset"
                )

                if (serviceUUID == characteristic.uuid) {
                    var valuePassed = ""
                    value?.let {
                        valuePassed = String(value, Charsets.UTF_8)
                    }
                    CentralLog.i(
                        TAG,
                        "onCharacteristicWriteRequest from ${device.address} - $valuePassed"
                    )
                    if (value != null) {
                        var dataBuffer = writeDataPayload[device.address]

                        if (dataBuffer == null) {
                            dataBuffer = ByteArray(0)
                        }

                        dataBuffer = dataBuffer.plus(value)
                        writeDataPayload[device.address] = dataBuffer

                        CentralLog.i(
                            TAG,
                            "Accumulated characteristic: ${String(
                                dataBuffer,
                                Charsets.UTF_8
                            )}"
                        )

                        if (responseNeeded) {
                            CentralLog.i(TAG, "Sending response offset: ${dataBuffer.size}")
                            bluetoothGattServer?.sendResponse(
                                device,
                                requestId,
                                GATT_SUCCESS,
                                dataBuffer.size,
                                value
                            )
                        }
                    }
                } else {
                    CentralLog.i(TAG, "no data from ${device.address}")
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, null)
                }

                if (!preparedWrite) {
                    CentralLog.i(
                        TAG,
                        "onCharacteristicWriteRequest - ${device.address} - preparedWrite: $preparedWrite"
                    )

                    saveDataSaved(device)
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, null)
                }
            }

            if (device == null) {
                CentralLog.e(TAG, "Write stopped - no device")
            }
        }

        override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            val data = writeDataPayload[device.address]

            data.let { dataBuffer ->

                if (dataBuffer != null) {
                    CentralLog.i(
                        TAG,
                        "onExecuteWrite - $requestId- ${device.address} - ${String(
                            dataBuffer,
                            Charsets.UTF_8
                        )}"
                    )
                    saveDataSaved(device)
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, null)

                } else {
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_FAILURE, 0, null)
                }
            }
        }

        fun saveDataSaved(device: BluetoothDevice) {
            val data = writeDataPayload[device.address]

            data?.let {
                try {
                    val dataWritten = WriteRequestPayload.createReadRequestPayload(data)
                    device.let {
                        val centralDevice: CentralDevice?

                        try {
                            centralDevice = CentralDevice(dataWritten.modelC, device.address)
                            val connectionRecord = ConnectionRecord(
                                version = dataWritten.v,
                                msg = dataWritten.msg,
                                org = dataWritten.org,
                                peripheral = TracerApp.asPeripheralDevice(),
                                central = centralDevice,
                                rssi = dataWritten.rssi,
                                txPower = dataWritten.txPower
                            )

                            Utils.broadcastStreetPassReceived(
                                context,
                                connectionRecord
                            )
                        } catch (e: Throwable) {
                            CentralLog.e(TAG, "caught error here ${e.message}")
                        }
                    }
                } catch (e: Throwable) {
                    CentralLog.e(TAG, "Failed to save write payload - ${e.message}")
                }

                Utils.broadcastDeviceProcessed(context, device.address)
                writeDataPayload.remove(device.address)
                readPayloadMap.remove(device.address)

            }
        }
    }

    fun startServer(): Boolean {

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        bluetoothGattServer?.let {
            it.clearServices()
            return true
        }
        return false
    }

    fun addService(service: GattService) {
        bluetoothGattServer?.addService(service.gattService)
    }

    fun stop() {
        try {
            bluetoothGattServer?.clearServices()
            bluetoothGattServer?.close()
        } catch (e: Throwable) {
            CentralLog.e(TAG, "GATT server can't be closed elegantly ${e.localizedMessage}")
        }
    }

}
