package com.djisyncflow.flight

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class DjiKeychainRequest(
    val version: Int,
    val department: Int,
    val keychainGroups: List<List<DjiEncodedKeychain>>,
) {
    fun toJson(): JSONObject =
        JSONObject()
            .put("version", version)
            .put("department", department)
            .put(
                "keychainsArray",
                JSONArray(
                    keychainGroups.map { group ->
                        JSONArray(
                            group.map { keychain ->
                                JSONObject()
                                    .put("featurePoint", keychain.featurePoint)
                                    .put("aesCiphertext", keychain.aesCiphertext)
                            },
                        )
                    },
                ),
            )
}

data class DjiEncodedKeychain(
    val featurePoint: String,
    val aesCiphertext: String,
)

data class DjiKeychainResult(
    val available: Boolean,
    val message: String,
    val featureCount: Int = 0,
    val keychainGroups: List<List<DjiDecodedKeychain>> = emptyList(),
)

data class DjiDecodedKeychain(
    val featurePoint: String,
    val aesKey: ByteArray,
    val aesIv: ByteArray,
)

class DjiKeychainClient {
    fun fetch(apiKey: String, request: DjiKeychainRequest): DjiKeychainResult {
        runCatching { sendParserReport(apiKey, request.version) }

        val connection = (URL(KEYCHAIN_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            setRequestProperty("Api-Key", apiKey)
            setRequestProperty("User-Agent", "OrangeDroneKit/1.10")
        }

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(request.toJson().toString())
        }

        val responseCode = connection.responseCode
        val body = runCatching {
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }.getOrDefault("")

        if (responseCode !in 200..299) {
            return DjiKeychainResult(
                available = false,
                message = djiHttpErrorMessage(responseCode, body),
            )
        }

        val json = JSONObject(body)
        val result = json.optJSONObject("result")
        val resultCode = result?.optInt("code", -1) ?: -1
        if (resultCode != 0) {
            return DjiKeychainResult(
                available = false,
                message = result?.optString("msg").orEmpty().ifBlank { "API DJI refuse la keychain" },
            )
        }

        val data = json.optJSONArray("data")
        val groups = mutableListOf<List<DjiDecodedKeychain>>()
        if (data != null) {
            for (groupIndex in 0 until data.length()) {
                val group = data.optJSONArray(groupIndex) ?: continue
                val decodedGroup = mutableListOf<DjiDecodedKeychain>()
                for (entryIndex in 0 until group.length()) {
                    val entry = group.optJSONObject(entryIndex) ?: continue
                    val featurePoint = entry.optString("featurePoint")
                    val aesKey = entry.optString("aesKey")
                    val aesIv = entry.optString("aesIv")
                    if (featurePoint.isNotBlank() && aesKey.isNotBlank() && aesIv.isNotBlank()) {
                        decodedGroup.add(
                            DjiDecodedKeychain(
                                featurePoint = featurePoint,
                                aesKey = Base64.decode(aesKey, Base64.DEFAULT),
                                aesIv = Base64.decode(aesIv, Base64.DEFAULT),
                            ),
                        )
                    }
                }
                if (decodedGroup.isNotEmpty()) groups.add(decodedGroup)
            }
        }
        val featureCount = groups.sumOf { it.size }

        return DjiKeychainResult(
            available = featureCount > 0,
            message = if (featureCount > 0) {
                "Keychain DJI recuperee ($featureCount cle(s))"
            } else {
                "API DJI OK, mais aucune keychain retournee"
            },
            featureCount = featureCount,
            keychainGroups = groups,
        )
    }

    companion object {
        private const val KEYCHAIN_ENDPOINT = "https://dev.dji.com/openapi/v1/flight-records/keychains"
        private const val REPORT_ENDPOINT = "https://statistical-report.djiservice.org/api/report/web"
        private const val REPORT_APP_ID = "572918"
        private const val REPORT_APP_KEY = "VnvGg8ApqcbyFrc"
    }

    private fun sendParserReport(apiKey: String, version: Int) {
        val reportBody = JSONArray()
            .put(
                JSONObject()
                    .put("version", version)
                    .put("sdk_key", apiKey)
                    .put("type", "StartParser"),
            )
            .toString()
        val connection = (URL(REPORT_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("appid", REPORT_APP_ID)
            setRequestProperty("sign", hmacMd5Hex(REPORT_APP_KEY, reportBody))
            setRequestProperty("Content-Type", "application/json;charset=UTF-8")
        }
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(reportBody)
        }
        runCatching {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.close()
        }
        connection.disconnect()
    }

