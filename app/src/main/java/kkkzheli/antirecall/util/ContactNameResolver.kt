package kkkzheli.antirecall.wechat.util

import android.content.ContentResolver
import android.provider.ContactsContract

class ContactNameResolver {

    private val cache = mutableMapOf<String, String>()

    fun resolve(rawName: String, resolver: ContentResolver): String {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) return ""

        val cached = cache[trimmed]
        if (cached != null) return cached

        val wechatId = extractWechatId(trimmed)
        if (wechatId != null) {
            val name = lookupByWechatId(wechatId, resolver)
            if (name.isNotEmpty()) {
                cache[trimmed] = name
                return name
            }
        }

        cache[trimmed] = trimmed
        return trimmed
    }

    private fun extractWechatId(name: String): String? {
        val wxidPattern = Regex("wxid_[a-zA-Z0-9_]+")
        return wxidPattern.find(name)?.value
    }

    private fun lookupByWechatId(
        wxid: String,
        resolver: ContentResolver
    ): String {
        try {
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Email.DATA,
                    ContactsContract.CommonDataKinds.Email.DISPLAY_NAME
                ),
                "${ContactsContract.CommonDataKinds.Email.DATA} = ?",
                arrayOf(wxid),
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME
                    )
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore lookup failures
        }

        return ""
    }

    fun clearCache() {
        cache.clear()
    }
}
