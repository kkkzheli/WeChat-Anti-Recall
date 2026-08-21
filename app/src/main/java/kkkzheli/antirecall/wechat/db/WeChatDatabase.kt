package kkkzheli.antirecall.wechat.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeChatMessageEntity::class], version = 1, exportSchema = false)
abstract class WeChatDatabase : RoomDatabase() {
    abstract fun messageDao(): WeChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: WeChatDatabase? = null

        fun getInstance(context: Context): WeChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeChatDatabase::class.java,
                    "wechat_antirecall_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
