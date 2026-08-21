package kkkzheli.antirecall.wechat

import android.app.Application
import kkkzheli.antirecall.wechat.db.WeChatDatabase
import kkkzheli.antirecall.wechat.repository.MessageRepository

class App : Application() {

    lateinit var database: WeChatDatabase
    lateinit var repository: MessageRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = WeChatDatabase.getInstance(this)
        repository = MessageRepository(database)
    }

    companion object {
        lateinit var instance: App
    }
}
