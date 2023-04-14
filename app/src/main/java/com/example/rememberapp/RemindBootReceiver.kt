package com.example.rememberapp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.example.rememberapp.data.RememberRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RemindBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != "android.intent.action.BOOT_COMPLETED" || context == null) return

        Log.i("RemindAlarms", "RemindBootReceiver Activated!")
        CoroutineScope(Dispatchers.IO).launch {
            val remindDao = RememberRoomDatabase.getDatabase(context).remindDao()
            val scheduler = RemindAlarmScheduler(context)

            val reminders = remindDao.getAllReminders()

            // Disables BootReceiver if no Reminders remain.
            if(reminders.isEmpty()) {
                val receiver = ComponentName(context, RemindBootReceiver::class.java)

                context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                return@launch
            }

            reminders.forEach {
                if (it.remindTime > LocalDateTime.now()) { scheduler.schedule(it) }
            }
        }
    }
}