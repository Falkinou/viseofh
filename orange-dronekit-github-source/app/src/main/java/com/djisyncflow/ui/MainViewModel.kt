package com.djisyncflow.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.djisyncflow.data.AppDatabase
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.DeliveryMode
import com.djisyncflow.data.LogStatus
import com.djisyncflow.data.SettingsRepository
import com.djisyncflow.dji.DjiMediaController
import com.djisyncflow.dji.DjiSdkController
import com.djisyncflow.flight.DjiFlightLogDecoder
import com.djisyncflow.flight.saveDecodeResult
import com.djisyncflow.mail.SmtpMailer
import com.djisyncflow.sync.ActivityLogger
import com.djisyncflow.sync.FtpUploader
import com.djisyncflow.sync.LogScanner
import com.djisyncflow.sync.NotificationHelper
import com.djisyncflow.sync.SyncScheduler
import com.djisyncflow.sync.UpdateChecker
import com.djisyncflow.sync.UsbExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val EMBEDDED_DJI_OPEN_API_KEY = "408e8f1ecdbd7b2bc65e32192c36d57"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val database = AppDatabase.get(appContext)
    private val dao = database.logFileDao()
    private val eventDao = database.activityEventDao()
    private val flightLogDecoder = DjiFlightLogDecoder(appContext)
    private val logger = ActivityLogger(appContext)

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    val logs = dao.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val events = eventDao.observeRecent().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val djiSdkState = DjiSdkController.state

    private val mutableUsbKitProgress = MutableStateFlow(UsbKitProgress())
    val usbKitProgress: StateFlow<UsbKitProgress> = mutableUsbKitProgress.asStateFlow()

    val actionMessage = MutableStateFlow("")

    fun saveSettings(next: AppSettings) {
        viewModelScope.launch {
            settingsRepository.save(next)
            SyncScheduler.schedulePeriodic(appContext)
            actionMessage.value = "Configuration enregistree."
            logger.success("Configuration enregistrée.")
        }
    }

    fun saveFolder(uri: Uri, label: String, currentDraft: AppSettings) {
        viewModelScope.launch {
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }

            settingsRepository.save(
                currentDraft.copy(
                    folderUri = uri.toString(),
                    folderLabel = label,
                ),
            )
            actionMessage.value = "Dossier de logs enregistre."
            logger.success("Dossier logs enregistré : $label")
        }
    }

    fun saveUsbExportFolder(uri: Uri, label: String, currentDraft: AppSettings) {
        viewModelScope.launch {
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }

            val nextSettings = currentDraft.copy(
                deliveryMode = DeliveryMode.USB,
                usbExportUri = uri.toString(),
                usbExportLabel = label,
            )
            settingsRepository.save(
                nextSettings,
            )
            val writeCheck = withContext(Dispatchers.IO) {
                runCatching { UsbExporter(appContext).testDestination(nextSettings) }
            }
            actionMessage.value = writeCheck.fold(
                onSuccess = { "Dossier USB enregistré et écriture validée." },
                onFailure = { "Dossier USB enregistré, mais écriture à vérifier : ${it.message ?: it.javaClass.simpleName}" },
            )
            writeCheck
                .onSuccess { logger.success("Dossier USB enregistré et écriture validée : $label") }
                .onFailure { logger.warning("Dossier USB enregistré mais écriture à vérifier : ${it.message ?: it.javaClass.simpleName}") }
        }
    }

    fun saveMediaFolder(uri: Uri, label: String, currentDraft: AppSettings) {
        viewModelScope.launch {
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }

            settingsRepository.save(
                currentDraft.copy(
                    mediaFolderUri = uri.toString(),
                    mediaFolderLabel = label,
                    usbIncludeMedia = true,
                ),
            )
            actionMessage.value = "Dossier photos/vidéos enregistré."
            logger.success("Dossier photos/vidéos enregistré : $label")
        }
    }

    fun saveScreenExportFolder(uri: Uri, label: String) {
        viewModelScope.launch {
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            settingsRepository.updateScreenExportFolder(uri.toString(), label)
            actionMessage.value = "Dossier d'export Screen enregistré."
            logger.success("Dossier d'export Screen enregistré : $label")
        }
    }

    fun saveScreenProject(project: String) {
        viewModelScope.launch {
            settingsRepository.updateScreenProject(project)
            actionMessage.value = "Projet Screen enregistré."
        }
    }

    fun saveAppTheme(themeId: String) {
        viewModelScope.launch {
            settingsRepository.updateAppTheme(themeId)
            actionMessage.value = "Thème appliqué."
        }
    }

    fun saveSyncLogMailSettings(recipientEmail: String, technicianEmail: String) {
        viewModelScope.launch {
            settingsRepository.updateSyncLogMailSettings(recipientEmail, technicianEmail)
            actionMessage.value = "Paramètres mail SyncLog enregistrés."
            logger.success("Paramètres mail SyncLog enregistrés.")
        }
    }

    fun addScreenExportHistory(entry: String) {
        viewModelScope.launch {
            settingsRepository.addScreenExportHistory(entry)
        }
    }

    fun testDestination(draft: AppSettings) {
        viewModelScope.launch {
            settingsRepository.save(draft)
            actionMessage.value = when (draft.deliveryMode) {
                DeliveryMode.FTP -> "Test FTP en cours..."
                DeliveryMode.SMTP -> "Test SMTP en cours..."
                DeliveryMode.USB -> "Vérification USB..."
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    when (draft.deliveryMode) {
                        DeliveryMode.FTP -> FtpUploader(appContext).testConnection(draft)
                        DeliveryMode.SMTP -> SmtpMailer(appContext).sendTest(draft)
                        DeliveryMode.USB -> UsbExporter(appContext).testDestination(draft)
                    }
                }
            }

            actionMessage.value = result.fold(
                onSuccess = {
                    settingsRepository.markOnboardingCompleted()
                    when (draft.deliveryMode) {
                        DeliveryMode.FTP -> "Connexion FTP valide."
                        DeliveryMode.SMTP -> "Mail de test envoyé."
                        DeliveryMode.USB -> "Dossier USB prêt pour l'export."
                    }
                },
                onFailure = { "Échec du test : ${it.message ?: it.javaClass.simpleName}" },
            )
            result
                .onSuccess { logger.success("Test destination réussi : ${draft.deliveryMode}") }
                .onFailure { logger.error("Test destination échoué : ${it.message ?: it.javaClass.simpleName}") }
        }
    }

    fun syncNow(draft: AppSettings) {
        viewModelScope.launch {
            settingsRepository.save(draft)
            SyncScheduler.syncNow(appContext)
            actionMessage.value = "Configuration enregistrée. Synchronisation demandée."
            logger.info("Synchronisation manuelle demandée.")
        }
    }

    fun retryErrors(draft: AppSettings) {
        viewModelScope.launch {
            settingsRepository.save(draft)
            val resetCount = dao.resetStatus(currentStatus = LogStatus.ERROR, status = LogStatus.PENDING)
            SyncScheduler.syncNow(appContext)
            actionMessage.value = if (resetCount > 0) {
                "$resetCount log(s) en erreur remis en attente."
            } else {
                "Aucun log en erreur à relancer."
            }
            logger.info(actionMessage.value)
        }
    }

    fun exportLatestUsb(draft: AppSettings, logId: Long? = null) {
        viewModelScope.launch {
            settingsRepository.save(draft.copy(deliveryMode = DeliveryMode.USB))
            actionMessage.value = "Export USB du dernier vol..."
            val message = withContext(Dispatchers.IO) {
                runCatching {
                    val settings = settingsRepository.getSettings()
                    val log = resolveMissionLog(logId)
                        ?: return@runCatching "Aucun log disponible."
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.LOG_READY,
                        selectedLogName = log.fileName,
                        message = "Log sélectionné : ${log.fileName}",
                        transferPercent = 0,
                    )
                    val result = UsbExporter(appContext).export(settings, log)
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.DONE,
                        selectedLogName = log.fileName,
                        transferPercent = 100,
                        message = if (result.copiedMediaCount > 0) {
                            "Log et ${result.copiedMediaCount} média(s) local(aux) copiés sur USB"
                        } else {
                            "Log copié sur USB"
                        },
                    )
                    dao.updateStatus(
                        id = log.id,
                        status = LogStatus.SENT,
                        sentAtMillis = System.currentTimeMillis(),
                        lastError = null,
                    )
                    val media = if (result.copiedMediaCount > 0) " avec ${result.copiedMediaCount} média(s)" else ""
                    "${log.fileName} exporté sur USB$media."
                }.getOrElse {
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.ERROR,
                        message = it.message ?: it.javaClass.simpleName,
                    )
                    "Export USB impossible : ${it.message ?: it.javaClass.simpleName}"
                }
            }
            actionMessage.value = message
            if (message.startsWith("Export USB impossible")) {
                logger.error(message)
            } else {
                logger.success(message)
            }
        }
    }

    fun sendLatestLogByMail(recipientEmail: String? = null, technicianEmail: String? = null) {
        viewModelScope.launch {
            if (recipientEmail != null || technicianEmail != null) {
                settingsRepository.updateSyncLogMailSettings(
                    recipientEmail = recipientEmail.orEmpty(),
                    technicianEmail = technicianEmail.orEmpty(),
                )
            }
            sendLogByMailInternal(logId = null)
        }
    }

    fun sendLogByMail(logId: Long) {
        viewModelScope.launch {
            sendLogByMailInternal(logId = logId)
        }
    }

    private suspend fun sendLogByMailInternal(logId: Long?) {
        actionMessage.value = "Envoi mail SyncLog..."
        val message = withContext(Dispatchers.IO) {
            var attemptedLogId: Long? = null
            runCatching {
                val settings = settingsRepository.getSettings()
                require(settings.recipientEmail.isNotBlank()) { "Adresse destinataire manquante" }
                val log = resolveMissionLog(logId) ?: return@runCatching "Aucun log disponible."
                attemptedLogId = log.id
                SmtpMailer(appContext).sendLogWithEmbeddedConfig(settings, log)
                dao.updateStatus(
                    id = log.id,
                    status = LogStatus.SENT,
                    sentAtMillis = System.currentTimeMillis(),
                    lastError = null,
                )
                "${log.fileName} envoyé à ${settings.recipientEmail}."
            }.getOrElse { error ->
                val failedLog = attemptedLogId?.let { dao.getById(it) }
                failedLog?.let {
                    dao.updateStatus(
                        id = it.id,
                        status = LogStatus.ERROR,
                        sentAtMillis = null,
                        lastError = error.message ?: error.javaClass.simpleName,
                    )
                }
                "Envoi mail impossible : ${error.message ?: error.javaClass.simpleName}"
            }
        }
        actionMessage.value = message
        if (message.startsWith("Envoi mail impossible")) {
            logger.error(message)
        } else {
            logger.success(message)
        }
    }

    fun recoverDroneMedia(logId: Long? = null) {
        viewModelScope.launch {
            actionMessage.value = "Recherche des médias du dernier vol..."
            mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                stage = UsbKitStage.LOG_READY,
                message = "Selection du log de mission...",
                transferPercent = 0,
            )
            val message = withContext(Dispatchers.IO) {
                runCatching {
                    val latestLog = resolveMissionLog(logId)
                        ?: return@runCatching "Aucun log disponible pour associer les médias."
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.DETECTING_MEDIA,
                        selectedLogName = latestLog.fileName,
                        message = "Détection des médias pour ${latestLog.fileName}",
                        transferPercent = 0,
                    )
                    val preparedLog = if (latestLog.flightStartTimeMillis != null) {
                        latestLog
                    } else {
                        val decoded = flightLogDecoder.decode(latestLog, resolveDjiOpenApiKey(settingsRepository.getSettings().djiApiKey))
                        dao.saveDecodeResult(latestLog.id, decoded)
                        dao.getById(latestLog.id) ?: latestLog
                    }
                    if (preparedLog.flightStartTimeMillis == null) {
                        return@runCatching "Heure de vol introuvable dans ${latestLog.fileName}."
                    }
                    val settings = settingsRepository.getSettings()
                    require(settings.usbExportUri.isNotBlank()) { "Clé USB non configurée" }
                    val exporter = UsbExporter(appContext)
                    val copiedLog = exporter.exportLogOnly(settings, preparedLog)
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.LOG_READY,
                        selectedLogName = preparedLog.fileName,
                        message = if (copiedLog) {
                            "Log copié sur la clé USB"
                        } else {
                            "Log déjà présent sur la clé USB"
                        },
                        transferPercent = 0,
                    )
                    val mediaController = DjiMediaController()
                    val summary = mediaController.pullMissionMediaSummary(preparedLog)
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = if (summary.matchedCount > 0) UsbKitStage.MEDIA_READY else UsbKitStage.NO_MEDIA,
                        detectedMediaCount = summary.matchedCount,
                        totalDroneMediaCount = summary.count,
                        message = if (summary.matchedCount > 0) {
                            "${summary.matchedCount} média(s) du vol détecté(s)"
                        } else {
                            "Aucun média trouvé dans le timing du vol"
                        },
                        transferPercent = 0,
                    )
                    if (summary.matchedCount > 0) {
                        val mediaFiles = mediaController.pullMissionMediaFiles(preparedLog)
                        mediaFiles.forEachIndexed { index, mediaFile ->
                            val mediaName = mediaFile.fileName ?: "media_${mediaFile.fileIndex}"
                            val mediaStartedAtMillis = System.currentTimeMillis()
                            mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                                stage = UsbKitStage.TRANSFERRING,
                                currentMediaName = mediaName,
                                currentMediaIndex = index + 1,
                                currentMediaTotal = mediaFiles.size,
                                currentMediaPercent = 0,
                                currentMediaBytes = 0,
                                currentMediaTotalBytes = 0,
                                currentMediaSpeedBytesPerSecond = 0,
                                currentMediaRemainingSeconds = null,
                                message = "Transfert ${index + 1}/${mediaFiles.size} : $mediaName",
                            )
                            exporter.openMissionMediaOutput(
                                settings = settings,
                                log = preparedLog,
                                targetName = mediaName,
                                mimeType = mimeForMediaName(mediaFile.fileName.orEmpty()),
                            ).use { output ->
                                mediaController.downloadMediaFile(mediaFile, output) { filePercent, totalBytes, currentBytes ->
                                    val globalPercent = (((index * 100) + filePercent) / mediaFiles.size)
                                        .coerceIn(0, 100)
                                    val elapsedSeconds = ((System.currentTimeMillis() - mediaStartedAtMillis) / 1000L)
                                        .coerceAtLeast(1L)
                                    val speedBytesPerSecond = if (currentBytes > 0L) currentBytes / elapsedSeconds else 0L
                                    val remainingSeconds = if (speedBytesPerSecond > 0L && totalBytes > currentBytes) {
                                        (totalBytes - currentBytes) / speedBytesPerSecond
                                    } else {
                                        null
                                    }
                                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                                        stage = UsbKitStage.TRANSFERRING,
                                        transferPercent = globalPercent,
                                        currentMediaName = mediaName,
                                        currentMediaIndex = index + 1,
                                        currentMediaTotal = mediaFiles.size,
                                        currentMediaPercent = filePercent.coerceIn(0, 100),
                                        currentMediaBytes = currentBytes.coerceAtLeast(0L),
                                        currentMediaTotalBytes = totalBytes.coerceAtLeast(0L),
                                        currentMediaSpeedBytesPerSecond = speedBytesPerSecond.coerceAtLeast(0L),
                                        currentMediaRemainingSeconds = remainingSeconds,
                                        message = "Transfert ${index + 1}/${mediaFiles.size} : $mediaName",
                                    )
                                }
                            }
                        }
                        mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                            stage = UsbKitStage.DONE,
                            transferPercent = 100,
                            currentMediaPercent = 100,
                            message = "Vol exporté : log et ${mediaFiles.size} média(s) sur USB",
                        )
                    } else {
                        mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                            stage = UsbKitStage.DONE,
                            transferPercent = 100,
                            message = "Vol exporté : log copié, aucun média associé détecté",
                        )
                    }
                    dao.updateStatus(
                        id = preparedLog.id,
                        status = LogStatus.SENT,
                        sentAtMillis = System.currentTimeMillis(),
                        lastError = null,
                    )
                    val latest = summary.matchedLatestFileName?.let { " Dernier média : $it." }.orEmpty()
                    val logState = if (copiedLog) "log copié" else "log déjà présent"
                    "Vol exporté : $logState, ${summary.matchedCount}/${summary.count} média(s) associé(s).$latest"
                }.getOrElse {
                    mutableUsbKitProgress.value = mutableUsbKitProgress.value.copy(
                        stage = UsbKitStage.ERROR,
                        message = it.message ?: it.javaClass.simpleName,
                    )
                    "Médias drone indisponibles : ${it.message ?: it.javaClass.simpleName}"
                }
            }
            actionMessage.value = message
        }
    }

    fun refreshLogFolder() {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettings()
            if (currentSettings.folderUri.isBlank()) {
                actionMessage.value = "Aucun dossier log de vol configuré."
                return@launch
            }
            actionMessage.value = "Actualisation du dossier log de vol..."
            val detected = withContext(Dispatchers.IO) {
                LogScanner(appContext, dao, ActivityLogger(appContext)).scan(currentSettings.folderUri)
            }
            actionMessage.value = if (detected > 0) {
                "$detected nouveau(x) log(s) détecté(s)."
            } else {
                "Dossier log de vol actualisé."
            }
        }
    }

    fun checkForUpdateNow() {
        viewModelScope.launch {
            actionMessage.value = "Vérification de mise à jour..."
            val result = withContext(Dispatchers.IO) {
                runCatching { UpdateChecker().check() }
            }
            actionMessage.value = result.fold(
                onSuccess = { update ->
                    val now = System.currentTimeMillis()
                    settingsRepository.updateAvailableVersion(
                        versionName = update?.versionName.orEmpty(),
                        apkUrl = update?.apkUrl.orEmpty(),
                        checkedAtMillis = now,
                    )
                    if (update == null) {
                        "Orange DroneKit est à jour."
                    } else {
                        NotificationHelper(appContext).notifyUpdateAvailable(update.versionName)
                        "Mise à jour disponible : ${update.versionName}."
                    }
                },
                onFailure = { "Vérification impossible : ${it.message ?: it.javaClass.simpleName}" },
            )
        }
    }

    fun decodeLog(logId: Long) {
        viewModelScope.launch {
            actionMessage.value = "Analyse du log DJI..."
            val decodedName = withContext(Dispatchers.IO) {
                val log = dao.getById(logId) ?: return@withContext null
                val djiApiKey = resolveDjiOpenApiKey(settingsRepository.getSettings().djiApiKey)
                dao.saveDecodeResult(log.id, flightLogDecoder.decode(log, djiApiKey))
                log.fileName
            }
            actionMessage.value = if (decodedName != null) {
                "$decodedName analyse."
            } else {
                "Log introuvable."
            }
        }
    }

    private suspend fun resolveMissionLog(logId: Long?): com.djisyncflow.data.LogFileEntity? =
        logId?.let { dao.getById(it) }
            ?: logs.value.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }

    fun saveDjiApiKey(apiKey: String) {
        viewModelScope.launch {
            val cleanKey = apiKey.trim()
            settingsRepository.updateDjiApiKey(cleanKey)
            actionMessage.value = if (isValidDjiOpenApiKey(cleanKey)) {
                "Clé Open API DJI enregistrée."
            } else {
                "Clé enregistrée, mais PlayLog utilisera la clé intégrée."
            }
        }
    }

}

