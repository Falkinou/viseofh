package com.djisyncflow

import android.app.Application
import android.content.Context
import android.os.Build
import com.cySdkyc.clx.Helper
import com.djisyncflow.dji.DjiSdkController

class OrangeDroneCompagnonApp : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!isAndroidEmulator()) {
            runCatching {
                Helper.install(this)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        runCatching { DjiSdkController.start(applicationContext) }
    }

    private fun isAndroidEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("sdk") ||
            model.contains("emulator") ||
            product.contains("sdk") ||
            hardware.contains("ranchu") ||
            hardware.contains("goldfish") ||
            manufacturer.contains("genymotion")
    }
}
