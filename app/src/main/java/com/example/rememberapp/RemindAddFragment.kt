package com.example.rememberapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rememberapp.databinding.RemindListAddItemBinding
import com.example.rememberapp.viewmodel.TaskAddViewModel
import com.example.rememberapp.viewmodel.TaskAddViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class RemindAddFragment : Fragment() {

    private val viewModel: TaskAddViewModel by viewModels {
        TaskAddViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private var _binding: RemindListAddItemBinding? = null
    private val binding get() = _binding!!

    val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

    val timePicker =
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(10)
            .setTitleText("Select Appointment time")
            .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = RemindListAddItemBinding.inflate(inflater, container, false)
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

        binding.eventDate.setOnClickListener {

            // datePicker doesn't seem to save data yet.
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.show(parentFragmentManager, "eventDate")
        }
    }
}