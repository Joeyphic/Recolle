package com.example.recolle

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> { TaskListFragment() }
            1 -> { RemindListFragment() }
            else -> { throw IllegalStateException("Invalid position") }
        }
    }
}