package com.example.rememberapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.databinding.RemindListHeaderBinding
import com.example.rememberapp.databinding.RemindListItemBinding
import java.lang.IllegalStateException
import java.time.LocalTime

class RemindListAdapter(private val onReminderClicked: (RemindListElement) -> Unit) :
    ListAdapter<RemindListElement, RecyclerView.ViewHolder>(RemindListAdapter.DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == 0) {
            return HeaderViewHolder(
                RemindListHeaderBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                )
            )
        }

        // TODO: Fix error-prone code. Constants or error handling?
        else { // if(viewType == 1)
            return RemindViewHolder(
                RemindListItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                )
            )
        }
    }

    // Have to support two item types now?
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(val currentElement = getItem(position)) {

            is RemindListElement.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.bind(holder.itemView.context, currentElement.header)
            }

            is RemindListElement.Item -> {
                val reminderHolder = holder as RemindViewHolder
                reminderHolder.bind(holder.itemView.context, currentElement.reminder)

                reminderHolder.itemView.setOnClickListener {
                    onReminderClicked(currentElement)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RemindListElement.Header -> 0
            is RemindListElement.Item -> 1
        }
    }


    class RemindViewHolder(private var binding: RemindListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ctx: Context, reminder: Reminder) {

            val eventDate = reminder.eventTime.toLocalDate()
            val eventTime = reminder.eventTime.toLocalTime()
            val remindDate = reminder.remindTime.toLocalDate()
            val remindTime = reminder.remindTime.toLocalTime()

            val outputString = if(eventDate == remindDate && eventTime == remindTime) {
                "@ $eventTime."
            }
            else if(eventDate == remindDate) {
                "@ ${eventTime}. Remind ${eventTime.hour - remindTime.hour} hours before."
            }
            else if(eventDate.minusDays(1) == remindDate && remindTime.isAfter(LocalTime.of(19, 0))) {
                "@ ${eventTime}. Remind the night before."
            }
            else if(eventDate.minusDays(1) == remindDate) {
                "@ ${eventTime}. Remind the day before."
            }
            else {
                "@ ${eventTime}. Remind on ${reminder.eventTime}."
            }

            binding.apply {
                reminderName.text = outputString
            }
        }
    }

    class HeaderViewHolder(private var binding: RemindListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // TODO: Perhaps change from String to LocalDateTime
        fun bind(ctx: Context, header: String) {
            binding.apply {
                dayName.text = header
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<RemindListElement>() {
            override fun areItemsTheSame(oldItem: RemindListElement, newItem: RemindListElement): Boolean {

                return if (oldItem is RemindListElement.Item && newItem is RemindListElement.Item) {
                    oldItem.reminder.id == newItem.reminder.id
                } else if (oldItem is RemindListElement.Header && newItem is RemindListElement.Header) {
                    oldItem == newItem
                } else false
            }

            override fun areContentsTheSame(oldItem: RemindListElement, newItem: RemindListElement): Boolean {
                return oldItem == newItem
            }
        }
    }
}