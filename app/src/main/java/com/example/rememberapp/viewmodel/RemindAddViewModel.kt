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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.*
import java.time.format.DateTimeFormatter

class RemindAddViewModel(private val remindDao: RemindDao) : ViewModel() {

    data class RemindAddUiState(
        var isAutoEnabled: Boolean = false,
        var isSaveEnabled: Boolean = false,
        var errorMessage: String? = null
    )
    private val _uiState = MutableStateFlow(RemindAddUiState())
    val uiState: StateFlow<RemindAddUiState> = _uiState

    private val _eventDate = MutableStateFlow<LocalDate?>(null)
    val eventDate: StateFlow<LocalDate?> = _eventDate

    private val _eventTime = MutableStateFlow<LocalTime?>(null)
    val eventTime: StateFlow<LocalTime?> = _eventTime

    private val _remindDate = MutableStateFlow<LocalDate?>(null)
    val remindDate: StateFlow<LocalDate?> = _remindDate

    private val _remindTime = MutableStateFlow<LocalTime?>(null)
    val remindTime: StateFlow<LocalTime?> = _remindTime

    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun initializeEventDatePicker() : MaterialDatePicker<Long> {
        val eventDateEpochMilli = eventDate.value?.let {
            it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val selection = eventDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = createDatePicker(selection)

        datePicker.addOnPositiveButtonClickListener {
            val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
            _eventDate.value = Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
        }

        return datePicker
    }

    fun initializeEventTimePicker() : MaterialTimePicker {
        val timePicker = createTimePicker(eventTime.value ?: LocalTime.NOON)

        timePicker.addOnPositiveButtonClickListener {
            _eventTime.value = LocalTime.of(timePicker.hour, timePicker.minute)
        }

        return timePicker
    }

    fun initializeRemindDatePicker() : MaterialDatePicker<Long> {
        val initialDateEpochMilli = (remindDate.value ?: eventDate.value)?.let {
            it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val selection = initialDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = createDatePicker(selection)

        datePicker.addOnPositiveButtonClickListener {
            val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
            _remindDate.value = Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
        }

        return datePicker
    }

    fun initializeRemindTimePicker() : MaterialTimePicker {
        val timePicker = createTimePicker(remindTime.value ?: eventTime.value ?: LocalTime.NOON)

        timePicker.addOnPositiveButtonClickListener {
            _remindTime.value = LocalTime.of(timePicker.hour, timePicker.minute)
        }

        return timePicker
    }

    fun addNewReminder() {
        val eventDateTime = LocalDateTime.of(eventDate.value ?: return, eventTime.value ?: return)
        val remindDateTime = LocalDateTime.of(remindDate.value ?: return, remindTime.value ?: return)

    }

    fun updateUIState() {
        if(eventDate.value == null || eventTime.value == null) return
        else _uiState.value.isAutoEnabled = true

        if(remindDate.value == null || remindTime.value == null) return
        else _uiState.update { currentUiState ->
            currentUiState.copy(isSaveEnabled = true)
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

class RemindAddViewModelFactory(private val remindDao: RemindDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindAddViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindAddViewModel(remindDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}