package com.example.recolle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.recolle.databinding.RemindListAddItemBinding
import com.example.recolle.viewmodel.RemindEditViewModel
import com.example.recolle.viewmodel.RemindEditViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemindEditFragment : Fragment() {

    private val viewModel: RemindEditViewModel by viewModels {
        RemindEditViewModelFactory(
            (activity?.application as RecolleApplication).database.remindDao()
        )
    }

    private var _binding: RemindListAddItemBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: RemindEditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = RemindListAddItemBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alarmScheduler = RemindAlarmScheduler(view.context)

        val id = navigationArgs.reminderId

        // If ID exists, bind Reminder to fragment.
        if(id > 0) {
            viewModel.retrieveAndBindReminder(id)
        }
        // Otherwise, exit prematurely.
        else {
            val action = RemindEditFragmentDirections.actionRemindEditFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.reminderName.collect {
                        binding.reminderName.setText(it ?: "")
                    }
                }

                launch {
                    viewModel.eventDate.collect {
                        binding.eventDate.setText(it?.format(viewModel.dateFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.eventTime.collect {
                        binding.eventTime.setText(it?.format(viewModel.timeFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.remindDate.collect {
                        binding.remindDate.setText(it?.format(viewModel.dateFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.remindTime.collect {
                        binding.remindTime.setText(it?.format(viewModel.timeFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.uiState.collect {
                        if(it.isAutoEnabled) binding.autoButton.isEnabled = true
                        if(it.isSaveEnabled) binding.saveButton.isEnabled = true
                        if(it.errorMessage != null) {
                            withContext(Dispatchers.Main) {
                                context?.let { ctx ->
                                    MaterialAlertDialogBuilder(ctx)
                                        .setTitle("Invalid Reminder")
                                        .setMessage(it.errorMessage)
                                        .setPositiveButton("OK") { _, _ ->
                                            viewModel.errorMessageShown()
                                        }
                                        .setOnDismissListener {
                                            viewModel.errorMessageShown()
                                        }
                                        .show()
                                }
                            }
                        }
                        if(it.picker != null) {
                            it.picker?.show(parentFragmentManager, "RemindAddFragment")
                        }
                    }
                }
            }
        }

        binding.eventDate.setOnClickListener { viewModel.initializeEventDatePicker() }
        binding.eventTime.setOnClickListener { viewModel.initializeEventTimePicker() }
        binding.remindDate.setOnClickListener { viewModel.initializeRemindDatePicker() }
        binding.remindTime.setOnClickListener { viewModel.initializeRemindTimePicker() }

        binding.autoButton.setOnClickListener {
            viewModel.autoSetRemindVariables()
        }

        binding.saveButton.setOnClickListener {
            val newReminder = viewModel.createUpdatedReminderOrNull(binding.reminderName.text.toString())
                ?: return@setOnClickListener

            viewModel.updateReminder(newReminder)
            alarmScheduler.schedule(newReminder)

            val action = RemindEditFragmentDirections.actionRemindEditFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

}