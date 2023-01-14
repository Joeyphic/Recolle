package com.example.rememberapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TaskListAdapter {
            val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(it.id)
            this.findNavController().navigate(action)
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if(positionStart == 0) { binding.recyclerView.scrollToPosition(0) }
            }
        })

        binding.recyclerView.adapter = adapter

        viewModel.recordedTaskList?.let {
            adapter.submitList(viewModel.recordedTaskList)
        }
        viewModel.allTasks.observe(this.viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            viewModel.recordedTaskList = tasks
        }

        val itemTouchHelper = ItemTouchHelper(TaskListAdapter.TaskTouchHelper(adapter) { taskId, to ->
            viewModel.moveTaskPosition(taskId, to)
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.floatingActionButton.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToTaskAddFragment()
            this.findNavController().navigate(action)
        }
    }

    // Sets _binding to null, avoiding memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}