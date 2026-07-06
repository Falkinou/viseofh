package com.djisyncflow.sync

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.djisyncflow.data.LogFileDao
import com.djisyncflow.data.LogFileEntity

class LogScanner(
    private val context: Context,
    private val dao: LogFileDao,
    private val logger: ActivityLogger? = null,
) {
    suspend fun scan(folderUri: String): Int {
        val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri)) ?: return 0
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
}
