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
import com.example.rememberapp.databinding.RemindListAddItemBinding
import com.example.rememberapp.viewmodel.RemindAddViewModel
import com.example.rememberapp.viewmodel.RemindAddViewModelFactory
import com.example.rememberapp.viewmodel.TaskAddViewModel
import com.example.rememberapp.viewmodel.TaskAddViewModelFactory
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
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

        // TODO: Use UI State. https://developer.android.com/topic/architecture/ui-layer/events
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
                    }
                }
            }
        }

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

        binding.saveButton.setOnClickListener {
            viewModel.addNewReminder()
        }
    }
}