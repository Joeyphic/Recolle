package com.example.rememberapp

import android.app.Application
import com.example.rememberapp.data.RememberRoomDatabase

class RememberApplication : Application() {
    val database: RememberRoomDatabase by lazy { RememberRoomDatabase.getDatabase(this) }
}