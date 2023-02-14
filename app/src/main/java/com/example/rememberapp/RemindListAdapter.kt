package com.example.rememberapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Reminder
import com.example.rememberapp.databinding.RemindListItemBinding
import java.lang.IllegalStateException

class RemindListAdapter(private val onReminderClicked: (RemindListElement) -> Unit) :
    ListAdapter<RemindListElement, RemindListAdapter.RemindViewHolder>(RemindListAdapter.DiffCallback) {


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
        val currentElement = getItem(position)

        if(currentElement is RemindListElement.Header) {
            holder.bind(holder.itemView.context, currentElement.header)
        }
        else if(currentElement is RemindListElement.Item) {
            holder.bind(holder.itemView.context, currentElement.reminder)

            holder.itemView.setOnClickListener {
                onReminderClicked(currentElement)
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
            binding.apply {
                // binding
            }
        }

        fun bind(ctx: Context, header: String) {
            binding.apply {
                // binding
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