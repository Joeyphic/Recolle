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
    fun getNewOrderNumber(taskPriority: String): Int

    @Query("SELECT COUNT(*) FROM task WHERE priority=:taskPriorityStr OR priority=:taskPriorityStr2")
    fun getNumberOfTasksByPriorities(taskPriorityStr: String, taskPriorityStr2: String = ""): Int

    @Query("UPDATE task SET orderIndex = orderIndex + 1 WHERE orderIndex>=:taskSortOrder")
    suspend fun updateOrderNumbersBeforeInsertion(taskSortOrder: Int)

    @Query("UPDATE task SET orderIndex = orderIndex - 1 WHERE orderIndex>=:taskSortOrder")
    suspend fun updateOrderNumbersAfterDeletion(taskSortOrder: Int)

    @Transaction
    suspend fun insertTask(task: Task) {
        task.taskSortOrder = when (task.taskPriority) {
            PriorityLevel.HIGH -> 0
            PriorityLevel.MEDIUM -> getNumberOfTasksByPriorities("HIGH")
            PriorityLevel.LOW ->
                getNumberOfTasksByPriorities("HIGH", "MEDIUM")
        }
        updateOrderNumbersBeforeInsertion(task.taskSortOrder)
        insert(task)
    }

    @Transaction
    suspend fun deleteTask(task: Task) {
        delete(task)
        updateOrderNumbersAfterDeletion(task.taskSortOrder)
    }

    /**
     * Gets all tasks from database, with the highest priority ones on the top.
     *
     * Orders tasks of the same priority by task name.
     */
    @Query("SELECT * FROM task ORDER BY orderIndex ASC")
    fun getAllTasks(): Flow<List<Task>>
}