package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.launch

class TaskListViewModel(private val taskDao: TaskDao) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks().asLiveData()

    private fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
        }
    }

    private fun getNewTaskEntry(taskName: String, taskPriority: PriorityLevel): Task {
        return Task(
            taskName = taskName,
            taskPriority = taskPriority
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

    fun retrieveTask(id: Int): LiveData<Task> {
        return taskDao.getTaskById(id).asLiveData()
    }

    fun updateTask(taskId: Int, taskName: String, taskPriority: PriorityLevel) {
        val updatedTask = getUpdatedTaskEntry(taskId, taskName, taskPriority)
        updateTask(updatedTask)
    }

    private fun getUpdatedTaskEntry(taskId: Int, taskName: String, taskPriority: PriorityLevel): Task {
        return Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority
        )
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