package kkkzheli.antirecall.wechat.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeChatMessageDao {

    @Query("SELECT * FROM wechat_messages ORDER BY index DESC")
    fun getAllMessages(): Flow<List<WeChatMessageEntity>>

    @Query("SELECT * FROM wechat_messages ORDER BY index DESC")
    suspend fun getAllMessagesList(): List<WeChatMessageEntity>

    @Query(
        "SELECT * FROM wechat_messages WHERE content LIKE '%' || :query || '%' " +
            "OR senderName LIKE '%' || :query || '%' " +
            "OR chatName LIKE '%' || :query || '%' " +
            "OR displayDate LIKE '%' || :query || '%' " +
            "OR displayTime LIKE '%' || :query || '%' " +
            "ORDER BY index DESC"
    )
    fun searchMessages(query: String): Flow<List<WeChatMessageEntity>>

    @Query("SELECT * FROM wechat_messages WHERE senderName = :contactName ORDER BY index DESC")
    fun getMessagesByContact(contactName: String): Flow<List<WeChatMessageEntity>>

    @Query(
        "SELECT * FROM wechat_messages " +
            "WHERE displayDate >= :startDate AND displayDate <= :endDate " +
            "ORDER BY index DESC"
    )
    fun getMessagesByDateRange(startDate: String, endDate: String): Flow<List<WeChatMessageEntity>>

    @Query("SELECT * FROM wechat_messages WHERE isSpecial = 1 ORDER BY index DESC")
    fun getSpecialMessages(): Flow<List<WeChatMessageEntity>>

    @Query("SELECT DISTINCT senderName FROM wechat_messages ORDER BY senderName ASC")
    fun getDistinctContactNames(): Flow<List<String>>

    @Query("SELECT DISTINCT chatName FROM wechat_messages WHERE isGroup = 1 ORDER BY chatName ASC")
    fun getDistinctGroupNames(): Flow<List<String>>

    @Query("SELECT * FROM wechat_messages ORDER BY index DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<WeChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: WeChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<WeChatMessageEntity>)

    @Delete
    suspend fun delete(message: WeChatMessageEntity)

    @Query("DELETE FROM wechat_messages")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM wechat_messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT COUNT(*) FROM wechat_messages WHERE isSpecial = 1")
    suspend fun getSpecialMessageCount(): Int
}
