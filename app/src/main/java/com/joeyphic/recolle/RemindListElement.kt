package com.joeyphic.recolle

import com.joeyphic.recolle.data.Reminder

sealed class RemindListElement {
    data class Header(val header: String) : RemindListElement()
    data class Item(val reminder: Reminder) : RemindListElement()
}