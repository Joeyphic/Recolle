package com.example.recolle.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            // ZoneId.systemDefault() preferred over TimeZone.getDefault().toZoneId()
            LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        val deviceTimeOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())
        return date?.toEpochSecond(deviceTimeOffset)
    }
}