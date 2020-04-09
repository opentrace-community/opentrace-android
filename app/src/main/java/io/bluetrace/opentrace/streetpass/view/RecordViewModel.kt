package io.bluetrace.opentrace.streetpass.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecord
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecordDatabase
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecordRepository

class RecordViewModel(app: Application) : AndroidViewModel(app) {

    private var repo: StreetPassRecordRepository

    var allRecords: LiveData<List<StreetPassRecord>>

    init {
        val recordDao = StreetPassRecordDatabase.getDatabase(app).recordDao()
        repo = StreetPassRecordRepository(recordDao)
        allRecords = repo.allRecords
    }


}
