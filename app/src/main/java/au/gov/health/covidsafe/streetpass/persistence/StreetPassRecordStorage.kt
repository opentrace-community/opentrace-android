package au.gov.health.covidsafe.streetpass.persistence

import android.content.Context

class StreetPassRecordStorage(val context: Context) {

    private val recordDao = StreetPassRecordDatabase.getDatabase(context).recordDao()

    suspend fun saveRecord(record: StreetPassRecord) {
        recordDao.insert(record)
    }

    fun deleteDataOlderThan(timeInMs: Long): Int {
        return recordDao.deleteDataOlder(timeInMs)
    }

    fun nukeDb() {
        recordDao.nukeDb()
    }

    suspend fun nukeDbAsync() {
        recordDao.nukeDb()
    }

    fun getAllRecords(): List<StreetPassRecord> {
        return recordDao.getCurrentRecords()
    }

}