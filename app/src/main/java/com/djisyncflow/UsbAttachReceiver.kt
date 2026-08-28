package com.djisyncflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.djisyncflow.sync.SyncScheduler

class UsbAttachReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED ||
            intent.action == Intent.ACTION_MEDIA_MOUNTED ||
            intent.action == Intent.ACTION_MEDIA_CHECKING
        ) {
            SyncScheduler.syncNow(context.applicationContext)
        }
    }
}
