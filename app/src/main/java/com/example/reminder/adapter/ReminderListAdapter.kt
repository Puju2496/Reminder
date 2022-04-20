package com.example.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.reminder.database.Reminder
import com.example.reminder.databinding.LayoutHeaderBinding
import com.example.reminder.databinding.LayoutReminderBinding
import com.example.reminder.utils.DateUtils
import timber.log.Timber

class ReminderListAdapter : RecyclerView.Adapter<ReminderListAdapter.ViewHolder>() {

    companion object {
        private const val HEADER = 1
        private const val REMINDER_ITEM = 2
    }

    var onReminderCheckedListener: OnReminderCheckedListener? = null

    private var remindersList: HashMap<String, ArrayList<Reminder>?> = hashMapOf()

    private val dataIndices: ArrayList<DataIndex> = arrayListOf()

    fun addReminder(list: HashMap<String, ArrayList<Reminder>?>) {
        remindersList = list
        addIndices()
    }

    private fun addIndices() {
        dataIndices.clear()
        remindersList.forEach { (t, u) ->
            dataIndices.add(DataIndex(HEADER, t))
            u?.forEachIndexed { index, _ ->
                dataIndices.add(DataIndex(REMINDER_ITEM, t, index))
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            1 -> HeaderViewHolder(
                LayoutHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            2 -> ReminderViewHolder(
                LayoutReminderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onReminderCheckedListener
            )
            else -> ReminderViewHolder(
                LayoutReminderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onReminderCheckedListener
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.onBind(dataIndices[position].key)
            is ReminderViewHolder -> {
                val key = dataIndices[position].key
                val reminder = remindersList[key]?.get(dataIndices[position].internalPosition ?: 0)
                holder.onBind(reminder, onReminderCheckedListener)
            }
        }
    }

    override fun getItemCount(): Int = dataIndices.size

    override fun getItemViewType(position: Int): Int = dataIndices[position].type

    inner class DataIndex(val type: Int, val key: String, val internalPosition: Int? = null)

    abstract class ViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun onBind(header: String)
        abstract fun onBind(
            reminder: Reminder?,
            onReminderCheckedListener: OnReminderCheckedListener?
        )
    }

    class HeaderViewHolder(private val binding: LayoutHeaderBinding) : ViewHolder(binding) {
        override fun onBind(header: String) {
            binding.header.text = DateUtils.getDateOrDay(header)
        }

        override fun onBind(
            reminder: Reminder?,
            onReminderCheckedListener: OnReminderCheckedListener?
        ) {
        }
    }

    class ReminderViewHolder(
        private val binding: LayoutReminderBinding,
        private val onReminderCheckedListener: OnReminderCheckedListener?
    ) : ViewHolder(binding) {

        private var reminder: Reminder? = null

        init {
            binding.enabled.setOnCheckedChangeListener { _, isChecked ->
                if (reminder?.isEnabled != isChecked)
                    onReminderCheckedListener?.onReminderChecked(reminder?.id, isChecked)
            }
        }

        override fun onBind(header: String) {
        }

        override fun onBind(
            reminder: Reminder?,
            onReminderCheckedListener: OnReminderCheckedListener?
        ) {
            this.reminder = reminder
            binding.title.text =
                reminder?.name?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            binding.time.text = reminder?.time
            binding.enabled.isChecked = reminder?.isEnabled ?: false
        }

    }

    fun interface OnReminderCheckedListener {
        fun onReminderChecked(id: Int?, isChecked: Boolean)
    }
}