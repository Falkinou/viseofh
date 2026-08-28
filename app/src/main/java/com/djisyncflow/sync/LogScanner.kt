package com.djisyncflow.sync

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.djisyncflow.data.DEFAULT_DJI_FLIGHT_RECORD_PATHS
import com.djisyncflow.data.DEFAULT_DJI_FLIGHT_RECORD_URI
import com.djisyncflow.data.LogFileDao
import com.djisyncflow.data.LogFileEntity
import java.io.File

class LogScanner(
    private val context: Context,
    private val dao: LogFileDao,
    private val logger: ActivityLogger? = null,
) {
    suspend fun scan(folderUri: String): Int {
        val normalizedUri = folderUri.ifBlank { DEFAULT_DJI_FLIGHT_RECORD_URI }
        val uri = Uri.parse(normalizedUri)
        if (uri.scheme == "file") {
            val requestedPath = uri.path
            val candidates = (
                listOfNotNull(requestedPath) +
                    DEFAULT_DJI_FLIGHT_RECORD_PATHS.takeIf { normalizedUri == DEFAULT_DJI_FLIGHT_RECORD_URI }.orEmpty()
                ).distinct()
            candidates.forEach { path ->
                val detected = scanLocalFolder(File(path), warnIfUnavailable = path == candidates.last())
                if (detected > 0 || File(path).canRead()) return detected
            }
            return 0
        }

        val folder = DocumentFile.fromTreeUri(context, uri) ?: return 0
        var detected = 0

        folder.listFiles()
            .asSequence()
            .filter { it.isFile }
            .filter { it.name?.endsWith(".txt", ignoreCase = true) == true }
            .forEach { file ->
                val name = file.name ?: return@forEach
                val size = file.length()
                val modified = file.lastModified()
                // Stable enough to avoid duplicate sends without reading or changing the DJI file.
                val fingerprint = "$name|$size|$modified"

                val entity = LogFileEntity(
                    fingerprint = fingerprint,
                    filePath = file.uri.toString(),
                    fileName = name,
                    sizeBytes = size,
                    lastModifiedMillis = modified,
                )
                val inserted = dao.insert(entity)
                if (inserted > 0) {
                    detected++
                    logger?.info("Nouveau log detecte : $name")
                }
            }

        return detected
    }

    private suspend fun scanLocalFolder(folder: File, warnIfUnavailable: Boolean = true): Int {
        if (!folder.exists() || !folder.isDirectory || !folder.canRead()) {
            if (warnIfUnavailable) {
                logger?.warning("Dossier DJI FlightRecord automatique inaccessible : ${folder.absolutePath}")
            }
            return 0
        }

        var detected = 0
        folder.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isFile }
            .filter { it.name.endsWith(".txt", ignoreCase = true) }
            .forEach { file ->
                val size = file.length()
                val modified = file.lastModified()
                val fingerprint = "${file.name}|$size|$modified"
                val entity = LogFileEntity(
                    fingerprint = fingerprint,
                    filePath = Uri.fromFile(file).toString(),
                    fileName = file.name,
                    sizeBytes = size,
                    lastModifiedMillis = modified,
                )
                val inserted = dao.insert(entity)
                if (inserted > 0) {
                    detected++
                    logger?.info("Nouveau log detecte : ${file.name}")
                }
            }

        return detected
    }
}
