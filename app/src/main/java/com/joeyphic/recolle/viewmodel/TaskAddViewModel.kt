package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.*
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.data.Task
import com.joeyphic.recolle.data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAddViewModel(private val taskDao: TaskDao,
    private val applicationScope: CoroutineScope) : ViewModel() {

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Prepares an asynchronous thread that the database
                  uses to insert the Task submitted in the parameter.
    ----------------------------------------------------
    */
    private fun insertTask(task: Task) {
        applicationScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
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
    Returns:      Boolean
    Description:  -Ensures the taskName contains at least one character, and
                  that the taskPriority is a valid PriorityLevel.
    ----------------------------------------------------
    */
    fun isEntryValid(taskName: String, taskPriority: PriorityLevel) : Boolean {
        if(taskName.isBlank() || taskPriority !in PriorityLevel.entries.toTypedArray()) {
            return false
        }
        return true
    }
}

class TaskAddViewModelFactory(private val taskDao: TaskDao,
                              private val applicationScope: CoroutineScope) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskAddViewModel(taskDao, applicationScope) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}