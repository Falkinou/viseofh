package com.djisyncflow.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.os.storage.StorageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.djisyncflow.data.AppDatabase
import com.djisyncflow.data.DeliveryMode
import com.djisyncflow.data.LogStatus
import com.djisyncflow.data.SettingsRepository
import com.djisyncflow.data.isReadyForSync
import com.djisyncflow.mail.SmtpMailer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val settingsRepository = SettingsRepository(applicationContext)
        val settings = settingsRepository.getSettings()
        val logger = ActivityLogger(applicationContext)
        val notifier = NotificationHelper(applicationContext)
        if (settings.folderUri.isBlank()) return Result.success()

        val dao = AppDatabase.get(applicationContext).logFileDao()
        val detected = LogScanner(applicationContext, dao, logger).scan(settings.folderUri)
        settingsRepository.updateLastSync(System.currentTimeMillis())
        if (detected > 0) logger.info("$detected nouveau(x) log(s) détecté(s).")

        if (!settings.isReadyForSync()) {
            logger.warning("Synchronisation ignorée : vérifier le dossier logs, le destinataire mail ou la racine USB dans Réglages.")
            if (
                settings.deliveryMode == DeliveryMode.USB &&
                settings.folderUri.isNotBlank() &&
                settings.usbExportUri.isBlank() &&
                hasMountedRemovableStorage()
            ) {
                notifier.notifyUsbAuthorizationRequired()
            }
            return Result.success()
        }

        // Offline scans still record pending logs; only network destinations need connectivity.
        if (settings.deliveryMode != DeliveryMode.USB && !hasNetwork()) {
            logger.warning("Réseau indisponible : les logs restent en attente et seront réessayés automatiquement.")
            return Result.retry()
        }

        val mailer = SmtpMailer(applicationContext)
        val ftpUploader = FtpUploader(applicationContext)
        val usbExporter = UsbExporter(applicationContext)
        var hadFailure = false
        var sentCount = 0
        var errorCount = 0

        dao.getRetryable().forEach { log ->
            try {
                val detail = when (settings.deliveryMode) {
                    DeliveryMode.FTP -> when (ftpUploader.upload(settings, log)) {
                        FtpUploadResult.UPLOADED -> "envoyé"
                        FtpUploadResult.SKIPPED_EXISTING -> "déjà présent sur le serveur"
                        FtpUploadResult.UPLOADED_RENAMED -> "envoyé avec nom renommé"
                    }
                    DeliveryMode.SMTP -> {
                        mailer.sendLog(settings, log)
                        "envoyé par e-mail"
                    }
                    DeliveryMode.USB -> {
                        val export = usbExporter.export(settings, log)
                        if (export.copiedMediaCount > 0) {
                            "exporté sur USB avec ${export.copiedMediaCount} média(s)"
                        } else if (export.copiedLog) {
                            "exporté sur USB"
                        } else {
                            "déjà présent sur USB"
                        }
                    }
                }
                dao.updateStatus(
                    id = log.id,
                    status = LogStatus.SENT,
                    sentAtMillis = System.currentTimeMillis(),
                    lastError = null,
                )
                sentCount++
                logger.success("${log.fileName} : $detail.")
            } catch (error: Exception) {
                hadFailure = true
                errorCount++
                dao.updateStatus(
                    id = log.id,
                    status = LogStatus.ERROR,
                    sentAtMillis = null,
                    lastError = error.message ?: error.javaClass.simpleName,
                )
                logger.error("${log.fileName} : ${error.message ?: error.javaClass.simpleName}")
            }
        }

        val pending = dao.getRetryable().size
        if (!settings.silentMode || errorCount > 0) {
            notifier.notifySyncResult(sent = sentCount, pending = pending, errors = errorCount)
        }
        checkForUpdatesIfNeeded(settingsRepository, settings, logger, notifier)
        return if (hadFailure) Result.retry() else Result.success()
    }

    private fun hasNetwork(): Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun hasMountedRemovableStorage(): Boolean =
        runCatching {
            val storageManager = applicationContext.getSystemService(StorageManager::class.java)
            storageManager.storageVolumes.any { volume ->
                volume.isRemovable && volume.state == Environment.MEDIA_MOUNTED
            }
        }.getOrDefault(false)

    private suspend fun checkForUpdatesIfNeeded(
        settingsRepository: SettingsRepository,
        settings: com.djisyncflow.data.AppSettings,
        logger: ActivityLogger,
        notifier: NotificationHelper,
    ) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        if (now - settings.lastUpdateCheckAtMillis < oneDayMillis) return

        withContext(Dispatchers.IO) {
            runCatching { UpdateChecker().check() }
        }.onSuccess { update ->
            settingsRepository.updateAvailableVersion(
                versionName = update?.versionName.orEmpty(),
                apkUrl = update?.apkUrl.orEmpty(),
                checkedAtMillis = now,
            )
            if (update != null) {
                logger.info("Mise à jour disponible : version ${update.versionName}.")
                notifier.notifyUpdateAvailable(update.versionName)
            }
        }.onFailure {
            logger.warning("Vérification de mise à jour impossible : ${it.message ?: it.javaClass.simpleName}")
        }
    }
}
