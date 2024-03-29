package com.joeyphic.recolle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.joeyphic.recolle.HomeFragmentArgs
import com.joeyphic.recolle.R
import com.joeyphic.recolle.databinding.HomeFragmentBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: HomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = HomeFragmentBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        // -A better user experience is provided by loading both pages at once.
        // -If this line is removed, then dragging logic in TaskListFragment must be modified to
        //  ensure it works even when RemindListFragment is loaded first.
        viewPager.offscreenPageLimit = 1

        viewPager.adapter = PagerAdapter(this)

        val startingPageNumber = navigationArgs.pageNum
        viewPager.setCurrentItem(startingPageNumber, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, index ->
            tab.text = when(index) {
                0 -> getString(R.string.home_tab_tasks)
                1 -> getString(R.string.home_tab_reminders)
                else -> throw IllegalStateException("Invalid Index")
            }
        }.attach()

    }
}