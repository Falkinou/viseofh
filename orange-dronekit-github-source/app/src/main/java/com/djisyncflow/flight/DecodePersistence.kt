package com.djisyncflow.flight

import com.djisyncflow.data.LogFileDao

suspend fun LogFileDao.saveDecodeResult(logId: Long, decoded: DecodedFlightLog) {
    updateDecodeResult(
        id = logId,
        decodedAtMillis = System.currentTimeMillis(),
        decodeStatus = decoded.status,
        decodeError = decoded.error,
        djiLogVersion = decoded.djiLogVersion,
        flightStartTimeMillis = decoded.flightStartTimeMillis,
        flightDurationSeconds = decoded.flightDurationSeconds,
        totalDistanceMeters = decoded.totalDistanceMeters,
        maxHeightMeters = decoded.maxHeightMeters,
        maxHorizontalSpeedMetersPerSecond = decoded.maxHorizontalSpeedMetersPerSecond,
        maxVerticalSpeedMetersPerSecond = decoded.maxVerticalSpeedMetersPerSecond,
        takeoffAltitudeMeters = decoded.takeoffAltitudeMeters,
        homeLatitude = decoded.homeLatitude,
        homeLongitude = decoded.homeLongitude,
        productType = decoded.productType,
        aircraftName = decoded.aircraftName,
        aircraftSerial = decoded.aircraftSerial,
        cameraSerial = decoded.cameraSerial,
        rcSerial = decoded.rcSerial,
        batterySerial = decoded.batterySerial,
        appPlatform = decoded.appPlatform,
        appVersion = decoded.appVersion,
        recordLineCount = decoded.recordLineCount,
        trajectoryPointCount = decoded.trajectoryPointCount,
        trajectoryStartLatitude = decoded.trajectoryStartLatitude,
        trajectoryStartLongitude = decoded.trajectoryStartLongitude,
        trajectoryEndLatitude = decoded.trajectoryEndLatitude,
        trajectoryEndLongitude = decoded.trajectoryEndLongitude,
        trajectoryPoints = decoded.trajectoryPoints,
    )
}
