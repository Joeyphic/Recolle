package com.example.rememberapp.data

import androidx.room.Query
import kotlinx.coroutines.flow.Flow

interface RemindDao {

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllReminders(): Flow<List<Reminder>>
}