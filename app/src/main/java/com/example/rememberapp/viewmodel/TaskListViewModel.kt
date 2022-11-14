package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaskListViewModel(private val taskDao: TaskDao) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks().asLiveData()

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