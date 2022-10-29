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

    @Query("SELECT * FROM task WHERE position=:position")
    fun getTaskByPosition(position: Int): Task

    @Query("SELECT COUNT(*) FROM task WHERE priority=:taskPriorityStr OR priority=:taskPriorityStr2")
    fun getNumberOfTasksByPriorities(taskPriorityStr: String, taskPriorityStr2: String = ""): Int

    @Query("UPDATE task SET position = position + 1 WHERE position>=:taskSortOrder")
    suspend fun updatePositionsBeforeInsertion(taskSortOrder: Int)

    @Query("UPDATE task SET position = position - 1 WHERE position>=:taskSortOrder")
    suspend fun updatePositionsAfterDeletion(taskSortOrder: Int)

    @Query("UPDATE task SET position = (position + :shiftAmount) WHERE position BETWEEN :taskFromPosition AND :taskToPosition")
    suspend fun shiftTaskPositions(shiftAmount: Int, taskFromPosition: Int, taskToPosition: Int)

    @Transaction
    suspend fun insertTask(task: Task) {
        task.taskListPosition = when (task.taskPriority) {
            PriorityLevel.HIGH -> 0
            PriorityLevel.MEDIUM -> getNumberOfTasksByPriorities("HIGH")
            PriorityLevel.LOW ->
                getNumberOfTasksByPriorities("HIGH", "MEDIUM")
        }
        updatePositionsBeforeInsertion(task.taskListPosition)
        insert(task)
    }

    @Transaction
    suspend fun deleteTask(task: Task) {
        delete(task)
        updatePositionsAfterDeletion(task.taskListPosition)
    }

    @Transaction
    suspend fun updateTask(task: Task, isPriorityChanged: Boolean) {

        if(isPriorityChanged) {
            val oldTask = getTaskByPosition(task.taskListPosition)
            deleteTask(oldTask)
            insertTask(task)
        }
        else {
            update(task)
        }
    }

    @Transaction
    suspend fun moveTask(taskFromPosition: Int, taskToPosition: Int) {

        val currentTask = getTaskByPosition(taskFromPosition)

        // lower to higher
        if(taskFromPosition < taskToPosition) {
            shiftTaskPositions(-1, taskFromPosition + 1, taskToPosition)
        }
        // higher to lower
        else {
            shiftTaskPositions(+1, taskToPosition, taskFromPosition - 1)
        }

        currentTask.taskListPosition = taskToPosition
        update(currentTask)
    }

    /**
     * Gets all tasks from database, with the highest priority ones on the top.
     *
     * Orders tasks of the same priority by task name.
     */
    @Query("SELECT * FROM task ORDER BY position ASC")
    fun getAllTasks(): Flow<List<Task>>
}