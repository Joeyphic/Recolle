package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    // This fragment uses a list with Subtask items that have temporary id values.
    private var _currentSubtaskList = MutableStateFlow(listOf<Subtask>())
    var currentSubtaskList: StateFlow<List<Subtask>> = _currentSubtaskList

    /*
    ----------------------------------------------------
    Description:  -This function uses the class variables to gather and submit the Task and
                   Subtask values to the database for editing.
    ----------------------------------------------------
    */
    fun update() {
        viewModelScope.launch(Dispatchers.IO) {
            updateTask(mainTask, isMainTaskPriorityChanged)
            subtaskDao.refreshSubtasksForTask(mainTask.id, _currentSubtaskList.value)
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int), taskName (String), taskPriority (PriorityLevel), taskListPosition (Int)
    Description:  -Is a helper function that uses given information to create a Task within this
                   fragment. Also sets the flag representing if the main's Task priority is changed.
                  -These parameters represent the Task that is chosen by the user to be modified.
    ----------------------------------------------------
    */
    fun initializeMainTask(taskId: Int, taskName: String, taskPriority: PriorityLevel, taskListPosition: Int, isPriorityChanged: Boolean) {
        mainTask = Task(
            id = taskId,
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = taskListPosition
        )
        isMainTaskPriorityChanged = isPriorityChanged
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int), taskName (String), taskListPosition (Int), taskPriority (PriorityLevel)
    Description:  -Is a helper function that uses given information to create a List of Subtasks
                   within this fragment.
                  -This parameter represent the Subtasks that are chosen by the user to be modified.
    ----------------------------------------------------
    */
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
        _currentSubtaskList.value = _currentSubtaskList.value.sortedBy { subtask -> subtask.checked }
    }

    fun insertSubtaskToTemporaryList(subtaskName: String) {
        val newSubtask = getNewSubtaskEntry(subtaskName)
        insertSubtaskToTemporaryList(newSubtask)
    }

    /*
    ----------------------------------------------------
    Parameters:   subtaskName (String)
    Returns:      Subtask
    Description:  -Is a helper function that uses a String to create a Subtask, and returns it.
                  -We know the ID of the Task to be modified, since it will be initialized by this
                   point. As a result, we can attach it to the Subtasks.
    ----------------------------------------------------
    */
    private fun getNewSubtaskEntry(subtaskName: String): Subtask {
        return Subtask(
            subtaskName = subtaskName,
            checked = false,
            mainTaskId = mainTask.id
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