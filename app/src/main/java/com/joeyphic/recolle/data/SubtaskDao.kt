package com.joeyphic.recolle.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subtask: Subtask)

    @Update
    suspend fun update(subtask: Subtask)

    @Delete
    suspend fun delete(subtask: Subtask)

    @Query("SELECT * FROM subtask WHERE id=:id")
    fun getSubtaskById(id: Int): Subtask?

    @Query("UPDATE subtask SET checked=1 WHERE id=:id")
    fun checkReminderById(id: Int)

    @Query("UPDATE subtask SET checked=0 WHERE id=:id")
    fun uncheckReminderById(id: Int)

    @Query("SELECT * FROM subtask WHERE main_id=:mainId ORDER BY id ASC")
    fun getAllSubtasksByMainId(mainId: Int): List<Subtask>

    @Query("DELETE FROM subtask WHERE main_id=:mainId")
    fun deleteAllSubtasksByMainId(mainId: Int)

    @Query("SELECT * FROM subtask WHERE main_id=:mainId ORDER BY checked ASC, id ASC")
    fun getAllSubtasksFlow(mainId: Int): Flow<List<Subtask>>

    @Query("SELECT * FROM subtask WHERE main_id=:mainId ORDER BY checked ASC, id ASC")
    fun getAllSubtasksByMainIdFlow(mainId: Int): Flow<List<Subtask>>
}