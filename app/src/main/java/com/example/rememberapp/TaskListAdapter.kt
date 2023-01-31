package com.example.rememberapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.databinding.TaskListItemBinding

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
            TaskListItemBinding.inflate(
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
        holder.bind(holder.itemView.context, currentTask)
    }

    /*
    ----------------------------------------------------
    Inner class:  TaskViewHolder
    Description:  -The ViewHolder for the RecyclerView. Represents a single
                  Task in the list.
    ----------------------------------------------------
    */
    class TaskViewHolder(private var binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /*
        ----------------------------------------------------
        Parameters:   ctx (Context), task (Task)
        Description:  -Binds the task details to the ViewHolder.
        ----------------------------------------------------
        */
        fun bind(ctx: Context, task: Task) {
            binding.apply {
                taskName.text = task.taskName
                relativeLayout.setBackgroundColor(getColorByPriority(ctx, task.taskPriority))
            }
        }

        /*
        ----------------------------------------------------
        Parameters:   ctx (Context), level (PriorityLevel)
        Returns:      Int
        Description:  -Takes a PriorityLevel as a parameter, and returns an integer representing its
                       corresponding color.
                      -We use the Context in order to access the color resources.
        ----------------------------------------------------
        */
        private fun getColorByPriority(ctx: Context, level: PriorityLevel): Int {
            return when (level) {
                PriorityLevel.LOW -> ContextCompat.getColor(ctx, R.color.priorityLow)
                PriorityLevel.MEDIUM -> ContextCompat.getColor(ctx, R.color.priorityMedium)
                PriorityLevel.HIGH -> ContextCompat.getColor(ctx, R.color.priorityHigh)
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
    Description:  -This class is used to hold, drag, and rearrange Tasks in TaskListFragment.
                   It will also interact with the Room database to save the order.
                  -This feature is important, because the user can organize tasks closer to
                   their personal preference. It helps make the app feel like their own.
                  -The user can rearrange tasks that are in the same priority, but not those that
                   are of different priority. This makes it so the highest priority tasks are
                   always on the top, maintaining organization for the user.
    ----------------------------------------------------
    */

    class TaskTouchHelper(
        private val adapter: TaskListAdapter,
        private val onItemMove: (taskId: Int, to: Int) -> Unit
    ) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        lateinit var temporaryList : MutableList<Task>

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if(actionState == ACTION_STATE_DRAG && viewHolder != null) {
                setDefaultDragDirs(0)

                viewHolder.itemView.background.alpha = 200
                viewHolder.itemView.isClickable = false

                temporaryList = adapter.currentList.toMutableList()
                adapter.submitList(temporaryList)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            setDefaultDragDirs(ItemTouchHelper.UP or ItemTouchHelper.DOWN)

            viewHolder.itemView.background.alpha = 255
            viewHolder.itemView.isClickable = true

            val taskId = temporaryList[viewHolder.adapterPosition].id
            val originalPosition = temporaryList[viewHolder.adapterPosition].taskListPosition
            val toPosition = viewHolder.adapterPosition

            val maxIndex = maxOf(originalPosition, toPosition)
            val minIndex = minOf(originalPosition, toPosition)
            for(i in minIndex..maxIndex) {
                temporaryList[i].taskListPosition = i
            }

            onItemMove(taskId, toPosition)
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