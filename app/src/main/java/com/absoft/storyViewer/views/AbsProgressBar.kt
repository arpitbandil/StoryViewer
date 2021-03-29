package com.absoft.storyViewer.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.absoft.storyViewer.R
import com.absoft.storyViewer.animation.ProgressPauseScaleAnimation
import com.absoft.storyViewer.databinding.ItemProgressPauseBinding
import com.absoft.storyViewer.extensions.hide
import com.absoft.storyViewer.extensions.show

class AbsProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var animation: ProgressPauseScaleAnimation? = null
    private var duration = DEFAULT_PROGRESS_DURATION
    private var callback: Callback? = null
    private var isStarted = false

    private var binding: ItemProgressPauseBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_progress_pause, this, true)

    fun setDuration(duration: Long) {
        this.duration = duration
        if (animation != null) {
            animation = null
            startProgress()
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setMax() {
        finishProgress(true)
    }

    fun setMin() {
        finishProgress(false)
    }

    fun setMinWithoutCallback() {
        binding.maxProgress.setBackgroundResource(R.color.progress_secondary)
        binding.maxProgress.show()
        animation?.setAnimationListener(null)
        animation?.cancel()
    }

    fun setMaxWithoutCallback() {
        binding.maxProgress.setBackgroundResource(R.color.progress_max_active)
        binding.maxProgress.show()
        animation?.setAnimationListener(null)
        animation?.cancel()
    }

    private fun finishProgress(isMax: Boolean) {
        if (isMax) {
            binding.maxProgress.setBackgroundResource(R.color.progress_max_active)
            binding.maxProgress.show()
        } else {
            binding.maxProgress.hide()
        }
        animation?.setAnimationListener(null)
        animation?.cancel()
        callback?.onFinishProgress()
    }

    fun startProgress() {
        binding.maxProgress.hide()
        if (duration <= 0) duration = DEFAULT_PROGRESS_DURATION
        animation =
            ProgressPauseScaleAnimation(
                0f,
                1f,
                1f,
                1f,
                Animation.ABSOLUTE,
                0f,
                Animation.RELATIVE_TO_SELF,
                0f
            )
        animation?.duration = duration
        animation?.interpolator = LinearInterpolator()
        animation?.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (isStarted) {
                    return
                }
                isStarted = true
                binding.frontProgress.show()
                if (callback != null) callback!!.onStartProgress()
            }

            override fun onAnimationEnd(animation: Animation) {
                isStarted = false
                if (callback != null) callback!!.onFinishProgress()
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        animation?.fillAfter = true
        binding.frontProgress.startAnimation(animation)
    }

    fun pauseProgress() {
        animation?.pause()
    }

    fun resumeProgress() {
        animation?.resume()
    }

    fun clear() {
        animation?.setAnimationListener(null)
        animation?.cancel()
        animation = null
    }

    interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

    companion object {
        private const val DEFAULT_PROGRESS_DURATION = 4000L
    }
}