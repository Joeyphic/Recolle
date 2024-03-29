package com.joeyphic.recolle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.joeyphic.recolle.R
import com.joeyphic.recolle.RemindDetailFragmentArgs
import com.joeyphic.recolle.RemindDetailFragmentDirections
import com.joeyphic.recolle.data.Reminder
import com.joeyphic.recolle.databinding.RemindDetailFragmentBinding
import com.joeyphic.recolle.viewmodel.RemindDetailViewModel
import com.joeyphic.recolle.viewmodel.RemindDetailViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


class RemindDetailFragment : Fragment() {

    private val viewModel: RemindDetailViewModel by viewModels {
        RemindDetailViewModelFactory(
            (activity?.application as RecolleApplication).database.remindDao()
        )
    }

    private val navigationArgs: RemindDetailFragmentArgs by navArgs()

    private var _binding: RemindDetailFragmentBinding? = null
    private val binding get() = _binding!!

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
            val currentTask = viewModel.retrieveReminder(taskId) ?: viewModel.reminder

            withContext(Dispatchers.Main) {
                viewModel.reminder = currentTask

                binding.apply {
                    reminderName.text = currentTask.name
                    reminderEventTime.text = currentTask.eventTime.format(viewModel.dateTimeFormat)
                    reminderRemindTime.text = currentTask.remindTime.format(viewModel.dateTimeFormat)

                    if(currentTask.checked) {
                        showReminderIsChecked()
                    }
                    else {
                        buttonCheck.isEnabled = LocalDateTime.now() >= currentTask.remindTime
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = RemindDetailFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use navigation argument to bind correct Reminder to views
        val id = navigationArgs.reminderId
        bind(id)

        // Set onClick Listener for buttonCheck
        binding.buttonCheck.setOnClickListener {
            viewModel.checkReminder()

            /*
            val ns = Context.NOTIFICATION_SERVICE
            (context?.getSystemService(ns) as NotificationManager).cancel(viewModel.reminder.id)
             */
            NotificationManagerCompat.from(requireContext()).cancel(viewModel.reminder.id)

            showReminderIsChecked()
        }

        // Initializing MenuProvider
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_fragment_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)

                /*
                if(viewModel.completeState) {
                    menu.getItem(0).isEnabled = false
                    menu.getItem(1).isEnabled = false
                }
                */
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {
                    R.id.edit -> {
                        editReminder(viewModel.reminder)
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

    private fun editReminder(reminder: Reminder) {
        val action = RemindDetailFragmentDirections.actionRemindDetailFragmentToRemindEditFragment(
            reminder.id
        )

        this.findNavController().navigate(action)
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.reminder_delete_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteReminder()
            }
            .show()
    }

    private fun deleteReminder() {
        viewModel.deleteReminder()
        NotificationManagerCompat.from(requireContext()).cancel(viewModel.reminder.id)
        // Allows us to retain our place in the RemindList, instead of being at the top.
        this.findNavController().navigateUp()
    }

    private fun showReminderIsChecked() {
        (binding.buttonCheck as? MaterialButton)?.setIconResource(R.drawable.ic_baseline_check_24)
        binding.buttonCheck.text = "Checked in"
        binding.buttonCheck.isEnabled = false
        binding.checkedDescriptionLabel.visibility = View.VISIBLE
    }
}