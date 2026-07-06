package com.djisyncflow.flight

import android.content.Context
import android.util.Base64
import android.net.Uri
import com.djisyncflow.data.LogFileEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DecodeStatus {
    const val METADATA_DECODED = "Metadonnees decodees"
    const val KEY_REQUIRED = "Metadonnees decodees - cle DJI requise pour trajectoire"
    const val UNSUPPORTED = "Format non reconnu"
    const val ERROR = "Erreur de decodage"
}

data class DecodedFlightLog(
    val status: String,
    val error: String? = null,
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

private data class FlightPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val timeSeconds: Double,
    val speedMetersPerSecond: Double,
    val satellites: Int,
    val batteryPercent: Int,
)

private data class BatterySample(
    val timeSeconds: Double,
    val percent: Int,
)

private data class TrajectorySummary(
    val pointCount: Int,
    val start: FlightPoint,
    val end: FlightPoint,
    val encodedPoints: String,
    val durationSeconds: Double?,
    val totalDistanceMeters: Double?,
    val maxDistanceFromStartMeters: Double?,
    val maxHeightMeters: Double?,
    val maxHorizontalSpeedMetersPerSecond: Double?,
)

class DjiFlightLogDecoder(private val context: Context) {
    private val keychainClient = DjiKeychainClient()

    fun decode(log: LogFileEntity, djiApiKey: String = ""): DecodedFlightLog =
        runCatching {
            val bytes = context.contentResolver.openInputStream(Uri.parse(log.filePath))
                ?.use { it.readBytes() }
                ?: return DecodedFlightLog(
                    status = DecodeStatus.ERROR,
                    error = "Fichier inaccessible depuis Android",
                )

            val decoded = decodeBytes(bytes)
            if (decoded.djiLogVersion != null && decoded.djiLogVersion >= 13 && djiApiKey.isNotBlank()) {
                fetchKeychain(log, bytes, djiApiKey, decoded)
            } else {
                decoded
            }
        }.getOrElse {
            DecodedFlightLog(
                status = DecodeStatus.ERROR,
                error = it.message ?: it.javaClass.simpleName,
            )
        }

    private fun fetchKeychain(
        log: LogFileEntity,
        bytes: ByteArray,
        djiApiKey: String,
        decoded: DecodedFlightLog,
    ): DecodedFlightLog {
        val request = DjiKeychainRequestExtractor.extract(bytes)
            ?: return decoded.copy(
                status = DecodeStatus.KEY_REQUIRED,
                error = "Demande keychain introuvable dans ce FlightRecord",
            )

        val cachedKeychains = readCachedKeychains(log.fingerprint)
        val result = cachedKeychains?.let {
            DjiKeychainResult(
                available = it.isNotEmpty(),
                message = "Keychain DJI chargee depuis le cache local",
                featureCount = it.sumOf { group -> group.size },
                keychainGroups = it,
            )
        } ?: keychainClient.fetch(djiApiKey, request).also {
            if (it.available) writeCachedKeychains(log.fingerprint, it.keychainGroups)
        }
        return if (result.available) {
            val trajectory = decodeTrajectory(bytes, decoded.djiLogVersion ?: request.version, result.keychainGroups)
            decoded.copy(
                status = if (trajectory != null) {
                    "Trajectoire décodée"
                } else {
                    "Keychain DJI récupérée"
                },
                error = if (trajectory != null) {
                    "${result.message}. ${trajectory.pointCount} point(s) GPS exploitable(s)."
                } else {
                    "${result.message}. Aucun point GPS exploitable pour l'instant."
                },
                trajectoryPointCount = trajectory?.pointCount,
                trajectoryStartLatitude = trajectory?.start?.latitude,
                trajectoryStartLongitude = trajectory?.start?.longitude,
                trajectoryEndLatitude = trajectory?.end?.latitude,
                trajectoryEndLongitude = trajectory?.end?.longitude,
                trajectoryPoints = trajectory?.encodedPoints,
                flightDurationSeconds = trajectory?.durationSeconds ?: decoded.flightDurationSeconds,
                totalDistanceMeters = trajectory?.totalDistanceMeters ?: decoded.totalDistanceMeters,
                maxHeightMeters = trajectory?.maxHeightMeters ?: decoded.maxHeightMeters,
                maxHorizontalSpeedMetersPerSecond = trajectory?.maxHorizontalSpeedMetersPerSecond
                    ?: decoded.maxHorizontalSpeedMetersPerSecond,
            )
        } else {
            decoded.copy(
                status = DecodeStatus.KEY_REQUIRED,
                error = result.message,
            )
        }
    }

