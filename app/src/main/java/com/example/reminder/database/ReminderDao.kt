package com.example.reminder.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {

    @Query("SELECT * FROM Reminder")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM Reminder")
    fun getReminderList(): List<Reminder>

    @Query("SELECT * FROM reminder WHERE id = :id")
    fun getReminderByID(id: Int): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(reminder: Reminder): Long

    @Query("DELETE FROM Reminder WHERE id = :id")
    fun deleteReminder(id: Int)

    @Query("UPDATE Reminder SET isEnabled = :isChecked WHERE id = :id")
    fun updateReminder(id: Int?, isChecked: Boolean): Int
}