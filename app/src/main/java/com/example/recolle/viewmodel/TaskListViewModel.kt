package com.example.recolle.viewmodel

import androidx.lifecycle.*
import com.example.recolle.data.Task
import com.example.recolle.data.TaskDao
import kotlinx.coroutines.launch

class TaskListViewModel(private val taskDao: TaskDao) : ViewModel() {

    // Contains every task in the database, sorted by their taskPosition value in ascending order.
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks().asLiveData()

    // Holds the state of allTasks as it was when the user was previously in TaskListFragment.
    var recordedTaskList: List<Task>? = null

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int), toPosition (Int)
    Description:  -Prepares an asynchronous thread that calls the taskDao to move the
                   position of a Task.
                  -As a result, the Task's index in allTasks will change, as well as
                   the taskListPosition values of all affected Tasks.
    ----------------------------------------------------
    */
    fun moveTaskPosition(taskId: Int, toPosition: Int) {
        viewModelScope.launch {
            taskDao.moveTask(taskId, toPosition)
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