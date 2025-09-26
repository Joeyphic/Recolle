package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavArgs
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.data.Subtask
import com.joeyphic.recolle.data.SubtaskDao
import com.joeyphic.recolle.data.Task
import com.joeyphic.recolle.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: Modify all documentation to Subtasks instead of Tasks
class SubtaskEditViewModel(private val subtaskDao: SubtaskDao, private val taskDao: TaskDao) :
    ViewModel() {

    // Holds the Task that is related to the subtasks created in this fragment.
    lateinit var mainTask: Task

    // Set to false as a placeholder
    var isMainTaskPriorityChanged: Boolean = false

    lateinit var subtask: Subtask

    // This fragment uses a list with Subtask items that have temporary mainTaskId values,
    // since the Task has not yet been inserted into the database.
    private var _currentSubtaskList = MutableStateFlow(listOf<Subtask>())
    var currentSubtaskList: StateFlow<List<Subtask>> = _currentSubtaskList

    fun update() {
        viewModelScope.launch(Dispatchers.IO) {

            updateTask(mainTask, isMainTaskPriorityChanged)

            subtaskDao.deleteAllSubtasksByMainId(mainTask.id)

            _currentSubtaskList.value.forEach {
                subtaskDao.insert(
                    Subtask(
                        subtaskName = it.subtaskName,
                        checked = it.checked,
                        mainTaskId = mainTask.id
                    )
                )
            }
        }
    }

    fun initializeMainTask(taskId: Int, taskName: String, taskPriority: PriorityLevel) {

        mainTask = Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = -1
        )
    }

    fun initializeSubtasks(subtaskList: List<Subtask>) {
        _currentSubtaskList.value = subtaskList
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Inserts subtask into the currentSubtaskList. At this point, the parameter will
                   have a temporary value for mainTaskId.
    ----------------------------------------------------
    */
    private fun insertSubtaskToTemporaryList(subtask: Subtask) {
        _currentSubtaskList.value = _currentSubtaskList.value.plus(subtask)
    }

    fun insertSubtaskToTemporaryList(subtaskName: String) {
        val newSubtask = getNewTaskEntry(subtaskName)
        insertSubtaskToTemporaryList(newSubtask)
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Returns:      Task
    Description:  -Is a helper function that uses a String and PriorityLevel
                  to create a Task, and returns it.
                  -taskListPosition is set to -1 as a temporary measure.
                  During any insertions of this Task (eg. when insertTask() is
                  called), then this value will be corrected.
    ----------------------------------------------------
    */
    private fun getNewTaskEntry(subtaskName: String): Subtask {

        val subtaskTempId = -1

        return Subtask(
            id = subtaskTempId,
            subtaskName = subtaskName,
            checked = false,
            mainTaskId = -1
        )
    }

    fun isSubtaskEntryValid(subtaskName: String) : Boolean {
        return subtaskName.isNotBlank()
    }

    fun isEntryValid() : Boolean {
        return _currentSubtaskList.value.isNotEmpty()
    }

    fun removeSubtaskFromTemporaryList(subtask: Subtask) {
        _currentSubtaskList.value = _currentSubtaskList.value.minus(subtask)
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
}

class SubtaskEditViewModelFactory(private val subtaskDao: SubtaskDao, private val taskDao: TaskDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(SubtaskEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubtaskEditViewModel(subtaskDao, taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}