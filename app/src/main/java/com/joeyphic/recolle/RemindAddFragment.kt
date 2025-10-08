package com.joeyphic.recolle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.joeyphic.recolle.databinding.RemindListAddItemBinding
import com.joeyphic.recolle.viewmodel.RemindAddViewModel
import com.joeyphic.recolle.viewmodel.RemindAddViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemindAddFragment : Fragment() {

    private val viewModel: RemindAddViewModel by viewModels {
        RemindAddViewModelFactory(
            (activity?.application as RecolleApplication).database.remindDao()
        )
    }

    private var _binding: RemindListAddItemBinding? = null
    private val binding get() = _binding!!

    lateinit var alarmScheduler: RemindAlarmScheduler

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            saveAndScheduleReminder()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = RemindListAddItemBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmScheduler = RemindAlarmScheduler(view.context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.eventDate.collect {
                        binding.eventDate.setText(it?.format(viewModel.dateFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.eventTime.collect {
                        binding.eventTime.setText(it?.format(viewModel.timeFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.remindDate.collect {
                        binding.remindDate.setText(it?.format(viewModel.dateFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.remindTime.collect {
                        binding.remindTime.setText(it?.format(viewModel.timeFormat) ?: "")
                        viewModel.updateUIState()
                    }
                }

                launch {
                    viewModel.uiState.collect {
                        if(it.isAutoEnabled) binding.autoButton.isEnabled = true
                        if(it.isSaveEnabled) binding.saveButton.isEnabled = true
                        if(it.errorMessage != null) {
                            withContext(Dispatchers.Main) {
                                context?.let { ctx ->
                                    MaterialAlertDialogBuilder(ctx)
                                        .setTitle(getString(R.string.dialog_invalid_reminder_title))
                                        .setMessage(it.errorMessage)
                                        .setPositiveButton(getString(R.string.dialog_invalid_reminder_OK)) { _, _ ->
                                            viewModel.errorMessageShown()
                                        }
                                        .setOnDismissListener {
                                            viewModel.errorMessageShown()
                                        }
                                        .show()
                                }
                            }
                        }
                        if(it.permissionMessage != null) {
                            withContext(Dispatchers.Main) {
                                context?.let { ctx ->
                                    MaterialAlertDialogBuilder(ctx)
                                        .setMessage(it.permissionMessage)
                                        .setPositiveButton(getString(R.string.dialog_invalid_reminder_OK)) { _, _ ->
                                            viewModel.permissionMessageShown()
                                            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                                        }
                                        .setNegativeButton(getString(R.string.notification_rationale_refuse)) { _, _ ->
                                            viewModel.permissionMessageShown()
                                            saveAndScheduleReminder()
                                        }
                                        .setOnDismissListener {
                                            viewModel.permissionMessageShown()
                                        }
                                        .show()
                                }
                            }
                        }
                        if(it.picker != null) {
                            it.picker?.show(parentFragmentManager, "RemindAddFragment")
                        }
                    }
                }
            }
        }

        binding.eventDate.setOnClickListener { viewModel.initializeEventDatePicker() }
        binding.eventTime.setOnClickListener { viewModel.initializeEventTimePicker() }
        binding.remindDate.setOnClickListener { viewModel.initializeRemindDatePicker() }
        binding.remindTime.setOnClickListener { viewModel.initializeRemindTimePicker() }

        binding.autoButton.setOnClickListener {
            viewModel.autoSetRemindVariables()
        }

        binding.saveButton.setOnClickListener {
            requestPermissionsAndSave()
        }
    }

    /*
    ----------------------------------------------------
    Description:  -After the user saves a reminder, permissions are checked to ensure the user will
                   receive notifications regarding their reminder. If necessary, we ask them for the
                   POST_NOTIFICATIONS permission here.
                  -A permission rationale may be shown depending on if the user has declined or
                   turned off notifications previously. Android determines whether it will show
                   using the .showShouldRequestPermissionRationale() function.
                  -Whether the user accepts notifications or not, we move onto the
                   saveAndScheduleReminder() function afterwards.
    ----------------------------------------------------
    */
    fun requestPermissionsAndSave() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            saveAndScheduleReminder()
            return
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED -> {
                saveAndScheduleReminder()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), "android.permission.POST_NOTIFICATIONS"
            ) -> {
                viewModel.displayPermissionRationale()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    "android.permission.POST_NOTIFICATIONS"
                )
            }
        }
    }

    /*
    ----------------------------------------------------
    Description:  -Here, we save the Reminder into the database and schedule it with alarmScheduler.
                  -We're using alarmScheduler in the Fragment because we can access the necessary
                   context to create the alarmScheduler, as performed earlier.
                  -A better practice is to use dependency injection (Hilt) to provide it in the
                   ViewModel's parameters. However, this project does not use it.
    ----------------------------------------------------
    */
    fun saveAndScheduleReminder() {
        var newReminder = viewModel.createNewReminderOrNull(binding.reminderName.text.toString())
            ?: return


        CoroutineScope(Dispatchers.IO).launch {
            val reminderId = viewModel.insertReminder(newReminder)
            newReminder = newReminder.copy(id = reminderId.toInt())
            alarmScheduler.schedule(newReminder)
        }
        val action = RemindAddFragmentDirections.actionRemindAddFragmentToHomeFragment(1)
        findNavController().navigate(action)
    }
}