package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis ASC, id ASC")
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Dao
interface ReminderNoteDao {
    @Query("SELECT * FROM local_reminders ORDER BY triggerTimeMillis ASC")
    fun observeReminders(): Flow<List<ReminderNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderNoteEntity): Long

    @Query("DELETE FROM local_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}

@Database(
    entities = [ChatMessageEntity::class, ReminderNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SiyaDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun reminderNoteDao(): ReminderNoteDao

    companion object {
        @Volatile
        private var INSTANCE: SiyaDatabase? = null

        fun getInstance(context: Context): SiyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SiyaDatabase::class.java,
                    "siya_assistant.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
