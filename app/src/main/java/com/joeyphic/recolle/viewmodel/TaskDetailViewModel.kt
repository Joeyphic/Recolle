package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.*
import com.joeyphic.recolle.data.Subtask
import com.joeyphic.recolle.data.SubtaskDao
import com.joeyphic.recolle.data.Task
import com.joeyphic.recolle.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val taskDao: TaskDao, private val subtaskDao: SubtaskDao) :
    ViewModel() {

    lateinit var task: Task
    lateinit var allSubtasks: StateFlow<List<Subtask>>

    // If true, then task(^) has been removed from database, and we are preparing to leave fragment.
    var completeState = false

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      Task?
    Description:  -Retrieves the task from the database, using
                  its ID as identification.
    ----------------------------------------------------
    */
    fun retrieveTask(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Deletes the task from the database.
                  -The task affected should be the same one
                  obtained from retrieveTask() earlier.
                  -Its related Subtasks are also deleted.
    ----------------------------------------------------
    */
    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
            subtaskDao.deleteAllSubtasksByMainId(task.id)
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      Task?
    Description:  -Retrieves the task from the database, using
                  its ID as identification.
    ----------------------------------------------------
    */
    fun retrieveAllSubtasksFlow(mainId: Int): Flow<List<Subtask>> {
        return subtaskDao.getAllSubtasksByMainIdFlow(mainId)
    }

    fun subtaskCheckChange(subtask: Subtask) {
        viewModelScope.launch(Dispatchers.IO) {
            if (subtask.checked) {
                subtaskDao.uncheckReminderById(subtask.id)
            } else {
                subtaskDao.checkReminderById(subtask.id)
            }
        }
    }
}

class TaskDetailViewModelFactory(private val taskDao: TaskDao, private val subtaskDao: SubtaskDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(taskDao, subtaskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}