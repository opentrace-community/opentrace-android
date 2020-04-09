package io.bluetrace.opentrace.protocol

import io.bluetrace.opentrace.streetpass.ConnectionRecord

open class BlueTraceProtocol(
    val versionInt: Int,
    val central: CentralInterface,
    val peripheral: PeripheralInterface
)

interface PeripheralInterface {
    fun prepareReadRequestData(protocolVersion: Int): ByteArray
    //needs to be in try-catch in case of parse failure
    fun processWriteRequestDataReceived(
        dataWritten: ByteArray,
        centralAddress: String
    ): ConnectionRecord?
}

interface CentralInterface {
    fun prepareWriteRequestData(protocolVersion: Int, rssi: Int, txPower: Int?): ByteArray
    //needs to be in try-catch in case of parse failure
    fun processReadRequestDataReceived(
        dataRead: ByteArray,
        peripheralAddress: String,
        rssi: Int,
        txPower: Int?
    ): ConnectionRecord?
}
