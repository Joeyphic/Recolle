package com.example.rememberapp

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.getColorByPriority
import com.example.rememberapp.databinding.TaskListItemFragmentBinding

class TaskListAdapter(private val onTaskClicked: (Task) -> Unit) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): TaskViewHolder {
        return TaskViewHolder(
            TaskListItemFragmentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.itemView.setOnClickListener {
            onTaskClicked(currentTask)
        }
        holder.bind(currentTask)
    }

    // TODO: Determine if this is a necessary function
    override fun getItemViewType(position: Int) : Int {
        val currentTask = getItem(position)
        return currentTask.taskPriority.ordinal
    }

    class TaskViewHolder(private var binding: TaskListItemFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                taskName.text = task.taskName
                relativeLayout.setBackgroundColor(task.getColorByPriority())
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }

    sealed class DataItem {

        abstract val id: Int

        data class TaskListItem(val task: Task): DataItem() {
            override val id = task.id
        }

        object Header: DataItem() {
            override val id = Int.MIN_VALUE
        }
    }

    // TODO: Finish implementing ItemTouchHelper
    class SimpleCallback(
        private val adapter: TaskListAdapter,
        private val onItemMove: (from: Int, to: Int) -> Unit
    ) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Log.i("TaskListAdapter", "vh pos " + viewHolder.adapterPosition)
                Log.i("TaskListAdapter", "target pos " + target.adapterPosition)

                // Although adapter is passed parameter, we can still use getItem since we are in
                // TaskListAdapter.kt. That's why this class is in the adapter!
                val currentTask = viewHolder.adapterPosition
                val affectedTask = target.adapterPosition

                // Perhaps use DragDirs for better readability?
                if(currentTask < affectedTask) {
                    for (i in currentTask until affectedTask) {
                        onItemMove(i, i + 1)
                        adapter.notifyItemMoved(i, i + 1)
                    }
                }
                else {
                    if(affectedTask > -1) {
                        for (i in currentTask downTo affectedTask+1) {
                            onItemMove(i, i - 1)
                            adapter.notifyItemMoved(i, i - 1)
                        }
                    }
                }
                return true
            }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

        }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        }
}