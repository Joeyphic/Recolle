package com.example.recolle

interface AlarmScheduler<T> {
    fun schedule(reminder: T)
    fun cancel(reminder: T)
}