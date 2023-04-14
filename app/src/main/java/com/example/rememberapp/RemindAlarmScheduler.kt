package com.example.rememberapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.rememberapp.data.Reminder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RemindAlarmScheduler(private val context: Context): AlarmScheduler<Reminder> {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: Reminder) {
        val alarmTitle = "Reminder: ${reminder.name}"
        val alarmMessage = createAlarmMessage(reminder.eventTime, reminder.remindTime)

        val intent = Intent(context, RemindAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", alarmTitle)
            putExtra("EXTRA_MESSAGE", alarmMessage)
            putExtra("EXTRA_ID", reminder.id)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.remindTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        // Enables BootReceiver
        val receiver = ComponentName(context, RemindBootReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

    }

    override fun cancel(reminder: Reminder) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                reminder.id,
                Intent(context, RemindAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    // TODO: Add to string resources.
    private fun createAlarmMessage(eventDateTime: LocalDateTime, remindDateTime: LocalDateTime) : String {
        val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val dateTimeFormatWithoutYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d 'at' hh:mm a")
        val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' hh:mm a")

        return if(eventDateTime == remindDateTime) {
            "Event occurring right now."
        }
        else if(eventDateTime.toLocalDate() == remindDateTime.toLocalDate()) {
            "Event occurring at ${eventDateTime.format(timeFormat)}."
        }
        else if(eventDateTime.year == remindDateTime.year) {
            "Event occurring on ${eventDateTime.format(dateTimeFormatWithoutYear)}."
        }
        else {
            "Event occurring on ${eventDateTime.format(dateTimeFormat)}."
        }
    }
}