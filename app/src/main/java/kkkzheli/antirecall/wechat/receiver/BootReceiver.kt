package kkkzheli.antirecall.wechat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> startDaemon(context)
            Intent.ACTION_MY_PACKAGE_REPLACED -> startDaemon(context)
        }
    }

    private fun startDaemon(context: Context) {
        try {
            val nls = Intent(context, NotificationCaptureService::class.java)
            val daemon = Intent(context, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(daemon)
            } else {
                context.startService(nls)
                context.startService(daemon)
            }
        } catch (_: Exception) {
        }
    }
}
