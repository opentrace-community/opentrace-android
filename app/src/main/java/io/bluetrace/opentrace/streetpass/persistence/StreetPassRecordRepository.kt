package io.bluetrace.opentrace.streetpass.persistence

import androidx.lifecycle.LiveData

class StreetPassRecordRepository(private val recordDao: StreetPassRecordDao) {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRecords: LiveData<List<StreetPassRecord>> = recordDao.getRecords()

    suspend fun insert(word: StreetPassRecord) {
        recordDao.insert(word)
    }
}
