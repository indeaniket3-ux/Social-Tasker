package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_messages")
data class SocialMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val text: String,
    val platform: String, // "Instagram", "Snapchat"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
