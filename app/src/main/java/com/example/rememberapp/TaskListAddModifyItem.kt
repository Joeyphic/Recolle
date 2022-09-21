package com.example.rememberapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
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

    lateinit var task: Task
    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

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

        val id = navigationArgs.taskId

        // If there's an ID, link selected task to fragment.
        if(id > 0) {
            viewModel.retrieveTask(id).observe(this.viewLifecycleOwner) { selectedTask ->
                task = selectedTask
                bind(task)
            }
        }
        else {
            binding.buttonSave.setOnClickListener {
                addNewItem()
            }

            binding.radioGroupTaskPriority.check(binding.radioPriorityHigh.id)
            binding.radioGroupTaskPriority.jumpDrawablesToCurrentState()
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

    private fun bind(task: Task) {
        binding.apply {
            taskName.setText(task.taskName, TextView.BufferType.SPANNABLE)

            when (task.taskPriority) {
                PriorityLevel.LOW -> radioGroupTaskPriority.check(radioPriorityLow.id)
                PriorityLevel.MEDIUM -> radioGroupTaskPriority.check(radioPriorityMedium.id)
                PriorityLevel.HIGH -> radioGroupTaskPriority.check(radioPriorityHigh.id)
            }
            radioGroupTaskPriority.jumpDrawablesToCurrentState()

            buttonSave.setOnClickListener { updateTask() }
        }
    }

    private fun updateTask() {
        if(isEntryValid()) {
            viewModel.updateTask(
                this.navigationArgs.taskId,
                binding.taskName.text.toString(),
                getPriorityFromRadioId() ?: return // Since valid, should be non-null
            )

            val action = TaskListAddModifyItemDirections
                .actionTaskListAddModifyItemToTaskListFragment()
            findNavController().navigate(action)
        }
    }


}