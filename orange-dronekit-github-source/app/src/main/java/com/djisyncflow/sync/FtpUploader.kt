package com.djisyncflow.sync

import android.content.Context
import android.net.Uri
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.LogFileEntity
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import java.time.Duration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FtpUploadResult {
    UPLOADED,
    SKIPPED_EXISTING,
    UPLOADED_RENAMED,
}

class FtpUploader(private val context: Context) {
    fun testConnection(settings: AppSettings) {
        val client = connectAndLogin(settings)
        try {
            val targetDir = buildRemoteDirectory(settings)
            ensureDirectory(client, targetDir)
        } finally {
            runCatching {
                if (client.isConnected) client.logout()
            }
            runCatching {
                if (client.isConnected) client.disconnect()
            }
        }
    }

    fun upload(settings: AppSettings, log: LogFileEntity): FtpUploadResult {
        val client = connectAndLogin(settings)
        try {
            val targetDir = buildRemoteDirectory(settings, log)
            ensureDirectory(client, targetDir)

            val remoteName = resolveRemoteName(client, sanitizeRemoteName(log.fileName), log.sizeBytes, settings.skipExistingRemoteFiles)
            if (remoteName.result == FtpUploadResult.SKIPPED_EXISTING) return remoteName.result
            context.contentResolver.openInputStream(Uri.parse(log.filePath)).use { input ->
                requireNotNull(input) { "Impossible de lire ${log.fileName}" }
                if (!client.storeFile(remoteName.name, input)) {
                    error("Upload FTP refuse : ${client.replyString.trim()}")
                }
            }
            return remoteName.result
        } finally {
            runCatching {
                if (client.isConnected) client.logout()
            }
            runCatching {
                if (client.isConnected) client.disconnect()
            }
        }
    }

    private fun connectAndLogin(settings: AppSettings): FTPClient {
        val client = if (settings.ftpUseFtps) FTPSClient(false) else FTPClient()
        client.connectTimeout = 20_000
        client.defaultTimeout = 20_000
        client.connect(settings.ftpHost, settings.ftpPort.toInt())
        client.soTimeout = 30_000
        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            error("Connexion FTP refusee : ${client.replyString.trim()}")
        }

        if (!client.login(settings.ftpUsername, settings.ftpPassword)) {
            error("Identifiants FTP refuses")
        }

        if (client is FTPSClient) {
            client.execPBSZ(0)
            client.execPROT("P")
        }

        client.enterLocalPassiveMode()
        client.setFileType(FTP.BINARY_FILE_TYPE)
        client.setControlKeepAliveTimeout(Duration.ofSeconds(30))
        return client
    }

    private fun buildRemoteDirectory(settings: AppSettings, log: LogFileEntity? = null): String {
        val cleanRoot = settings.ftpRemoteDir.trim().ifBlank { "/DJI" }.trimEnd('/')
        val radioDir = "$cleanRoot/${sanitizeRemoteName(settings.radioId)}"
        if (!settings.ftpUseDateFolders) return radioDir

        val dateMillis = log?.lastModifiedMillis?.takeIf { it > 0 } ?: System.currentTimeMillis()
        val dateDir = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dateMillis))
        return "$radioDir/$dateDir"
    }

    private fun ensureDirectory(client: FTPClient, path: String) {
        val parts = path.split('/').filter { it.isNotBlank() }
        if (path.startsWith('/')) client.changeWorkingDirectory("/")

        parts.forEach { part ->
            if (!client.changeWorkingDirectory(part)) {
                if (!client.makeDirectory(part)) {
                    error("Impossible de creer le dossier FTP $path : ${client.replyString.trim()}")
                }
                if (!client.changeWorkingDirectory(part)) {
                    error("Impossible d'ouvrir le dossier FTP $path : ${client.replyString.trim()}")
                }
            }
        }
    }

    private fun sanitizeRemoteName(value: String): String =
        value.replace(Regex("""[^\w.\-]+"""), "_")

    private data class RemoteName(val name: String, val result: FtpUploadResult)

    private fun resolveRemoteName(
        client: FTPClient,
        preferredName: String,
        localSize: Long,
        skipExisting: Boolean,
    ): RemoteName {
        val existing = client.listFiles(preferredName).firstOrNull()
        if (existing == null) return RemoteName(preferredName, FtpUploadResult.UPLOADED)

        if (skipExisting && existing.size == localSize) {
            return RemoteName(preferredName, FtpUploadResult.SKIPPED_EXISTING)
        }

        val dotIndex = preferredName.lastIndexOf('.')
        val stem = if (dotIndex > 0) preferredName.substring(0, dotIndex) else preferredName
        val extension = if (dotIndex > 0) preferredName.substring(dotIndex) else ""
        for (index in 2..999) {
            val candidate = "${stem}_$index$extension"
            if (client.listFiles(candidate).isEmpty()) {
                return RemoteName(candidate, FtpUploadResult.UPLOADED_RENAMED)
            }
        }
        error("Impossible de trouver un nom FTP disponible pour $preferredName")
    }
}
