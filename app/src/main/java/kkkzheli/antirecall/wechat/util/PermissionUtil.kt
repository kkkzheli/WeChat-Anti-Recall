package kkkzheli.antirecall.wechat.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.PowerManager

object PermissionUtil {

    /**
     * True only when the app can genuinely run in the background unrestricted.
     *
     * On stock Android this is [PowerManager.isIgnoringBatteryOptimizations]. On
     * Xiaomi/HyperOS the Doze whitelist is auto-populated for apps that declare
     * REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, but the system's real per-app battery
     * policy ("省电策略") is enforced through the RUN_ANY_IN_BACKGROUND appop — so
     * we additionally require that op to not be explicitly ignored, otherwise the
     * badge would claim "unrestricted" while the OEM still restricts the app.
     */
    fun isBatteryOptimizationExempt(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val am = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                // OPSTR_RUN_ANY_IN_BACKGROUND is @hide; its value is "android:run_any_in_background".
                val mode = am.unsafeCheckOpNoThrow(
                    "android:run_any_in_background",
                    android.os.Process.myUid(),
                    context.packageName,
                )
                if (mode == AppOpsManager.MODE_IGNORED) return false
            } catch (_: Exception) {
                // Some OEMs restrict appop inspection; fall back to the whitelist result.
            }
        }
        return true
    }
}
