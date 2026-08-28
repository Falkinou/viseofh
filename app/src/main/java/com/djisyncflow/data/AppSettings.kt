package com.djisyncflow.data

const val DEFAULT_DJI_FLIGHT_RECORD_URI = "file:///sdcard/DJI/com.dji.industry.pilot/FlightRecord"
const val DEFAULT_DJI_FLIGHT_RECORD_LABEL = "DJI FlightRecord automatique"

val DEFAULT_DJI_FLIGHT_RECORD_PATHS = listOf(
    "/sdcard/DJI/com.dji.industry.pilot/FlightRecord",
    "/storage/emulated/0/DJI/com.dji.industry.pilot/FlightRecord",
)

data class AppSettings(
    val folderUri: String = DEFAULT_DJI_FLIGHT_RECORD_URI,
    val folderLabel: String = DEFAULT_DJI_FLIGHT_RECORD_LABEL,
    val deliveryMode: DeliveryMode = DeliveryMode.USB,
    val ftpHost: String = "",
    val ftpPort: String = "21",
    val ftpUsername: String = "",
    val ftpPassword: String = "",
    val ftpRemoteDir: String = "/DJI",
    val ftpUseFtps: Boolean = false,
    val ftpUseDateFolders: Boolean = true,
    val skipExistingRemoteFiles: Boolean = true,
    val usbExportUri: String = "",
    val usbExportLabel: String = "",
    val mediaFolderUri: String = "",
    val mediaFolderLabel: String = "",
    val usbIncludeMedia: Boolean = true,
    val recipientEmail: String = "",
    val technicianEmail: String = "",
    val smtpHost: String = "",
    val smtpPort: String = "587",
    val smtpSecurity: SmtpSecurity = SmtpSecurity.STARTTLS,
    val senderEmail: String = "",
    val smtpPassword: String = "",
    val radioId: String = "UAS-FR-",
    val lastSyncAtMillis: Long = 0,
    val onboardingCompleted: Boolean = false,
    val silentMode: Boolean = true,
    val lastUpdateCheckAtMillis: Long = 0,
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val latestApkUrl: String = "",
    val latestApkSha256: String = "",
    val latestApkSizeBytes: Long = 0,
    val djiApiKey: String = "",
    val appTheme: String = "compagnon",
    val screenExportUri: String = "",
    val screenExportLabel: String = "",
    val screenProject: String = "",
    val screenExportHistory: String = "",
)

enum class DeliveryMode {
    FTP,
    SMTP,
    USB,
}

enum class SmtpSecurity {
    STARTTLS,
    SSL_TLS,
    NONE,
}

fun AppSettings.isReadyForSync(): Boolean =
    folderUri.isNotBlank() &&
        radioId.isNotBlank() &&
        when (deliveryMode) {
            DeliveryMode.FTP ->
                ftpHost.isNotBlank() &&
                    ftpPort.toIntOrNull() != null &&
                    ftpUsername.isNotBlank() &&
                    ftpPassword.isNotBlank() &&
                    ftpRemoteDir.isNotBlank()
            DeliveryMode.SMTP ->
                recipientEmail.isNotBlank() &&
                    smtpHost.isNotBlank() &&
                    smtpPort.toIntOrNull() != null &&
                    senderEmail.isNotBlank() &&
                    smtpPassword.isNotBlank()
            DeliveryMode.USB ->
                usbExportUri.isNotBlank()
        }
