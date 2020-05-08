package au.gov.health.covidsafe.streetpass.view

import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecord

class StreetPassRecordViewModel(record: StreetPassRecord, val number: Int) {
    val version = record.v
    val modelC = record.modelC
    val modelP = record.modelP
    val msg = record.msg
    val timeStamp = record.timestamp
    val rssi = record.rssi
    val transmissionPower = record.txPower
    val org = record.org

    constructor(record: StreetPassRecord) : this(record, 1)
}