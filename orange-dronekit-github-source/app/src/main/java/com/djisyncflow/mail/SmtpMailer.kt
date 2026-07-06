package com.djisyncflow.mail

import android.content.Context
import android.net.Uri
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.LogFileEntity
import com.djisyncflow.data.SmtpSecurity
import java.io.InputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class SmtpMailer(private val context: Context) {
    fun sendTest(settings: AppSettings) {
        val message = createMessage(settings).apply {
            setSubject("[DJI-LOG] ${settings.radioId} | Test Orange DroneKit", "UTF-8")
            setText(
                "Test d'envoi Orange DroneKit depuis ${settings.radioId}.\n\n" +
                    "Si vous recevez ce message, la configuration SMTP est valide.",
                "UTF-8",
            )
        }
        Transport.send(message)
    }

    fun sendLog(settings: AppSettings, log: LogFileEntity) {
        val textPart = MimeBodyPart().apply {
            setText(
                EmbeddedMailConfig.body(settings.radioId, settings.technicianEmail, log),
                "UTF-8",
            )
        }

        val attachmentPart = MimeBodyPart().apply {
            dataHandler = DataHandler(ContentUriDataSource(context, Uri.parse(log.filePath), log.fileName))
            fileName = log.fileName
        }

        val multipart: Multipart = MimeMultipart().apply {
            addBodyPart(textPart)
            addBodyPart(attachmentPart)
        }

        val message = createMessage(settings).apply {
            setSubject(EmbeddedMailConfig.subject(settings.radioId, log), "UTF-8")
            setContent(multipart)
        }

        Transport.send(message)
    }

    fun sendLogWithEmbeddedConfig(baseSettings: AppSettings, log: LogFileEntity) {
        check(EmbeddedMailConfig.isSmtpConfigured) {
            "SMTP embarqué non configuré"
        }
        sendLog(EmbeddedMailConfig.smtpSettings(baseSettings), log)
    }

    private fun createMessage(settings: AppSettings): MimeMessage {
        val session = Session.getInstance(mailProperties(settings), object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(settings.senderEmail, settings.smtpPassword)
        })

        return MimeMessage(session).apply {
            setFrom(InternetAddress(settings.senderEmail))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(settings.recipientEmail, false))
            if (settings.technicianEmail.isNotBlank()) {
                replyTo = arrayOf(InternetAddress(settings.technicianEmail))
            }
            sentDate = Date()
        }
    }

    private fun mailProperties(settings: AppSettings): Properties =
        Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", settings.smtpHost)
            put("mail.smtp.port", settings.smtpPort)
            put("mail.smtp.connectiontimeout", "20000")
            put("mail.smtp.timeout", "30000")
            put("mail.smtp.writetimeout", "30000")

            when (settings.smtpSecurity) {
                SmtpSecurity.STARTTLS -> put("mail.smtp.starttls.enable", "true")
                SmtpSecurity.SSL_TLS -> put("mail.smtp.ssl.enable", "true")
                SmtpSecurity.NONE -> Unit
            }
        }

    private fun formatDate(millis: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(millis))
}

private class ContentUriDataSource(
    private val context: Context,
    private val uri: Uri,
    private val displayName: String,
) : DataSource {
    override fun getInputStream(): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: error("Impossible de lire la pièce jointe $displayName : refaire l'autorisation du dossier logs.")

    override fun getOutputStream() =
        throw UnsupportedOperationException("Orange DroneKit ne modifie jamais les logs DJI")

    override fun getContentType(): String = "text/plain"

    override fun getName(): String = displayName
}
