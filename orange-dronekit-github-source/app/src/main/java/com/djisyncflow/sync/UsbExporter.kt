package com.djisyncflow.sync

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.LogFileEntity
import com.djisyncflow.flight.DjiFlightLogDecoder
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UsbExportResult(
    val copiedLog: Boolean,
    val copiedMediaCount: Int,
)

class UsbExporter(private val context: Context) {
    private val decoder = DjiFlightLogDecoder(context)

    fun testDestination(settings: AppSettings) {
        val root = exportRoot(settings)
        val probe = root.createFile("text/plain", ".orange_dronekit_probe")
            ?: error("Dossier USB non accessible en écriture : choisir la racine de la clé dans Réglages.")
        runCatching { probe.delete() }
    }

    fun export(settings: AppSettings, log: LogFileEntity): UsbExportResult {
        val flightDir = missionDirectory(settings, log)
        val copiedLog = exportLogOnly(settings, log)

        val copiedMedia = if (settings.usbIncludeMedia && settings.mediaFolderUri.isNotBlank()) {
            copyMatchingMedia(settings, log, flightDir.ensureDirectory("Media"))
        } else {
            0
        }

        return UsbExportResult(copiedLog = copiedLog, copiedMediaCount = copiedMedia)
    }

    fun exportLogOnly(settings: AppSettings, log: LogFileEntity): Boolean {
        val logDir = missionDirectory(settings, log).ensureDirectory("FlightRecord")
        return copySourceToDirectory(
            sourceUri = Uri.parse(log.filePath),
            targetDir = logDir,
            targetName = sanitizeFileName(log.fileName),
            mimeType = "text/plain",
            skipExisting = settings.skipExistingRemoteFiles,
            expectedSize = log.sizeBytes,
        )
    }

    fun openMissionMediaOutput(
        settings: AppSettings,
        log: LogFileEntity,
        targetName: String,
        mimeType: String,
    ): OutputStream {
        val mediaDir = missionDirectory(settings, log).ensureDirectory("Media")
        val safeName = sanitizeFileName(targetName)
        val target = mediaDir.findFile(safeName)?.takeIf { settings.skipExistingRemoteFiles }
            ?: mediaDir.createFile(mimeType, safeName)
            ?: error("Impossible de créer $safeName sur USB : vérifier l'autorisation d'écriture de la clé.")
        return context.contentResolver.openOutputStream(target.uri, "w")
            ?: error("Impossible d'écrire $safeName sur USB : rebrancher la clé puis refaire l'autorisation Android.")
    }

    private fun copyMatchingMedia(
        settings: AppSettings,
        log: LogFileEntity,
        targetDir: DocumentFile,
    ): Int {
        val mediaRoot = DocumentFile.fromTreeUri(context, Uri.parse(settings.mediaFolderUri)) ?: return 0
        val flightWindow = flightWindowMillis(log) ?: return 0
        var copied = 0
        mediaRoot.walkFiles(MAX_MEDIA_FILES).forEach { media ->
            val name = media.name ?: return@forEach
            if (!isSupportedMedia(name)) return@forEach
            val modified = media.lastModified()
            if (modified !in flightWindow.first..flightWindow.second) return@forEach

            if (copySourceToDirectory(
                    sourceUri = media.uri,
                    targetDir = targetDir,
                    targetName = sanitizeFileName(name),
                    mimeType = mimeForName(name),
                    skipExisting = settings.skipExistingRemoteFiles,
                    expectedSize = media.length(),
                )
            ) {
                copied++
            }
        }
        return copied
    }

    private fun flightWindowMillis(log: LogFileEntity): Pair<Long, Long>? {
        val decoded = if (log.flightStartTimeMillis != null) {
            null
        } else {
            runCatching { decoder.decode(log) }.getOrNull()
        }

        val start = log.flightStartTimeMillis ?: decoded?.flightStartTimeMillis ?: return null
        val durationMillis = ((log.flightDurationSeconds ?: decoded?.flightDurationSeconds ?: 0.0) * 1000.0)
            .toLong()
            .coerceAtLeast(0L)
        return (start - MEDIA_BEFORE_FLIGHT_MARGIN_MS) to (start + durationMillis + MEDIA_AFTER_FLIGHT_MARGIN_MS)
    }

    private fun exportRoot(settings: AppSettings): DocumentFile {
        require(settings.usbExportUri.isNotBlank()) {
            "Dossier USB non configuré : brancher la clé puis choisir sa racine dans Réglages."
        }
        val root = requireNotNull(DocumentFile.fromTreeUri(context, Uri.parse(settings.usbExportUri))) {
            "Dossier USB inaccessible : rebrancher la clé ou refaire l'autorisation Android."
        }
        require(root.exists()) {
            "Dossier USB introuvable : la clé semble démontée, rebranchez-la puis relancez Orange DroneKit."
        }
        return root
    }

