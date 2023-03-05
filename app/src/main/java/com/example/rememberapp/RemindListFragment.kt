package com.example.rememberapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.RemindListViewModel
import com.example.rememberapp.viewmodel.RemindListViewModelFactory
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory

class RemindListFragment : Fragment() {

    private val viewModel: RemindListViewModel by activityViewModels {
        RemindListViewModelFactory(
            (activity?.application as RememberApplication).database.remindDao()
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

        // May not need this code if we disable clicking on headers. However, good for
        // readability for now.
        val adapter = RemindListAdapter {
            if(it is RemindListElement.Item) {
                val action = HomeFragmentDirections.actionHomeFragmentToRemindDetailFragment(it.reminder.id)
                this.findNavController().navigate(action)
            }
        }


    }
}