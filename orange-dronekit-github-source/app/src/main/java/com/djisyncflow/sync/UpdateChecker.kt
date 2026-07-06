package com.djisyncflow.sync

import com.djisyncflow.BuildConfig
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String,
)

class UpdateChecker {
    fun check(): UpdateInfo? {
        val text = URL("https://viseofh.fr/orange-dronekit/version.json").readText()
        val json = JSONObject(text)
        val versionName = json.optString("versionName")
        val versionCode = json.optInt("versionCode", 0)
        val apkUrl = json.optString("apkUrl", "https://viseofh.fr/orange-dronekit/Orange-DroneKit.apk")
        if (versionCode <= BuildConfig.VERSION_CODE || versionName.isBlank()) return null
        return UpdateInfo(versionName = versionName, apkUrl = apkUrl)
    }
}
