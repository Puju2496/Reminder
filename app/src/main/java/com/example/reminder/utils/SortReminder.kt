package com.example.reminder.utils

import com.example.reminder.database.Reminder
import java.util.*

object SortReminder {

    fun sortReminder(reminders: List<Reminder>): HashMap<String, ArrayList<Reminder>?> {
        val reminderList = hashMapOf<String, ArrayList<Reminder>?>()

        reminders.forEach { reminder ->
            if (reminderList.containsKey(reminder.date)) {
                val list = reminderList[reminder.date]
                list?.filter { it.id == reminder.id }.also {
                    if (it.isNullOrEmpty()) {
                        list?.add(reminder)
                        reminderList.replace(reminder.date, list)
                    }
                }
            } else {
                reminderList[reminder.date] = arrayListOf(reminder)
            }
        }

        return arrangeReminder(reminderList)
    }

    private fun arrangeReminder(reminderList: HashMap<String, ArrayList<Reminder>?>): HashMap<String, ArrayList<Reminder>?> {
        val list = LinkedList(reminderList.entries)
        list.sortWith { o1, o2 ->
            val firstDate = DateUtils.parseDate(o1.key)
            val secondDate = DateUtils.parseDate(o2.key)

            firstDate?.time?.compareTo(secondDate?.time ?: 0) ?: 0
        }

        list.forEach {
            it.value?.sortWith { o1, o2 ->
                val firstCalendar = Calendar.getInstance()
                val secondCalendar = Calendar.getInstance()

                val firstTime = o1.time.split(":")
                val secondTime = o2.time.split(":")

                firstCalendar.time = DateUtils.parseDate(o1.date)
                secondCalendar.time = DateUtils.parseDate(o2.date)

                firstCalendar.set(Calendar.HOUR_OF_DAY, firstTime[0].toInt())
                firstCalendar.set(Calendar.MINUTE, firstTime[1].toInt())

                firstCalendar.set(Calendar.HOUR_OF_DAY, secondTime[0].toInt())
                firstCalendar.set(Calendar.MINUTE, secondTime[1].toInt())

                firstCalendar.timeInMillis.compareTo(secondCalendar.timeInMillis)
            }
        }

        return list.associateBy( { it.key }, { it.value }) as HashMap<String, ArrayList<Reminder>?>
    }
}