package com.example.rememberapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.databinding.RemindListItemBinding

class RemindListAdapter(private val onReminderClicked: (Reminder) -> Unit) :
    ListAdapter<Reminder, RemindListAdapter.RemindViewHolder>(RemindListAdapter.DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): RemindListAdapter.RemindViewHolder {
        return RemindListAdapter.RemindViewHolder(
            RemindListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    // Have to support two item types now?
    override fun onBindViewHolder(holder: RemindListAdapter.RemindViewHolder, position: Int) {
        val currentReminder = getItem(position)
        holder.itemView.setOnClickListener {
            onReminderClicked(currentReminder)
        }
        holder.bind(holder.itemView.context, currentReminder)
    }


    class RemindViewHolder(private var binding: RemindListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ctx: Context, reminder: Reminder) {
            binding.apply {
                // binding
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
                return oldItem == newItem
            }
        }
    }
}