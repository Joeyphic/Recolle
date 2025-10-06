package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joeyphic.recolle.data.RemindDao
import com.joeyphic.recolle.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class RemindDetailViewModel(private val remindDao: RemindDao,
                            private val applicationScope: CoroutineScope
) : ViewModel() {
    // Holds the reminder to be edited in this fragment.
    lateinit var reminder: Reminder
    val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy @ h:mm a")

    fun retrieveReminder(id: Int): Reminder? {
        return remindDao.getReminderById(id)
    }

    fun checkReminder() {
        applicationScope.launch(Dispatchers.IO) {
            remindDao.checkReminderById(reminder.id)
        }
    }

    fun deleteReminder() {
        applicationScope.launch(Dispatchers.IO) {
            remindDao.delete(reminder)
        }
    }
}

class RemindDetailViewModelFactory(private val remindDao: RemindDao,
                                   private val applicationScope: CoroutineScope) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindDetailViewModel(remindDao, applicationScope) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}