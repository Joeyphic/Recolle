package com.example.rememberapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RemindAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        Log.i("RemindAlarms", "An alarm just triggered: $message")
    }
}