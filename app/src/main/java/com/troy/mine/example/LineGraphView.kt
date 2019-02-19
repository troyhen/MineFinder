package com.troy.mine.example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.troy.mine.R

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
open class LineGraphView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : InteractiveView(context, attrs, defStyle) {

//    private val TAG = "LineGraphView"

    companion object {
        // Viewport extremes. See currentViewport for a discussion of the viewport.
        private val AXIS_X_MIN = -1f
        private val AXIS_X_MAX = 1f
        private val AXIS_Y_MIN = -1f
        private val AXIS_Y_MAX = 1f
    }

    /**
     * The number of individual points (samples) in the chart series to draw onscreen.
     */
    private val DRAW_STEPS = 30

    // Current attribute values and Paints.

    var labelSeparation = 0
    var labelTextSize = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }

    var labelTextColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    var maxLabelWidth = 1
    var labelHeight = 1
    var gridThickness = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    var gridColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    var axisThickness = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    var axisColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    var dataThickness = 0f
    var dataColor = 0

    private val mLabelTextPaint = Paint()
    private val mGridPaint = Paint()
    private val mAxisPaint = Paint()
    private val mDataPaint = Paint()

    // Buffers for storing current X and Y stops. See the computeAxisStops method for more details.
    private val mXStopsBuffer = AxisStops()
    private val mYStopsBuffer = AxisStops()

    // Buffers used during drawing. These are defined as fields to avoid allocation during
    // draw calls.
    private var mAxisXPositionsBuffer = floatArrayOf()
    private var mAxisYPositionsBuffer = floatArrayOf()
    private var mAxisXLinesBuffer = floatArrayOf()
    private var mAxisYLinesBuffer = floatArrayOf()
    private val mSeriesLinesBuffer = FloatArray((DRAW_STEPS + 1) * 4)
    private val mLabelBuffer = CharArray(100)

    /**
     * The simple math function Y = fun(X) to draw on the chart.
     * @param x The X value
     * @return The Y value
     */
    protected fun `fun`(x: Float): Float {
        return Math.pow(x.toDouble(), 3.0).toFloat() - x / 4
    }

    init {
        currentViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)
        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.LineGraphView, defStyle, defStyle
        )

        try {
            labelTextColor = a.getColor(R.styleable.LineGraphView_labelTextColor, labelTextColor)
            labelTextSize = a.getDimension(R.styleable.LineGraphView_labelTextSize, labelTextSize)
            labelSeparation = a.getDimensionPixelSize(R.styleable.LineGraphView_labelSeparation, labelSeparation)

            gridThickness = a.getDimension(R.styleable.LineGraphView_gridThickness, gridThickness)
            gridColor = a.getColor(R.styleable.LineGraphView_gridColor, gridColor)

            axisThickness = a.getDimension(R.styleable.LineGraphView_axisThickness, axisThickness)
            axisColor = a.getColor(R.styleable.LineGraphView_axisColor, axisColor)

            dataThickness = a.getDimension(R.styleable.LineGraphView_dataThickness, dataThickness)
            dataColor = a.getColor(R.styleable.LineGraphView_dataColor, dataColor)
        } finally {
            a.recycle()
        }

        initPaints()
    }

    /**
     * (Re)initializes [Paint] objects based on current attribute values.
     */
    private fun initPaints() {
        mLabelTextPaint.isAntiAlias = true
        mLabelTextPaint.textSize = labelTextSize
        mLabelTextPaint.color = labelTextColor
        labelHeight = Math.max(Math.abs(mLabelTextPaint.fontMetrics.top), 1f).toInt()
        maxLabelWidth = Math.max(mLabelTextPaint.measureText("0000"), 1f).toInt()

        mGridPaint.strokeWidth = gridThickness
        mGridPaint.color = gridColor
        mGridPaint.style = Paint.Style.STROKE

        mAxisPaint.strokeWidth = axisThickness
        mAxisPaint.color = axisColor
        mAxisPaint.style = Paint.Style.STROKE

        mDataPaint.strokeWidth = dataThickness
        mDataPaint.color = dataColor
        mDataPaint.style = Paint.Style.STROKE
        mDataPaint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        contentRect.set(
            paddingLeft + maxLabelWidth + labelSeparation,
            paddingTop,
            width - paddingRight,
            height - paddingBottom - labelHeight - labelSeparation
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minChartSize = resources.getDimensionPixelSize(R.dimen.min_chart_size)
        setMeasuredDimension(
            Math.max(
                suggestedMinimumWidth,
                View.resolveSize(
                    minChartSize + paddingLeft + maxLabelWidth
                            + labelSeparation + paddingRight,
                    widthMeasureSpec
                )
            ),
            Math.max(
                suggestedMinimumHeight,
                View.resolveSize(
                    minChartSize + paddingTop + labelHeight
                            + labelSeparation + paddingBottom,
                    heightMeasureSpec
                )
            )
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draws axes and text labels
        drawAxes(canvas)

        // Clips the next few drawing operations to the content area
        val clipRestoreCount = canvas.save()
        canvas.clipRect(contentRect)

        drawDataSeriesUnclipped(canvas)
        drawEdgeEffectsUnclipped(canvas)

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount)

        // Draws chart container
        canvas.drawRect(contentRect, mAxisPaint)
    }

    /**
     * Draws the chart axes and labels onto the canvas.
     */
    private fun drawAxes(canvas: Canvas) {
        // Computes axis stops (in terms of numerical value and position on screen)

        computeAxisStops(
            currentViewport.left,
            currentViewport.right,
            contentRect.width() / maxLabelWidth / 2,
            mXStopsBuffer
        )
        computeAxisStops(
            currentViewport.top,
            currentViewport.bottom,
            contentRect.height() / labelHeight / 2,
            mYStopsBuffer
        )

        // Avoid unnecessary allocations during drawing. Re-use allocated
        // arrays and only reallocate if the number of stops grows.
        if (mAxisXPositionsBuffer.size < mXStopsBuffer.numStops) {
            mAxisXPositionsBuffer = FloatArray(mXStopsBuffer.numStops)
        }
        if (mAxisYPositionsBuffer.size < mYStopsBuffer.numStops) {
            mAxisYPositionsBuffer = FloatArray(mYStopsBuffer.numStops)
        }
        if (mAxisXLinesBuffer.size < mXStopsBuffer.numStops * 4) {
            mAxisXLinesBuffer = FloatArray(mXStopsBuffer.numStops * 4)
        }
        if (mAxisYLinesBuffer.size < mYStopsBuffer.numStops * 4) {
            mAxisYLinesBuffer = FloatArray(mYStopsBuffer.numStops * 4)
        }

        // Compute positions
        var i = 0
        while (i < mXStopsBuffer.numStops) {
            mAxisXPositionsBuffer[i] = getDrawX(mXStopsBuffer.stops[i])
            i++
        }
        i = 0
        while (i < mYStopsBuffer.numStops) {
            mAxisYPositionsBuffer[i] = getDrawY(mYStopsBuffer.stops[i])
            i++
        }

        // Draws grid lines using drawLines (faster than individual drawLine calls)
        i = 0
        while (i < mXStopsBuffer.numStops) {
            mAxisXLinesBuffer[i * 4 + 0] = Math.floor(mAxisXPositionsBuffer[i].toDouble()).toFloat()
            mAxisXLinesBuffer[i * 4 + 1] = contentRect.top.toFloat()
            mAxisXLinesBuffer[i * 4 + 2] = Math.floor(mAxisXPositionsBuffer[i].toDouble()).toFloat()
            mAxisXLinesBuffer[i * 4 + 3] = contentRect.bottom.toFloat()
            i++
        }
        canvas.drawLines(mAxisXLinesBuffer, 0, mXStopsBuffer.numStops * 4, mGridPaint)

        i = 0
        while (i < mYStopsBuffer.numStops) {
            mAxisYLinesBuffer[i * 4 + 0] = contentRect.left.toFloat()
            mAxisYLinesBuffer[i * 4 + 1] = Math.floor(mAxisYPositionsBuffer[i].toDouble()).toFloat()
            mAxisYLinesBuffer[i * 4 + 2] = contentRect.right.toFloat()
            mAxisYLinesBuffer[i * 4 + 3] = Math.floor(mAxisYPositionsBuffer[i].toDouble()).toFloat()
            i++
        }
        canvas.drawLines(mAxisYLinesBuffer, 0, mYStopsBuffer.numStops * 4, mGridPaint)

        // Draws X labels
        var labelOffset: Int
        var labelLength: Int
        mLabelTextPaint.textAlign = Paint.Align.CENTER
        i = 0
        while (i < mXStopsBuffer.numStops) {
            // Do not use String.format in high-performance code such as onDraw code.
            labelLength = formatFloat(mLabelBuffer, mXStopsBuffer.stops[i], mXStopsBuffer.decimals)
            labelOffset = mLabelBuffer.size - labelLength
            canvas.drawText(
                mLabelBuffer, labelOffset, labelLength,
                mAxisXPositionsBuffer[i],
                (contentRect.bottom + labelHeight + labelSeparation).toFloat(),
                mLabelTextPaint
            )
            i++
        }

        // Draws Y labels
        mLabelTextPaint.textAlign = Paint.Align.RIGHT
        i = 0
        while (i < mYStopsBuffer.numStops) {
            // Do not use String.format in high-performance code such as onDraw code.
            labelLength = formatFloat(mLabelBuffer, mYStopsBuffer.stops[i], mYStopsBuffer.decimals)
            labelOffset = mLabelBuffer.size - labelLength
            canvas.drawText(
                mLabelBuffer, labelOffset, labelLength,
                (contentRect.left - labelSeparation).toFloat(),
                mAxisYPositionsBuffer[i] + labelHeight * .5f,
                mLabelTextPaint
            )
            i++
        }
    }

    /**
     * Rounds the given number to the given number of significant digits. Based on an answer on
     * [Stack Overflow](http://stackoverflow.com/questions/202302).
     */
    private fun roundToOneSignificantFigure(num: Double): Float {
        val d = Math.ceil(Math.log10(if (num < 0) -num else num).toFloat().toDouble()).toFloat()
        val power = 1 - d.toInt()
        val magnitude = Math.pow(10.0, power.toDouble()).toFloat()
        val shifted = Math.round(num * magnitude)
        return shifted / magnitude
    }

    private val POW10 = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000)

    /**
     * Formats a float value to the given number of decimals. Returns the length of the string.
     * The string begins at out.length - [return value].
     */
    private fun formatFloat(out: CharArray, value0: Float, digits0: Int): Int {
        var negative = false
        var value = value0
        var digits = digits0
        if (value == 0f) {
            out[out.size - 1] = '0'
            return 1
        }
        if (value < 0) {
            negative = true
            value = -value
        }
        if (digits > POW10.size) {
            digits = POW10.size - 1
        }
        value *= POW10[digits].toFloat()
        var lval = Math.round(value).toLong()
        var index = out.size - 1
        var charCount = 0
        while (lval != 0L || charCount < digits + 1) {
            val digit = (lval % 10).toInt()
            lval /= 10L
            out[index--] = (digit + '0'.toInt()).toChar()
            charCount++
            if (charCount == digits) {
                out[index--] = '.'
                charCount++
            }
        }
        if (negative) {
            out[index] = '-'
            charCount++
        }
        return charCount
    }

    /**
     * Computes the set of axis labels to show given start and stop boundaries and an ideal number
     * of stops between these boundaries.

     * @param start The minimum extreme (e.g. the left edge) for the axis.
     * *
     * @param stop The maximum extreme (e.g. the right edge) for the axis.
     * *
     * @param steps The ideal number of stops to create. This should be based on available screen
     * *              space; the more space there is, the more stops should be shown.
     * *
     * @param outStops The destination [AxisStops] object to populate.
     */
    private fun computeAxisStops(start: Float, stop: Float, steps: Int, outStops: AxisStops) {
        val range = (stop - start).toDouble()
        if (steps == 0 || range <= 0) {
            outStops.stops = floatArrayOf()
            outStops.numStops = 0
            return
        }

        val rawInterval = range / steps
        var interval = roundToOneSignificantFigure(rawInterval)
        val intervalMagnitude = Math.pow(10.0, Math.log10(interval.toDouble()).toInt().toDouble())
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            interval = Math.floor(10 * intervalMagnitude).toFloat()
        }

        val first = Math.ceil((start / interval).toDouble()) * interval
        val last = Math.nextUp(Math.floor((stop / interval).toDouble()) * interval)

        var f = first
        var n = 0
        while (f <= last) {
            ++n
            f += interval
        }

        outStops.numStops = n

        if (outStops.stops.size < n) {
            // Ensure stops contains at least numStops elements.
            outStops.stops = FloatArray(n)
        }

        f = first
        var i = 0
        while (i < n) {
            outStops.stops[i] = f.toFloat()
            f += interval
            ++i
        }

        if (interval < 1) {
            outStops.decimals = Math.ceil(-Math.log10(interval.toDouble())).toInt()
        } else {
            outStops.decimals = 0
        }
    }

    /**
     * Draws the currently visible portion of the data series defined by [.fun] to the
     * canvas. This method does not clip its drawing, so users should call [ before calling this method.][Canvas.clipRect]
     */
    private fun drawDataSeriesUnclipped(canvas: Canvas) {
        mSeriesLinesBuffer[0] = contentRect.left.toFloat()
        mSeriesLinesBuffer[1] = getDrawY(`fun`(currentViewport.left))
        mSeriesLinesBuffer[2] = mSeriesLinesBuffer[0]
        mSeriesLinesBuffer[3] = mSeriesLinesBuffer[1]
        var x: Float
        for (i in 1..DRAW_STEPS) {
            mSeriesLinesBuffer[i * 4 + 0] = mSeriesLinesBuffer[(i - 1) * 4 + 2]
            mSeriesLinesBuffer[i * 4 + 1] = mSeriesLinesBuffer[(i - 1) * 4 + 3]

            x = currentViewport.left + currentViewport.width() / DRAW_STEPS * i
            mSeriesLinesBuffer[i * 4 + 2] = getDrawX(x)
            mSeriesLinesBuffer[i * 4 + 3] = getDrawY(`fun`(x))
        }
        canvas.drawLines(mSeriesLinesBuffer, mDataPaint)
    }

    /**
     * A simple class representing axis label values.
     * @see .computeAxisStops
     */
    private class AxisStops {
        internal var stops = floatArrayOf()
        internal var numStops = 0
        internal var decimals = 0
    }

}
