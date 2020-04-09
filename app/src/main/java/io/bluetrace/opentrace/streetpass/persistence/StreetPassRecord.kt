package io.bluetrace.opentrace.streetpass.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
class StreetPassRecord constructor(
    @ColumnInfo(name = "v")
    var v: Int,

    @ColumnInfo(name = "msg")
    var msg: String,

    @ColumnInfo(name = "org")
    var org: String,

    @ColumnInfo(name = "modelP")
    val modelP: String,

    @ColumnInfo(name = "modelC")
    val modelC: String,

    @ColumnInfo(name = "rssi")
    val rssi: Int,

    @ColumnInfo(name = "txPower")
    val txPower: Int?

) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

}
