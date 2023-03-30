package com.example.rememberapp

interface AlarmScheduler<T> {
    fun schedule(reminder: T)
    fun cancel(reminder: T)
}