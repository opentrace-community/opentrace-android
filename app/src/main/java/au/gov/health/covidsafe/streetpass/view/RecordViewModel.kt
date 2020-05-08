package au.gov.health.covidsafe.streetpass.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecord
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecordDatabase
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecordRepository

class RecordViewModel(app: Application) : AndroidViewModel(app) {

    private var repo: StreetPassRecordRepository

    var allRecords: LiveData<List<StreetPassRecord>>

    init {
        val recordDao = StreetPassRecordDatabase.getDatabase(app).recordDao()
        repo = StreetPassRecordRepository(recordDao)
        allRecords = repo.allRecords
    }


}