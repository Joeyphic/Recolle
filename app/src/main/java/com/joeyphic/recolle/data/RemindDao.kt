package com.joeyphic.recolle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminder WHERE id=:id")
    fun getReminderById(id: Int): Reminder?

    @Query("UPDATE reminder SET checked=true WHERE id=:id")
    fun checkReminderById(id: Int)

    @Query("DELETE FROM reminder WHERE checked=true AND event_time <= :seconds")
    fun clearCheckedRemindersBeforeTime(seconds: Long)

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminder ORDER BY event_time ASC")
    fun getAllRemindersFlow(): Flow<List<Reminder>>
}