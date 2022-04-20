package com.example.reminder

import android.app.Application
import com.example.reminder.database.ReminderDatabase
import timber.log.Timber

class ReminderApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        initializeTimber()

        ReminderDatabase.init(this)
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}