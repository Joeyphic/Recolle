package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaskListViewModel(private val taskDao: TaskDao) : ViewModel() {

    // Contains every task in the database, sorted by their taskPosition value in ascending order.
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks().asLiveData()

    // Holds the state of allTasks as it was when the user was previously in TaskListFragment.
    var recordedTaskList: List<Task>? = null

    /*
    ----------------------------------------------------
    Parameters:   fromPosition (Int), toPosition (Int)
    Description:  -Prepares an asynchronous thread that calls
                  the taskDao to move the position of a task.
                  -As a result, the task's index in allTasks will
                  change, as well as the taskPosition values of
                  all affected Tasks.
    ----------------------------------------------------
    */
    fun moveTaskPosition(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            taskDao.moveTask(fromPosition, toPosition)
        }
    }
}

class TaskListViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}