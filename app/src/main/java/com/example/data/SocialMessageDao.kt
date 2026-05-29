package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialMessageDao {
    @Query("SELECT * FROM social_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SocialMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SocialMessage)

    @Update
    suspend fun updateMessage(message: SocialMessage)

    @Query("UPDATE social_messages SET isRead = true WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE social_messages SET isRead = true")
    suspend fun markAllAsRead()

    @Query("DELETE FROM social_messages")
    suspend fun clearAllMessages()
}
