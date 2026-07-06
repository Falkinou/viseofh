package com.djisyncflow.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object LogStatus {
    const val PENDING = "En attente"
    const val SENT = "Envoye"
    const val ERROR = "Erreur"
}

@Entity(
    tableName = "flight_logs",
    indices = [Index(value = ["fingerprint"], unique = true)],
)
data class LogFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fingerprint: String,
    val filePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val status: String = LogStatus.PENDING,
    val detectedAtMillis: Long = System.currentTimeMillis(),
    val sentAtMillis: Long? = null,
    val lastError: String? = null,
    val decodedAtMillis: Long? = null,
    val decodeStatus: String? = null,
    val decodeError: String? = null,
    val djiLogVersion: Int? = null,
    val flightStartTimeMillis: Long? = null,
    val flightDurationSeconds: Double? = null,
    val totalDistanceMeters: Double? = null,
    val maxHeightMeters: Double? = null,
    val maxHorizontalSpeedMetersPerSecond: Double? = null,
    val maxVerticalSpeedMetersPerSecond: Double? = null,
    val takeoffAltitudeMeters: Double? = null,
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null,
    val productType: String? = null,
    val aircraftName: String? = null,
    val aircraftSerial: String? = null,
    val cameraSerial: String? = null,
    val rcSerial: String? = null,
    val batterySerial: String? = null,
    val appPlatform: String? = null,
    val appVersion: String? = null,
    val recordLineCount: Int? = null,
    val trajectoryPointCount: Int? = null,
    val trajectoryStartLatitude: Double? = null,
    val trajectoryStartLongitude: Double? = null,
    val trajectoryEndLatitude: Double? = null,
    val trajectoryEndLongitude: Double? = null,
    val trajectoryPoints: String? = null,
)
