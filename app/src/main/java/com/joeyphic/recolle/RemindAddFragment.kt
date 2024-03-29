package com.joeyphic.recolle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val alarmScheduler = RemindAlarmScheduler(view.context)

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
            var newReminder = viewModel.createNewReminderOrNull(binding.reminderName.text.toString())
                ?: return@setOnClickListener

            CoroutineScope(Dispatchers.IO).launch {
                val reminderId = viewModel.insertReminder(newReminder)
                newReminder = newReminder.copy(id = reminderId.toInt())
                alarmScheduler.schedule(newReminder)
            }
            val action = RemindAddFragmentDirections.actionRemindAddFragmentToHomeFragment(1)
            findNavController().navigate(action)
        }
    }
}