package com.example.rememberapp

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.example.rememberapp.data.PriorityLevel
import com.example.rememberapp.data.Task
import com.example.rememberapp.databinding.TaskDetailFragmentBinding
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.TaskDetailViewModel
import com.example.rememberapp.viewmodel.TaskDetailViewModelFactory
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailFragment : Fragment() {

    private val viewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

    /*
     We have variable 'binding' and backing property '_binding' for View Binding
     because it is similar to using a lateinit property, but we can set a backing
     property to null (done in onDestroyView() to avoid memory leaks).
     */
    private var _binding: TaskDetailFragmentBinding? = null
    private val binding get() = _binding!!

    /*
    ----------------------------------------------------
    Parameters:   inflater (LayoutInflater), container (ViewGroup?), savedInstanceState (Bundle?)
    Returns:      View?
    Description:  -Inflates the view, and initializes _binding.
    ----------------------------------------------------
    */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = TaskDetailFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    /*
    ----------------------------------------------------
    Parameters:   taskId (Int)
    Description:  -Uses IO thread to retrieve the Task from database, then switches back to main
                   thread and updates the views with its data.
                  -Uses currentTask variable as an intermediary to ensure viewModel.task contains
                   a value before using it for binding.
                  -If retrieveTask(taskId) is null, then we can assume a task was previously
                   assigned to viewModel.task, and use that for binding. This can happen when we
                   are in completeState, meaning the Task has been deleted from the database.
    ----------------------------------------------------
    */
    private fun bind(taskId: Int) {

        lifecycleScope.launch(Dispatchers.IO) {
            val currentTask = viewModel.retrieveTask(taskId) ?: viewModel.task

            withContext(Dispatchers.Main) {
                viewModel.task = currentTask

                binding.apply {
                    taskName.text = viewModel.task.taskName

                    // Capitalize first letter
                    taskPriority.text = viewModel.task.taskPriority.name.lowercase()
                        .replaceFirstChar { it.uppercase() }

                    // Add Color depending on Task priority
                    taskDetailBanner.setColorFilter(getColorByPriority(viewModel.task.taskPriority))
                }
            }
        }
    }

    /*
    ----------------------------------------------------
    Parameters:   view (View), savedInstanceState (Bundle?)
    Description:  -The navigation argument represents a Task's id. If one exists, then we
                   retrieve the corresponding Task and display its details to the user.
                  -A TouchListener is set for the completeTask imageView. When held for some
                   time, then completeTask() is called.
                  -Uses MenuProvider to add Edit and Delete options to the top app bar.
                   These added options are disabled when the Task is completed, since the
                   Task would already be removed from the database.
                  -Lastly, this function checks whether the viewModel.completeState variable
                   is true, and calls completeTask() if so. Handles configuration changes,
                   such as if the user rotates their screen in completeState.
    ----------------------------------------------------
    */
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use navigation argument to bind correct Task to views
        val id = navigationArgs.taskId
        bind(id)

        // Complete Task Button
        binding.imageView.setOnTouchListener(View.OnTouchListener { v, event ->
            val imageViewDrawable = binding.imageView.drawable as AnimatedVectorDrawable

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    imageViewDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            this@TaskDetailFragment.completeTask(viewModel.task)
                        }
                    })
                    imageViewDrawable.start()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    imageViewDrawable.clearAnimationCallbacks()
                    imageViewDrawable.reset()
                }
            }
            return@OnTouchListener true
        })

        // Initializing MenuProvider
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_fragment_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)

                if(viewModel.completeState) {
                    menu.getItem(0).isEnabled = false
                    menu.getItem(1).isEnabled = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {
                    R.id.edit -> {
                        editTask(viewModel.task)
                        true
                    }
                    R.id.delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    // The back button
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Checking completeState
        if(viewModel.completeState) {
            completeTask(viewModel.task)
        }
    }

    /*
    ----------------------------------------------------
    Description:  -Clears all animation callbacks from the completeTask ImageView,
                   resetting its state so it functions correctly if used again.
                  -Sets _binding to null, avoiding memory leaks.
    ----------------------------------------------------
    */
    override fun onDestroyView() {
        super.onDestroyView()

        // Should always be AnimatedVectorDrawable, but safe calls used anyway
        (binding.imageView.drawable as? AnimatedVectorDrawable)?.clearAnimationCallbacks()

        _binding = null
    }

    /*
    ----------------------------------------------------
    Description:  -If we are in completeState, then calls completeTask().
                  -Without this override, if the user paused and resumed the app,
                   then we'd be in completeState but the ImageView's animation
                   wouldn't play. As such, the user would be frozen on the screen
                   unless they pressed the Back or Up button.
    ----------------------------------------------------
    */
    override fun onResume() {
        super.onResume()

        if(viewModel.completeState) {
            completeTask(viewModel.task)
        }
    }

    /*
    ----------------------------------------------------
    Description:  -This function is called when the user taps the 'delete'
                   menu button on the top app bar.
                  -Displays a dialog to the user, asking to confirm if they'd
                   like to delete the Task. If yes, then calls deleteTask().
    ----------------------------------------------------
    */
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteTask(viewModel.task)
            }
            .show()
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -This function is called when the user taps the 'edit'
                   menu button on the top app bar.
                  -Moves to TaskEditFragment, passing the current
                   Task's ID as a navigation argument.
    ----------------------------------------------------
    */
    private fun editTask(task: Task) {
        val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTaskEditFragment(
            task.id
        )

        this.findNavController().navigate(action)
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Calls the deleteTask() function from the ViewModel, and
                   navigates back to TaskListFragment.
    ----------------------------------------------------
    */
    private fun deleteTask(task: Task) {
        viewModel.deleteTask(task)

        val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    /*
    ----------------------------------------------------
    Parameters:   task (Task)
    Description:  -Completes the Task by deleting it from the database.
                  -The edit and delete Menu options are disabled, as the Task no longer
                   exists for them to perform operations on.
                  -A new animation is displayed to signify Task completion. The user
                   does not need to continue holding the ImageView, and can even exit
                   the app or navigate back before this animation ends.
                  -When the animation finishes, the user will automatically be navigated
                   to the TaskListFragment. This callback is cleared in onDestroyView(),
                   so the user must still be on this Fragment for the navigation to occur.
    ----------------------------------------------------
    */
    @SuppressLint("ClickableViewAccessibility")
    private fun completeTask(task: Task) {

        viewModel.deleteTask(task)
        binding.imageView.setOnTouchListener(null)
        viewModel.completeState = true
        activity?.invalidateOptionsMenu()

        // Play next animation
        binding.imageView.setImageResource(R.drawable.complete_task_anim_2)
        val imageViewDrawable = binding.imageView.drawable as AnimatedVectorDrawable

        imageViewDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToHomeFragment()
                findNavController().navigate(action)
            }
        })
        imageViewDrawable.start()
    }

    /*
    ----------------------------------------------------
    Parameters:   level (PriorityLevel)
    Returns:      Int
    Description:  -Takes a PriorityLevel as a parameter, and returns an integer representing its
                   corresponding color.
    ----------------------------------------------------
    */
    private fun getColorByPriority(level: PriorityLevel): Int {
        return when (level) {
            PriorityLevel.LOW -> ContextCompat.getColor(requireContext(), R.color.priorityLow)
            PriorityLevel.MEDIUM -> ContextCompat.getColor(requireContext(), R.color.priorityMedium)
            PriorityLevel.HIGH -> ContextCompat.getColor(requireContext(), R.color.priorityHigh)
        }
    }
}