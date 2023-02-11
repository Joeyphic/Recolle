package com.example.rememberapp

import com.example.rememberapp.data.Reminder

sealed class RemindListElement {
    data class Header(val header: String) : RemindListElement()
    data class Item(val reminder: Reminder) : RemindListElement()
}