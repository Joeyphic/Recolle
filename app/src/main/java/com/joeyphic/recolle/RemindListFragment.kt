package com.joeyphic.recolle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.joeyphic.recolle.HomeFragmentDirections
import com.joeyphic.recolle.databinding.RemindListFragmentBinding
import com.joeyphic.recolle.viewmodel.RemindListViewModel
import com.joeyphic.recolle.viewmodel.RemindListViewModelFactory

class RemindListFragment : Fragment() {

    private val viewModel: RemindListViewModel by activityViewModels {
        RemindListViewModelFactory(
            (activity?.application as RecolleApplication).database.remindDao()
        )
    }

    private var _binding: RemindListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = RemindListFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clearOutdatedCheckedReminders()

        // May not need the 'if statement' if we disable clicking on headers. However, good for
        // readability for now.
        val adapter = RemindListAdapter {
            if(it is RemindListElement.Item) {
                val action = HomeFragmentDirections.actionHomeFragmentToRemindDetailFragment(it.reminder.id)
                this.findNavController().navigate(action)
            }
        }

        binding.recyclerView.adapter = adapter

        viewModel.allReminders.observe(this.viewLifecycleOwner) { reminders ->
            adapter.submitList(viewModel.generateRemindList(reminders))
        }

        // Configure FAB
        binding.floatingActionButton.doOnLayout {
            binding.recyclerView.setPadding(0,0,0,
                (it.paddingBottom + it.measuredHeight)
            )
        }

        binding.floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToRemindAddFragment()
            this.findNavController().navigate(action)
        }

    }
}