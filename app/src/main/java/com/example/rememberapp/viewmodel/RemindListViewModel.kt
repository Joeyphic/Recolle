package com.example.rememberapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.rememberapp.RemindListElement
import com.example.rememberapp.data.RemindDao
import com.example.rememberapp.data.Reminder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RemindListViewModel(private val remindDao: RemindDao) : ViewModel() {

    val allReminders: LiveData<List<Reminder>> = remindDao.getAllRemindersFlow().asLiveData()

    fun generateRemindList(sortedReminders: List<Reminder>) : List<RemindListElement> {

        val outputList = mutableListOf<RemindListElement>()
        var previousDate : LocalDate? = null

        for(reminder in sortedReminders) {
            val currentDate = reminder.eventTime.toLocalDate()

            if(previousDate != currentDate) {

                val formattedDate = if(currentDate.year == LocalDateTime.now().year) {
                    currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                }
                else {
                    currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                }
                outputList.add(RemindListElement.Header(formattedDate))
                previousDate = currentDate
            }
            outputList.add(RemindListElement.Item(reminder))
        }

        return outputList
    }
}

class RemindListViewModelFactory(private val remindDao: RemindDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(RemindListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindListViewModel(remindDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}