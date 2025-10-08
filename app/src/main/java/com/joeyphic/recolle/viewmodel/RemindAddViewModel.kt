package com.joeyphic.recolle.viewmodel

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.joeyphic.recolle.data.RemindDao
import com.joeyphic.recolle.data.Reminder
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
        var picker: DialogFragment? = null,
        var errorMessage: String? = null,
        var permissionMessage: String? = null
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
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    suspend fun insertReminder(reminder: Reminder): Long {
        return remindDao.insert(reminder)
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

    fun autoSetRemindVariables() {
        val eventDateTime = LocalDateTime.of(eventDate.value ?: return, eventTime.value ?: return)
        val remindDateTime = eventDateTime.minusHours(3)
        _remindDate.value = remindDateTime.toLocalDate()
        _remindTime.value = remindDateTime.toLocalTime()
    }

    fun createNewReminderOrNull(reminderName: String): Reminder? {
        val eventDateTime =
            LocalDateTime.of(eventDate.value ?: return null, eventTime.value ?: return null)
        val remindDateTime =
            LocalDateTime.of(remindDate.value ?: return null, remindTime.value ?: return null)
        val newReminder = getNewReminderEntry(reminderName, eventDateTime, remindDateTime)

        checkForReminderErrors(newReminder)
        return if(uiState.value.errorMessage == null) newReminder else null
    }

    fun updateUIState() {
        val isAutoEnabled = eventDate.value != null && eventTime.value != null
        val isSaveEnabled = isAutoEnabled && remindDate.value != null && remindTime.value != null

        _uiState.update { currentUiState ->
            currentUiState.copy(
                isAutoEnabled = isAutoEnabled,
                isSaveEnabled = isSaveEnabled
            )
        }
    }

    fun displayPermissionRationale() {
        _uiState.update {
            it.copy(permissionMessage = "To make sure you get reminded on time, please allow " +
                    "Recolle to send you notifications.")
        }
    }

    fun permissionMessageShown() {
        _uiState.update {
            it.copy(permissionMessage = null)
        }
    }

    private fun getNewReminderEntry(reminderName: String, eventDateTime: LocalDateTime, remindDateTime: LocalDateTime): Reminder {
        return Reminder(
            name = reminderName,
            checked = false,
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
        else if(LocalDateTime.now() >= reminder.eventTime) {
            _uiState.update { currentUiState ->
                currentUiState.copy(errorMessage = "The event time should be after the current time.")
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update {
            it.copy(errorMessage = null)
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