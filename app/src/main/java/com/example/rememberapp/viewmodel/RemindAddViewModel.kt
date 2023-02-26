package com.example.rememberapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rememberapp.data.RemindDao
import com.example.rememberapp.data.TaskDao
import java.time.LocalDate
import java.time.LocalTime

class RemindAddViewModel(private val remindDao: RemindDao) : ViewModel() {

    val eventDate: LocalDate? = null
    val eventTime: LocalTime? = null
    val remindDate: LocalDate? = null
    val remindTime: LocalTime? = null

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