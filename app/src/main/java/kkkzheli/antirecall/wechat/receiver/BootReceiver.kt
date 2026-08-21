package kkkzheli.antirecall.wechat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                context.startService(Intent(context, NotificationCaptureService::class.java))
                context.startService(Intent(context, KeepAliveService::class.java))
            }
        }
    }
}
