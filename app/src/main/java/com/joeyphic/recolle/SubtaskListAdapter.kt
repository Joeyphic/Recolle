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

    /*
    ----------------------------------------------------
    Parameters:   holder (SubtaskViewHolder), position (int)
    Description:  -Binds the data to the SubtaskViewHolder at the specified
                   position.
                  -A click listener is set here. Each fragment that uses
                   the SubtaskListAdapter can customize what action will be
                   taken on click.
                  -When clicked in TaskDetailFragment, it will modify Subtask.checked to the
                   opposite value.
    ----------------------------------------------------
    */
    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        val currentSubtask = getItem(position)
        holder.itemView.setOnClickListener {
            onSubtaskClicked(currentSubtask)
        }
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
        Parameters:   ctx (Context), subtask (Subtask)
        Description:  -Binds the Subtask details to the ViewHolder.
        ----------------------------------------------------
        */
        fun bind(ctx: Context, subtask: Subtask) {
            binding.apply {
                subtaskName.text = subtask.subtaskName
                Log.i("recolletesting", "bound. " + subtaskName.text.toString())
                if(subtask.checked) {
                    relativeLayout.setBackgroundColor(ctx.getColor(R.color.subtaskListItemCheckedBackgroundColor))
                }
                else {
                    relativeLayout.setBackgroundColor(ctx.getColor(R.color.subtaskListItemBackgroundColor))
                }
            }
        }
    }

    /*
    ----------------------------------------------------
    Description:  -DiffUtil.ItemCallback, used to identify changes in
                   Subtasks from the SubtaskList.
                  -We are using .subtaskName to determine the same items, as this ensures the list
                   behaves correctly when using placeholder IDs in SubtaskEditFragment. Since the
                   subtask name is never edited, irregular behavior is limited.
                  -The only issue is minor: if a subtask is given an identical name and the list
                   items are next to each other, there is a slight visual bug as it appears
                   inserted into the second-to-last value of the unchecked items.
    ----------------------------------------------------
    */
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subtask>() {
            override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem.subtaskName == newItem.subtaskName
            }

            override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem == newItem
            }
        }
    }
}