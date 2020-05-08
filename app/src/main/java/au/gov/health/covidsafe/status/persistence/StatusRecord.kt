package au.gov.health.covidsafe.status.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_table")
class StatusRecord constructor(

    @ColumnInfo(name = "msg")
    var msg: String
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()
}