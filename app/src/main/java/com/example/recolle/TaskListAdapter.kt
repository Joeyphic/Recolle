package com.example.recolle

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recolle.data.PriorityLevel
import com.example.recolle.data.Task
import com.example.recolle.databinding.TaskListItemBinding

class TaskListAdapter(private val onTaskClicked: (Task) -> Unit) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallback) {

    /*
    ----------------------------------------------------
    Parameters:   parent (ViewGroup), viewType (int)
    Description:  -Initializes ViewHolder.
                  -The same ViewHolder will be used for each View in the RecyclerView.
                   Therefore, ItemType is always the same.
    ----------------------------------------------------
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
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

        /*
        ----------------------------------------------------
        Parameters:   viewHolder (RecyclerView.ViewHolder?), actionState (Int)
        Description:  -The function is called when the actionState changes. We're only interested in
                       when the ViewHolder's dragging begins, as we determine what happens after
                       dragging finishes in the clearView() function.
                      -DragDirs is set to 0, as this ensures no other ViewHolder can be dragged
                       while our current one is being interacted with.
                      -We change the adapter's list to a TemporaryList. This allows us to modify the
                       list to the user without changing the underlying Room database quite yet.
        ----------------------------------------------------
        */
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

        /*
        ----------------------------------------------------
        Parameters:   recyclerView (RecyclerView), current: (RecyclerView.ViewHolder),
                       target: (RecyclerView.ViewHolder)
        Returns:      boolean
        Description:  -We ensure that Tasks can only change positions in the list if their
                       taskPriority is the same as the one which the dragged ViewHolder represents.
        ----------------------------------------------------
        */
        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val posFrom = current.adapterPosition
            val posTo = target.adapterPosition

            return temporaryList[posFrom].taskPriority == temporaryList[posTo].taskPriority
        }

        /*
        ----------------------------------------------------
        Parameters:   recyclerView (RecyclerView), viewHolder: (RecyclerView.ViewHolder),
                       target: (RecyclerView.ViewHolder)
        Returns:      boolean
        Description:  -We are using temporaryList during this move operation, because otherwise we'd
                       have to modify the Room database on each move, which is unnecessary overhead.
                      -We remove the represented Task from temporaryList then immediately add it
                       back to the new location. A swap would not work because it is possible for a
                       ViewHolder to be moved multiple spots away from its original position.
        ----------------------------------------------------
        */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val posFrom = viewHolder.adapterPosition
            val posTo = target.adapterPosition

            val currentTask = temporaryList[posFrom]
            temporaryList.removeAt(posFrom)
            temporaryList.add(posTo, currentTask)

            adapter.notifyItemMoved(posFrom, posTo)

            return true
        }

        /*
        ----------------------------------------------------
        Parameters:   recyclerView (RecyclerView), viewHolder: (RecyclerView.ViewHolder)
        Description:  -This function is called after the ViewHolder is done moving. As such, the
                       the finishing operations are handled here.
                      -The DefaultDragDirs are changed back to their original values, which allows
                       for other ViewHolders to be dragged again.
                      -The taskListPosition variable of the Tasks is modified in accordance to their
                       final location after moving. This is done to avoid visual effects that occur
                       when the TaskList is later updated from the database.
                      -Lastly, the taskId and position after moving are sent to the ViewModel,
                       which performs the moveTask operation in the TaskDao.
        ----------------------------------------------------
        */
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

        // Swiping is not supported in this list
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return
        }
    }
}