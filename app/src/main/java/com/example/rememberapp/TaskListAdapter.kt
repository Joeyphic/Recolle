package com.example.rememberapp

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rememberapp.data.Task
import com.example.rememberapp.databinding.TaskListItemBinding

class TaskListAdapter() : ListAdapter<Task, TaskListAdapter.TaskViewHolder> {

    class TaskViewHolder(private var binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        }

}