package com.example.rememberapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rememberapp.databinding.RemindListAddItemBinding
import com.example.rememberapp.viewmodel.RemindAddViewModel
import com.example.rememberapp.viewmodel.RemindAddViewModelFactory
import com.example.rememberapp.viewmodel.TaskAddViewModel
import com.example.rememberapp.viewmodel.TaskAddViewModelFactory
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter

class RemindAddFragment : Fragment() {

    private val viewModel: RemindAddViewModel by viewModels {
        RemindAddViewModelFactory(
            (activity?.application as RememberApplication).database.remindDao()
        )
    }

    private var _binding: RemindListAddItemBinding? = null
    private val binding get() = _binding!!

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

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
                    }
                }
            }
        }

        //TODO: Fix bug where multiple picker options can be shown at once.
        binding.eventDate.setOnClickListener {
            val eventDatePicker = viewModel.initializeEventDatePicker()
            eventDatePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.eventTime.setOnClickListener {
            val eventTimePicker = viewModel.initializeEventTimePicker()
            eventTimePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.remindDate.setOnClickListener {
            val remindDatePicker = viewModel.initializeRemindDatePicker()
            remindDatePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.remindTime.setOnClickListener {
            val remindTimePicker = viewModel.initializeRemindTimePicker()
            remindTimePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.autoButton.setOnClickListener {
            viewModel.autoSetRemindVariables()
        }

        binding.saveButton.setOnClickListener {
            val newReminder = viewModel.createNewReminderOrNull(binding.reminderName.text.toString())
                ?: return@setOnClickListener

            viewModel.insertReminder(newReminder)

            val action = RemindAddFragmentDirections.actionRemindAddFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }
}