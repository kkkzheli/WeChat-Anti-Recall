package kkkzheli.antirecall.wechat.util

import android.content.Context
import android.os.Build

object AutoStartUtil {

    enum class State { ENABLED, DISABLED, UNKNOWN }

    /**
     * Best-effort auto-start state.
     *
     * We reflect into MIUI/HyperOS's hidden AppOpsUtils.getApplicationAutoStart
     * which returns 0 = enabled, 1 = disabled. On MIUI 11-14 this works and
     * reports the real toggle. On HyperOS the class survives as an empty stub,
     * so the reflection fails and we return UNKNOWN rather than guess — a false
     * "granted" is worse than an honest "can't tell" (the row tap opens the real
     * auto-start screen for manual confirmation).
     */
    fun detect(context: Context): State {
        if (isXiaomi()) {
            val direct = detectViaAppOpsUtils(context)
            if (direct != State.UNKNOWN) return direct
        }
        return State.UNKNOWN
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

    private fun isXiaomi(): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") ||
            manufacturer.contains("xiaomi")
    }
}
