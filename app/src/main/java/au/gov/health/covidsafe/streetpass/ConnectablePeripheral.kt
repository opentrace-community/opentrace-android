package au.gov.health.covidsafe.streetpass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConnectablePeripheral(
    var transmissionPower: Int?,
    var rssi: Int
) : Parcelable

@Parcelize
data class PeripheralDevice(
    val modelP: String,
    val address: String?
) : Parcelable

@Parcelize
data class CentralDevice(
    val modelC: String,
    val address: String?
) : Parcelable

@Parcelize
data class ConnectionRecord(
    val version: Int,

    val msg: String,
    val org: String,

    val peripheral: PeripheralDevice,
    val central: CentralDevice,

    var rssi: Int,
    var txPower: Int?
) : Parcelable {
    override fun toString(): String {
        return "Central ${central.modelC} - ${central.address} ---> Peripheral ${peripheral.modelP} - ${peripheral.address}"
    }
}
