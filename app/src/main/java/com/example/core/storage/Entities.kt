package com.example.core.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "MAYA"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "auto",
    val isVoice: Boolean = false
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "preference", "fact", "custom"
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
