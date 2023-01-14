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
    fun getTaskById(id: Int): Task?

    @Query("SELECT COUNT(*) FROM task WHERE priority=:taskPriorityStr OR priority=:taskPriorityStr2")
    fun getNumberOfTasksByPriorities(taskPriorityStr: String, taskPriorityStr2: String = ""): Int

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
        shiftTaskPositions(+1, task.taskListPosition, Int.MAX_VALUE)
        insert(task)
    }

    @Transaction
    suspend fun deleteTask(task: Task) {

        getTaskById(task.id) ?: return

        delete(task)
        shiftTaskPositions(-1, task.taskListPosition, Int.MAX_VALUE)
    }

    @Transaction
    suspend fun updateTask(task: Task, isPriorityChanged: Boolean) {

        if(isPriorityChanged) {
            val oldTask = getTaskById(task.id) ?: return
            deleteTask(oldTask)
            insertTask(task)
        }
        else {
            update(task)
        }
    }

    @Transaction
    suspend fun moveTask(taskId: Int, toPosition: Int) {

        val currentTask = getTaskById(taskId) ?: return
        val fromPosition = currentTask.taskListPosition

        // lower to higher
        if(fromPosition < toPosition) {
            shiftTaskPositions(-1, fromPosition + 1, toPosition)
        }
        // higher to lower
        else if(fromPosition > toPosition) {
            shiftTaskPositions(+1, toPosition, fromPosition - 1)
        }

        currentTask.taskListPosition = toPosition
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