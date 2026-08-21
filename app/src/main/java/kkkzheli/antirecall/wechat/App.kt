package kkkzheli.antirecall.wechat

import android.app.Application
import kkkzheli.antirecall.wechat.db.MessageStore
import kkkzheli.antirecall.wechat.repository.MessageRepository

class App : Application() {

    companion object {
        lateinit var instance: App
        lateinit var store: MessageStore
        lateinit var repository: MessageRepository
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        store = MessageStore()
        repository = MessageRepository(store)
    }
}
