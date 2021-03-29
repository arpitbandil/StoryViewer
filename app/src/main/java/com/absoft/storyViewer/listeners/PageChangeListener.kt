package com.absoft.storyViewer.listeners

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.viewpager.widget.ViewPager.*

abstract class PageChangeListener : OnPageChangeListener {

    private var pageBeforeDragging = 0
    private var currentPage = 0
    private var lastTime = DEBOUNCE_TIMES + 1L

    override fun onPageScrollStateChanged(state: Int) {
        when (state) {
            SCROLL_STATE_IDLE -> {
                if (System.currentTimeMillis() - lastTime < DEBOUNCE_TIMES) {
                    return
                }
                lastTime = System.currentTimeMillis()
                Handler(Looper.myLooper()!!).postDelayed({
                    if (pageBeforeDragging == currentPage) {
                        onPageScrollCanceled()
                    }
                }, 300L)
            }
            SCROLL_STATE_DRAGGING -> {
                pageBeforeDragging = currentPage
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        currentPage = position
    }

    abstract fun onPageScrollCanceled()

    companion object {
        private const val DEBOUNCE_TIMES = 500L
    }
}
