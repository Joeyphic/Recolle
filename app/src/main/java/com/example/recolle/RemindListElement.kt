package com.example.recolle

import com.example.recolle.data.Reminder

sealed class RemindListElement {
    data class Header(val header: String) : RemindListElement()
    data class Item(val reminder: Reminder) : RemindListElement()
}