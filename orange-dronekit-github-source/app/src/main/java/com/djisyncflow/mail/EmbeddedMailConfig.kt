package com.djisyncflow.mail

import com.djisyncflow.BuildConfig
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.LogFileEntity
import com.djisyncflow.data.SmtpSecurity
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object EmbeddedMailConfig {
    const val senderEmail: String = "contact@viseofh.fr"

    // OVH mailbox password is injected at build time through ORANGE_DRONEKIT_SMTP_PASSWORD.
    private const val smtpHost: String = "ssl0.ovh.net"
    private const val smtpPort: String = "465"
    private val smtpPassword: String
        get() = BuildConfig.EMBEDDED_SMTP_PASSWORD
    private val smtpSecurity: SmtpSecurity = SmtpSecurity.SSL_TLS

    val isSmtpConfigured: Boolean
        get() = smtpHost.isNotBlank() &&
            smtpPort.toIntOrNull() != null &&
            senderEmail.isNotBlank() &&
            smtpPassword.isNotBlank()

    fun smtpSettings(base: AppSettings): AppSettings =
        base.copy(
            recipientEmail = base.recipientEmail.trim(),
            smtpHost = smtpHost,
            smtpPort = smtpPort,
            smtpSecurity = smtpSecurity,
            senderEmail = senderEmail,
            smtpPassword = smtpPassword,
        )

    fun subject(radioId: String, log: LogFileEntity): String =
        "[DJI-LOG] ${radioId.ifBlank { "UAS-FR-" }} | ${log.fileName}"

    fun body(radioId: String, technicianEmail: String, log: LogFileEntity): String =
        buildString {
            appendLine("Bonjour,")
            appendLine()
            appendLine("Veuillez trouver ci-joint le log de vol DJI transmis automatiquement depuis Orange DroneKit.")
            appendLine()
            appendLine("Ce message est généré automatiquement depuis la radiocommande ${radioId.ifBlank { "UAS-FR-" }}.")
            appendLine()
            appendLine("Informations du log :")
            appendLine("Radiocommande : ${radioId.ifBlank { "UAS-FR-" }}")
            if (technicianEmail.isNotBlank()) {
                appendLine("Contact technicien : $technicianEmail")
            }
            appendLine("Nom du fichier : ${log.fileName}")
            appendLine("Taille : ${log.sizeBytes} octets")
            appendLine("Date de détection : ${formatDate(log.detectedAtMillis)}")
            appendLine()
            appendLine("Merci de ne pas répondre directement à ce message automatique, sauf si un contact technicien est indiqué.")
            appendLine()
            appendLine("Orange DroneKit")
        }

    private fun formatDate(millis: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(millis))
}