    private fun hmacMd5Hex(key: String, body: String): String {
        val mac = Mac.getInstance("HmacMD5")
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacMD5"))
        return mac.doFinal(body.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}

private fun djiHttpErrorMessage(responseCode: Int, body: String): String {
    val djiMessage = runCatching {
        JSONObject(body).optJSONObject("result")?.optString("msg").orEmpty()
    }.getOrDefault("")
    val cleanBody = djiMessage.ifBlank { body }
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(360)
    val detail = cleanBody.ifBlank { "aucun détail retourné" }
    return when (responseCode) {
        401 -> "Open API DJI refusée (401) : clé absente ou invalide. Détail : $detail"
        403 -> "Open API DJI refusée (403) : vérifier que la clé vient bien d'une app Open API FlightRecord et que l'accès est validé. Détail : $detail"
        else -> "Open API DJI HTTP $responseCode. Détail : $detail"
    }
}

object DjiKeychainRequestExtractor {
    fun extract(bytes: ByteArray): DjiKeychainRequest? {
        if (bytes.size < PREFIX_SIZE || bytes[10].toInt().and(0xff) < 13) return null

        var cursor = PREFIX_SIZE
        val firstAuxLength = readUShortLE(bytes, cursor + 1)
        cursor += 3 + firstAuxLength
        if (cursor + 7 >= bytes.size || bytes[cursor] != 1.toByte()) return null

        val secondAuxLength = readUShortLE(bytes, cursor + 1)
        val version = readUShortLE(bytes, cursor + 3)
        val department = bytes.getOrNull(cursor + 5)?.toInt()?.and(0xff) ?: 3
        cursor += 3 + secondAuxLength

        val recordsOffset = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).long.toInt()
            .takeIf { it in 1 until bytes.size }
            ?: cursor

        val groups = mutableListOf(mutableListOf<DjiEncodedKeychain>())
        var position = recordsOffset
        var scanned = 0
        while (position + 4 < bytes.size && scanned < MAX_RECORDS_TO_SCAN) {
            val recordType = bytes[position].toInt().and(0xff)
            val length = readUShortLE(bytes, position + 1)
            if (length <= 2 || length > MAX_RECORD_LENGTH || position + 3 + length >= bytes.size) {
                position++
                scanned++
                continue
            }

            when (recordType) {
                KEY_STORAGE_RECORD -> {
                    val decoded = decodeXorRecord(bytes, position + 3, recordType, length)
                    val featureId = readUShortLE(decoded, 0)
                    val dataLength = readUShortLE(decoded, 2)
                    val data = decoded.copyOfRangeSafe(4, 4 + dataLength)
                    val featurePoint = featurePointName(featureId)
                    if (featurePoint != null && data.isNotEmpty()) {
                        groups.last().add(
                            DjiEncodedKeychain(
                                featurePoint = featurePoint,
                                aesCiphertext = Base64.encodeToString(data, Base64.NO_WRAP),
                            ),
                        )
                    }
                }
                KEY_STORAGE_RECOVER_RECORD -> {
                    if (groups.last().isNotEmpty()) groups.add(mutableListOf())
                }
            }

            position += 3 + length + 1
            scanned++
        }

        val cleanedGroups = groups.filter { it.isNotEmpty() }
        if (cleanedGroups.isEmpty()) return null

        return DjiKeychainRequest(
            version = version,
            department = department,
            keychainGroups = cleanedGroups,
        )
    }

    private fun decodeXorRecord(bytes: ByteArray, start: Int, recordType: Int, length: Int): ByteArray {
        if (start >= bytes.size || length <= 1) return ByteArray(0)
        val firstByte = bytes[start].toInt().and(0xff)
        val key = xorKey(firstByte, recordType)
        val decoded = ByteArray(length - 1)
        for (index in decoded.indices) {
            decoded[index] = (bytes[start + 1 + index].toInt() xor key[index % key.size].toInt()).toByte()
        }
        return decoded
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

    private fun readUShortLE(bytes: ByteArray, offset: Int): Int {
        if (offset + 1 >= bytes.size) return 0
        return (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
    }

    private fun ByteArray.copyOfRangeSafe(fromIndex: Int, toIndex: Int): ByteArray {
        if (fromIndex < 0 || fromIndex >= size || toIndex <= fromIndex) return ByteArray(0)
        return copyOfRange(fromIndex, toIndex.coerceAtMost(size))
    }

    private fun featurePointName(id: Int): String? =
        when (id) {
            1 -> "FR_Standardization_Feature_Base_1"
            2 -> "FR_Standardization_Feature_Vision_2"
            3 -> "FR_Standardization_Feature_Waypoint_3"
            5 -> "FR_Standardization_Feature_AirLink_5"
            6 -> "FR_Standardization_Feature_AfterSales_6"
            7 -> "FR_Standardization_Feature_DJIFlyCustom_7"
            9 -> "FR_Standardization_Feature_FlightHub_9"
            10 -> "FR_Standardization_Feature_Gimbal_10"
            11 -> "FR_Standardization_Feature_RC_11"
            12 -> "FR_Standardization_Feature_Camera_12"
            13 -> "FR_Standardization_Feature_Battery_13"
            14 -> "FR_Standardization_Feature_FlySafe_14"
            else -> null
        }

    private const val PREFIX_SIZE = 100
    private const val KEY_STORAGE_RECORD = 56
    private const val KEY_STORAGE_RECOVER_RECORD = 50
    private const val MAX_RECORD_LENGTH = 10_000
    private const val MAX_RECORDS_TO_SCAN = 80_000
    private val CRC64_JONES_POLY: Long = java.lang.Long.parseUnsignedLong("95AC9329AC4BC9B5", 16)
}
