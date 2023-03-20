package com.example.rememberapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rememberapp.data.RemindDao
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.data.Task

class RemindDetailViewModel(private val remindDao: RemindDao) : ViewModel() {
    // Holds the reminder to be edited in this fragment.
    lateinit var reminder: Reminder

    fun retrieveReminder(id: Int): Reminder? {
        return remindDao.getReminderById(id)
    }
}

class RemindDetailViewModelFactory(private val remindDao: RemindDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindDetailViewModel(remindDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}