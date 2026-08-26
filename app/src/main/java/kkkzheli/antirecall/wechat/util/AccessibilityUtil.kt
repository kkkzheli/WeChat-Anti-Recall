package kkkzheli.antirecall.wechat.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import kkkzheli.antirecall.wechat.service.KeepAliveAccessibilityService

object AccessibilityUtil {

    fun isServiceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, KeepAliveAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        for (name in splitter) {
            // Standard flattened form: package/class
            if (name.equals(expected, ignoreCase = true)) return true
            // Some OEMs persist the dot form: package.class
            if (name.replace('/', '.').equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun openSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
