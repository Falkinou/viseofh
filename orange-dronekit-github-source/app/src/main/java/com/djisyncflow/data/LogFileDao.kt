package com.djisyncflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogFileDao {
    @Query("SELECT * FROM flight_logs ORDER BY detectedAtMillis DESC")
    fun observeAll(): Flow<List<LogFileEntity>>

    @Query("SELECT * FROM flight_logs WHERE id = :id")
    suspend fun getById(id: Long): LogFileEntity?

    @Query("SELECT * FROM flight_logs WHERE status IN (:statuses) ORDER BY detectedAtMillis ASC")
    suspend fun getRetryable(statuses: List<String> = listOf(LogStatus.PENDING, LogStatus.ERROR)): List<LogFileEntity>

    @Query("SELECT * FROM flight_logs WHERE decodedAtMillis IS NULL ORDER BY detectedAtMillis DESC")
    suspend fun getUndecoded(): List<LogFileEntity>

    @Query("SELECT COUNT(*) FROM flight_logs WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: LogFileEntity): Long

    @Query(
        """
        UPDATE flight_logs
        SET status = :status,
            sentAtMillis = NULL,
            lastError = NULL
        WHERE status = :currentStatus
        """,
    )
    suspend fun resetStatus(currentStatus: String = LogStatus.ERROR, status: String = LogStatus.PENDING): Int

    @Query(
        """
        UPDATE flight_logs
        SET status = :status,
            sentAtMillis = :sentAtMillis,
            lastError = :lastError
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(
        id: Long,
        status: String,
        sentAtMillis: Long?,
        lastError: String?,
    )

    @Query(
        """
        UPDATE flight_logs
        SET decodedAtMillis = :decodedAtMillis,
            decodeStatus = :decodeStatus,
            decodeError = :decodeError,
            djiLogVersion = :djiLogVersion,
            flightStartTimeMillis = :flightStartTimeMillis,
            flightDurationSeconds = :flightDurationSeconds,
            totalDistanceMeters = :totalDistanceMeters,
            maxHeightMeters = :maxHeightMeters,
            maxHorizontalSpeedMetersPerSecond = :maxHorizontalSpeedMetersPerSecond,
            maxVerticalSpeedMetersPerSecond = :maxVerticalSpeedMetersPerSecond,
            takeoffAltitudeMeters = :takeoffAltitudeMeters,
            homeLatitude = :homeLatitude,
            homeLongitude = :homeLongitude,
            productType = :productType,
            aircraftName = :aircraftName,
            aircraftSerial = :aircraftSerial,
            cameraSerial = :cameraSerial,
            rcSerial = :rcSerial,
            batterySerial = :batterySerial,
            appPlatform = :appPlatform,
            appVersion = :appVersion,
            recordLineCount = :recordLineCount,
            trajectoryPointCount = :trajectoryPointCount,
            trajectoryStartLatitude = :trajectoryStartLatitude,
            trajectoryStartLongitude = :trajectoryStartLongitude,
            trajectoryEndLatitude = :trajectoryEndLatitude,
            trajectoryEndLongitude = :trajectoryEndLongitude,
            trajectoryPoints = :trajectoryPoints
        WHERE id = :id
        """,
    )
    suspend fun updateDecodeResult(
        id: Long,
        decodedAtMillis: Long,
        decodeStatus: String,
        decodeError: String?,
        djiLogVersion: Int?,
        flightStartTimeMillis: Long?,
        flightDurationSeconds: Double?,
        totalDistanceMeters: Double?,
        maxHeightMeters: Double?,
        maxHorizontalSpeedMetersPerSecond: Double?,
        maxVerticalSpeedMetersPerSecond: Double?,
        takeoffAltitudeMeters: Double?,
        homeLatitude: Double?,
        homeLongitude: Double?,
        productType: String?,
        aircraftName: String?,
        aircraftSerial: String?,
        cameraSerial: String?,
        rcSerial: String?,
        batterySerial: String?,
        appPlatform: String?,
        appVersion: String?,
        recordLineCount: Int?,
        trajectoryPointCount: Int?,
        trajectoryStartLatitude: Double?,
        trajectoryStartLongitude: Double?,
        trajectoryEndLatitude: Double?,
        trajectoryEndLongitude: Double?,
        trajectoryPoints: String?,
    )
}
