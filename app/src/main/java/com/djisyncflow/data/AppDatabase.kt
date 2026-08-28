package com.djisyncflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LogFileEntity::class, ActivityEventEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logFileDao(): LogFileDao
    abstract fun activityEventDao(): ActivityEventDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS activity_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        level TEXT NOT NULL,
                        message TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN decodedAtMillis INTEGER")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN decodeStatus TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN decodeError TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN djiLogVersion INTEGER")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN flightStartTimeMillis INTEGER")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN flightDurationSeconds REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN totalDistanceMeters REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN maxHeightMeters REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN maxHorizontalSpeedMetersPerSecond REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN maxVerticalSpeedMetersPerSecond REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN takeoffAltitudeMeters REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN homeLatitude REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN homeLongitude REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN productType TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN aircraftName TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN aircraftSerial TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN cameraSerial TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN rcSerial TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN batterySerial TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN appPlatform TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN appVersion TEXT")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN recordLineCount INTEGER")
            }
        }

        private val migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryPointCount INTEGER")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryStartLatitude REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryStartLongitude REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryEndLatitude REAL")
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryEndLongitude REAL")
            }
        }

        private val migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flight_logs ADD COLUMN trajectoryPoints TEXT")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dji-syncflow.db",
                )
                    .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5)
                    .build()
                    .also { instance = it }
            }
    }
}
