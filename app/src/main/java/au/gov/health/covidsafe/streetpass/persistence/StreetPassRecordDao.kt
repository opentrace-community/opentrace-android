package au.gov.health.covidsafe.streetpass.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface StreetPassRecordDao {

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    fun getRecords(): LiveData<List<StreetPassRecord>>

    @Query("SELECT * from record_table ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentRecord(): LiveData<StreetPassRecord?>

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    fun getCurrentRecords(): List<StreetPassRecord>

    @Query("DELETE FROM record_table WHERE timestamp <= :timeInMs")
    fun deleteDataOlder(timeInMs: Long): Int

    @Query("DELETE FROM record_table")
    fun nukeDb()

    @RawQuery
    fun getRecordsViaQuery(query: SupportSQLiteQuery): List<StreetPassRecord>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: StreetPassRecord)

}