package io.bluetrace.opentrace.bluetooth.gatt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import io.bluetrace.opentrace.BuildConfig
import java.util.*
import kotlin.properties.Delegates

class GattService constructor(val context: Context, serviceUUIDString: String) {

    private var serviceUUID = UUID.fromString(serviceUUIDString)

    var gattService: BluetoothGattService by Delegates.notNull()

    private var characteristicV2: BluetoothGattCharacteristic

    init {
        gattService = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        characteristicV2 = BluetoothGattCharacteristic(
            UUID.fromString(BuildConfig.V2_CHARACTERISTIC_ID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        gattService.addCharacteristic(characteristicV2)
    }
}
