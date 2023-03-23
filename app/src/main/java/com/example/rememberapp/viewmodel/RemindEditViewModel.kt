package com.example.rememberapp.viewmodel

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rememberapp.data.RemindDao
import com.example.rememberapp.data.Reminder
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter

class RemindEditViewModel(private val remindDao: RemindDao) : ViewModel() {

    data class RemindEditUiState(
        var isAutoEnabled: Boolean = false,
        var isSaveEnabled: Boolean = false,
        var picker: DialogFragment? = null,
        var errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(RemindEditUiState())
    val uiState: StateFlow<RemindEditUiState> = _uiState

    var reminderId: Int? = null

    private val _reminderName = MutableStateFlow<String?>(null)
    val reminderName: StateFlow<String?> = _reminderName

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

    fun retrieveAndBindReminder(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentReminder = remindDao.getReminderById(id) ?: return@launch

            reminderId = currentReminder.id
            _reminderName.update { currentReminder.name }
            _eventDate.update { currentReminder.eventTime.toLocalDate() }
            _eventTime.update { currentReminder.eventTime.toLocalTime() }
            _remindDate.update { currentReminder.remindTime.toLocalDate() }
            _remindTime.update { currentReminder.remindTime.toLocalTime() }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            remindDao.update(reminder)
        }
    }

    fun initializeEventDatePicker() {

        if(uiState.value.picker != null) return

        val eventDateEpochMilli = eventDate.value?.let {
            it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val selection = eventDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = createDatePicker(selection)

        datePicker.addOnPositiveButtonClickListener {
            _uiState.update { it.copy(picker = null) }
            val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
            _eventDate.value =
                Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
        }

        datePicker.addOnDismissListener {
            _uiState.update { it.copy(picker = null) }
        }

        _uiState.update { it.copy(picker = datePicker) }
    }

    fun initializeEventTimePicker() {
        if(uiState.value.picker != null) return

        val timePicker = createTimePicker(eventTime.value ?: LocalTime.NOON)

        timePicker.addOnPositiveButtonClickListener {
            _uiState.update { it.copy(picker = null) }
            _eventTime.value = LocalTime.of(timePicker.hour, timePicker.minute)
        }

        timePicker.addOnDismissListener {
            _uiState.update { it.copy(picker = null) }
        }

        _uiState.update { it.copy(picker = timePicker) }
    }

    fun initializeRemindDatePicker() {
        if(uiState.value.picker != null) return

        val initialDateEpochMilli = (remindDate.value ?: eventDate.value)?.let {
            it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val selection = initialDateEpochMilli ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = createDatePicker(selection)

        datePicker.addOnPositiveButtonClickListener {
            _uiState.update { it.copy(picker = null) }
            val newSelection = datePicker.selection ?: return@addOnPositiveButtonClickListener
            _remindDate.value =
                Instant.ofEpochMilli(newSelection).atZone(ZoneOffset.UTC).toLocalDate()
        }

        datePicker.addOnDismissListener {
            _uiState.update { it.copy(picker = null) }
        }

        _uiState.update { it.copy(picker = datePicker) }
    }

    fun initializeRemindTimePicker() {
        if(uiState.value.picker != null) return

        val timePicker = createTimePicker(remindTime.value ?: eventTime.value ?: LocalTime.NOON)

        timePicker.addOnPositiveButtonClickListener {
            _uiState.update { it.copy(picker = null) }
            _remindTime.value = LocalTime.of(timePicker.hour, timePicker.minute)
        }

        timePicker.addOnDismissListener {
            _uiState.update { it.copy(picker = null) }
        }

        _uiState.update { it.copy(picker = timePicker) }
    }

    // TODO: Decide about existence of the Auto button in RemindEditFragment
    fun autoSetRemindVariables() {
        val eventDateTime = LocalDateTime.of(eventDate.value ?: return, eventTime.value ?: return)
        val remindDateTime = eventDateTime.minusHours(3)
        _remindDate.value = remindDateTime.toLocalDate()
        _remindTime.value = remindDateTime.toLocalTime()
    }

    fun createUpdatedReminderOrNull(reminderName: String): Reminder? {
        val eventDateTime =
            LocalDateTime.of(eventDate.value ?: return null, eventTime.value ?: return null)
        val remindDateTime =
            LocalDateTime.of(remindDate.value ?: return null, remindTime.value ?: return null)
        val id = reminderId ?: return null
        val updatedReminder = getUpdatedReminderEntry(id, reminderName, eventDateTime, remindDateTime)

        checkForReminderErrors(updatedReminder)
        return if(uiState.value.errorMessage == null) updatedReminder else null
    }

    fun updateUIState() {
        if(eventDate.value == null || eventTime.value == null) return
        else _uiState.update { currentUiState ->
            currentUiState.copy(isAutoEnabled = true)
        }

        if(remindDate.value == null || remindTime.value == null) return
        else _uiState.update { currentUiState ->
            currentUiState.copy(isSaveEnabled = true)
        }
    }

    private fun getUpdatedReminderEntry(id: Int, reminderName: String, eventDateTime: LocalDateTime, remindDateTime: LocalDateTime): Reminder {
        return Reminder(
            id = id,
            name = reminderName,
            eventTime = eventDateTime,
            remindTime = remindDateTime)
    }

    private fun checkForReminderErrors(reminder: Reminder) {
        if(reminder.name.isEmpty()) {
            _uiState.update { currentUiState ->
                currentUiState.copy(errorMessage = "Don't forget to give an event name for your new reminder.")
            }
        }
        else if(reminder.remindTime.isAfter(reminder.eventTime)) {
            _uiState.update { currentUiState ->
                currentUiState.copy(errorMessage = "Make sure your remind time is not after your event time.")
            }
        }
    }

    fun errorMessageShown() {
        _uiState.value.errorMessage = null
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

class RemindEditViewModelFactory(private val remindDao: RemindDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindEditViewModel(remindDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}