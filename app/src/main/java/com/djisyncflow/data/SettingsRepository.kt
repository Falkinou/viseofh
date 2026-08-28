package com.djisyncflow.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "syncflow_settings")

class SettingsRepository(private val context: Context) {
    private val secureValueStore = SecureValueStore()

    private object Keys {
        val folderUri = stringPreferencesKey("folder_uri")
        val folderLabel = stringPreferencesKey("folder_label")
        val deliveryMode = stringPreferencesKey("delivery_mode")
        val ftpHost = stringPreferencesKey("ftp_host")
        val ftpPort = stringPreferencesKey("ftp_port")
        val ftpUsername = stringPreferencesKey("ftp_username")
        val ftpPassword = stringPreferencesKey("ftp_password")
        val ftpRemoteDir = stringPreferencesKey("ftp_remote_dir")
        val ftpUseFtps = stringPreferencesKey("ftp_use_ftps")
        val ftpUseDateFolders = booleanPreferencesKey("ftp_use_date_folders")
        val skipExistingRemoteFiles = booleanPreferencesKey("skip_existing_remote_files")
        val usbExportUri = stringPreferencesKey("usb_export_uri")
        val usbExportLabel = stringPreferencesKey("usb_export_label")
        val mediaFolderUri = stringPreferencesKey("media_folder_uri")
        val mediaFolderLabel = stringPreferencesKey("media_folder_label")
        val usbIncludeMedia = booleanPreferencesKey("usb_include_media")
        val recipientEmail = stringPreferencesKey("recipient_email")
        val technicianEmail = stringPreferencesKey("technician_email")
        val smtpHost = stringPreferencesKey("smtp_host")
        val smtpPort = stringPreferencesKey("smtp_port")
        val smtpSecurity = stringPreferencesKey("smtp_security")
        val senderEmail = stringPreferencesKey("sender_email")
        val smtpPassword = stringPreferencesKey("smtp_password")
        val radioId = stringPreferencesKey("radio_id")
        val lastSyncAtMillis = longPreferencesKey("last_sync_at_millis")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val silentMode = booleanPreferencesKey("silent_mode")
        val lastUpdateCheckAtMillis = longPreferencesKey("last_update_check_at_millis")
        val latestVersionCode = intPreferencesKey("latest_version_code")
        val latestVersionName = stringPreferencesKey("latest_version_name")
        val latestApkUrl = stringPreferencesKey("latest_apk_url")
        val latestApkSha256 = stringPreferencesKey("latest_apk_sha256")
        val latestApkSizeBytes = longPreferencesKey("latest_apk_size_bytes")
        val djiApiKey = stringPreferencesKey("dji_api_key")
        val appTheme = stringPreferencesKey("app_theme")
        val screenExportUri = stringPreferencesKey("screen_export_uri")
        val screenExportLabel = stringPreferencesKey("screen_export_label")
        val screenProject = stringPreferencesKey("screen_project")
        val screenExportHistory = stringPreferencesKey("screen_export_history")
    }

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        AppSettings(
            folderUri = prefs[Keys.folderUri].takeUnless { it.isNullOrBlank() } ?: DEFAULT_DJI_FLIGHT_RECORD_URI,
            folderLabel = prefs[Keys.folderLabel].takeUnless { it.isNullOrBlank() } ?: DEFAULT_DJI_FLIGHT_RECORD_LABEL,
            deliveryMode = normalizeDeliveryMode(prefs[Keys.deliveryMode]),
            ftpHost = prefs[Keys.ftpHost].orEmpty(),
            ftpPort = prefs[Keys.ftpPort] ?: "21",
            ftpUsername = prefs[Keys.ftpUsername].orEmpty(),
            ftpPassword = secureValueStore.decrypt(prefs[Keys.ftpPassword]),
            ftpRemoteDir = prefs[Keys.ftpRemoteDir] ?: "/DJI",
            ftpUseFtps = prefs[Keys.ftpUseFtps]?.toBooleanStrictOrNull() ?: false,
            ftpUseDateFolders = prefs[Keys.ftpUseDateFolders] ?: true,
            skipExistingRemoteFiles = prefs[Keys.skipExistingRemoteFiles] ?: true,
            usbExportUri = prefs[Keys.usbExportUri].orEmpty(),
            usbExportLabel = prefs[Keys.usbExportLabel].orEmpty(),
            mediaFolderUri = prefs[Keys.mediaFolderUri].orEmpty(),
            mediaFolderLabel = prefs[Keys.mediaFolderLabel].orEmpty(),
            usbIncludeMedia = prefs[Keys.usbIncludeMedia] ?: true,
            recipientEmail = prefs[Keys.recipientEmail].orEmpty(),
            technicianEmail = prefs[Keys.technicianEmail].orEmpty(),
            smtpHost = prefs[Keys.smtpHost].orEmpty(),
            smtpPort = prefs[Keys.smtpPort] ?: "587",
            smtpSecurity = runCatching {
                SmtpSecurity.valueOf(prefs[Keys.smtpSecurity] ?: SmtpSecurity.STARTTLS.name)
            }.getOrDefault(SmtpSecurity.STARTTLS),
            senderEmail = prefs[Keys.senderEmail].orEmpty(),
            smtpPassword = secureValueStore.decrypt(prefs[Keys.smtpPassword]),
            radioId = normalizeRadioId(prefs[Keys.radioId]),
            lastSyncAtMillis = prefs[Keys.lastSyncAtMillis] ?: 0,
            onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
            silentMode = prefs[Keys.silentMode] ?: true,
            lastUpdateCheckAtMillis = prefs[Keys.lastUpdateCheckAtMillis] ?: 0,
            latestVersionCode = prefs[Keys.latestVersionCode] ?: 0,
            latestVersionName = prefs[Keys.latestVersionName].orEmpty(),
            latestApkUrl = prefs[Keys.latestApkUrl].orEmpty(),
            latestApkSha256 = prefs[Keys.latestApkSha256].orEmpty(),
            latestApkSizeBytes = prefs[Keys.latestApkSizeBytes] ?: 0,
            djiApiKey = secureValueStore.decrypt(prefs[Keys.djiApiKey]),
            appTheme = normalizeThemeId(prefs[Keys.appTheme]),
            screenExportUri = prefs[Keys.screenExportUri].orEmpty(),
            screenExportLabel = prefs[Keys.screenExportLabel].orEmpty(),
            screenProject = prefs[Keys.screenProject].orEmpty(),
            screenExportHistory = prefs[Keys.screenExportHistory].orEmpty(),
        )
    }

    suspend fun getSettings(): AppSettings {
        migrateLegacySecrets()
        return settings.first()
    }

    suspend fun save(settings: AppSettings) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.folderUri] = settings.folderUri
            prefs[Keys.folderLabel] = settings.folderLabel
            prefs[Keys.deliveryMode] = settings.deliveryMode.name
            prefs[Keys.ftpHost] = settings.ftpHost
            prefs[Keys.ftpPort] = settings.ftpPort
            prefs[Keys.ftpUsername] = settings.ftpUsername
            prefs[Keys.ftpPassword] = secureValueStore.encrypt(settings.ftpPassword)
            prefs[Keys.ftpRemoteDir] = settings.ftpRemoteDir
            prefs[Keys.ftpUseFtps] = settings.ftpUseFtps.toString()
            prefs[Keys.ftpUseDateFolders] = settings.ftpUseDateFolders
            prefs[Keys.skipExistingRemoteFiles] = settings.skipExistingRemoteFiles
            prefs[Keys.usbExportUri] = settings.usbExportUri
            prefs[Keys.usbExportLabel] = settings.usbExportLabel
            prefs[Keys.mediaFolderUri] = settings.mediaFolderUri
            prefs[Keys.mediaFolderLabel] = settings.mediaFolderLabel
            prefs[Keys.usbIncludeMedia] = settings.usbIncludeMedia
            prefs[Keys.recipientEmail] = settings.recipientEmail
            prefs[Keys.technicianEmail] = settings.technicianEmail
            prefs[Keys.smtpHost] = settings.smtpHost
            prefs[Keys.smtpPort] = settings.smtpPort
            prefs[Keys.smtpSecurity] = settings.smtpSecurity.name
            prefs[Keys.senderEmail] = settings.senderEmail
            prefs[Keys.smtpPassword] = secureValueStore.encrypt(settings.smtpPassword)
            prefs[Keys.radioId] = normalizeRadioId(settings.radioId)
            prefs[Keys.lastSyncAtMillis] = settings.lastSyncAtMillis
            prefs[Keys.onboardingCompleted] = settings.onboardingCompleted
            prefs[Keys.silentMode] = settings.silentMode
            prefs[Keys.djiApiKey] = secureValueStore.encrypt(settings.djiApiKey)
            prefs[Keys.appTheme] = settings.appTheme
            prefs[Keys.screenExportUri] = settings.screenExportUri
            prefs[Keys.screenExportLabel] = settings.screenExportLabel
            prefs[Keys.screenProject] = settings.screenProject
            prefs[Keys.screenExportHistory] = settings.screenExportHistory
        }
    }

    suspend fun updateScreenExportFolder(uri: String, label: String) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.screenExportUri] = uri
            prefs[Keys.screenExportLabel] = label
        }
    }

    suspend fun updateScreenProject(project: String) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.screenProject] = project
        }
    }

    suspend fun addScreenExportHistory(entry: String) {
        context.settingsStore.edit { prefs ->
            val existing = prefs[Keys.screenExportHistory].orEmpty()
                .split("||")
                .filter { it.isNotBlank() }
            prefs[Keys.screenExportHistory] = (listOf(entry) + existing).distinct().take(8).joinToString("||")
        }
    }

    suspend fun updateDjiApiKey(apiKey: String) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.djiApiKey] = secureValueStore.encrypt(apiKey)
        }
    }

    suspend fun updateMsurveyMailSettings(recipientEmail: String, technicianEmail: String) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.recipientEmail] = recipientEmail.trim()
            prefs[Keys.technicianEmail] = technicianEmail.trim()
        }
    }

    suspend fun updateAppTheme(themeId: String) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.appTheme] = themeId
        }
    }

    suspend fun updateLastSync(millis: Long) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.lastSyncAtMillis] = millis
        }
    }

    suspend fun markOnboardingCompleted() {
        context.settingsStore.edit { prefs ->
            prefs[Keys.onboardingCompleted] = true
        }
    }

    suspend fun updateAvailableVersion(
        versionCode: Int = 0,
        versionName: String,
        apkUrl: String,
        checkedAtMillis: Long,
        apkSha256: String = "",
        apkSizeBytes: Long = 0,
    ) {
        context.settingsStore.edit { prefs ->
            prefs[Keys.latestVersionCode] = versionCode.coerceAtLeast(0)
            prefs[Keys.latestVersionName] = versionName
            prefs[Keys.latestApkUrl] = apkUrl
            prefs[Keys.latestApkSha256] = apkSha256
            prefs[Keys.latestApkSizeBytes] = apkSizeBytes.coerceAtLeast(0)
            prefs[Keys.lastUpdateCheckAtMillis] = checkedAtMillis
        }
    }

    private fun normalizeRadioId(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        return when {
            trimmed.isBlank() -> DEFAULT_RADIO_ID_PREFIX
            trimmed == LEGACY_RADIO_ID -> DEFAULT_RADIO_ID_PREFIX
            else -> trimmed
        }
    }

    private suspend fun migrateLegacySecrets() {
        context.settingsStore.edit { prefs ->
            prefs[Keys.ftpPassword]?.takeIf { it.isNotBlank() && !secureValueStore.isEncrypted(it) }?.let {
                prefs[Keys.ftpPassword] = secureValueStore.encrypt(it)
            }
            prefs[Keys.smtpPassword]?.takeIf { it.isNotBlank() && !secureValueStore.isEncrypted(it) }?.let {
                prefs[Keys.smtpPassword] = secureValueStore.encrypt(it)
            }
            prefs[Keys.djiApiKey]?.takeIf { it.isNotBlank() && !secureValueStore.isEncrypted(it) }?.let {
                prefs[Keys.djiApiKey] = secureValueStore.encrypt(it)
            }
        }
    }

    private fun normalizeDeliveryMode(value: String?): DeliveryMode =
        runCatching { DeliveryMode.valueOf(value ?: DeliveryMode.USB.name) }
            .getOrDefault(DeliveryMode.USB)
            .takeIf { it == DeliveryMode.USB }
            ?: DeliveryMode.USB

    private fun normalizeThemeId(value: String?): String =
        when (value.orEmpty()) {
            "", LEGACY_THEME_ID -> DEFAULT_THEME_ID
            else -> value.orEmpty()
        }

    private companion object {
        const val DEFAULT_RADIO_ID_PREFIX = "UAS-FR-"
        const val LEGACY_RADIO_ID = "RCPLUS2-EST01"
        const val DEFAULT_THEME_ID = "compagnon"
        const val LEGACY_THEME_ID = "dronekit"
    }
}