private fun resolveDjiOpenApiKey(savedKey: String): String {
    val cleanKey = savedKey.trim()
    return if (isValidDjiOpenApiKey(cleanKey)) cleanKey else EMBEDDED_DJI_OPEN_API_KEY
}

private fun isValidDjiOpenApiKey(value: String): Boolean =
    value.matches(Regex("^[A-Fa-f0-9]{32}$"))

private fun mimeForMediaName(name: String): String =
    when {
        name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
        name.endsWith(".png", true) -> "image/png"
        name.endsWith(".dng", true) -> "image/x-adobe-dng"
        name.endsWith(".mp4", true) -> "video/mp4"
        name.endsWith(".mov", true) -> "video/quicktime"
        name.endsWith(".srt", true) -> "application/x-subrip"
        else -> "application/octet-stream"
    }

data class UsbKitProgress(
    val stage: UsbKitStage = UsbKitStage.IDLE,
    val selectedLogName: String = "",
    val detectedMediaCount: Int = 0,
    val totalDroneMediaCount: Int = 0,
    val transferPercent: Int = 0,
    val currentMediaName: String = "",
    val currentMediaIndex: Int = 0,
    val currentMediaTotal: Int = 0,
    val currentMediaPercent: Int = 0,
    val currentMediaBytes: Long = 0,
    val currentMediaTotalBytes: Long = 0,
    val currentMediaSpeedBytesPerSecond: Long = 0,
    val currentMediaRemainingSeconds: Long? = null,
    val message: String = "En attente",
)

enum class UsbKitStage {
    IDLE,
    LOG_READY,
    DETECTING_MEDIA,
    MEDIA_READY,
    NO_MEDIA,
    TRANSFERRING,
    DONE,
    ERROR,
}
