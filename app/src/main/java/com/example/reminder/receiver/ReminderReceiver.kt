package com.example.reminder.receiver

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import timber.log.Timber

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("<<>> called")
        Toast.makeText(context, "Alarm received", Toast.LENGTH_SHORT).show()
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val notification = Notification.Builder(context)
    }
}