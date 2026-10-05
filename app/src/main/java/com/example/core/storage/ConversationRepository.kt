package com.example.core.storage

import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val memoryDao: MemoryDao
) {
    val allMessages: Flow<List<ConversationEntity>> = conversationDao.getAllMessages()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun addMessage(sender: String, text: String, isVoice: Boolean = false, language: String = "auto"): Long {
        return conversationDao.insertMessage(
            ConversationEntity(
                sender = sender,
                message = text,
                timestamp = System.currentTimeMillis(),
                language = language,
                isVoice = isVoice
            )
        )
    }

    suspend fun getRecentTurns(limit: Int = 4): List<Pair<String, String>> {
        val messages = conversationDao.getRecentMessages(limit * 2)
        // Group into (user, maya) pairs
        val pairs = mutableListOf<Pair<String, String>>()
        var lastUserMsg: String? = null

        for (msg in messages.reversed()) {
            if (msg.sender == "USER") {
                lastUserMsg = msg.message
            } else if (msg.sender == "MAYA" && lastUserMsg != null) {
                pairs.add(Pair(lastUserMsg, msg.message))
                lastUserMsg = null
            }
        }
        return pairs
    }

    suspend fun deleteMessage(id: Long) = conversationDao.deleteMessage(id)

    suspend fun clearHistory() = conversationDao.deleteAllConversations()

    suspend fun saveMemory(category: String, key: String, value: String) {
        memoryDao.insertOrUpdate(
            MemoryEntity(category = category, key = key, value = value, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun clearMemories() = memoryDao.deleteAllMemories()
}
