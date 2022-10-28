package com.example.rememberapp

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.example.rememberapp.data.Task
import com.example.rememberapp.data.getColorByPriority
import com.example.rememberapp.databinding.TaskDetailFragmentBinding
import com.example.rememberapp.databinding.TaskListFragmentBinding
import com.example.rememberapp.viewmodel.TaskListViewModel
import com.example.rememberapp.viewmodel.TaskListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TaskDetailFragment : Fragment() {

    private val viewModel: TaskListViewModel by activityViewModels {
        TaskListViewModelFactory(
            (activity?.application as RememberApplication).database.taskDao()
        )
    }

    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

    private var _binding: TaskDetailFragmentBinding? = null

    // TODO: Find a way to remove this non-null asserted call
    private val binding get() = _binding!!

    lateinit var task: Task

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = TaskDetailFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    private fun bind(task: Task) {
        binding.apply {
            taskName.text = task.taskName

            // Capitalize first letter
            taskPriority.text = task.taskPriority.name.lowercase()
                .replaceFirstChar { it.uppercase() }

            // Adding Color depending on Task priority
            binding.taskDetailBanner.setColorFilter(task.getColorByPriority())
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigation argument
        val id = navigationArgs.taskId
        viewModel.retrieveTask(id).observe(this.viewLifecycleOwner) { selectedTask ->
            selectedTask?.let {
                task = selectedTask
                bind(task)
            }
        }

        // Complete Task Button
        // TODO: Fix bug of animation staying completed between fragments
        // TODO: Change to Touch Listener
        binding.imageView.setOnTouchListener(View.OnTouchListener { v, event ->
            val imageViewDrawable = binding.imageView.drawable as AnimatedVectorDrawable

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    //Selected task is null before navigating back bc theres an observer.
                    imageViewDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            this@TaskDetailFragment.completeTask(task)
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

        // Using MenuProvider to add Edit and Delete options to the top app bar.
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.detail_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {
                    R.id.edit -> {
                        editTask(task)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteTask(task)
            }
            .show()
    }

    private fun editTask(task: Task) {
        val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTaskListAddModifyItem(
            "Edit Task",
            task.id
        )

        this.findNavController().navigate(action)
    }

    private fun deleteTask(task: Task) {
        viewModel.deleteTask(task)
        findNavController().navigateUp()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun completeTask(task: Task) {

        viewModel.deleteTask(task)
        binding.imageView.setOnTouchListener(null);

        // Play second half of animation
        binding.imageView.setImageResource(R.drawable.complete_task_anim_2)
        val imageViewDrawable = binding.imageView.drawable as AnimatedVectorDrawable

        imageViewDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTaskListFragment()
                findNavController().navigate(action)
            }
        })
        imageViewDrawable.start()
    }
}