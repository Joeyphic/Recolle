package com.example.rememberapp.viewmodel

import androidx.lifecycle.*
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.TaskDao
import kotlinx.coroutines.launch

class TaskAddModifyViewModel(private val taskDao: TaskDao) : ViewModel() {

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      LiveData<Task?>
    Description:  -Retrieves the task from the database, using
                  its ID as identification.
                  -This function is only called when the fragment is requesting to
                  be supplied with a Task for editing.
    ----------------------------------------------------
    */
    fun retrieveTask(id: Int): LiveData<Task?> {
        return taskDao.getTaskById(id).asLiveData()
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Prepares an asynchronous thread that the database
                  uses to insert the Task submitted in the parameter.
    ----------------------------------------------------
    */
    private fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Prepares an asynchronous thread that the database
                  uses to update the Task submitted in the parameter.
                  -The isPriorityChanged flag is important, as it signals that
                  the taskListPosition value of the Task needs to be recalibrated.
                  Otherwise, the task would be at an incorrect place in the list.
    ----------------------------------------------------
    */
    private fun updateTask(task: Task, isPriorityChanged: Boolean) {
        viewModelScope.launch {
            taskDao.updateTask(task, isPriorityChanged)
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Description:  -Uses the parameters to create a Task, then
                  sends it off to another function to add the
                  Task to the database asynchronously.
    ----------------------------------------------------
    */
    fun insertTask(taskName: String, taskPriority: PriorityLevel) {
        val newTask = getNewTaskEntry(taskName, taskPriority)
        insertTask(newTask)
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Returns:      Task
    Description:  -Is a helper function that uses a String and PriorityLevel
                  to create a Task, and returns it.
                  -taskListPosition is set to -1 as a temporary measure.
                  During any insertions of this Task (ie. when insertTask() is
                  called), then this value will be corrected.
    ----------------------------------------------------
    */
    private fun getNewTaskEntry(taskName: String, taskPriority: PriorityLevel): Task {

        return Task(
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = -1
        )
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Description:  -Uses the parameters to modify a Task, then
                  sends it off to another function to update the
                  Task from the database asynchronously.
                  -Only the taskName and taskPriority variables are
                  modifiable by the user within the fragment.
                  -The isPriorityChanged flag detects if the
                  PriorityLevel of the Task has been changed.
    ----------------------------------------------------
    */
    fun updateTask(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int, isPriorityChanged: Boolean) {
        val updatedTask = getUpdatedTaskEntry(taskId, taskName, taskPriority, taskListPosition)
        updateTask(updatedTask, isPriorityChanged)
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel), taskListPosition (Int)
    Returns:      Task
    Description:  -Is a helper function that uses a String and PriorityLevel
                  combined with an existing ID and taskListPosition to create an
                  updated Task, and returns it.
    ----------------------------------------------------
    */
    private fun getUpdatedTaskEntry(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int): Task {
        return Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = taskListPosition
        )
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Returns:      Boolean
    Description:  -Ensures the taskName contains at least one character, and
                  that the taskPriority is a valid PriorityLevel.
    ----------------------------------------------------
    */
    fun isEntryValid(taskName: String, taskPriority: PriorityLevel) : Boolean {
        if(taskName.isBlank() || taskPriority !in PriorityLevel.values()) {
            return false
        }
        return true
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