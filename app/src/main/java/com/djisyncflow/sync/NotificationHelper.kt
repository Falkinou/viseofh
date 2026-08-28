package com.djisyncflow.sync

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.djisyncflow.R

class NotificationHelper(private val context: Context) {
    private val channelId = "orange_drone_compagnon_sync"

    fun notifySyncResult(sent: Int, pending: Int, errors: Int) {
        if (sent == 0 && errors == 0) return
        val title = if (errors > 0) "Orange Drone Compagnon : erreur de synchro" else "Orange Drone Compagnon : logs exportés"
        val body = when {
            errors > 0 -> "$errors erreur(s), $pending log(s) en attente."
            pending > 0 -> "$sent log(s) exporté(s), $pending en attente."
            else -> "$sent log(s) exporté(s)."
        }
        show(id = 1001, title = title, body = body)
    }

    fun notifyUpdateAvailable(versionName: String) {
        show(
            id = 1002,
            title = "Mise à jour Orange Drone Compagnon disponible",
            body = "Version $versionName disponible sur viseofh.fr.",
        )
    }

    fun notifyUsbAuthorizationRequired() {
        show(
            id = 1003,
            title = "Orange Drone Compagnon : clé USB détectée",
            body = "Ouvrez Orange Drone Compagnon puis autorisez le dossier de la clé USB une seule fois.",
        )
    }

    @SuppressLint("MissingPermission")
    private fun show(id: Int, title: String, body: String) {
        if (!canNotify()) return
        ensureChannel()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            channelId,
            "Synchronisation des logs",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
