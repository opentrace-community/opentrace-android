package au.gov.health.covidsafe.bluetooth.gatt

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.streetpass.PeripheralDevice

const val ACTION_RECEIVED_STREETPASS =
    "${BuildConfig.APPLICATION_ID}.ACTION_RECEIVED_STREETPASS"
const val ACTION_RECEIVED_STATUS =
    "${BuildConfig.APPLICATION_ID}.ACTION_RECEIVED_STATUS"

const val DEVICE_ADDRESS = "${BuildConfig.APPLICATION_ID}.DEVICE_ADDRESS"
const val CONNECTION_DATA = "${BuildConfig.APPLICATION_ID}.CONNECTION_DATA"

const val STREET_PASS = "${BuildConfig.APPLICATION_ID}.STREET_PASS"
const val STATUS = "${BuildConfig.APPLICATION_ID}.STATUS"

const val ACTION_DEVICE_PROCESSED = "${BuildConfig.APPLICATION_ID}.ACTION_DEVICE_PROCESSED"
const val ACTION_GATT_DISCONNECTED = "${BuildConfig.APPLICATION_ID}.ACTION_GATT_DISCONNECTED"

class ReadRequestPayload(
    val v: Int,
    val msg: String,
    val org: String,
    peripheral: PeripheralDevice
) {
    val modelP = peripheral.modelP

    fun getPayload(): ByteArray {
        return gson.toJson(this).toByteArray(Charsets.UTF_8)
    }

    companion object {
        val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

        fun createReadRequestPayload(dataBytes: ByteArray) : ReadRequestPayload {
            val dataString = String(dataBytes, Charsets.UTF_8)
            return gson.fromJson(dataString, ReadRequestPayload::class.java)
        }
    }
}

class WriteRequestPayload(
    val v: Int,
    val msg: String,
    val org: String,
    val modelC: String,
    val rssi: Int,
    val txPower: Int?
) {

    fun getPayload(): ByteArray {
        return gson.toJson(this).toByteArray(Charsets.UTF_8)
    }

    companion object {
        val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

        fun createReadRequestPayload(dataBytes: ByteArray) : WriteRequestPayload {
            val dataString = String(dataBytes, Charsets.UTF_8)
            return gson.fromJson(dataString, WriteRequestPayload::class.java)
        }
    }
}
