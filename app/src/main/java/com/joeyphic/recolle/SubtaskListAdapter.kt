package com.joeyphic.recolle

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joeyphic.recolle.data.PriorityLevel
import com.joeyphic.recolle.data.Subtask
import com.joeyphic.recolle.data.Task
import com.joeyphic.recolle.databinding.SubtaskListItemBinding
import com.joeyphic.recolle.databinding.TaskListItemBinding

    class SubtaskListAdapter(private val onSubtaskClicked: (Subtask) -> Unit) :
    ListAdapter<Subtask, SubtaskListAdapter.SubtaskViewHolder>(DiffCallback) {

    /*
    ----------------------------------------------------
    Parameters:   parent (ViewGroup), viewType (int)
    Description:  -Initializes ViewHolder.
                  -The same ViewHolder will be used for each View in the RecyclerView.
                   Therefore, ItemType is always the same.
    ----------------------------------------------------
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        return SubtaskViewHolder(
            SubtaskListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        val currentSubtask = getItem(position)
        holder.bind(holder.itemView.context, currentSubtask)
    }

    /*
    ----------------------------------------------------
    Inner class:  SubtaskViewHolder
    Description:  -The ViewHolder for the RecyclerView. Represents a single
                  Subtask in the list.
    ----------------------------------------------------
    */
    class SubtaskViewHolder(private var binding: SubtaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /*
        ----------------------------------------------------
        Parameters:   ctx (Context), task (Task)
        Description:  -Binds the task details to the ViewHolder.
        ----------------------------------------------------
        */
        fun bind(ctx: Context, subtask: Subtask) {
            binding.apply {
                subtaskName.text = subtask.subtaskName
                Log.i("recolletesting", "bound. " + subtaskName.text.toString())
            }
        }
    }

    /*
    ----------------------------------------------------
    Description:  -DiffUtil.ItemCallback, used to identify changes in
                   Tasks from the TaskList.
    ----------------------------------------------------
    */
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subtask>() {
            override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem == newItem
            }
        }
    }
}