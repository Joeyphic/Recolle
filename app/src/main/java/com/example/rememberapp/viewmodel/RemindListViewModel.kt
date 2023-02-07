package com.example.rememberapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rememberapp.data.TaskDao

class RemindListViewModel(private val taskDao: TaskDao) : ViewModel() {

}