package com.troy.mine.example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.EdgeEffect
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.EdgeEffectCompat
import com.troy.mine.R
import java.lang.Math.max
import java.lang.Math.min
import java.lang.Math.nextUp

/**
 * A view representing a simple yet interactive line chart for the function <code>x^3 - x/4</code>.
 * <p>
 * This view isn't all that useful on its own; rather it serves as an example of how to correctly
 * implement these types of gestures to perform zooming and scrolling with interesting content
 * types.
 * <p>
 * The view is interactive in that it can be zoomed and panned using
 * typical <a href="http://developer.android.com/design/patterns/gestures.html">gestures</a> such
 * as double-touch, drag, pinch-open, and pinch-close. This is done using the
 * {@link ScaleGestureDetector}, {@link GestureDetector}, and {@link OverScroller} classes. Note
 * that the platform-provided view scrolling behavior (e.g. {@link View#scrollBy(int, int)} is NOT
 * used.
 * <p>
 * The view also demonstrates the correct use of
 * <a href="http://developer.android.com/design/style/touch-feedback.html">touch feedback</a> to
 * indicate to users that they've reached the content edges after a pan or fling gesture. This
 * is done using the {@link EdgeEffectCompat} class.
 * <p>
 * Finally, this class demonstrates the basics of creating a custom view, including support for
 * custom attributes (see the constructors), a simple implementation for
 * {@link #onMeasure(int, int)}, an implementation for {@link #onSaveInstanceState()} and a fairly
 * straightforward {@link Canvas}-based rendering implementation in
 * {@link #onDraw(android.graphics.Canvas)}.
 * <p>
 * Note that this view doesn't automatically support directional navigation or other accessibility
 * methods. Activities using this view should generally provide alternate navigation controls.
 * Activities using this view should also present an alternate, text-based representation of this
 * view's content for vision-impaired users.
 */
open class InteractiveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

