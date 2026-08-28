package com.djisyncflow.sync

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.djisyncflow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

class UpdateInstaller(private val context: Context) {
    suspend fun downloadAndInstall(update: UpdateInfo) {
        val apk = withContext(Dispatchers.IO) { downloadVerified(update) }
        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileProvider",
                apk,
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, APK_MIME_TYPE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        }
    }

    private fun downloadVerified(update: UpdateInfo): File {
        require(update.versionCode > BuildConfig.VERSION_CODE) { "Version déjà installée" }
        require(SHA256_PATTERN.matches(update.apkSha256.lowercase(Locale.ROOT))) {
            "Empreinte SHA-256 d’APK manquante ou invalide"
        }
        val apkUrl = URL(update.apkUrl).also { url ->
            require(url.protocol.equals("https", ignoreCase = true)) { "URL APK non sécurisée" }
            require(url.host.lowercase(Locale.ROOT) in ALLOWED_APK_HOSTS) { "Domaine APK non autorisé" }
        }
        val directory = File(context.cacheDir, UPDATE_DIRECTORY).apply {
            require(mkdirs() || exists()) { "Répertoire de mise à jour indisponible" }
        }
        val target = File(directory, "Orange-Drone-Compagnon-${update.versionCode}.apk")
        val temporary = File(directory, "${target.name}.part")
        temporary.delete()
        target.delete()

        val connection = apkUrl.openConnection() as? HttpURLConnection
            ?: error("Connexion de téléchargement indisponible")
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", APK_MIME_TYPE)
            val responseCode = connection.responseCode
            require(responseCode in 200..299) { "Téléchargement APK HTTP $responseCode" }
            val advertisedLength = connection.contentLengthLong
            require(advertisedLength <= 0L || update.apkSizeBytes <= 0L || advertisedLength == update.apkSizeBytes) {
                "Taille de l’APK inattendue"
            }

            val digest = MessageDigest.getInstance("SHA-256")
            var downloadedBytes = 0L
            connection.inputStream.use { input ->
                temporary.outputStream().buffered().use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        downloadedBytes += count
                    }
                }
            }
            require(update.apkSizeBytes <= 0L || downloadedBytes == update.apkSizeBytes) {
                "Taille téléchargée inattendue"
            }
            val actualHash = digest.digest().joinToString("") { byte ->
                byte.toInt().and(0xff).toString(16).padStart(2, '0')
            }
            require(actualHash == update.apkSha256.lowercase(Locale.ROOT)) {
                "Empreinte SHA-256 de l’APK incorrecte"
            }
            require(temporary.renameTo(target)) { "Validation du fichier APK impossible" }
            return target
        } finally {
            connection.disconnect()
            if (temporary.exists()) temporary.delete()
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val UPDATE_DIRECTORY = "verified-updates"
        const val BUFFER_SIZE = 64 * 1024
        val SHA256_PATTERN = Regex("[0-9a-f]{64}")
        val ALLOWED_APK_HOSTS = setOf("github.com", "viseofh.fr", "www.viseofh.fr")
    }
}
