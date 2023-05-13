package com.joeyphic.recolle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task WHERE id=:id")
    fun getTaskById(id: Int): Task?

    @Query("SELECT COUNT(*) FROM task WHERE priority=:priority1 OR priority=:priority2")
    fun taskCountByPriority(priority1: PriorityLevel, priority2: PriorityLevel? = null): Int

    @Query("UPDATE task SET position = (position + :shiftAmount) WHERE position BETWEEN :firstIndex AND :lastIndex")
    suspend fun shiftTaskPositions(shiftAmount: Int, firstIndex: Int, lastIndex: Int)

    @Transaction
    suspend fun insertTask(task: Task) {
        task.taskListPosition = when (task.taskPriority) {
            PriorityLevel.HIGH -> 0
            PriorityLevel.MEDIUM -> taskCountByPriority(PriorityLevel.HIGH)
            PriorityLevel.LOW ->
                taskCountByPriority(PriorityLevel.HIGH, PriorityLevel.MEDIUM)
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
        else if(toPosition < fromPosition) {
            shiftTaskPositions(+1, toPosition, fromPosition - 1)
        }

        currentTask.taskListPosition = toPosition
        updateTask(currentTask, false)
    }

    /**
     * Gets all tasks from database, with the highest priority ones on the top.
     *
     * Orders tasks of the same priority by task name.
     */
    @Query("SELECT * FROM task ORDER BY position ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Only call from insertTask()
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    // Only call from updateTask()
    @Update
    suspend fun update(task: Task)

    // Only call from deleteTask()
    @Delete
    suspend fun delete(task: Task)
}