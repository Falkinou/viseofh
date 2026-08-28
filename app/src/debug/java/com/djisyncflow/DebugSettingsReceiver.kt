package com.djisyncflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.djisyncflow.data.DeliveryMode
import com.djisyncflow.data.SettingsRepository
import com.djisyncflow.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugSettingsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = SettingsRepository(context.applicationContext)
                val current = repository.getSettings()
                repository.save(
                    current.copy(
                        deliveryMode = DeliveryMode.FTP,
                        ftpHost = intent.getStringExtra("ftp_host") ?: current.ftpHost,
                        ftpPort = intent.getStringExtra("ftp_port") ?: current.ftpPort,
                        ftpUsername = intent.getStringExtra("ftp_username") ?: current.ftpUsername,
                        ftpPassword = intent.getStringExtra("ftp_password") ?: current.ftpPassword,
                        ftpRemoteDir = intent.getStringExtra("ftp_remote_dir") ?: current.ftpRemoteDir,
                        radioId = intent.getStringExtra("radio_id") ?: current.radioId,
                    ),
                )
                if (intent.getBooleanExtra("sync_now", false)) {
                    SyncScheduler.syncNow(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
