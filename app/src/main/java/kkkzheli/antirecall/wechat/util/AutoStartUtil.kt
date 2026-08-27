package kkkzheli.antirecall.wechat.util

import android.content.Context
import android.os.Build

object AutoStartUtil {

    enum class State { ENABLED, DISABLED, UNKNOWN }

    private const val PREF_NAME = "auto_start_detect"
    private const val KEY_BOOT_CONFIRMED_MS = "boot_confirmed_ms"

    /**
     * Best-effort auto-start state.
     *
     * Primary: on Xiaomi devices we reflect into MIUI/HyperOS's hidden
     * AppOpsUtils.getApplicationAutoStart(context, pkg) which returns
     * 0 = enabled, 1 = disabled. It fails gracefully (ClassNotFoundException
     * or SecurityException) into UNKNOWN.
     *
     * Fallback: a reboot-confirmed flag — MIUI/HyperOS only delivers
     * BOOT_COMPLETED to apps in the auto-start whitelist, so a recent boot
     * receipt proves the permission is actually working.
     */
    fun detect(context: Context): State {
        if (isXiaomi()) {
            val direct = detectViaAppOpsUtils(context)
            if (direct != State.UNKNOWN) return direct
        }
        return if (bootConfirmed(context)) State.ENABLED else State.UNKNOWN
    }

    /** Called by the boot receiver when BOOT_COMPLETED actually arrives. */
    fun markBootConfirmed(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_BOOT_CONFIRMED_MS, System.currentTimeMillis())
            .apply()
    }

    private fun detectViaAppOpsUtils(context: Context): State {
        return try {
            val clazz = Class.forName("android.miui.AppOpsUtils")
            val method = clazz.getDeclaredMethod(
                "getApplicationAutoStart",
                Context::class.java,
                String::class.java,
            )
            method.isAccessible = true
            when (method.invoke(null, context, context.packageName) as? Int) {
                0 -> State.ENABLED
                1 -> State.DISABLED
                else -> State.UNKNOWN
            }
        } catch (_: Exception) {
            State.UNKNOWN
        }
    }

    private fun bootConfirmed(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_BOOT_CONFIRMED_MS, 0L) > 0L
    }

    private fun isXiaomi(): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") ||
            manufacturer.contains("xiaomi")
    }
}
