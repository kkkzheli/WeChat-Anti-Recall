package kkkzheli.antirecall.wechat.App

import android.app.Application
import kkkzheli.antirecall.wechat.db.WeChatDatabase
import kkkzheli.antirecall.wechat.repository.MessageRepository

/**
 * Application entry point for the Anti Recall module.
 * Author: kkkzheli
 */
class App : Application() {

    companion object {
        lateinit var instance: App
            private set
        lateinit var database: WeChatDatabase
            private set
        lateinit var repository: MessageRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = WeChatDatabase.getInstance(this)
        repository = MessageRepository(database)
    }
}
