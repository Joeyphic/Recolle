package com.example.rememberapp.data

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val taskName: String,
    @ColumnInfo(name = "priority")
    val taskPriority: PriorityLevel,
    @ColumnInfo(name = "position")
    var taskListPosition: Int
)

/**
 * The user can assign one of these three levels to a task based on how urgent it is for
 * them to complete.
 */
enum class PriorityLevel {
    LOW, MEDIUM, HIGH
}