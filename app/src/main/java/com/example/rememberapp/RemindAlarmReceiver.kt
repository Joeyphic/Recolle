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
import androidx.navigation.NavDeepLinkBuilder

class RemindAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("EXTRA_TITLE") ?: return
        val message = intent.getStringExtra("EXTRA_MESSAGE")
        val reminderId = intent.getIntExtra("EXTRA_ID", -1)
        Log.i("RemindAlarms", "An alarm just triggered: $message")

        context?.let {
            var builder = NotificationCompat.Builder(context, "RemindNotificationChannel")
                .setSmallIcon(R.drawable.ic_material_schedule_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val arg = RemindDetailFragmentArgs(reminderId).toBundle()

            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.remindDetailFragment)
                .setArguments(arg)
                .createPendingIntent()

            builder.setContentIntent(pendingIntent)

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