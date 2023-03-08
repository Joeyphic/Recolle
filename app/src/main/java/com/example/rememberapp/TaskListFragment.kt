package com.example.rememberapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory

class TaskListFragment : Fragment() {

    private val viewModel: TaskListViewModel by activityViewModels {
        TaskListViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private var _binding: TaskListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = TaskListFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    /*
    ----------------------------------------------------
    Parameters:   view (View), savedInstanceState (Bundle?)
    Description:  -First, the adapter is initialized and linked to the RecyclerView. The ViewModel
                   persists throughout the app, so we can call upon a previous state of the list
                   (recordedTaskList) in order to show changes made by the user in other fragments.
                  -Next, the FAB button is configured. Most notably, its height and padding is used
                   to influence the RecyclerView's padding, so it is never covering any details.
                  -Lastly, the ItemTouchHelper is attached to the RecyclerView, which allows the
                   user to drag TaskList items.
    ----------------------------------------------------
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Adapter
        val adapter = TaskListAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToTaskDetailFragment(it.id)
            this.findNavController().navigate(action)
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if(positionStart == 0) { binding.recyclerView.scrollToPosition(0) }
            }
        })

        // TODO: Fix Dragging not working when returning from RemindAddFragment
        binding.recyclerView.adapter = adapter

        viewModel.recordedTaskList?.let {
            adapter.submitList(viewModel.recordedTaskList)
        }
        viewModel.allTasks.observe(this.viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            viewModel.recordedTaskList = tasks
        }

        // Configure FAB
        binding.floatingActionButton.doOnLayout {
            binding.recyclerView.setPadding(0,0,0,
                (it.paddingBottom + it.measuredHeight)
            )
        }

        binding.floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTaskAddFragment()
            this.findNavController().navigate(action)
        }

        // Attach ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(TaskListAdapter.TaskTouchHelper(adapter) { taskId, to ->
            viewModel.moveTaskPosition(taskId, to)
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    // Sets _binding to null, avoiding memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}