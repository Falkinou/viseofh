package com.djisyncflow

import android.app.Application
import android.content.Context
import com.cySdkyc.clx.Helper
import com.djisyncflow.dji.DjiSdkController
import com.djisyncflow.sync.NetworkSyncMonitor
import com.djisyncflow.sync.SyncScheduler

class DjiSyncflowApp : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Helper.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        DjiSdkController.start(this)
        SyncScheduler.schedulePeriodic(this)
        NetworkSyncMonitor(this).start()
    }
}