    private fun missionDirectory(settings: AppSettings, log: LogFileEntity): DocumentFile {
        val root = exportRoot(settings)
        val radioDir = root.ensureDirectory("OrangeDroneKit")
            .ensureDirectory(sanitize(settings.radioId.ifBlank { "UAS-FR" }))
        val dateDir = radioDir.ensureDirectory(dateFolder(log.lastModifiedMillis))
        return dateDir.ensureDirectory(sanitize(log.fileName.substringBeforeLast('.')))
    }

    private fun copySourceToDirectory(
        sourceUri: Uri,
        targetDir: DocumentFile,
        targetName: String,
        mimeType: String,
        skipExisting: Boolean,
        expectedSize: Long,
    ): Boolean {
        val existing = targetDir.findFile(targetName)
        if (existing != null && skipExisting && existing.length() == expectedSize) return false

        val outputName = if (existing == null) targetName else uniqueName(targetDir, targetName)
        val target = targetDir.createFile(mimeType, outputName)
            ?: error("Impossible de créer $outputName sur USB : vérifier l'autorisation d'écriture de la clé.")

        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Fichier source inaccessible : refaire l'autorisation du dossier logs." }
            writeToTarget(input, target.uri)
        }
        return true
    }

    private fun writeToTarget(input: InputStream, targetUri: Uri) {
        context.contentResolver.openOutputStream(targetUri, "w").use { output ->
            requireNotNull(output) { "Impossible d'écrire sur USB : rebrancher la clé puis refaire l'autorisation Android." }
            input.copyTo(output)
        }
    }

    private fun uniqueName(directory: DocumentFile, preferred: String): String {
        val dotIndex = preferred.lastIndexOf('.')
        val stem = if (dotIndex > 0) preferred.substring(0, dotIndex) else preferred
        val extension = if (dotIndex > 0) preferred.substring(dotIndex) else ""
        for (index in 2..999) {
            val candidate = "${stem}_$index$extension"
            if (directory.findFile(candidate) == null) return candidate
        }
        error("Impossible de trouver un nom disponible sur USB : trop de fichiers similaires dans le dossier cible.")
    }

    private fun DocumentFile.ensureDirectory(name: String): DocumentFile =
        findFile(name)?.takeIf { it.isDirectory } ?: createDirectory(name)
            ?: error("Impossible de créer le dossier USB $name : vérifier l'autorisation d'écriture de la clé.")

    private fun DocumentFile.walkFiles(maxFiles: Int): Sequence<DocumentFile> = sequence {
        val stack = ArrayDeque<DocumentFile>()
        stack.add(this@walkFiles)
        var emitted = 0
        while (stack.isNotEmpty() && emitted < maxFiles) {
            val folder = stack.removeLast()
            folder.listFiles().forEach { child ->
                when {
                    child.isDirectory -> stack.add(child)
                    child.isFile && emitted < maxFiles -> {
                        emitted++
                        yield(child)
                    }
                }
            }
        }
    }

    private fun dateFolder(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis.takeIf { it > 0 } ?: System.currentTimeMillis()))

    private fun sanitize(value: String): String =
        value.replace(Regex("""[^\w.\-]+"""), "_").trim('_').ifBlank { "export" }

    private fun sanitizeFileName(value: String): String =
        sanitize(value).replace("/", "_")

    private fun isSupportedMedia(name: String): Boolean =
        name.endsWith(".jpg", true) ||
            name.endsWith(".jpeg", true) ||
            name.endsWith(".png", true) ||
            name.endsWith(".dng", true) ||
            name.endsWith(".mp4", true) ||
            name.endsWith(".mov", true) ||
            name.endsWith(".srt", true)

    private fun mimeForName(name: String): String =
        when {
            name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
            name.endsWith(".png", true) -> "image/png"
            name.endsWith(".dng", true) -> "image/x-adobe-dng"
            name.endsWith(".mp4", true) -> "video/mp4"
            name.endsWith(".mov", true) -> "video/quicktime"
            name.endsWith(".srt", true) -> "application/x-subrip"
            else -> "application/octet-stream"
        }

    private companion object {
        const val MAX_MEDIA_FILES = 10_000
        const val MEDIA_BEFORE_FLIGHT_MARGIN_MS = 5 * 60 * 1000L
        const val MEDIA_AFTER_FLIGHT_MARGIN_MS = 10 * 60 * 1000L
    }
}
