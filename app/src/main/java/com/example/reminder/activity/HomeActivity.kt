package com.example.reminder.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.reminder.AddReminderDialog
import com.example.reminder.R
import com.example.reminder.adapter.ReminderListAdapter
import com.example.reminder.database.Reminder
import com.example.reminder.database.ReminderDatabase
import com.example.reminder.databinding.ActivityHomeBinding
import com.example.reminder.receiver.ReminderReceiver
import com.example.reminder.utils.DateUtils
import com.example.reminder.utils.SortReminder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.math.ceil

class HomeActivity : AppCompatActivity(), OnAddReminderListener,
    ReminderListAdapter.OnReminderCheckedListener {

    private lateinit var binding: ActivityHomeBinding

    private val dao = ReminderDatabase.instance?.reminderDao()
    private val adapter = ReminderListAdapter().apply {
        onReminderCheckedListener = this@HomeActivity
    }

    private val addReminderDialog by lazy { AddReminderDialog(this, this) }

    private val pendingIntentList = arrayListOf<Pair<Int, PendingIntent>>()

    private var alarmManager: AlarmManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.addReminder.setOnClickListener {
            addReminderDialog.displayReminderDialog()
        }

        binding.list.adapter = adapter

        removeOldReminders()
        dao?.getAllReminders()?.observe(this, remindersListObserver)
    }

    private val remindersListObserver = Observer<List<Reminder>> {
        adapter.addReminder(SortReminder.sortReminder(it))
        binding.list.isVisible = it.isNotEmpty()
        binding.noReminder.isVisible = it.isEmpty()
    }

    private fun removeOldReminders() {
        lifecycleScope.launch(IO) {
            val list = dao?.getReminderList()
            list?.forEach {
                val date = DateUtils.parseDate(it.date)
                if (DateUtils.isOlderDate(date) == true) {
                    dao?.deleteReminder(it.id)
                }
            }
        }
    }

    private fun setReminder(id: Int, reminder: Reminder?) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.action = "com.example.reminder.receiver.ReminderReceiver"
        intent.putExtra(ADD_REMINDER_REQUEST_KEY_NAME, ADD_REMINDER_REQUEST_KEY)
        intent.putExtra("REMINDER", reminder)

        val pendingIntent = PendingIntent.getBroadcast(this, id, intent, FLAG_ONE_SHOT)
        pendingIntentList.add(Pair(id, pendingIntent))

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = DateUtils.parseDate(reminder?.date!!)?.time ?: 0

        val time = DateUtils.parseTime(reminder.time)
        calendar[Calendar.HOUR_OF_DAY] = time[0].toInt()
        calendar[Calendar.MINUTE] = time[1].toInt()
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        val timeForAlarm = ceil(((calendar.timeInMillis - System.currentTimeMillis()).toFloat()/1000)/60).toInt()

        val timeLeft = if (timeForAlarm > 0) "Alarm set for $timeForAlarm minutes from now" else "Alarm set in less than a minute now"
        runOnUiThread {
            Toast.makeText(this, timeLeft, Toast.LENGTH_LONG).show()
        }
    }

    private fun removeReminder(reminder: Reminder?) {
        val pendingIntent = pendingIntentList.firstOrNull {
            it.first == reminder?.id }
        alarmManager?.cancel(pendingIntent?.second)
        pendingIntentList.remove(pendingIntent)
    }

    override fun onAddReminder(reminder: Reminder) {
        lifecycleScope.launch(IO) {
            val inserted = dao?.insertReminder(reminder) ?: return@launch
            setReminder(inserted.toInt(), reminder)
        }
    }

    override fun onReminderChecked(id: Int?, isChecked: Boolean) {
        lifecycleScope.launch(IO) {
            dao?.updateReminder(id, isChecked) ?: return@launch

            val reminder = dao.getReminderByID(id ?: 0)[0]
            if (isChecked)
                setReminder(id!!, reminder)
            else
                removeReminder(reminder)
        }
    }

    companion object {
        private const val ADD_REMINDER_REQUEST_KEY_NAME = "ADD_REMINDER_REQUEST_KEY"
        private const val ADD_REMINDER_REQUEST_KEY = 10
    }
}

fun interface OnAddReminderListener {
    fun onAddReminder(reminder: Reminder)
}