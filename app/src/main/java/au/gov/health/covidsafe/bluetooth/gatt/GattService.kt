package au.gov.health.covidsafe.bluetooth.gatt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import java.util.*
import kotlin.properties.Delegates

class GattService constructor(val context: Context, serviceUUIDString: String) {

    private var serviceUUID = UUID.fromString(serviceUUIDString)

    var gattService: BluetoothGattService by Delegates.notNull()

    private var devicePropertyCharacteristic: BluetoothGattCharacteristic by Delegates.notNull()

    init {
        gattService = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        devicePropertyCharacteristic = BluetoothGattCharacteristic(
            serviceUUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        gattService.addCharacteristic(devicePropertyCharacteristic)
    }

    fun setValue(value: String) {
        setValue(value.toByteArray(Charsets.UTF_8))
    }

    fun setValue(value: ByteArray) {
        devicePropertyCharacteristic.value = value
    }
}