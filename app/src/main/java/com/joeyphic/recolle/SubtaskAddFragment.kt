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
import com.joeyphic.recolle.databinding.SubtaskListAddItemBinding
import com.joeyphic.recolle.viewmodel.SubtaskAddViewModel
import com.joeyphic.recolle.viewmodel.SubtaskAddViewModelFactory
import kotlinx.coroutines.launch

class SubtaskAddFragment : Fragment() {
    private val viewModel: SubtaskAddViewModel by viewModels {
        SubtaskAddViewModelFactory(
            (activity?.application as RecolleApplication).database.subtaskDao(),
            (activity?.application as RecolleApplication).database.taskDao()
        )
    }

    private var _binding: SubtaskListAddItemBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: SubtaskAddFragmentArgs by navArgs()


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
    Description:  TODO: Finish desc
    ----------------------------------------------------
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initializeMainTask(navigationArgs.taskName, navigationArgs.taskPriority)

        val adapter = SubtaskListAdapter {
            MaterialAlertDialogBuilder(this.requireContext())
                .setMessage("Remove subtask from being added?")
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
            viewModel.insert()

            val action = SubtaskAddFragmentDirections.actionSubtaskAddFragmentToHomeFragment(0)
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