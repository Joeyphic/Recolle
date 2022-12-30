package com.example.rememberapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.databinding.TaskListAddItemBinding
import com.example.rememberapp.viewmodel.TaskEditViewModel
import com.example.rememberapp.viewmodel.TaskEditViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class TaskEditFragment : Fragment() {

    private val viewModel: TaskEditViewModel by viewModels {
        TaskEditViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private var _binding: TaskListAddItemBinding? = null
    private val binding get() = _binding!!

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
            bind(id)
        }
        // Otherwise, there has been invalid input and we navigate back to the Task List.
        else {
            val action = TaskEditFragmentDirections.actionTaskEditFragmentToTaskListFragment()
            findNavController().navigate(action)
        }

        // Hides keyboard when taskName (EditText) is no longer focused.
        binding.taskName.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    private fun isEntryValid(): Boolean {
        val currentPriority = getPriorityFromRadioId() ?: return false
        return viewModel.isEntryValid(binding.taskName.text.toString(), currentPriority)
    }

    private fun getPriorityFromRadioId() : PriorityLevel? {
        return when (binding.radioGroupTaskPriority.checkedRadioButtonId) {
            binding.radioPriorityHigh.id -> PriorityLevel.HIGH
            binding.radioPriorityMedium.id -> PriorityLevel.MEDIUM
            binding.radioPriorityLow.id -> PriorityLevel.LOW
            else -> null
        }
    }

    // TODO: Separate into two functions. Too much happening at once.
    private fun bind(taskId: Int) {

        lifecycleScope.launch(Dispatchers.IO) {
            val currentTask = viewModel.retrieveTask(taskId)

            withContext(Dispatchers.Main) {
                if(currentTask == null) {
                    val action = TaskEditFragmentDirections
                        .actionTaskEditFragmentToTaskListFragment()
                    findNavController().navigate(action)
                }
                else {
                    viewModel.task = currentTask
                    binding.buttonSave.setOnClickListener { updateTask() }

                    // EditText and RadioButton config changes are saved. Since the fragment's
                    // default state is an invalid input, that means it's safe to override.
                    if(isEntryValid()) return@withContext

                    binding.apply {
                        layoutTaskName.isHintAnimationEnabled = false
                        taskName.setText(viewModel.task.taskName, TextView.BufferType.SPANNABLE)

                        when (viewModel.task.taskPriority) {
                            PriorityLevel.LOW -> radioGroupTaskPriority.check(radioPriorityLow.id)
                            PriorityLevel.MEDIUM -> radioGroupTaskPriority.check(radioPriorityMedium.id)
                            PriorityLevel.HIGH -> radioGroupTaskPriority.check(radioPriorityHigh.id)
                        }
                        radioGroupTaskPriority.jumpDrawablesToCurrentState()
                    }
                }
            }
        }
    }

    private fun updateTask() {
        if(isEntryValid()) {
            val updatedPriority = getPriorityFromRadioId() ?: return // Since valid, is non-null
            val isPriorityChanged = viewModel.task.taskPriority != updatedPriority

            viewModel.updateTask(
                this.navigationArgs.taskId,
                binding.taskName.text.toString(),
                updatedPriority,
                viewModel.task.taskListPosition,
                isPriorityChanged
            )

            val action = TaskEditFragmentDirections
                .actionTaskEditFragmentToTaskListFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}