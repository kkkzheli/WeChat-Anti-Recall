package kkkzheli.antirecall.wechat

import android.content.Context
import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import kkkzheli.antirecall.wechat.db.WeChatDatabase
import kkkzheli.antirecall.wechat.repository.MessageRepository
import kkkzheli.antirecall.wechat.util.NotificationHelper

private val Context.dataStore by preferencesDataStore("app_settings")

class App : Application() {

    lateinit var database: WeChatDatabase
    lateinit var repository: MessageRepository

    companion object {
        lateinit var instance: App
        val dataStore by lazy { instance.dataStore }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = WeChatDatabase.getInstance(this)
        repository = MessageRepository(database)
        NotificationHelper.createSpecialChannel(this)
        NotificationHelper.createKeepAliveChannel(this)
    }
}
