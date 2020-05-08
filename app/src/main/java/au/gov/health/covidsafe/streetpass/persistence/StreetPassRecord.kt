package au.gov.health.covidsafe.streetpass.persistence

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "record_table")
class StreetPassRecord(
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

    override fun toString(): String {
        return "StreetPassRecord(v=$v, msg='$msg', org='$org', modelP='$modelP', modelC='$modelC', rssi=$rssi, txPower=$txPower, id=$id, timestamp=$timestamp)"
    }

}
