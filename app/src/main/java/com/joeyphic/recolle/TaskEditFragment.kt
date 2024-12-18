package com.joeyphic.recolle

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.joeyphic.recolle.TaskDetailFragmentArgs
import com.joeyphic.recolle.TaskEditFragmentDirections
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.databinding.TaskListAddItemBinding
import com.joeyphic.recolle.viewmodel.TaskEditViewModel
import com.joeyphic.recolle.viewmodel.TaskEditViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskEditFragment : Fragment() {

    private val viewModel: TaskEditViewModel by viewModels {
        TaskEditViewModelFactory(
            (activity?.application as RecolleApplication).database.taskDao()
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

    /*
    ----------------------------------------------------
    Parameters:   view (View), savedInstanceState (Bundle?)
    Description:  -First, we record the navigation argument "taskId". Then, we use it as a parameter
                   for the bind() function, which stores the Task in the viewModel.task
                   variable, and also configures the views to represent the Task.
                  -If no Task exists which has the same ID as the one in the navigation argument,
                   then we simply go back to TaskListFragment without making changes.
                  -Lastly, we hide keyboard when EditText is no longer focused. This is
                   intended to give room for the user to look at their changes before
                   finalizing them.
    ----------------------------------------------------
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.taskId

        // If ID exists, bind Task to fragment.
        if(id > 0) {
            bind(id)
        }
        // Otherwise, exit prematurely.
        else {
            val action = TaskEditFragmentDirections.actionTaskEditFragmentToHomeFragment(0)
            findNavController().navigate(action)
        }

        // Hide keyboard if EditText is not focused.
        binding.taskName.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        // TODO: Add ability to edit Tasks from TaskEditFragment
        // Until then..
        binding.labelSubtaskTitle.visibility = View.INVISIBLE
        binding.checkboxSubtask.visibility = View.INVISIBLE
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int)
    Description:  -Uses the IO thread to obtain the Task corresponding to parameter taskId.
                  -We use the currentTask variable as an intermediary. When we move back to the
                   main thread, then we can insert its value into viewModel.task without worrying
                   about race conditions.
                  -Next, we populate the EditText and radio buttons with the Task's
                   values. This shows the user its details before any edits are made.
                  -If isEntryValid() is true, then the Views have already been populated with
                   values. This would happen during configuration changes. Therefore, we should not
                   change the Views, as we'd be overwriting previous changes the user made.
    ----------------------------------------------------
    */
    private fun bind(taskId: Int) {

        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve Task using taskId
            val currentTask = viewModel.retrieveTask(taskId)

            withContext(Dispatchers.Main) {
                if(currentTask == null) {
                    val action = TaskEditFragmentDirections.actionTaskEditFragmentToHomeFragment(0)
                    findNavController().navigate(action)
                }
                else {
                    // Initialize viewModel.task
                    viewModel.task = currentTask
                    binding.buttonSave.setOnClickListener { updateTask() }

                    // Return early if Views already populated.
                    if(isEntryValid()) return@withContext

                    // Populate views with Task details.
                    binding.apply {
                        layoutTaskName.isHintAnimationEnabled = false
                        taskName.setText(viewModel.task.taskName, TextView.BufferType.SPANNABLE)
                        layoutTaskName.isHintAnimationEnabled = true

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

    /*
    ----------------------------------------------------
    Returns:      Boolean
    Description:  -Checks if priority RadioButton is selected, then sends to viewModel
                   to determine validity of both fields.
    ----------------------------------------------------
    */
    private fun isEntryValid(): Boolean {
        val currentPriority = getPriorityFromRadioId() ?: return false
        return viewModel.isEntryValid(binding.taskName.text.toString(), currentPriority)
    }

    /*
    ----------------------------------------------------
    Returns:      PriorityLevel?
    Description:  -Returns the PriorityLevel corresponding to which radio button
                   in the fragment is selected. Returns null if none are selected.
    ----------------------------------------------------
    */
    private fun getPriorityFromRadioId() : PriorityLevel? {
        return when (binding.radioGroupTaskPriority.checkedRadioButtonId) {
            binding.radioPriorityHigh.id -> PriorityLevel.HIGH
            binding.radioPriorityMedium.id -> PriorityLevel.MEDIUM
            binding.radioPriorityLow.id -> PriorityLevel.LOW
            else -> null
        }
    }

    /*
    ----------------------------------------------------
    Description:  -If isEntryValid() returns true, then the Task would still be valid even
                   after updating. Therefore, it is sent to the ViewModel to perform the update.
                  -Afterwards, we return to TaskListFragment.
    ----------------------------------------------------
    */
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

            val action = TaskEditFragmentDirections.actionTaskEditFragmentToHomeFragment(0)
            findNavController().navigate(action)
        }
    }

    // Sets _binding to null, avoiding memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}