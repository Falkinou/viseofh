package com.djisyncflow.sync

import com.djisyncflow.BuildConfig
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val apkSha256: String,
    val apkSizeBytes: Long,
)

class UpdateChecker(
    private val manifestUrls: List<String> = listOf(PRIMARY_VERSION_URL, LEGACY_VERSION_URL),
    private val manifestLoader: (String) -> String = ::fetchManifest,
    private val manifestParser: ((String) -> UpdateInfo?)? = null,
) {
    fun check(): UpdateInfo? {
        var lastError: Throwable? = null
        for (manifestUrl in manifestUrls) {
            try {
                return (manifestParser ?: ::parseManifest)(manifestLoader(manifestUrl))
            } catch (error: Exception) {
                lastError = error
            }
        }
        throw IllegalStateException("Manifest de mise à jour inaccessible", lastError)
    }

    private fun parseManifest(text: String): UpdateInfo? {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_MANIFEST_BYTES) {
            "Manifest de mise à jour trop volumineux"
        }
        val json = JSONObject(text)
        val versionCode = json.optInt("versionCode", 0)
        val versionName = json.optString("versionName").trim()
        require(versionCode > 0 && versionName.isNotBlank()) {
            "Manifest de mise à jour incomplet"
        }
        if (versionCode <= BuildConfig.VERSION_CODE) return null

        val apkUrl = validateApkUrl(
            json.optString("apkUrl", FALLBACK_APK_URL).trim(),
        )
        val apkSha256 = json.optString("apkSha256").trim().lowercase(Locale.ROOT)
        require(SHA256_PATTERN.matches(apkSha256)) {
            "Empreinte SHA-256 de l’APK absente ou invalide"
        }
        val apkSizeBytes = json.optLong("apkSizeBytes", 0).coerceAtLeast(0)

        return UpdateInfo(
            versionCode = versionCode,
            versionName = versionName,
            apkUrl = apkUrl,
            apkSha256 = apkSha256,
            apkSizeBytes = apkSizeBytes,
        )
    }

    private fun validateApkUrl(rawUrl: String): String {
        val url = URL(rawUrl)
        require(url.protocol.equals("https", ignoreCase = true)) {
            "URL APK non sécurisée"
        }
        val host = url.host.lowercase(Locale.ROOT)
        require(host == "github.com" || host == "viseofh.fr" || host == "www.viseofh.fr") {
            "Domaine APK non autorisé"
        }
        return url.toExternalForm()
    }

    private companion object {
        const val PRIMARY_VERSION_URL = "https://viseofh.fr/odc/version.json"
        const val LEGACY_VERSION_URL = "https://viseofh.fr/orange-drone-compagnon/version.json"
        const val FALLBACK_APK_URL = "https://github.com/Falkinou/viseofh/releases/latest/download/Orange-Drone-Compagnon.apk"
        const val MAX_MANIFEST_BYTES = 64 * 1024
        val SHA256_PATTERN = Regex("[0-9a-f]{64}")

        fun fetchManifest(manifestUrl: String): String {
            val connection = URL(manifestUrl).openConnection() as? HttpURLConnection
                ?: error("Connexion HTTP indisponible")
            return try {
                connection.connectTimeout = 8_000
                connection.readTimeout = 8_000
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                val responseCode = connection.responseCode
                require(responseCode in 200..299) {
                    "Réponse HTTP $responseCode pour le manifest"
                }
                connection.inputStream.use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8 * 1024)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        total += count
                        require(total <= MAX_MANIFEST_BYTES) { "Manifest de mise à jour trop volumineux" }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray().toString(Charsets.UTF_8)
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}
