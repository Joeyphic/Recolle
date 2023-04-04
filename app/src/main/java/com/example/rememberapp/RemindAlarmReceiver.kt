package com.example.rememberapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class RemindAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        Log.i("RemindAlarms", "An alarm just triggered: $message")

        context?.let {
            var builder = NotificationCompat.Builder(context, "RemindNotificationChannel")
                .setSmallIcon(R.drawable.ic_material_schedule_24)
                .setContentTitle(message)
                .setContentText("And here's the text!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) return

                notify(2, builder.build())
            }
        }
    }
}