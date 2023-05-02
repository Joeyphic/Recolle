package com.example.recolle

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.recolle.data.RecolleDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemindBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != "android.intent.action.BOOT_COMPLETED" || context == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val remindDao = RecolleDatabase.getDatabase(context).remindDao()
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
                if (!it.checked) { scheduler.schedule(it) }
            }
        }
    }
}