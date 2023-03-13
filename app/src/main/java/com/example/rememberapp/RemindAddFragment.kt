package com.example.rememberapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        binding.eventDate.setOnClickListener {
            val datePicker = viewModel.initializeEventDatePicker()
            datePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.eventTime.setOnClickListener {
            val timePicker = createTimePicker(viewModel.eventTime ?: LocalTime.NOON)

            timePicker.addOnPositiveButtonClickListener {
                viewModel.eventTime = LocalTime.of(timePicker.hour, timePicker.minute)
                binding.eventTime.setText(
                    viewModel.eventTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                )
            }
            timePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.remindDate.setOnClickListener {
            val remindDateEpochMilli = viewModel.remindDate?.let {
                it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
            val selection = remindDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
            val datePicker = createDatePicker(selection)

            datePicker.addOnPositiveButtonClickListener {
                val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
                viewModel.remindDate = Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
                binding.remindDate.setText(
                    viewModel.remindDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                )
            }
            datePicker.show(parentFragmentManager, "RemindAddFragment")
        }

        binding.remindTime.setOnClickListener {
            val timePicker = createTimePicker(viewModel.remindTime ?: viewModel.eventTime ?: LocalTime.NOON)

            timePicker.addOnPositiveButtonClickListener {
                viewModel.remindTime = LocalTime.of(timePicker.hour, timePicker.minute)
                binding.remindTime.setText(
                    viewModel.remindTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                )
            }
            timePicker.show(parentFragmentManager, "RemindAddFragment")
        }
    }

    private fun createDatePicker(selection: Long): MaterialDatePicker<Long> {

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(MaterialDatePicker.todayInUtcMilliseconds())
                .setValidator(DateValidatorPointForward.now())

        return MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selection)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
    }

    private fun createTimePicker(time: LocalTime): MaterialTimePicker {

        return MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .setTitleText("Select time")
            .build()
    }
}