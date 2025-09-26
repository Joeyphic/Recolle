package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.*
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.data.Subtask
import com.joeyphic.recolle.data.SubtaskDao
import com.joeyphic.recolle.data.Task
import com.joeyphic.recolle.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskEditViewModel(private val taskDao: TaskDao, private val subtaskDao: SubtaskDao) : ViewModel() {

    // Holds the task to be edited in this fragment.
    lateinit var task: Task

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      Task?
    Description:  -Retrieves the task from the database, using
                  its ID as identification.
                  -This is one of the earliest functions called in the Fragment,
                  as a Task needs to be supplied for editing.
    ----------------------------------------------------
    */
    fun retrieveTask(id: Int): Task? {
        return taskDao.getTaskById(id)
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
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.updateTask(task, isPriorityChanged)
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   id (Int)
    Returns:      List<Subtask>
    Description:  -Returns a List of Subtasks associated with the
                  given Task ID.
    ----------------------------------------------------
    */
    fun retrieveSubtasks(mainId: Int): List<Subtask> {
        return subtaskDao.getAllSubtasksByMainId(mainId)
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
        if(taskName.isBlank() || taskPriority !in PriorityLevel.entries.toTypedArray()) {
            return false
        }
        return true
    }
}

class TaskEditViewModelFactory(private val taskDao: TaskDao, private val subtaskDao: SubtaskDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(TaskEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskEditViewModel(taskDao, subtaskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}