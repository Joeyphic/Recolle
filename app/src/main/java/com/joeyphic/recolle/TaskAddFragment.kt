package com.joeyphic.recolle

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.joeyphic.recolle.TaskAddFragmentDirections
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.databinding.TaskListAddItemBinding
import com.joeyphic.recolle.viewmodel.TaskAddViewModel
import com.joeyphic.recolle.viewmodel.TaskAddViewModelFactory


class TaskAddFragment : Fragment() {

    private val viewModel: TaskAddViewModel by viewModels {
        TaskAddViewModelFactory(
            (activity?.application as RecolleApplication).database.taskDao()
        )
    }

    private var _binding: TaskListAddItemBinding? = null
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

    /*
    ----------------------------------------------------
    Parameters:   view (View), savedInstanceState (Bundle?)
    Description:  -We set the default state of the screen, which is binding the save button
                   to addNewItem(), and setting the radio button to the highest priority.
                  -We hide the keyboard when EditText is no longer focused. This is
                   intended to give room for the user to look at the details of their
                   new Task before submitting it.
    ----------------------------------------------------
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSave.setOnClickListener {
            addNewItem()
        }

        binding.radioGroupTaskPriority.check(binding.radioPriorityHigh.id)
        binding.radioGroupTaskPriority.jumpDrawablesToCurrentState()

        // Hides keyboard when taskName (EditText) is no longer focused.
        binding.taskName.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                val imm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        binding.checkboxSubtask.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.buttonSave.text = getString(R.string.next)
                binding.buttonSave.setOnClickListener {
                    addSubtasks()
                }
            }
            else {
                binding.buttonSave.text = getString(R.string.save)
                binding.buttonSave.setOnClickListener {
                    addNewItem()
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

    private fun addSubtasks() {

        if(isEntryValid()) {
            val taskName = binding.taskName.text.toString()
            // Should always be non-null since entry is validated, but the check is done anyways
            val currentPriority = getPriorityFromRadioId() ?: return

            // TODO: We can use PriorityLevel in NavArgs, but we'll have to make PriorityLevel its
            //       own enum file. Perhaps call it taskPriorityLevel.
            val priorityArgument = when(currentPriority) {
                PriorityLevel.HIGH -> 0
                PriorityLevel.MEDIUM -> 1
                PriorityLevel.LOW -> 2
            }
            val action = TaskAddFragmentDirections.actionTaskAddFragmentToSubtaskAddFragment(
                taskName, priorityArgument
            )
            findNavController().navigate(action)
        }
    }

    /*
    ----------------------------------------------------
    Description:  -If isEntryValid() returns true, then the Task is suitable to be added to the
                   database. As such, it is sent to the ViewModel to perform the insertion.
                  -Afterwards, we return to TaskListFragment.
    ----------------------------------------------------
    */
    private fun addNewItem() {

        if(isEntryValid()) {
            // Should always be non-null since entry is validated, but the check is done anyways
            val currentPriority = getPriorityFromRadioId() ?: return

            viewModel.insertTask(binding.taskName.text.toString(), currentPriority)

            val action = TaskAddFragmentDirections.actionTaskAddFragmentToHomeFragment(0)
            findNavController().navigate(action)
        }
    }

    // Sets _binding to null, avoiding memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}