//    private val TAG = "InteractiveView"

    /**
     * Initial fling velocity for pan operations, in screen widths (or heights) per second.
     * @see .panLeft
     * @see .panRight
     * @see .panUp
     * @see .panDown
     */
    protected val PAN_VELOCITY_FACTOR = 2f

    /**
     * The scaling factor for a single zoom 'step'.
     * @see .zoomIn
     * @see .zoomOut
     */
    protected val ZOOM_AMOUNT = 0.25f

    protected var constrainAspects = true

    /**
     * Minimum area which can be zoomed in to.
     * @see .currentViewport
     */
    protected var minViewportWidth = .001f
    protected var minViewportHeight = .001f

    /**
     * Maximum area which can be zoomed out or scrolled to.
     * @see .currentViewport
     */
    protected var maxViewport: RectF = RectF(-1f, -1f, 1f, 1f)
        set(value) {
            field = value
            constrainViewport()
            ViewCompat.postInvalidateOnAnimation(this)
        }

    /**
     * The current viewport. This rectangle represents the currently visible chart domain
     * and range. The currently visible chart X values are from this rectangle's left to its right.
     * The currently visible chart Y values are from this rectangle's top to its bottom.
     *
     * Note that this rectangle's top is actually the smaller Y value, and its bottom is the larger
     * Y value. Since the chart is drawn onscreen in such a way that chart Y values increase
     * towards the top of the screen (decreasing pixel Y positions), this rectangle's "top" is drawn
     * above this rectangle's "bottom" value.
     * @see .contentRect
     * @see .maxViewport
     */
    protected var currentViewport: RectF = maxViewport
        set(value) {
            field = value
            constrainViewport()
            ViewCompat.postInvalidateOnAnimation(this)
        }

    /**
     * The current destination rectangle (in pixel coordinates) into which the chart data should
     * be drawn. Area outside of this rectangle outside does not zoom.
     * @see .currentViewport
     */
    protected val contentRect = Rect()

    // State objects and values related to gesture tracking.
    private val mScroller: OverScroller = OverScroller(context)
    private val mZoomer: Zoomer = Zoomer(context)
    private val mZoomFocalPoint = PointF()
    private val mScrollerStartViewport = RectF() // Used only for zooms and flings.

    // Edge effect / over-scroll tracking objects.
    private val mEdgeEffectTop = EdgeEffect(context)
    private val mEdgeEffectBottom = EdgeEffect(context)
    private val mEdgeEffectLeft = EdgeEffect(context)
    private val mEdgeEffectRight = EdgeEffect(context)

    private var mEdgeEffectTopActive = false
    private var mEdgeEffectBottomActive = false
    private var mEdgeEffectLeftActive = false
    private var mEdgeEffectRightActive = false

    // Used during scrolling
    private val mSurfaceSizeBuffer = Point()

    private val mScaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /**
         * This is the active focal point in terms of the viewport. Could be a local
         * variable but kept here to minimize per-frame allocations.
         */
        private val viewportFocus = PointF()
        private var spanX = 0f
        private var spanY = 0f
        private var lastSpanX = 0f
        private var lastSpanY = 0f

        private fun currentScale(scaleGestureDetector: ScaleGestureDetector) {
            spanX = scaleGestureDetector.currentSpanX
            spanY = scaleGestureDetector.currentSpanY
            if (constrainAspects) {
                spanX = max(spanX, spanY)
                spanY = spanX
            }
        }

        override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
            currentScale(scaleGestureDetector)
            lastSpanX = spanX
            lastSpanY = spanY
            return true
        }

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            currentScale(scaleGestureDetector)

            val newWidth = lastSpanX / spanX * currentViewport.width()
            val newHeight = lastSpanY / spanY * currentViewport.height()

            val focusX = scaleGestureDetector.focusX
            val focusY = scaleGestureDetector.focusY
            hitTest(focusX, focusY, viewportFocus)

            currentViewport.left = viewportFocus.x - newWidth * (focusX - contentRect.left) / contentRect.width()
            currentViewport.top = viewportFocus.y - newHeight * (contentRect.bottom - focusY) / contentRect.height()
            currentViewport.right = currentViewport.left + newWidth
            currentViewport.bottom = currentViewport.top + newHeight
            constrainViewport()
            ViewCompat.postInvalidateOnAnimation(this@InteractiveView)

            lastSpanX = spanX
            lastSpanY = spanY
            return true
        }
    })

    private val mGestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            releaseEdgeEffects()
            mScrollerStartViewport.set(currentViewport)
            mScroller.forceFinished(true)
            ViewCompat.postInvalidateOnAnimation(this@InteractiveView)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            mZoomer.forceFinished(true)
            if (hitTest(e.x, e.y, mZoomFocalPoint)) {
                mZoomer.startZoom(ZOOM_AMOUNT)
            }
            ViewCompat.postInvalidateOnAnimation(this@InteractiveView)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            /**
             * Pixel offset is the offset in screen pixels, while viewport offset is the
             * offset within the current viewport. For additional information on surface sizes
             * and pixel offsets, see the docs for {@link computeScrollSurfaceSize()}. For
             * additional information about the viewport, see the comments for
             * {@link mCurrentViewport}.
             */
            val viewportOffsetX = distanceX * currentViewport.width() / contentRect.width()
            val viewportOffsetY = -distanceY * currentViewport.height() / contentRect.height()
            computeScrollSurfaceSize(mSurfaceSizeBuffer)
            val scrolledX = (mSurfaceSizeBuffer.x
                    * (currentViewport.left + viewportOffsetX - maxViewport.left)
                    / maxViewport.width()).toInt()
            val scrolledY = (mSurfaceSizeBuffer.y
                    * (maxViewport.top - currentViewport.bottom - viewportOffsetY)
                    / maxViewport.height()).toInt()
            val canScrollX = currentViewport.left > maxViewport.left
                    || currentViewport.right < maxViewport.right
            val canScrollY = currentViewport.top > maxViewport.top
                    || currentViewport.bottom < maxViewport.bottom
            setViewportBottomLeft(
                currentViewport.left + viewportOffsetX,
                currentViewport.bottom + viewportOffsetY
            )

            if (canScrollX && scrolledX < 0) {
                mEdgeEffectLeft.onPull(scrolledX / contentRect.width().toFloat())
                mEdgeEffectLeftActive = true
            }
            if (canScrollY && scrolledY < 0) {
                mEdgeEffectTop.onPull(scrolledY / contentRect.height().toFloat())
                mEdgeEffectTopActive = true
            }
            if (canScrollX && scrolledX > mSurfaceSizeBuffer.x - contentRect.width()) {
                mEdgeEffectRight.onPull(
                    (scrolledX - mSurfaceSizeBuffer.x + contentRect.width())
                            / contentRect.width().toFloat()
                )
                mEdgeEffectRightActive = true
            }
            if (canScrollY && scrolledY > mSurfaceSizeBuffer.y - contentRect.height()) {
                mEdgeEffectBottom.onPull(
                    (scrolledY - mSurfaceSizeBuffer.y + contentRect.height())
                            / contentRect.height().toFloat()
                )
                mEdgeEffectBottomActive = true
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) = onLongClick(e)

        override fun onSingleTapConfirmed(e: MotionEvent) = onClick(e)

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            fling(-velocityX.toInt(), -velocityY.toInt())
            return true
        }
    })

    open fun onClick(e: MotionEvent): Boolean = false

    open fun onLongClick(e: MotionEvent) {
    }

    /**
     * Ensures that current viewport is inside the viewport extremes defined by [.maxViewport.left],
     * [.maxViewport.right], [.maxViewport.top] and [.maxViewport.bottom].
     */
    private fun constrainViewport() {
        if (currentViewport.width() < minViewportWidth) {
            val mid = (currentViewport.left + currentViewport.right) * .5f
            val dist = minViewportWidth * .5f
            currentViewport.left = mid - dist
            currentViewport.right = mid + dist
        }
        if (currentViewport.height() < minViewportHeight) {
            val mid = (currentViewport.top + currentViewport.bottom) * .5f
            val dist = minViewportHeight * .5f
            currentViewport.top = mid - dist
            currentViewport.bottom = mid + dist
        }
        currentViewport.left = max(maxViewport.left, currentViewport.left)
        currentViewport.top = max(maxViewport.top, currentViewport.top)
        currentViewport.bottom = max(nextUp(currentViewport.top), min(maxViewport.bottom, currentViewport.bottom))
        currentViewport.right = max(nextUp(currentViewport.left), min(maxViewport.right, currentViewport.right))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        contentRect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minChartSize = resources.getDimensionPixelSize(R.dimen.min_chart_size)
        setMeasuredDimension(
            max(
                suggestedMinimumWidth,
                resolveSize(
                    minChartSize + paddingLeft + paddingRight,
                    widthMeasureSpec
                )
            ),
            max(
                suggestedMinimumHeight,
                resolveSize(
                    minChartSize + paddingTop + paddingBottom,
                    heightMeasureSpec
                )
            )
        )
    }

    /**
     * Computes the pixel offset for the given X chart value. This may be outside the view bounds.
     */
    protected fun getDrawX(x: Float) = contentRect.left + contentRect.width() * (x - currentViewport.left) / currentViewport.width()

    /**
     * Computes the pixel offset for the given Y chart value. This may be outside the view bounds.
     */
    protected fun getDrawY(y: Float) = contentRect.bottom - contentRect.height() * (y - currentViewport.top) / currentViewport.height()

    /**
     * Draws the overscroll "glow" at the four edges of the chart region, if necessary. The edges
     * of the chart region are stored in [.mContentRect].
     * @see EdgeEffectCompat
     */
    protected fun drawEdgeEffectsUnclipped(canvas: Canvas) {
        // The methods below rotate and translate the canvas as needed before drawing the glow,
        // since EdgeEffectCompat always draws a top-glow at 0,0.

        var needsInvalidate = false

        if (!mEdgeEffectTop.isFinished) {
            val restoreCount = canvas.save()
            canvas.translate(contentRect.left.toFloat(), contentRect.top.toFloat())
            mEdgeEffectTop.setSize(contentRect.width(), contentRect.height())
            if (mEdgeEffectTop.draw(canvas)) {
                needsInvalidate = true
            }
            canvas.restoreToCount(restoreCount)
        }

        if (!mEdgeEffectBottom.isFinished) {
            val restoreCount = canvas.save()
            canvas.translate(2f * contentRect.left - contentRect.right, contentRect.bottom.toFloat())
            canvas.rotate(180f, contentRect.width().toFloat(), 0f)
            mEdgeEffectBottom.setSize(contentRect.width(), contentRect.height())
            if (mEdgeEffectBottom.draw(canvas)) {
                needsInvalidate = true
            }
            canvas.restoreToCount(restoreCount)
        }

        if (!mEdgeEffectLeft.isFinished) {
            val restoreCount = canvas.save()
            canvas.translate(contentRect.left.toFloat(), contentRect.bottom.toFloat())
            canvas.rotate(-90f, 0f, 0f)
            mEdgeEffectLeft.setSize(contentRect.height(), contentRect.width())
            if (mEdgeEffectLeft.draw(canvas)) {
                needsInvalidate = true
            }
            canvas.restoreToCount(restoreCount)
        }

        if (!mEdgeEffectRight.isFinished) {
            val restoreCount = canvas.save()
            canvas.translate(contentRect.right.toFloat(), contentRect.top.toFloat())
            canvas.rotate(90f, 0f, 0f)
            mEdgeEffectRight.setSize(contentRect.height(), contentRect.width())
            if (mEdgeEffectRight.draw(canvas)) {
                needsInvalidate = true
            }
            canvas.restoreToCount(restoreCount)
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finds the chart point (i.e. within the chart's domain and range) represented by the
     * given pixel coordinates, if that pixel is within the chart region described by
     * {@link #mContentRect}. If the point is found, the "dest" argument is set to the point and
     * this function returns true. Otherwise, this function returns false and "dest" is unchanged.
     */
    private fun hitTest(x: Float, y: Float, dest: PointF): Boolean {
        if (!contentRect.contains(x.toInt(), y.toInt())) {
            return false
        }

        dest.set(
            currentViewport.left
                    + currentViewport.width()
                    * (x - contentRect.left) / contentRect.width(),
            currentViewport.top
                    + currentViewport.height()
                    * (y - contentRect.bottom) / -contentRect.height()
        )
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var retVal = mScaleGestureDetector.onTouchEvent(event)
        retVal = mGestureDetector.onTouchEvent(event) || retVal
        return retVal || super.onTouchEvent(event)
    }

    private fun releaseEdgeEffects() {
        mEdgeEffectLeftActive = false
        mEdgeEffectTopActive = false
        mEdgeEffectRightActive = false
        mEdgeEffectBottomActive = false
        mEdgeEffectLeft.onRelease()
        mEdgeEffectTop.onRelease()
        mEdgeEffectRight.onRelease()
        mEdgeEffectBottom.onRelease()
    }

    private fun fling(velocityX: Int, velocityY: Int) {
        releaseEdgeEffects()
        // Flings use math in pixels (as opposed to math based on the viewport).
        computeScrollSurfaceSize(mSurfaceSizeBuffer)
        mScrollerStartViewport.set(currentViewport)
        val startX = (mSurfaceSizeBuffer.x * (mScrollerStartViewport.left - maxViewport.left) / (
                maxViewport.right - maxViewport.left)).toInt()
        val startY = (mSurfaceSizeBuffer.y * (maxViewport.bottom - mScrollerStartViewport.bottom) / (
                maxViewport.bottom - maxViewport.top)).toInt()
        mScroller.forceFinished(true)
        mScroller.fling(
            startX,
            startY,
            velocityX,
            velocityY,
            0, mSurfaceSizeBuffer.x - contentRect.width(),
            0, mSurfaceSizeBuffer.y - contentRect.height(),
            contentRect.width() / 2,
            contentRect.height() / 2
        )
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Computes the current scrollable surface size, in pixels. For example, if the entire chart
     * area is visible, this is simply the current size of {@link #mContentRect}. If the chart
     * is zoomed in 200% in both directions, the returned size will be twice as large horizontally
     * and vertically.
     */
    private fun computeScrollSurfaceSize(out: Point) {
        out.set(
            (contentRect.width() * maxViewport.width() / currentViewport.width()).toInt(),
            (contentRect.height() * maxViewport.height() / currentViewport.height()).toInt()
        )
    }

    override fun computeScroll() {
        super.computeScroll()

        var needsInvalidate = false

        if (mScroller.computeScrollOffset()) {
            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            computeScrollSurfaceSize(mSurfaceSizeBuffer)
            val currX = mScroller.currX
            val currY = mScroller.currY

            val canScrollX = (currentViewport.left > maxViewport.left
                    || currentViewport.right < maxViewport.right)
            val canScrollY = (currentViewport.top > maxViewport.top
                    || currentViewport.bottom < maxViewport.bottom)

            if (canScrollX
                && currX < 0
                && mEdgeEffectLeft.isFinished
                && !mEdgeEffectLeftActive
            ) {
                mEdgeEffectLeft.onAbsorb(mScroller.currVelocity.toInt())
                mEdgeEffectLeftActive = true
                needsInvalidate = true
            } else if (canScrollX
                && currX > (mSurfaceSizeBuffer.x - contentRect.width())
                && mEdgeEffectRight.isFinished
                && !mEdgeEffectRightActive
            ) {
                mEdgeEffectRight.onAbsorb(mScroller.currVelocity.toInt())
                mEdgeEffectRightActive = true
                needsInvalidate = true
            }

            if (canScrollY
                && currY < 0
                && mEdgeEffectTop.isFinished
                && !mEdgeEffectTopActive
            ) {
                mEdgeEffectTop.onAbsorb(mScroller.currVelocity.toInt())
                mEdgeEffectTopActive = true
                needsInvalidate = true
            } else if (canScrollY
                && currY > (mSurfaceSizeBuffer.y - contentRect.height())
                && mEdgeEffectBottom.isFinished
                && !mEdgeEffectBottomActive
            ) {
                mEdgeEffectBottom.onAbsorb(mScroller.currVelocity.toInt())
                mEdgeEffectBottomActive = true
                needsInvalidate = true
            }

            val currXRange = maxViewport.left + (maxViewport.right - maxViewport.left) * currX / mSurfaceSizeBuffer.x
            val currYRange = maxViewport.bottom - (maxViewport.bottom - maxViewport.top) * currY / mSurfaceSizeBuffer.y
            setViewportBottomLeft(currXRange, currYRange)
        }

        if (mZoomer.computeZoom()) {
            // Performs the zoom since a zoom is in progress (either programmatically or via
            // double-touch).
            val newWidth = (1f - mZoomer.currZoom) * mScrollerStartViewport.width()
            val newHeight = (1f - mZoomer.currZoom) * mScrollerStartViewport.height()
            val pointWithinViewportX = (mZoomFocalPoint.x - mScrollerStartViewport.left) / mScrollerStartViewport.width()
            val pointWithinViewportY = (mZoomFocalPoint.y - mScrollerStartViewport.top) / mScrollerStartViewport.height()
            currentViewport.set(
                mZoomFocalPoint.x - newWidth * pointWithinViewportX,
                mZoomFocalPoint.y - newHeight * pointWithinViewportY,
                mZoomFocalPoint.x + newWidth * (1 - pointWithinViewportX),
                mZoomFocalPoint.y + newHeight * (1 - pointWithinViewportY)
            )
            constrainViewport()
            needsInvalidate = true
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * Sets the current viewport (defined by [.mCurrentViewport]) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position, and thus
     * the bottom of the [.mCurrentViewport] rectangle. For more details on why top and
     * bottom are flipped, see [.mCurrentViewport].
     */
    private fun setViewportBottomLeft(x0: Float, y0: Float) {
        var x = x0
        var y = y0

        /**
         * Constrains within the scroll range. The scroll range is simply the viewport extremes
         * (maxViewport.right, etc.) minus the viewport size. For example, if the extrema were 0 and 10,
         * and the viewport size was 2, the scroll range would be 0 to 8.
         */
        val curWidth = currentViewport.width()
        val curHeight = currentViewport.height()
        x = max(maxViewport.left, min(x, maxViewport.right - curWidth))
        y = max(maxViewport.top + curHeight, min(y, maxViewport.bottom))

        currentViewport.set(x, y - curHeight, x + curWidth, y)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods for programmatically changing the viewport
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Smoothly zooms the chart in one step.
     */
    fun zoomIn() {
        mScrollerStartViewport.set(currentViewport)
        mZoomer.forceFinished(true)
        mZoomer.startZoom(ZOOM_AMOUNT)
        mZoomFocalPoint.set(
            (currentViewport.right + currentViewport.left) / 2,
            (currentViewport.bottom + currentViewport.top) / 2
        )
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Smoothly zooms the chart out one step.
     */
    fun zoomOut() {
        mScrollerStartViewport.set(currentViewport)
        mZoomer.forceFinished(true)
        mZoomer.startZoom(-ZOOM_AMOUNT)
        mZoomFocalPoint.set(
            (currentViewport.right + currentViewport.left) / 2,
            (currentViewport.bottom + currentViewport.top) / 2
        )
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Smoothly pans the chart left one step.
     */
    fun panLeft() {
        fling((-PAN_VELOCITY_FACTOR * width).toInt(), 0)
    }

    /**
     * Smoothly pans the chart right one step.
     */
    fun panRight() {
        fling((PAN_VELOCITY_FACTOR * width).toInt(), 0)
    }

    /**
     * Smoothly pans the chart up one step.
     */
    fun panUp() {
        fling(0, (-PAN_VELOCITY_FACTOR * height).toInt())
    }

    /**
     * Smoothly pans the chart down one step.
     */
    fun panDown() {
        fling(0, (PAN_VELOCITY_FACTOR * height).toInt())
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and classes related to view state persistence.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()!!
        val ss = SavedState(superState)
        ss.viewport = currentViewport
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        currentViewport = state.viewport
    }

    /**
     * Persistent state that is saved by InteractiveLineGraphView.
     */
    class SavedState : View.BaseSavedState {
        var viewport: RectF

        constructor(state: Parcel) : super(state) {
            viewport = RectF(state.readFloat(), state.readFloat(), state.readFloat(), state.readFloat())
        }

        constructor(state: Parcelable) : super(state) {
            viewport = RectF()  // do we really need this constructor?
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(viewport.left)
            out.writeFloat(viewport.top)
            out.writeFloat(viewport.right)
            out.writeFloat(viewport.bottom)
        }

        override fun toString(): String {
            return "InteractiveView.SavedState{${Integer.toHexString(System.identityHashCode(this))} viewport=$viewport}"
        }

        companion object {

            @JvmField
            @Suppress("unused")
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
