package io.bluetrace.opentrace.status.persistence

import android.content.Context
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecordDatabase

class StatusRecordStorage(val context: Context) {

    val statusDao = StreetPassRecordDatabase.getDatabase(context).statusDao()

    suspend fun saveRecord(record: StatusRecord) {
        statusDao.insert(record)
    }

    fun nukeDb() {
        statusDao.nukeDb()
    }

    fun getAllRecords(): List<StatusRecord> {
        return statusDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        statusDao.purgeOldRecords(before)
    }
}
