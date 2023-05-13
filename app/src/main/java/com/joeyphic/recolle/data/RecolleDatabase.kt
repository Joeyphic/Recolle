package com.joeyphic.recolle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class, Reminder::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RecolleDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun remindDao(): RemindDao

    companion object {

        @Volatile
        private var INSTANCE: RecolleDatabase? = null

        fun getDatabase(context: Context): RecolleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecolleDatabase::class.java,
                    "recolle_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}