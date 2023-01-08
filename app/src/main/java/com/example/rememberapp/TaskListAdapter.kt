package com.example.rememberapp

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.getColorByPriority
import com.example.rememberapp.databinding.TaskListItemFragmentBinding

class TaskListAdapter(private val onTaskClicked: (Task) -> Unit) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallback) {

    /*
    ----------------------------------------------------
    Parameters:   parent (ViewGroup), ViewType (int)
    Description:  -Initializes ViewHolder.
                  -The same ViewHolder will be used for each View in the RecyclerView.
                   Therefore, ItemType is always the same.
    ----------------------------------------------------
    */
    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): TaskViewHolder {
        return TaskViewHolder(
            TaskListItemFragmentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    /*
    ----------------------------------------------------
    Parameters:   holder (TaskViewHolder), position (int)
    Description:  -Binds the data to the TaskViewHolder at the specified
                   position.
                  -A click listener is set here. Each fragment that uses
                   the TaskListAdapter can customize what action will be
                   taken on click.
                  -When clicked in TaskListFragment, the user will be brought
                   to TaskDetailFragment with the ID of the bound Task
                   serving as a navigation argument.
    ----------------------------------------------------
    */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.itemView.setOnClickListener {
            onTaskClicked(currentTask)
        }
        holder.bind(currentTask)
    }

    /*
    ----------------------------------------------------
    Inner class:  TaskViewHolder
    Description:  -The ViewHolder for the RecyclerView. Represents a single
                  Task in the list.
    ----------------------------------------------------
    */
    class TaskViewHolder(private var binding: TaskListItemFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                taskName.text = task.taskName
                relativeLayout.setBackgroundColor(task.getColorByPriority())
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
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }

    /*
    ----------------------------------------------------
    Inner class:  TaskTouchHelper
    Parameters:   adapter (TaskListAdapter), onItemMove ((from: Int, to:Int) -> Unit)
    Description:  -This class is used to hold, drag, and rearrange TaskList items
                   in the TaskListFragment.
                  -This feature is important, because the user can organize tasks closer to
                   their personal preference. It helps make the app feel like their own.
                  -The user can rearrange tasks that are in the same priority, but not those that
                   are of different priority. This makes it so the highest priority tasks are
                   always on the top, maintaining organization for the user.
    ----------------------------------------------------
    */

    // TODO: Create isDraggable boolean flag or find another option.
    class TaskTouchHelper(
        private val adapter: TaskListAdapter,
        private val onItemMove: (from: Int, to: Int) -> Unit
    ) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        lateinit var temporaryList : MutableList<Task>
        private var from = -1
        private var to = -1

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if(actionState == ACTION_STATE_DRAG && viewHolder != null) {
                setDefaultDragDirs(0)
                from = viewHolder.adapterPosition

                viewHolder.itemView.background.alpha = 200
                viewHolder.itemView.isClickable = false

                temporaryList = adapter.currentList.toMutableList()
                adapter.submitList(temporaryList)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            setDefaultDragDirs(ItemTouchHelper.UP or ItemTouchHelper.DOWN)
            to = viewHolder.adapterPosition

            viewHolder.itemView.background.alpha = 255
            viewHolder.itemView.isClickable = true

            val maxIndex = maxOf(from, to)
            val minIndex = minOf(from, to)
            for(i in minIndex..maxIndex) {
                temporaryList[i].taskListPosition = i
            }

            onItemMove(from, to)
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val posFrom = current.adapterPosition
            val posTo = target.adapterPosition

            return temporaryList[posFrom].taskPriority == temporaryList[posTo].taskPriority
        }
        // Idea is use onMove to check Priority between Tasks and move if same.
        // Can set position at beginning (selChanged) and end (clrView), then pass that value
        // to database with orderPosition value in ViewModel.
        // notifyItemMoved seems to be the key BC theres no previous rev. (In dev diary)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            Log.i("TaskListAdapter", "vh pos " + viewHolder.adapterPosition)
            Log.i("TaskListAdapter", "target pos " + target.adapterPosition)
            Log.i("TaskListAdapter", "list: " + adapter.currentList)
            // Although adapter is passed parameter, we can still use adapterPosition since we
            // are in TaskListAdapter.kt. That's why this class is in the adapter!

            val posFrom = viewHolder.adapterPosition
            val posTo = target.adapterPosition

            val currentTask = temporaryList[posFrom]
            temporaryList.removeAt(posFrom)
            temporaryList.add(posTo, currentTask)

            adapter.notifyItemMoved(posFrom, posTo)

            Log.i("TaskListAdapter", "list2: " + adapter.currentList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return // Swiping is not supported in this list
        }
    }
}