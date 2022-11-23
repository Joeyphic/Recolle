package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val taskDao: TaskDao) : ViewModel() {

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      LiveData<Task?>
    Description:  -Retrieves the task from the database, using
                  its ID as identification.
                  -Returns LiveData since it will be used to
                  display updated data to views.
    ----------------------------------------------------
    */
    fun retrieveTask(id: Int): LiveData<Task?> {
        return taskDao.getTaskById(id).asLiveData()
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
        viewModelScope.launch {
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