package com.example.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.reminder.activity.OnAddReminderListener
import com.example.reminder.database.Reminder
import com.example.reminder.databinding.LayoutAddReminderBinding
import com.example.reminder.utils.DateUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import java.util.*

class AddReminderDialog(private val context: Context, private val listener: OnAddReminderListener) {
    private var title: String = ""
    private var date: String = ""
    private var time: String = ""

    private var isTimeClickFromListener = false

    private var dateCalendar: Calendar? = null
    private var timeCalendar: Calendar? = null

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null

    private var dateListener: DatePickerDialog.OnDateSetListener? = null
    private var timeListener: TimePickerDialog.OnTimeSetListener? = null

    lateinit var binding: LayoutAddReminderBinding

    fun displayReminderDialog() {
        val dialog = BottomSheetDialog(context)


        binding = LayoutAddReminderBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.show()

        binding.title.doAfterTextChanged {
            if (it != null) {
                updateLayoutHeight(binding.titleLayout, false)
                binding.titleLayout.error = null
                title = it.toString()
            }

        }

        dateListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance(Locale.getDefault())
                calendar.set(year, month, dayOfMonth)
                date = DateUtils.formatDate(calendar.time)
                updateLayoutHeight(binding.dateLayout, false)
                binding.dateLayout.error = null
                binding.date.setText(date)
            }

        timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val selectedDate = date.split(", ")
            if (selectedDate.size < 2) {
                Toast.makeText(context, "Select date first", Toast.LENGTH_SHORT).show()
                timePickerDialog?.dismiss()
                binding.date.callOnClick()
            } else {
                val currentHour = timeCalendar?.get(Calendar.HOUR_OF_DAY) ?: 0
                val currentMinute = timeCalendar?.get(Calendar.MINUTE) ?: 0
                if (DateUtils.isToday(date) == true) {
                    if (hourOfDay > currentHour) {
                        time = DateUtils.formatTime(hourOfDay, minute)
                        binding.time.setText(time)
                    } else {
                        if (minute > currentMinute) {
                            time = DateUtils.formatTime(hourOfDay, minute)
                            binding.time.setText(time)
                        } else {
                            Toast.makeText(context, "Time should be greater than now", Toast.LENGTH_SHORT).show()
                            isTimeClickFromListener = true
                            binding.time.callOnClick()
                        }
                    }
                } else {
                    time = DateUtils.formatTime(hourOfDay, minute)
                    binding.time.setText(time)
                }
            }

            if (binding.time.text != null) {
                updateLayoutHeight(binding.timeLayout, false)
                binding.timeLayout.error = null
            }
        }

        binding.date.setOnClickListener {
            if (dateCalendar == null) dateCalendar = Calendar.getInstance(Locale.getDefault())
            dateCalendar?.apply {
                datePickerDialog = DatePickerDialog(context, dateListener, this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DAY_OF_MONTH]).apply {
                    datePicker.minDate = timeInMillis
                    show()
                }
            }
        }

        binding.time.setOnClickListener {
            if (timeCalendar == null || isTimeClickFromListener) {
                timeCalendar = Calendar.getInstance(Locale.getDefault())
                isTimeClickFromListener = false
            }
            timeCalendar?.apply {
                timePickerDialog = TimePickerDialog(context, timeListener, this[Calendar.HOUR_OF_DAY], this[Calendar.MINUTE], true)
                timePickerDialog?.show()
            }
        }

        binding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.add.setOnClickListener {
            if (title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                dialog.dismiss()
                val reminder = Reminder(
                    title, date, time, true)
                listener.onAddReminder(reminder)
            } else {
                if (title.isEmpty()) {
                    updateLayoutHeight(binding.titleLayout, true)
                    binding.titleLayout.error = "Title is required for reminder"
                }

                if (date.isEmpty()) {
                    updateLayoutHeight(binding.dateLayout, true)
                    binding.dateLayout.error = "Date is required for reminder"
                }

                if (time.isEmpty()) {
                    updateLayoutHeight(binding.timeLayout, true)
                    binding.timeLayout.error = "Time is required for reminder"
                }
            }
        }
    }

    private fun updateLayoutHeight(textLayout: TextInputLayout, isError: Boolean) {
        val params = textLayout.layoutParams
        if (isError) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            params.height = context.resources.getDimensionPixelOffset(R.dimen.reminder_height)
        }
        textLayout.layoutParams = params
    }
}