package kkkzheli.antirecall.wechat.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeChatMessageDao {

    @Query("SELECT * FROM wechat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<WeChatMessageEntity>>

    @Query("SELECT * FROM wechat_messages WHERE content LIKE '%' || :query || '%' " +
           "OR senderName LIKE '%' || :query || '%' " +
           "OR chatName LIKE '%' || :query || '%' " +
           "ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<WeChatMessageEntity>>

    @Query("SELECT DISTINCT senderName FROM wechat_messages WHERE isGroup = 0 ORDER BY senderName ASC")
    fun getContactNames(): Flow<List<String>>

    @Query("SELECT DISTINCT chatName FROM wechat_messages WHERE isGroup = 1 ORDER BY chatName ASC")
    fun getGroupNames(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM wechat_messages")
    fun getMessageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: WeChatMessageEntity): Long

    @Query("DELETE FROM wechat_messages")
    suspend fun clearAll()

    @Query("DELETE FROM wechat_messages WHERE id = :id")
    suspend fun deleteById(id: Long)
}
