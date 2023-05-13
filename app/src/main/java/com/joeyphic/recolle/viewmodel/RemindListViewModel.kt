package com.joeyphic.recolle.viewmodel

import androidx.lifecycle.*
import com.joeyphic.recolle.RemindListElement
import com.joeyphic.recolle.data.RemindDao
import com.joeyphic.recolle.data.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.*
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

    fun clearOutdatedCheckedReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceTimeOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())
            remindDao.clearCheckedRemindersBeforeTime(
                LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC)
            )
        }
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