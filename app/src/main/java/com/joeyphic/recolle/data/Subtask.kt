package com.joeyphic.recolle.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtask")
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val subtaskName: String,
    @ColumnInfo(name = "main_id")
    val mainTaskId: Int,
    @ColumnInfo(name = "checked")
    var checked: Boolean
)