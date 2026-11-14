package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatMessageDao: ChatMessageDao,
    private val reminderNoteDao: ReminderNoteDao
) {
    val messages: Flow<List<ChatMessageEntity>> = chatMessageDao.observeMessages()
    val reminders: Flow<List<ReminderNoteEntity>> = reminderNoteDao.observeReminders()

    suspend fun addMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun clearMessages() {
        chatMessageDao.clearAll()
    }

    suspend fun addReminder(reminder: ReminderNoteEntity): Long {
        return reminderNoteDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(id: Long) {
        reminderNoteDao.deleteReminder(id)
    }
}
