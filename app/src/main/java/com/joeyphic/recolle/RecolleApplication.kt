package com.joeyphic.recolle

import android.app.Application
import com.joeyphic.recolle.data.RecolleDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class RecolleApplication : Application() {
    val database: RecolleDatabase by lazy { RecolleDatabase.getDatabase(this) }
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}