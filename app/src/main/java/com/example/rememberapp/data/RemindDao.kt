package com.example.rememberapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Query("SELECT * FROM reminder WHERE id=:id")
    fun getReminderById(id: Int): Reminder?

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllRemindersFlow(): Flow<List<Reminder>>
}