package com.joeyphic.recolle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.databinding.SubtaskListAddItemBinding
import com.joeyphic.recolle.viewmodel.SubtaskEditViewModel
import com.joeyphic.recolle.viewmodel.SubtaskEditViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class SubtaskEditFragment : Fragment() {
    private val viewModel: SubtaskEditViewModel by viewModels {
        SubtaskEditViewModelFactory(
            (activity?.application as RecolleApplication).database.subtaskDao(),
            (activity?.application as RecolleApplication).database.taskDao(),
            (activity?.application as RecolleApplication).applicationScope
        )
    }

    private var _binding: SubtaskListAddItemBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: SubtaskEditFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = SubtaskListAddItemBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    /*
    ----------------------------------------------------
    Parameters:   view (View), savedInstanceState (Bundle?)
    Description:  -Uses the navigation arguments to initialize the Task and prepare it for addition
                   into the database once the Subtasks have been chosen. Then, prepares the
                   SubtaskListAdapter and associated buttons.
                  -The Task is still stored in this Fragment because the Task in the database may no
                   longer hold the most updated information about the Task. If the user backs out
                   of editing the Task, the information in the database should stay unchanged.
    ----------------------------------------------------
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val priorityArgument = when (navigationArgs.taskPriority) {
            0 -> PriorityLevel.HIGH
            1 -> PriorityLevel.MEDIUM
            2 -> PriorityLevel.LOW
            else -> {PriorityLevel.LOW}
        }

        viewModel.initializeMainTask(navigationArgs.taskId, navigationArgs.taskName, priorityArgument, navigationArgs.taskListPosition, navigationArgs.isPriorityChanged)

        val adapter = SubtaskListAdapter {
            MaterialAlertDialogBuilder(this.requireContext())
                .setMessage("Remove subtask?")
                .setNegativeButton("Cancel") { dialog, which ->
                    // Do nothing
                }
                .setPositiveButton("OK") { dialog, which ->
                    viewModel.removeSubtaskFromTemporaryList(it)
                }
                .show()
        }

        binding.recyclerViewSubtask.adapter = adapter

        lifecycleScope.launch {
            viewModel.currentSubtaskList.collect { subtasks ->
                adapter.submitList(subtasks)
            }
        }

        binding.buttonAddSubtask.setOnClickListener {
            createSubtask()
        }

        binding.buttonSave.setOnClickListener {
            addNewItem()
        }

        bind(navigationArgs.taskId)
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int)
    Description:  -Retrieves the Subtasks from the database and submits them to a function that
                   will transfer the information to a class variable.
    ----------------------------------------------------
    */
    private fun bind(taskId: Int) {

        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve associated Subtasks using taskId
            val currentSubtasks = viewModel.retrieveSubtasks(taskId)

            withContext(Dispatchers.Main) {
                viewModel.initializeSubtasks(currentSubtasks)
            }
        }
    }
    /*
    ----------------------------------------------------
    Description:  -If isEntryValid() returns true, then the Subtask is suitable to be added to the
                   database. As such, it is sent to the ViewModel to perform the insertion.
                  -Afterwards, we return to TaskListFragment.
    ----------------------------------------------------
    */
    private fun createSubtask() {
        if(isSubtaskEntryValid()) {
            viewModel.insertSubtaskToTemporaryList(binding.subtaskName.text.toString())
        }
        binding.subtaskName.setText("")
    }

    /*
    ----------------------------------------------------
    Description:  -If isEntryValid() returns true, then the Subtask is suitable to be added to the
                   database. As such, it is sent to the ViewModel to perform the insertion.
                  -Afterwards, we return to TaskListFragment.
    ----------------------------------------------------
    */
    private fun addNewItem() {

        if(isEntryValid()) {
            viewModel.update()

            val action = SubtaskEditFragmentDirections.actionSubtaskEditFragmentToHomeFragment(0)
            findNavController().navigate(action)
        }
    }

    /*
    ----------------------------------------------------
    Returns:      Boolean
    Description:  -Sends to viewModel to determine validity of the text inside the text field.
    ----------------------------------------------------
    */
    private fun isSubtaskEntryValid(): Boolean {
        return viewModel.isSubtaskEntryValid(binding.subtaskName.text.toString())
    }

    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid()
    }
}