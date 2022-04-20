package com.example.reminder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.reminder.R
import java.lang.RuntimeException

@Database(entities = [Reminder::class], version = 1)
abstract class ReminderDatabase: RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        private var INSTANCE: ReminderDatabase? = null

        val instance: ReminderDatabase?
            get() {
                if (INSTANCE == null) {
                    throw RuntimeException("Database should be intialized with context before use")
                }
                return INSTANCE
            }

        fun init(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    ReminderDatabase::class.java,
                    context.getString(R.string.app_name)
                )
                    .build()
            }
        }
    }
}