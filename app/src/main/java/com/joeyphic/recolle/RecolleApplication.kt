package com.joeyphic.recolle

import android.app.Application
import com.joeyphic.recolle.data.RecolleDatabase

class RecolleApplication : Application() {
    val database: RecolleDatabase by lazy { RecolleDatabase.getDatabase(this) }
}