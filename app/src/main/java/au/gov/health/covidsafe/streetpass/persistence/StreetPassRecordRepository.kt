package au.gov.health.covidsafe.streetpass.persistence

import androidx.lifecycle.LiveData

class StreetPassRecordRepository(recordDao: StreetPassRecordDao) {
    val allRecords: LiveData<List<StreetPassRecord>> = recordDao.getRecords()

}