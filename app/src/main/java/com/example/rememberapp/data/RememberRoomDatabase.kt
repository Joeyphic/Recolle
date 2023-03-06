package com.example.rememberapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class, Reminder::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RememberRoomDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun remindDao(): RemindDao

    companion object {

        @Volatile
        private var INSTANCE: RememberRoomDatabase? = null

        fun getDatabase(context: Context): RememberRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RememberRoomDatabase::class.java,
                    "remember_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}