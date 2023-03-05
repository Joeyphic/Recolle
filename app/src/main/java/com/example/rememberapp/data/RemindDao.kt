package com.example.rememberapp.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindDao {

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllReminders(): Flow<List<Reminder>>
}