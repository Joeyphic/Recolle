package com.example.recolle

import android.app.Application
import com.example.recolle.data.RecolleDatabase

class RecolleApplication : Application() {
    val database: RecolleDatabase by lazy { RecolleDatabase.getDatabase(this) }
}