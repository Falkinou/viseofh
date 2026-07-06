package com.djisyncflow.dji

import com.djisyncflow.data.LogFileEntity
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.datacenter.media.MediaFile
import dji.v5.manager.datacenter.media.MediaFileDownloadListener
import dji.v5.manager.datacenter.media.PullMediaFileListParam
import dji.v5.manager.interfaces.IMediaManager
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class DjiMediaSummary(
    val count: Int,
    val latestFileName: String?,
    val matchedCount: Int = 0,
    val matchedLatestFileName: String? = null,
)

class DjiMediaController {
    suspend fun pullMediaSummary(): DjiMediaSummary {
        ensureSdkReady()
        val mediaManager = MediaDataCenter.getInstance().mediaManager
        mediaManager.awaitEnable().getOrThrow()
        mediaManager.awaitPullAllMediaFiles().getOrThrow()
        val files = mediaManager.mediaFileListData?.data.orEmpty()
        return DjiMediaSummary(
            count = files.size,
            latestFileName = files.maxByOrNull { it.fileIndex }?.fileName,
        )
    }

    suspend fun pullMissionMediaSummary(log: LogFileEntity): DjiMediaSummary {
        ensureSdkReady()
        val mediaManager = MediaDataCenter.getInstance().mediaManager
        mediaManager.awaitEnable().getOrThrow()
        mediaManager.awaitPullAllMediaFiles().getOrThrow()
        val files = mediaManager.mediaFileListData?.data.orEmpty()
        val flightWindow = log.flightWindowMillis()
        val matched = if (flightWindow == null) {
            emptyList()
        } else {
            files.filter { media ->
                val capturedAt = media.captureMillis()
                capturedAt != null && capturedAt in flightWindow.first..flightWindow.second
            }
        }
        return DjiMediaSummary(
            count = files.size,
            latestFileName = files.maxByOrNull { it.fileIndex }?.fileName,
            matchedCount = matched.size,
            matchedLatestFileName = matched.maxByOrNull { it.fileIndex }?.fileName,
        )
    }

    suspend fun pullMissionMediaFiles(log: LogFileEntity): List<MediaFile> {
        ensureSdkReady()
        val mediaManager = MediaDataCenter.getInstance().mediaManager
        mediaManager.awaitEnable().getOrThrow()
        mediaManager.awaitPullAllMediaFiles().getOrThrow()
        val flightWindow = log.flightWindowMillis() ?: return emptyList()
        return mediaManager.mediaFileListData?.data.orEmpty().filter { media ->
            val capturedAt = media.captureMillis()
            capturedAt != null && capturedAt in flightWindow.first..flightWindow.second
        }
    }

    suspend fun downloadMediaFile(
        mediaFile: MediaFile,
        output: OutputStream,
        onProgress: (percent: Int, totalBytes: Long, currentBytes: Long) -> Unit,
    ) {
        suspendCancellableCoroutine { continuation ->
            var completed = false
            var latestTotal = 0L
            var latestCurrent = 0L
            mediaFile.pullOriginalMediaFileFromCamera(0L, object : MediaFileDownloadListener {
                override fun onStart() {
                    onProgress(0, 0L, 0L)
                }

                override fun onProgress(total: Long, current: Long) {
                    latestTotal = total.coerceAtLeast(0L)
                    latestCurrent = current.coerceAtLeast(0L)
                    if (total > 0) {
                        onProgress(
                            ((current * 100) / total).toInt().coerceIn(0, 100),
                            latestTotal,
                            latestCurrent,
                        )
                    }
                }

                override fun onRealtimeDataUpdate(data: ByteArray, position: Long) {
                    output.write(data)
                    output.flush()
                }

                override fun onFinish() {
                    completed = true
                    output.flush()
                    onProgress(100, latestTotal, latestTotal.takeIf { it > 0 } ?: latestCurrent)
                    continuation.resume(Unit)
                }

                override fun onFailure(error: IDJIError) {
                    if (!completed) {
                        continuation.resumeWith(Result.failure(IllegalStateException(error.description())))
                    }
                }
            })
            continuation.invokeOnCancellation {
                runCatching {
                    mediaFile.stopPullOriginalMediaFileFromCamera(object : CommonCallbacks.CompletionCallback {
                        override fun onSuccess() = Unit
                        override fun onFailure(error: IDJIError) = Unit
                    })
                }
            }
        }
    }

    private fun ensureSdkReady() {
        val state = DjiSdkController.state.value
        require(state.available) {
            "SDK DJI indisponible : vérifier que l'APK Orange DroneKit complet est installé sur une radiocommande DJI compatible."
        }
        require(state.registered) {
            "SDK DJI non enregistré : connecter la radiocommande à Internet, fermer DJI Pilot 2, puis relancer Orange DroneKit."
        }
        require(state.productConnected) {
            "Drone DJI non connecté : allumer le drone, vérifier la liaison radio, fermer DJI Pilot 2 puis relancer Orange DroneKit."
        }
    }

    private suspend fun IMediaManager.awaitEnable(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            enable(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    continuation.resume(Result.success(Unit))
                }

                override fun onFailure(error: IDJIError) {
                    continuation.resume(Result.failure(IllegalStateException(error.description())))
                }
            })
        }

    private suspend fun IMediaManager.awaitPullAllMediaFiles(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            pullMediaFileListFromCamera(
                PullMediaFileListParam.Builder()
                    .mediaFileIndex(-1)
                    .count(-1)
                    .build(),
                object : CommonCallbacks.CompletionCallback {
                    override fun onSuccess() {
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onFailure(error: IDJIError) {
                        continuation.resume(Result.failure(IllegalStateException(error.description())))
                    }
                },
            )
        }

    private fun LogFileEntity.flightWindowMillis(): Pair<Long, Long>? {
        val start = flightStartTimeMillis ?: return null
        val durationMillis = ((flightDurationSeconds ?: 0.0) * 1000.0)
            .toLong()
            .coerceAtLeast(0L)
        return (start - MEDIA_BEFORE_FLIGHT_MARGIN_MS) to (start + durationMillis + MEDIA_AFTER_FLIGHT_MARGIN_MS)
    }

    private fun MediaFile.captureMillis(): Long? {
        val date = date ?: return null
        val year = date.year ?: return null
        val month = date.month ?: return null
        val day = date.day ?: return null
        val hour = date.hour ?: 0
        val minute = date.minute ?: 0
        val second = date.second ?: 0
        return runCatching {
            LocalDateTime.of(year, month, day, hour, minute, second)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private companion object {
        const val MEDIA_BEFORE_FLIGHT_MARGIN_MS = 2 * 60 * 1000L
        const val MEDIA_AFTER_FLIGHT_MARGIN_MS = 5 * 60 * 1000L
    }
}
