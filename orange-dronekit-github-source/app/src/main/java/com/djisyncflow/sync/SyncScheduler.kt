package com.djisyncflow.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODIC_WORK = "orange-dronekit-periodic-sync"
    private const val MANUAL_WORK = "orange-dronekit-manual-sync"
    private const val LEGACY_PERIODIC_WORK = "orange-synclog-periodic-sync"
    private const val LEGACY_MANUAL_WORK = "orange-synclog-manual-sync"

    fun schedulePeriodic(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_PERIODIC_WORK)
        workManager.cancelUniqueWork(LEGACY_MANUAL_WORK)

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun syncNow(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_MANUAL_WORK)

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        workManager.enqueueUniqueWork(
            MANUAL_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
