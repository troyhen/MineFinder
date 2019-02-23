package com.troy.mine.example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.troy.mine.R
import kotlin.random.Random

open class MineFieldView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : InteractiveView(context, attrs, defStyle) {

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
    private val coverPaint = Paint()
    private val revealPaint = Paint()
    private val textPaint = Paint()

    private var cells = emptyList<Cell>()
    private var columns = 0
    private var rows = 0

    init {
        maxViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)
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
        reset(COLUMNS, ROWS, MINES)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Clips the next few drawing operations to the content area
        val clipRestoreCount = canvas.save()
        canvas.clipRect(contentRect)

        drawCircles(canvas)

        drawEdgeEffectsUnclipped(canvas)

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount)

        // Draws chart container
        canvas.drawRect(contentRect, mAxisPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minChartSize = resources.getDimensionPixelSize(R.dimen.min_chart_size)
        setMeasuredDimension(
            Math.max(suggestedMinimumWidth, View.resolveSize(minChartSize + paddingLeft + maxLabelWidth + labelSeparation + paddingRight, widthMeasureSpec)),
            Math.max(suggestedMinimumHeight, View.resolveSize(minChartSize + paddingTop + labelHeight + labelSeparation + paddingBottom, heightMeasureSpec))
        )
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

    fun reset(columns: Int, rows: Int, mines: Int) {
        this.columns = columns
        this.rows = rows
        val size = columns * rows
        cells = MutableList(size) { index ->
            val column = index % columns
            val row = index / columns
            Cell(column, row, isRevealed = Random.nextBoolean())
        }
        for (i in 1..mines) {
            val index = Random.nextInt(size)
            val cell = cells[index]
            if (cell.hasMine) continue
            cell.hasMine = true
        }
        for (i in 0 until size) {
            val column = i % columns
            val row = i / columns
            val shift = 1 - row % 2
            val cell = cells[i]
            cell.neighborMines = count(column - shift, row + 1) + count(column + 1 - shift, row + 1) +
                    count(column - 1, row) + count(column + 1, row) +
                    count(column - shift, row - 1) + count(column + 1 - shift, row - 1)
        }
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

        coverPaint.color = 0xFF10E010.toInt()
        revealPaint.color = Color.BLACK
        revealPaint.style = Paint.Style.STROKE
        textPaint.color = Color.BLUE
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun count(column: Int, row: Int): Int {
        if (column < 0 || column >= columns || row < 0 || row >= rows) return 0
        val index = column + row * columns
        val cell = cells[index]
        return if (cell.hasMine) 1 else 0
    }

    private fun drawCircles(canvas: Canvas) {
        val zoom = maxViewport.height() / currentViewport.height()
        val ratio = contentRect.height() / maxViewport.height()
        val textSize = TEXT_SIZE * zoom * ratio
        val textShift = textSize / 3
        val radius = CELL * zoom * .5f * ratio
        val xSpace = CELL * 1.09f
        val xSpace2 = xSpace / 2
        val ySpace = CELL * .95f
        textPaint.textSize = textSize
        revealPaint.strokeWidth = zoom * 2
        for (y in 0 until rows) {
            val yc = getDrawY(y * ySpace + SHIFT)
            val yt = yc + textShift
            val xoffset = SHIFT + if (y % 2 == 0) 0f else xSpace2
            for (x in 0 until columns) {
                val xc = getDrawX(x * xSpace + xoffset)
                val index = y * columns + x
                val cell = cells[index]
                if (!cell.isRevealed) {
                    canvas.drawCircle(xc, yc, radius, coverPaint)
                } else {
                    canvas.drawCircle(xc, yc, radius, revealPaint)
                    val text = when {
                        cell.isMarked -> "\uD83D\uDEA9"
                        cell.hasMine -> "💣"
                        cell.neighborMines == 0 -> ""
                        else -> cell.neighborMines.toString()
                    }
                    canvas.drawText(text, xc, yt, textPaint)
                }
            }
        }
    }

    companion object {
        // Viewport extremes. See currentViewport for a discussion of the viewport.
        private const val AXIS_X_MIN = 0f
        private const val AXIS_X_MAX = 160f
        private const val AXIS_Y_MIN = 0f
        private const val AXIS_Y_MAX = 300f

        private const val COLUMNS = 13
        private const val ROWS = 30
        private const val MINES = 30

        private const val CELL = (AXIS_Y_MAX - AXIS_Y_MIN) / ROWS
        private const val TEXT_SIZE = CELL * .8f
        private const val SHIFT = CELL
    }

    data class Cell(val column: Int, val row: Int, var hasMine: Boolean = false, var neighborMines: Int = 0, var isRevealed: Boolean = false, var isMarked: Boolean = false)
}
