package com.example.rememberapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.databinding.TaskListAddItemBinding
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory

class TaskListAddModifyItem : Fragment() {

    private val viewModel: TaskListViewModel by activityViewModels {
        TaskListViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private var _binding: TaskListAddItemBinding? = null
    // TODO: Find a way to remove this non-null asserted call
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = TaskListAddItemBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSave.setOnClickListener {
            addNewItem()
        }
    }



    private fun isEntryValid(): Boolean {
        val currentPriority = getPriorityFromRadioId() ?: return false
        return viewModel.isEntryValid(binding.taskName.text.toString(), currentPriority)
    }

    private fun addNewItem() {

        if(isEntryValid()) {
            // Should always be non-null since entry is validated, but the check is done anyways
            val currentPriority = getPriorityFromRadioId() ?: return

            viewModel.addNewItem(binding.taskName.text.toString(), currentPriority)

            val action = TaskListAddModifyItemDirections
                         .actionTaskListAddModifyItemToTaskListFragment()
            findNavController().navigate(action)
        }
    }

    private fun getPriorityFromRadioId() : PriorityLevel? {
        return when (binding.radioGroupTaskPriority.checkedRadioButtonId) {
            binding.radioPriorityHigh.id -> PriorityLevel.HIGH
            binding.radioPriorityMedium.id -> PriorityLevel.MEDIUM
            binding.radioPriorityLow.id -> PriorityLevel.LOW
            else -> null
        }
    }
}