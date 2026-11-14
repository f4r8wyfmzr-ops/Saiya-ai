package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_reminders")
data class ReminderNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerTimeMillis: Long,
    val createdAtMillis: Long = System.currentTimeMillis()
)
