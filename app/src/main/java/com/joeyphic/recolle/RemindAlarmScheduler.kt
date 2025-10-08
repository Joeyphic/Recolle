package com.joeyphic.recolle

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.joeyphic.recolle.R
import com.joeyphic.recolle.data.Reminder
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

        // Can Rework Exact Alarms with outline given in Android Studio's SDK Upgrade Assistant.
        // Specifically in the Android 12L -> 13 section.
        // If this is done, re-add <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
        // to manifest.xml.
        alarmManager.setAndAllowWhileIdle(
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

    private fun createAlarmMessage(eventDateTime: LocalDateTime, remindDateTime: LocalDateTime) : String {
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
        val dateFormatWithoutYear = DateTimeFormatter.ofPattern("MMM d")
        val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

        return if(eventDateTime == remindDateTime) {
            context.getString(R.string.notification_remind_event_today)
        }
        else if(eventDateTime.toLocalDate() == remindDateTime.toLocalDate()) {
             context.getString(
                 R.string.notification_remind_event_this_month,
                 eventDateTime.format(timeFormat)
             )
        }
        else if(eventDateTime.year == remindDateTime.year) {
            context.getString(
                R.string.notification_remind_event_this_year,
                eventDateTime.format(dateFormatWithoutYear),
                eventDateTime.format(timeFormat)
            )
        }
        else {
            context.getString(
                R.string.notification_remind_event_other_years,
                eventDateTime.format(dateFormat),
                eventDateTime.format(timeFormat)
            )
        }
    }
}