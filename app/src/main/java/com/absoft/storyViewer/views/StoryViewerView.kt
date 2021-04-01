package com.absoft.storyViewer.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import com.absoft.storyViewer.R
import com.absoft.storyViewer.adapter.StoryPagerAdapter
import com.absoft.storyViewer.extensions.*
import com.absoft.storyViewer.extensions.addOnPageChangeListener
import com.absoft.storyViewer.extensions.applyMargin
import com.absoft.storyViewer.extensions.isRectVisible
import com.absoft.storyViewer.extensions.makeInvisible
import com.absoft.storyViewer.gestures.detector.SimpleOnGestureListener
import com.absoft.storyViewer.gestures.direction.SwipeDirection
import com.absoft.storyViewer.gestures.direction.SwipeDirectionDetector
import com.absoft.storyViewer.gestures.dismiss.SwipeToDismissHandler
import com.absoft.storyViewer.models.StoryDetails
import com.absoft.storyViewer.models.UserDetails
import kotlinx.android.synthetic.main.layout_story_viewer.view.*
import kotlin.math.abs

internal class StoryViewerView<T, E : StoryDetails> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) where T  : Parcelable, T : UserDetails<E> {


    internal var isZoomingAllowed = true
    internal var isSwipeToDismissAllowed = true

    internal var currentPosition: Int
        get() = imagesPager.currentItem
        set(value) {
            imagesPager.currentItem = value
        }

    internal var onDismiss: (() -> Unit)? = null
    internal var onPageChange: ((position: Int) -> Unit)? = null

//    internal val isScaled
//        get() = imagesAdapter?.isScaled(currentPosition) ?: false
    internal val isScaled
        get() = false

    internal var containerPadding = intArrayOf(0, 0, 0, 0)

    internal var imagesMargin
        get() = imagesPager.pageMargin
        set(value) {
            imagesPager.pageMargin = value
        }

    internal var overlayView: View? = null
        set(value) {
            field = value
            value?.let { rootContainer.addView(it) }
        }

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private val transitionImageView: ImageView
    private var externalTransitionImageView: ImageView? = null

    private var imagesPager: AbsViewPager
    private var imagesAdapter: StoryPagerAdapter<T, E>? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null

    private var images: List<T> = listOf()
    private var imageLoader: ImageLoader<T>? = null
    private lateinit var transitionImageAnimator: TransitionImageAnimator

    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
                || !externalTransitionImageView.isRectVisible
                || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    init {
        View.inflate(context, R.layout.layout_story_viewer, this)

        rootContainer = findViewById(R.id.root)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionImageView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.viewPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = {
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) makeInvisible() else show()
                }
                onPageChange?.invoke(it)
            })

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true) {
            return true
        }

        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1) {
            return true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            return imagesPager.dispatchTouchEvent(event)
        }

        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    internal fun setImages(images: List<T>, startPosition: Int, imageLoader: ImageLoader<T>) {
        this.images = images
        this.imageLoader = imageLoader
        this.imagesAdapter = ImagesPagerAdapter(context, images, imageLoader, isZoomingAllowed)
        this.imagesPager.adapter = imagesAdapter
        this.startPosition = startPosition
    }

    internal fun open(transitionImageView: ImageView?, animate: Boolean) {
        prepareViewsForTransition()

        externalTransitionImageView = transitionImageView

        imageLoader?.loadImage(this.transitionImageView, images[startPosition])
        this.transitionImageView.copyBitmapFrom(transitionImageView)

        transitionImageAnimator = createTransitionImageAnimator(transitionImageView)
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)

        if (animate) animateOpen() else prepareViewsForViewer()
    }

    internal fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
        } else {
            animateClose()
        }
    }

    internal fun updateImages(images: List<T>) {
        this.images = images
        imagesAdapter?.updateImages(images)
    }

    internal fun updateTransitionImage(imageView: ImageView?) {
        externalTransitionImageView?.makeVisible()
        imageView?.makeInvisible()

        externalTransitionImageView = imageView
        startPosition = currentPosition
        transitionImageAnimator = createTransitionImageAnimator(imageView)
        imageLoader?.loadImage(transitionImageView, images[startPosition])
    }

    internal fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            containerPadding = containerPadding,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView?.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = { prepareViewsForViewer() })
    }

    private fun animateClose() {
        prepareViewsForTransition()
        dismissContainer.applyMargin(0, 0, 0, 0)

        transitionImageAnimator.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
            },
            onTransitionEnd = { onDismiss?.invoke() })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.makeVisible()
        imagesPager.makeGone()
    }

    private fun prepareViewsForViewer() {
        backgroundView.alpha = 1f
        transitionImageContainer.makeGone()
        imagesPager.makeVisible()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            SwipeDirection.UP, SwipeDirection.DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }
            SwipeDirection.LEFT, SwipeDirection.RIGHT -> {
                imagesPager.dispatchTouchEvent(event)
            }
            else -> true
        }
    }

    private fun handleUpDownEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    private fun handleEventActionDown(event: MotionEvent) {
        swipeDirection = null
        wasScaled = false
        imagesPager.dispatchTouchEvent(event)

        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSingleTap(event: MotionEvent, isOverlayWasClicked: Boolean) {
        if (overlayView != null && !isOverlayWasClicked) {
            overlayView?.switchVisibilityWithAnimation()
            super.dispatchTouchEvent(event)
        }
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView?.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            ?.let { it.isVisible && it.dispatchTouchEvent(event) }
            ?: false

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onSingleTap = {
                if (imagesPager.isIdle) {
                    handleSingleTap(it, isOverlayWasClicked)
                }
                false
            },
            onDoubleTap = {
                wasDoubleTapped = !isScaled
                false
            }
        ))

    private fun createScaleGestureDetector() =
        ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())

    private fun createSwipeToDismissHandler()
            : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = dismissContainer,
        shouldAnimateDismiss = { shouldDismissToBottom },
        onDismiss = { animateClose() },
        onSwipeViewMove = ::handleSwipeViewMove)

    private fun createTransitionImageAnimator(transitionImageView: ImageView?) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = this.transitionImageView,
            internalImageContainer = this.transitionImageContainer)

}