    fun decodeBytes(bytes: ByteArray): DecodedFlightLog {
        if (bytes.size < DJI_PREFIX_SIZE) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                error = "Fichier trop court pour un FlightRecord DJI",
            )
        }

        val prefix = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val detailsOffset = prefix.long.takeIf { it >= 0 } ?: 0L
        val detailsLength = prefix.short.toInt() and 0xffff
        val version = prefix.get().toInt() and 0xff

        if (version !in 1..40 || detailsLength <= 0) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                djiLogVersion = version.takeIf { it > 0 },
                error = "Prefix DJI absent ou incoherent",
            )
        }

        val details = if (version >= 13) {
            decodeModernDetails(bytes)
        } else {
            val detailStart = if (version < 12) detailsOffset.toInt() else DJI_PREFIX_SIZE
            parseDetails(bytes.copyOfRangeSafe(detailStart, detailStart + detailsLength), version)
        }

        return details.copy(
            djiLogVersion = version,
            status = if (version >= 13) DecodeStatus.KEY_REQUIRED else DecodeStatus.METADATA_DECODED,
        )
    }

    private fun decodeTrajectory(
        bytes: ByteArray,
        version: Int,
        keychainGroups: List<List<DjiDecodedKeychain>>,
    ): TrajectorySummary? {
        if (keychainGroups.isEmpty()) return null
        val recordsOffset = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).long.toInt()
            .takeIf { it in DJI_PREFIX_SIZE until bytes.size }
            ?: return null

        val groups = ArrayDeque(keychainGroups.map { group ->
            group.associateBy(
                keySelector = { it.featurePoint },
                valueTransform = { MutableAesState(it.aesIv.copyOf(), it.aesKey.copyOf()) },
            ).toMutableMap()
        })
        var currentKeychain = groups.removeFirstOrNull() ?: mutableMapOf()
        val points = mutableListOf<FlightPoint>()
        val batterySamples = mutableListOf<BatterySample>()
        var lastFlightTimeSeconds = 0.0
        var lastBatteryPercent: Int? = null
        var position = recordsOffset
        var scanned = 0

        while (position + 4 < bytes.size && scanned < MAX_TRAJECTORY_RECORDS) {
            val recordType = bytes[position].toInt() and 0xff
            val length = readUShortLE(bytes, position + 1)
            val payloadStart = position + 3
            val recordEnd = payloadStart + length
            if (length <= 2 || length > MAX_TRAJECTORY_RECORD_LENGTH || recordEnd >= bytes.size) break

            if (recordType == KEY_STORAGE_RECOVER_RECORD) {
                currentKeychain = groups.removeFirstOrNull() ?: currentKeychain
            }

            val featurePoint = featurePointForRecordType(recordType, version)
            val plaintext = featurePoint
                ?.let { currentKeychain[it] }
                ?.let { state -> decryptRecord(bytes, payloadStart, length, recordType, state) }

            if (plaintext != null) {
                when (recordType) {
                    OSD_RECORD_TYPE -> {
                        parseOsdPoint(plaintext, lastBatteryPercent)?.let { point ->
                            points.add(point)
                            lastFlightTimeSeconds = point.timeSeconds
                        }
                    }
                    CENTER_BATTERY_RECORD_TYPE -> {
                        parseCenterBatteryPercent(plaintext)?.let {
                            lastBatteryPercent = it
                            batterySamples.add(BatterySample(lastFlightTimeSeconds, it))
                        }
                    }
                    SMART_BATTERY_RECORD_TYPE -> {
                        parseSmartBatteryPercent(plaintext)?.let {
                            lastBatteryPercent = it
                            batterySamples.add(BatterySample(lastFlightTimeSeconds, it))
                        }
                    }
                    SMART_BATTERY_GROUP_RECORD_TYPE -> {
                        parseSmartBatteryGroupPercent(plaintext)?.let {
                            lastBatteryPercent = it
                            batterySamples.add(BatterySample(lastFlightTimeSeconds, it))
                        }
                    }
                }
            }

            position = recordEnd + 1
            scanned++
        }

        return points.takeIf { it.isNotEmpty() }?.let {
            val pointsWithBattery = applyBatterySamples(it, batterySamples)
            val sampledTelemetry = sampleTrajectoryPoints(pointsWithBattery)
            TrajectorySummary(
                pointCount = it.size,
                start = pointsWithBattery.first(),
                end = pointsWithBattery.last(),
                encodedPoints = encodeTrajectoryPoints(sampledTelemetry),
                durationSeconds = it.maxOfOrNull { point -> point.timeSeconds }?.takeIf { seconds -> seconds > 0.0 },
                totalDistanceMeters = totalPathDistanceMeters(it).takeIf { meters -> meters > 0.0 },
                maxDistanceFromStartMeters = maxDistanceFromStartMeters(it).takeIf { meters -> meters > 0.0 },
                maxHeightMeters = it.maxOfOrNull { point -> point.altitudeMeters }?.takeIf { height -> height.isFinite() },
                maxHorizontalSpeedMetersPerSecond = it.maxOfOrNull { point -> point.speedMetersPerSecond }
                    ?.takeIf { speed -> speed.isFinite() },
            )
        }
    }

    private fun decryptRecord(
        bytes: ByteArray,
        payloadStart: Int,
        length: Int,
        recordType: Int,
        state: MutableAesState,
    ): ByteArray? {
        if (payloadStart >= bytes.size || length < AES_BLOCK_BYTES + 2) return null
        if (state.iv.size != AES_BLOCK_BYTES || state.key.size != AES_256_KEY_BYTES) return null

        val firstByte = bytes[payloadStart].toInt() and 0xff
        val xorKey = xorKey(firstByte, recordType)
        val encryptedLength = length - 2
        if (encryptedLength < AES_BLOCK_BYTES || payloadStart + 1 + encryptedLength > bytes.size) return null

        val encrypted = ByteArray(encryptedLength)
        for (index in encrypted.indices) {
            encrypted[index] = (bytes[payloadStart + 1 + index].toInt() xor xorKey[index % xorKey.size].toInt()).toByte()
        }
        state.iv = encrypted.copyOfRange(encrypted.size - AES_BLOCK_BYTES, encrypted.size)

        return runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(state.key, "AES"), IvParameterSpec(state.ivBeforeDecrypt))
            cipher.doFinal(encrypted)
        }.getOrNull()
    }

    private fun parseOsdPoint(bytes: ByteArray, batteryPercent: Int?): FlightPoint? {
        if (bytes.size < 44) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val longitude = buffer.getDouble(0).toDegrees().takeIf { it.isFiniteCoordinate(longitude = true) } ?: return null
        val latitude = buffer.getDouble(8).toDegrees().takeIf { it.isFiniteCoordinate(longitude = false) } ?: return null
        if (latitude == 0.0 && longitude == 0.0) return null
        val speedX = buffer.getShort(18).toDouble() / 10.0
        val speedY = buffer.getShort(20).toDouble() / 10.0
        return FlightPoint(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = buffer.getShort(16).toDouble() / 10.0,
            timeSeconds = readUShortLE(bytes, 42).toDouble() / 10.0,
            speedMetersPerSecond = sqrt(speedX * speedX + speedY * speedY).takeIf { it.isFinite() } ?: 0.0,
            satellites = bytes[36].toInt() and 0xff,
            batteryPercent = batteryPercent ?: -1,
        )
    }

    private fun parseCenterBatteryPercent(bytes: ByteArray): Int? =
        bytes.getOrNull(0)?.toInt()?.and(0xff)?.takeIf { it in 1..100 }

    private fun parseSmartBatteryPercent(bytes: ByteArray): Int? =
        bytes.getOrNull(22)?.toInt()?.and(0xff)?.takeIf { it in 1..100 }

    private fun parseSmartBatteryGroupPercent(bytes: ByteArray): Int? {
        if (bytes.size < 2) return null
        return when (bytes[0].toInt() and 0xff) {
            2 -> bytes.getOrNull(25)?.toInt()?.and(0xff)?.takeIf { it in 1..100 }
            else -> null
        }
    }

    private fun applyBatterySamples(
        points: List<FlightPoint>,
        samples: List<BatterySample>,
    ): List<FlightPoint> {
        if (samples.isEmpty()) return points
        val sortedSamples = samples.sortedBy { it.timeSeconds }
        var sampleIndex = 0
        var currentPercent = sortedSamples.first().percent
        return points.map { point ->
            while (
                sampleIndex + 1 < sortedSamples.size &&
                sortedSamples[sampleIndex + 1].timeSeconds <= point.timeSeconds
            ) {
                sampleIndex++
                currentPercent = sortedSamples[sampleIndex].percent
            }
            point.copy(batteryPercent = currentPercent)
        }
    }

    private fun sampleTrajectoryPoints(points: List<FlightPoint>, maxPoints: Int = 900): List<FlightPoint> {
        if (points.size <= maxPoints) return points
        val step = points.size.toDouble() / (maxPoints - 1).coerceAtLeast(1)
        return List(maxPoints) { index ->
            points[(index * step).toInt().coerceIn(points.indices)]
        }
    }

    private fun encodeTrajectoryPoints(points: List<FlightPoint>): String =
        points.joinToString(separator = ";") { point ->
            "%.7f,%.7f,%.1f,%.2f,%d,%.1f,%d".format(
                java.util.Locale.US,
                point.latitude,
                point.longitude,
                point.timeSeconds,
                point.speedMetersPerSecond,
                point.satellites,
                point.altitudeMeters,
                point.batteryPercent,
            )
        }

    private fun totalPathDistanceMeters(points: List<FlightPoint>): Double =
        points.zipWithNext().sumOf { (a, b) ->
            haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
        }

    private fun maxDistanceFromStartMeters(points: List<FlightPoint>): Double {
        val start = points.firstOrNull() ?: return 0.0
        return points.maxOf { point ->
            haversineMeters(start.latitude, start.longitude, point.latitude, point.longitude)
        }
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(rLat1) * cos(rLat2) * sin(dLon / 2) * sin(dLon / 2)
        return earthRadiusMeters * 2 * asin(sqrt(a))
    }

    private fun readCachedKeychains(fingerprint: String): List<List<DjiDecodedKeychain>>? =
        runCatching {
            val file = keychainCacheFile(fingerprint)
            if (!file.exists()) return@runCatching null
            val root = JSONArray(file.readText())
            List(root.length()) { groupIndex ->
                val group = root.getJSONArray(groupIndex)
                List(group.length()) { entryIndex ->
                    val entry = group.getJSONObject(entryIndex)
                    DjiDecodedKeychain(
                        featurePoint = entry.getString("featurePoint"),
                        aesKey = Base64.decode(entry.getString("aesKey"), Base64.DEFAULT),
                        aesIv = Base64.decode(entry.getString("aesIv"), Base64.DEFAULT),
                    )
                }
            }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
        }.getOrNull()

    private fun writeCachedKeychains(fingerprint: String, groups: List<List<DjiDecodedKeychain>>) {
        runCatching {
            val root = JSONArray(
                groups.map { group ->
                    JSONArray(
                        group.map { entry ->
                            JSONObject()
                                .put("featurePoint", entry.featurePoint)
                                .put("aesKey", Base64.encodeToString(entry.aesKey, Base64.NO_WRAP))
                                .put("aesIv", Base64.encodeToString(entry.aesIv, Base64.NO_WRAP))
                        },
                    )
                },
            )
            keychainCacheFile(fingerprint).writeText(root.toString())
        }
    }

    private fun keychainCacheFile(fingerprint: String): File {
        val dir = File(context.filesDir, "dji_keychains").apply { mkdirs() }
        val safeName = fingerprint.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(dir, "$safeName.json")
    }

    private class MutableAesState(
        iv: ByteArray,
        val key: ByteArray,
    ) {
        var iv: ByteArray = iv
            set(value) {
                ivBeforeDecrypt = field
                field = value
            }
        var ivBeforeDecrypt: ByteArray = iv
            private set
    }

    private fun decodeModernDetails(bytes: ByteArray): DecodedFlightLog {
        if (bytes.size <= DJI_PREFIX_SIZE + 4 || bytes[DJI_PREFIX_SIZE] != 0.toByte()) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                error = "Bloc d'information DJI introuvable",
            )
        }

        val blockLength = readUShortLE(bytes, DJI_PREFIX_SIZE + 1)
        val encodedStart = DJI_PREFIX_SIZE + 3
        val encodedEnd = (encodedStart + blockLength).coerceAtMost(bytes.size)
        if (encodedEnd - encodedStart < 8) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                error = "Bloc d'information DJI incomplet",
            )
        }

        val firstByte = bytes[encodedStart].toInt() and 0xff
        val key = xorKey(firstByte, recordType = 0)
        val decoded = ByteArray(encodedEnd - encodedStart - 1)
        for (index in decoded.indices) {
            decoded[index] = (bytes[encodedStart + 1 + index].toInt() xor key[index % key.size].toInt()).toByte()
        }

        if (decoded.size < 3) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                error = "Metadonnees DJI incompletes",
            )
        }

        val infoLength = readUShortLE(decoded, 1)
        val infoStart = 3
        return parseDetails(decoded.copyOfRangeSafe(infoStart, infoStart + infoLength), version = 13)
    }

    private fun parseDetails(infoData: ByteArray, version: Int): DecodedFlightLog {
        if (infoData.size < MIN_DETAILS_SIZE) {
            return DecodedFlightLog(
                status = DecodeStatus.UNSUPPORTED,
                error = "Details DJI trop courts (${infoData.size} octets)",
            )
        }

        val buffer = ByteBuffer.wrap(infoData).order(ByteOrder.LITTLE_ENDIAN)
        val rawDistance = buffer.getFloat(115).toDouble()
        val durationSeconds = buffer.getInt(119).toDouble() / 1000.0
        val maxHorizontalSpeed = buffer.getFloat(127).toDouble()
        val productId = infoData.getOrNull(271)?.toInt()?.and(0xff)
        val aircraftName = readString(infoData, 280, if (version <= 5) 24 else 32)
        val productLabel = aircraftName.ifBlank { productName(productId) }

        return DecodedFlightLog(
            status = DecodeStatus.METADATA_DECODED,
            flightStartTimeMillis = buffer.getLong(91).takeIf { it > 0 },
            homeLongitude = buffer.getDouble(99).takeIf { it.isFiniteCoordinate(longitude = true) },
            homeLatitude = buffer.getDouble(107).takeIf { it.isFiniteCoordinate(longitude = false) },
            totalDistanceMeters = normalizeDistanceMeters(rawDistance, durationSeconds, maxHorizontalSpeed),
            flightDurationSeconds = durationSeconds.takeIf { it >= 0 },
            maxHeightMeters = buffer.getFloat(123).toDouble().takeIf { it.isFinite() },
            maxHorizontalSpeedMetersPerSecond = maxHorizontalSpeed.takeIf { it.isFinite() },
            maxVerticalSpeedMetersPerSecond = buffer.getFloat(131).toDouble().takeIf { it.isFinite() },
            takeoffAltitudeMeters = buffer.getFloat(267).toDouble().takeIf { it.isFinite() },
            productType = productLabel,
            aircraftName = aircraftName.ifBlank { null },
            aircraftSerial = readString(infoData, 312, if (version <= 5) 10 else 16).ifBlank { null },
            cameraSerial = readString(infoData, 328, if (version <= 5) 10 else 16).ifBlank { null },
            rcSerial = readString(infoData, 344, if (version <= 5) 10 else 16).ifBlank { null },
            batterySerial = readString(infoData, 360, if (version <= 5) 10 else 16).ifBlank { null },
            appPlatform = platformName(infoData.getOrNull(376)?.toInt()?.and(0xff)),
            appVersion = infoData.getOrNull(379)?.let {
                "${infoData[377].toInt() and 0xff}.${infoData[378].toInt() and 0xff}.${it.toInt() and 0xff}"
            },
            recordLineCount = buffer.getInt(83).takeIf { it >= 0 },
        )
    }

    private fun xorKey(firstByte: Int, recordType: Int): ByteArray {
        val magic = 0x123456789ABCDEF0L
        val seed = ((firstByte + recordType) and 0xff).toLong()
        val material = (magic * firstByte.toLong()).toLittleEndianBytes()
        return crc64Jones(seed, material).toLittleEndianBytes()
    }

    private fun crc64Jones(seed: Long, data: ByteArray): Long {
        var crc = seed
        for (byte in data) {
            crc = crc xor (byte.toLong() and 0xffL)
            repeat(8) {
                crc = if ((crc and 1L) != 0L) {
                    (crc ushr 1) xor CRC64_JONES_POLY
                } else {
                    crc ushr 1
                }
            }
        }
        return crc
    }

    private fun Long.toLittleEndianBytes(): ByteArray =
        ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(this).array()

    private fun readString(bytes: ByteArray, offset: Int, length: Int): String =
        bytes.copyOfRangeSafe(offset, offset + length)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .toString(Charsets.UTF_8)
            .trim()

    private fun normalizeDistanceMeters(rawDistance: Double, durationSeconds: Double, maxSpeed: Double): Double? {
        if (!rawDistance.isFinite()) return null
        val looksLikeKilometers = rawDistance in 0.001..100.0 && durationSeconds > 60.0 && maxSpeed > 1.0
        return if (looksLikeKilometers) rawDistance * 1000.0 else rawDistance
    }

    private fun ByteArray.copyOfRangeSafe(fromIndex: Int, toIndex: Int): ByteArray {
        if (fromIndex < 0 || fromIndex >= size || toIndex <= fromIndex) return ByteArray(0)
        return copyOfRange(fromIndex, toIndex.coerceAtMost(size))
    }

    private fun readUShortLE(bytes: ByteArray, offset: Int): Int {
        if (offset + 1 >= bytes.size) return 0
        return (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
    }

    private fun Double.isFiniteCoordinate(longitude: Boolean): Boolean =
        isFinite() && if (longitude) this in -180.0..180.0 else this in -90.0..90.0

    private fun Double.toDegrees(): Double = (this * 180.0) / PI

    private fun featurePointForRecordType(recordType: Int, version: Int): String? =
        when (recordType) {
            1, 2, 6, 13, 14, 15, 40, 58, 59, 63 -> "FR_Standardization_Feature_Base_1"
            3 -> if (version == 13) "FR_Standardization_Feature_Base_1" else "FR_Standardization_Feature_Gimbal_10"
            4, 11, 29, 33, 62 -> if (version == 13) "FR_Standardization_Feature_Base_1" else "FR_Standardization_Feature_RC_11"
            5, 9, 10, 20, 24, 30, 54 -> "FR_Standardization_Feature_DJIFlyCustom_7"
            7, 8 -> if (version == 13) "FR_Standardization_Feature_Base_1" else "FR_Standardization_Feature_Battery_13"
            12, 16, 19, 26, 27 -> "FR_Standardization_Feature_AfterSales_6"
            17, 18 -> "FR_Standardization_Feature_Vision_2"
            21, 41, 43, 44, 45, 46, 47, 48 -> "FR_Standardization_Feature_Agriculture_4"
            22 -> if (version == 13) "FR_Standardization_Feature_AfterSales_6" else "FR_Standardization_Feature_Battery_13"
            25 -> if (version == 13) "FR_Standardization_Feature_Base_1" else "FR_Standardization_Feature_Camera_12"
            28, 51, 52 -> if (version == 13) "FR_Standardization_Feature_AfterSales_6" else "FR_Standardization_Feature_FlySafe_14"
            31, 32, 34, 35, 36, 38, 39 -> "FR_Standardization_Feature_Waypoint_3"
            49 -> "FR_Standardization_Feature_AirLink_5"
            53 -> if (version == 13) "FR_Standardization_Feature_AfterSales_6" else "FR_Standardization_Feature_FlightHub_9"
            55 -> "FR_Standardization_Feature_Security_15"
            else -> null
        }

    private fun productName(productId: Int?): String? =
        when (productId) {
            70 -> "Matrice 300 RTK"
            116 -> "Matrice 30"
            170 -> "Matrice 350 RTK"
            178 -> "Matrice 4D"
            else -> productId?.let { "Produit DJI $it" }
        }

    private fun platformName(platformId: Int?): String? =
        when (platformId) {
            1 -> "iOS"
            2 -> "Android"
            6 -> "DJI Fly"
            10 -> "Windows"
            11 -> "Mac"
            12 -> "Linux"
            null -> null
            else -> "Plateforme $platformId"
        }

    private companion object {
        const val DJI_PREFIX_SIZE = 100
        const val MIN_DETAILS_SIZE = 380
        const val OSD_RECORD_TYPE = 1
        const val CENTER_BATTERY_RECORD_TYPE = 7
        const val SMART_BATTERY_RECORD_TYPE = 8
        const val SMART_BATTERY_GROUP_RECORD_TYPE = 22
        const val KEY_STORAGE_RECOVER_RECORD = 50
        const val AES_BLOCK_BYTES = 16
        const val AES_256_KEY_BYTES = 32
        const val MAX_TRAJECTORY_RECORD_LENGTH = 20_000
        const val MAX_TRAJECTORY_RECORDS = 250_000
        val CRC64_JONES_POLY: Long = java.lang.Long.parseUnsignedLong("95AC9329AC4BC9B5", 16)
    }
}
