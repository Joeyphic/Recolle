package com.example.rememberapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rememberapp.data.RemindDao
import com.example.rememberapp.data.TaskDao
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RemindAddViewModel(private val remindDao: RemindDao) : ViewModel() {

    var eventDate: LocalDate? = null
    var eventTime: LocalTime? = null
    var remindDate: LocalDate? = null
    var remindTime: LocalTime? = null

    fun initializeEventDatePicker() : MaterialDatePicker<Long> {
        val eventDateEpochMilli = eventDate?.let {
            it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val selection = eventDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = createDatePicker(selection)

        datePicker.addOnPositiveButtonClickListener {
            val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
            eventDate = Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
        }

        return datePicker
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

class RemindAddViewModelFactory(private val remindDao: RemindDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindAddViewModel(remindDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}