package com.absoft.storyViewer.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup

fun View.show(){
    visibility = View.VISIBLE
}

fun View.hide(){
    visibility = View.GONE
}


internal val View?.localVisibleRect: Rect
    get() = Rect().also { this?.getLocalVisibleRect(it) }

internal val View?.globalVisibleRect: Rect
    get() = Rect().also { this?.getGlobalVisibleRect(it) }

internal val View?.hitRect: Rect
    get() = Rect().also { this?.getHitRect(it) }

internal val View?.isRectVisible: Boolean
    get() = this != null && globalVisibleRect != localVisibleRect

internal val View?.isVisible: Boolean
    get() = this != null && visibility == View.VISIBLE

internal fun View.makeVisible() {
    visibility = View.VISIBLE
}

internal fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

internal fun View.makeGone() {
    visibility = View.GONE
}

internal inline fun <T : View> T.postApply(crossinline block: T.() -> Unit) {
    post { apply(block) }
}

internal inline fun <T : View> T.postDelayed(delayMillis: Long, crossinline block: T.() -> Unit) {
    postDelayed({ block() }, delayMillis)
}

internal fun View.applyMargin(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = start ?: marginStart
            topMargin = top ?: topMargin
            marginEnd = end ?: marginEnd
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

internal fun View.requestNewSize(
    width: Int, height: Int) {
    layoutParams.width = width
    layoutParams.height = height
    layoutParams = layoutParams
}

internal fun View.makeViewMatchParent() {
    applyMargin(0, 0, 0, 0)
    requestNewSize(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
}

internal fun View.animateAlpha(from: Float?, to: Float?, duration: Long) {
    alpha = from ?: 0f
    clearAnimation()
    animate()
        .alpha(to ?: 0f)
        .setDuration(duration)
        .start()
}

internal fun View.switchVisibilityWithAnimation() {
    val isVisible = visibility == View.VISIBLE
    val from = if (isVisible) 1.0f else 0.0f
    val to = if (isVisible) 0.0f else 1.0f

    ObjectAnimator.ofFloat(this, "alpha", from, to).apply {
        duration = ViewConfiguration.getDoubleTapTimeout().toLong()

        if (isVisible) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    makeGone()
                }
            })
        } else {
            makeVisible()
        }

        start()
    }
}

