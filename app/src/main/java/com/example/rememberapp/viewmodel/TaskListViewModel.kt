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
            taskDao.insertTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
        }
    }

    fun moveTaskPosition(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            taskDao.moveTask(fromPosition, toPosition)
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

    fun retrieveTask(id: Int): LiveData<Task> {
        return taskDao.getTaskById(id).asLiveData()
    }

    fun updateTask(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int) {
        val updatedTask = getUpdatedTaskEntry(taskId, taskName, taskPriority, taskListPosition)
        updateTask(updatedTask)
    }

    private fun getUpdatedTaskEntry(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int): Task {
        return Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = taskListPosition
        )
    }

    fun completeTask(task: Task) {
        deleteTask(task)
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