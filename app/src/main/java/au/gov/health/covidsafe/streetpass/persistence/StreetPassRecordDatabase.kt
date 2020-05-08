package au.gov.health.covidsafe.streetpass.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import au.gov.health.covidsafe.status.persistence.StatusRecord
import au.gov.health.covidsafe.status.persistence.StatusRecordDao


@Database(
        entities = [StreetPassRecord::class, StatusRecord::class],
    version = 2,
    exportSchema = true
)
abstract class StreetPassRecordDatabase : RoomDatabase() {

    abstract fun recordDao(): StreetPassRecordDao
    abstract fun statusDao(): StatusRecordDao

    companion object {
        @Volatile
        private var INSTANCE: StreetPassRecordDatabase? = null

        fun getDatabase(context: Context): StreetPassRecordDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    StreetPassRecordDatabase::class.java,
                    "record_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //adding of status table
                database.execSQL("CREATE TABLE IF NOT EXISTS `status_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `msg` TEXT NOT NULL)")

                if (!isFieldExist(database, "record_table", "v")) {
                    database.execSQL("ALTER TABLE `record_table` ADD COLUMN `v` INTEGER NOT NULL DEFAULT 1")
                }

                if (!isFieldExist(database, "record_table", "org")) {
                    database.execSQL("ALTER TABLE `record_table` ADD COLUMN `org` TEXT NOT NULL DEFAULT 'AU_DTA'")
                }

            }
        }

        fun isFieldExist(db: SupportSQLiteDatabase, tableName: String, fieldName: String): Boolean {
            var isExist = false
            val res =
                db.query("PRAGMA table_info($tableName)", null)
            res.moveToFirst()
            do {
                val currentColumn = res.getString(1)
                if (currentColumn == fieldName) {
                    isExist = true
                }
            } while (res.moveToNext())
            return isExist
        }
    }

}
