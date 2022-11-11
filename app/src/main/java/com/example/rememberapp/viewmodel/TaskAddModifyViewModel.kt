package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.launch

class TaskAddModifyViewModel(private val taskDao: TaskDao) : ViewModel() {

    private fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    private fun updateTask(task: Task, isPriorityChanged: Boolean) {
        viewModelScope.launch {
            taskDao.updateTask(task, isPriorityChanged)
        }
    }

    private fun getNewTaskEntry(taskName: String, taskPriority: PriorityLevel): Task {

        return Task(
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = -1
        )
    }

    fun isEntryValid(taskName: String, taskPriority: PriorityLevel) : Boolean {
        if(taskName.isBlank() || taskPriority !in PriorityLevel.values()) {
            return false
        }
        return true
    }

    fun addNewItem(taskName: String, taskPriority: PriorityLevel) {
        val newTask = getNewTaskEntry(taskName, taskPriority)
        insertTask(newTask)
    }

    fun retrieveTask(id: Int): LiveData<Task?> {
        return taskDao.getTaskById(id).asLiveData()
    }

    fun updateTask(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int, isPriorityChanged: Boolean) {
        val updatedTask = getUpdatedTaskEntry(taskId, taskName, taskPriority, taskListPosition)
        updateTask(updatedTask, isPriorityChanged)
    }

    private fun getUpdatedTaskEntry(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int): Task {
        return Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = taskListPosition
        )
    }
}

class TaskAddModifyViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskAddModifyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskAddModifyViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}