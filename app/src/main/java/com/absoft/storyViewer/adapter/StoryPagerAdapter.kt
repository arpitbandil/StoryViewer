package com.absoft.storyViewer.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.absoft.storyViewer.StoryFragment
import com.absoft.storyViewer.models.StoryDetails
import com.absoft.storyViewer.models.UserDetails

class StoryPagerAdapter<T, E : StoryDetails> constructor(
    fragmentManager: FragmentManager,
    private val storyList: ArrayList<T>
) : FragmentStatePagerAdapter(
    fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) where  T : UserDetails<E>, T : Parcelable? {

    override fun getItem(position: Int): Fragment =
        StoryFragment.newInstance(position, storyList[position])

    override fun getCount(): Int {
        return storyList.size
    }

    fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment? {
        try {
            val f = instantiateItem(viewPager, position)
            return f as? Fragment
        } finally {
            finishUpdate(viewPager)
        }
    }
}