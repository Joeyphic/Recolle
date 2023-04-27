package com.example.rememberapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.databinding.RemindListHeaderBinding
import com.example.rememberapp.databinding.RemindListItemBinding
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

            val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            val dateTimeFormatWithoutYear: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a, MMM d")
            val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a, MMM d, yyyy")

            val eventDateTime = reminder.eventTime
            val remindDateTime = reminder.remindTime

            val outputString = if(eventDateTime == remindDateTime) {
                "@ ${eventDateTime.format(timeFormat)}."
            }
            else if(eventDateTime.toLocalDate() == remindDateTime.toLocalDate()) {
                "@ ${eventDateTime.format(timeFormat)}. Remind at ${remindDateTime.format(timeFormat)}."
            }
            else if(eventDateTime.year == remindDateTime.year) {
                "@ ${eventDateTime.format(timeFormat)}. Remind at ${remindDateTime.format(dateTimeFormatWithoutYear)}."
            }
            else {
                "@ ${eventDateTime.format(timeFormat)}. Remind at ${remindDateTime.format(dateTimeFormat)}."
            }

            binding.apply {
                reminderName.text = reminder.name
                reminderTimes.text = outputString

                if(reminder.checked) {
                    alertIcon.setImageResource(R.drawable.ic_baseline_check_24)
                    alertIcon.setColorFilter(ctx.getColor((R.color.primaryDarkColor)))
                    alertIcon.visibility = View.VISIBLE
                }
                else if(LocalDateTime.now() > reminder.eventTime) {
                    alertIcon.setImageResource(R.drawable.ic_baseline_priority_high_24)
                    alertIcon.setColorFilter(ctx.getColor((R.color.remindOverdueColor)))
                    alertIcon.visibility = View.VISIBLE
                }
                else if(LocalDateTime.now() >= reminder.remindTime) {
                    alertIcon.setImageResource(R.drawable.ic_baseline_priority_high_24)
                    alertIcon.setColorFilter(ctx.getColor((R.color.primaryDarkColor)))
                    alertIcon.visibility = View.VISIBLE
                }
            }
        }
    }

    class HeaderViewHolder(private var binding: RemindListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // TODO: Cannot be LocalDateTime, must be string. Want to add "(Today)" to current date.
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