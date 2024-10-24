package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.data.Subtask
import com.joeyphic.recolle.data.SubtaskDao
import com.joeyphic.recolle.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: Modify all documentation to Subtasks instead of Tasks
class SubtaskAddViewModel(private val subtaskDao: SubtaskDao) : ViewModel() {

    // Holds the Task that is related to the subtasks created in this fragment.
    lateinit var mainTask: Task

    // This fragment uses a list with Subtask items that have temporary mainTaskId values,
    // since the Task has not yet been inserted into the database.
    var _currentSubtaskList = MutableStateFlow(mutableListOf<Subtask>())
    var currentSubtaskList: StateFlow<List<Subtask>> = _currentSubtaskList

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Inserts subtask into the currentSubtaskList. At this point, the parameter will
                   have a temporary value for mainTaskId.
    ----------------------------------------------------
    */
    private fun insertSubtaskToTemporaryList(subtask: Subtask) {
        _currentSubtaskList.value.add(subtask)
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Description:  -...
    ----------------------------------------------------
    */
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
                  During any insertions of this Task (ie. when insertTask() is
                  called), then this value will be corrected.
    ----------------------------------------------------
    */
    private fun getNewTaskEntry(subtaskName: String): Subtask {

        return Subtask(
            subtaskName = subtaskName,
            checked = false,
            mainTaskId = mainTask.id
        )
    }

    /*
    ----------------------------------------------------
    Parameters:   taskName (String), taskPriority (PriorityLevel)
    Returns:      Boolean
    Description:  -Ensures the taskName contains at least one character.
    ----------------------------------------------------
    */
    fun isEntryValid(subtaskName: String) : Boolean {
        return subtaskName.isNotBlank()
    }
}

class SubtaskAddViewModelFactory(private val subtaskDao: SubtaskDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(SubtaskAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubtaskAddViewModel(subtaskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}