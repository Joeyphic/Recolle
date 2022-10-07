package com.example.rememberapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task WHERE id=:id")
    fun getTaskById(id: Int): Flow<Task>

    @Query("SELECT MIN(orderIndex) FROM task WHERE priority=:taskPriority")
    fun getNewOrderNumber(taskPriority: String): Flow<Task>

    @Query("UPDATE task SET orderIndex = orderIndex + 1 WHERE orderIndex>=:taskSortOrder")
    fun updateOrderNumbers(taskSortOrder: Int): Flow<Task>

    /**
     * Gets all tasks from database, with the highest priority ones on the top.
     *
     * Orders tasks of the same priority by task name.
     */
    @Query("SELECT * FROM task ORDER BY priority DESC, id ASC")
    fun getAllTasks(): Flow<List<Task>>
}