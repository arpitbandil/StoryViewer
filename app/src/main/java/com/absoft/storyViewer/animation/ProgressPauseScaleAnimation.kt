package com.absoft.storyViewer.animation

import android.view.animation.ScaleAnimation
import android.view.animation.Transformation

class ProgressPauseScaleAnimation internal constructor(
    fromX: Float, toX: Float, fromY: Float,
    toY: Float, pivotXType: Int, pivotXValue: Float, pivotYType: Int,
    pivotYValue: Float
) : ScaleAnimation(
    fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType,
    pivotYValue
) {
    private var elapsedTime = 0L
    private var isPaused = false

    override fun getTransformation(
        currentTime: Long,
        outTransformation: Transformation,
        scale: Float
    ): Boolean {
        if (isPaused) {
            if (elapsedTime == 0L) {
                elapsedTime = currentTime - startTime
            }
            startTime = currentTime - elapsedTime
        }
        return super.getTransformation(currentTime, outTransformation, scale)
    }

    fun pause() {
        if (isPaused) return
        elapsedTime = 0
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }
}