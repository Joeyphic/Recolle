package com.example.rememberapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rememberapp.data.Task
import com.example.rememberapp.databinding.TaskDetailFragmentBinding
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory

class TaskDetailFragment : Fragment() {

    private val viewModel: TaskListViewModel by activityViewModels {
        TaskListViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

    private var _binding: TaskDetailFragmentBinding? = null

    // TODO: Find a way to remove this non-null asserted call
    private val binding get() = _binding!!

    lateinit var task: Task
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = TaskDetailFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    private fun bind(task: Task) {
        binding.apply {
            taskName.text = task.taskName

            // TODO: Capitalize the first letter cleanly. Do after enum names are finalized.
            taskPriority.text = task.taskPriority.name.lowercase()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigation argument
        val id = navigationArgs.taskId
        viewModel.retrieveTask(id).observe(this.viewLifecycleOwner) { selectedTask ->
            task = selectedTask
            bind(task)
        }

        // Using MenuProvider to add Edit and Delete options to the top app bar.
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.detail_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                when (menuItem.itemId) {
                    R.id.edit -> {
                        editTask(task)
                        true
                    }
                    R.id.delete -> {
                        true
                    }
                    else -> false
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun editTask(task: Task) {
        val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTaskListAddModifyItem(
            "Edit Task",
            task.id
        )

        this.findNavController().navigate(action)
    }
}