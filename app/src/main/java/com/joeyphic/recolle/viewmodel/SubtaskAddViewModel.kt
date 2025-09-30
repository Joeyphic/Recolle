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

class SubtaskAddViewModel(private val subtaskDao: SubtaskDao, private val taskDao: TaskDao) :
    ViewModel() {

    // Holds the Task that is related to the subtasks created in this fragment.
    private lateinit var mainTask: Task

    // This fragment uses a list with Subtask items that have temporary id and mainTaskId values,
    // since the Task has not yet been inserted into the database.
    private var _currentSubtaskList = MutableStateFlow(listOf<Subtask>())
    var currentSubtaskList: StateFlow<List<Subtask>> = _currentSubtaskList

    /*
    ----------------------------------------------------
    Description:  -This function uses the class variables to gather and submit the Task and
                   Subtask values to the database for addition.
    ----------------------------------------------------
    */
    fun insert() {
        viewModelScope.launch(Dispatchers.IO) {
            // toInt() because that's the type in Task.kt
            val taskId = taskDao.insertTask(mainTask).toInt()

            _currentSubtaskList.value.forEach {
                subtaskDao.insert(
                    Subtask(
                        subtaskName = it.subtaskName,
                        checked = it.checked,
                        mainTaskId = taskId
                    )
                )
            }
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int), taskName (String), taskPriority (PriorityLevel)
    Description:  -Is a helper function that uses given information to create a Task within this
                   fragment.
                  -These parameters represent the Task that is chosen by the user to be added.
                  -taskListPosition is set to -1 as a temporary measure, During any insertions of
                  this Task (eg. when insertTask() is called), then this value will be corrected.
    ----------------------------------------------------
    */
    fun initializeMainTask(taskName: String, taskPriority: PriorityLevel) {
        mainTask = Task(
            taskName = taskName,
            taskPriority = taskPriority,
            taskListPosition = -1
        )
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
        val newSubtask = getNewSubtaskEntry(subtaskName)
        insertSubtaskToTemporaryList(newSubtask)
    }

    /*
    ----------------------------------------------------
    Parameters:   subtaskName (String)
    Returns:      Subtask
    Description:  -Is a helper function that uses a String to create a Subtask, and returns it.
                  -The Subtask ID is excluded because it will be auto-generated when inserted into
                   the database.
    ----------------------------------------------------
    */
    private fun getNewSubtaskEntry(subtaskName: String): Subtask {
        return Subtask(
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
}

class SubtaskAddViewModelFactory(private val subtaskDao: SubtaskDao, private val taskDao: TaskDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(SubtaskAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubtaskAddViewModel(subtaskDao, taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}