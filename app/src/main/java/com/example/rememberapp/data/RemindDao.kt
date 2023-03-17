package com.example.rememberapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllReminders(): Flow<List<Reminder>>
}