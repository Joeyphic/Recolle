package com.joeyphic.recolle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class, Reminder::class, Subtask::class],
    version = 2,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class RecolleDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun remindDao(): RemindDao
    abstract fun subtaskDao(): SubtaskDao

    companion object {

        @Volatile
        private var INSTANCE: RecolleDatabase? = null

        // Manual migration from Version 1 to 2. Next versions should allow for auto-migration
        // as exportSchema is now set to true
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `subtask` (`id` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL,`main_id` INTEGER NOT NULL, " +
                        "`checked` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        fun getDatabase(context: Context): RecolleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecolleDatabase::class.java,
                    "recolle_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}