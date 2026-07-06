package com.djisyncflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.djisyncflow.sync.SyncScheduler

class BootSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SyncScheduler.schedulePeriodic(context.applicationContext)
                SyncScheduler.syncNow(context.applicationContext)
            }
        }
    }
}
