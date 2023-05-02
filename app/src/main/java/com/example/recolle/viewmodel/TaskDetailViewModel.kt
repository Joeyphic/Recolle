package com.example.recolle.viewmodel

import androidx.lifecycle.*
import com.example.recolle.data.Task
import com.example.recolle.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val taskDao: TaskDao) : ViewModel() {

    lateinit var task: Task

    // If true, then task(^) has been removed from database, and we are preparing to leave fragment.
    var completeState = false

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      LiveData<Task?>
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
    ----------------------------------------------------
    */
    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
        }
    }

}

class TaskDetailViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}