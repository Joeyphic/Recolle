package com.example.rememberapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminder")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "checked")
    val checked: Boolean = false,
    @ColumnInfo(name = "remind_time")
    val remindTime: LocalDateTime,
    @ColumnInfo(name = "event_time")
    var eventTime: LocalDateTime
)