package com.troy.mine.game

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.troy.mine.R
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.core.KoinComponent
import org.koin.core.get
import kotlin.math.roundToInt

@TargetApi(Build.VERSION_CODES.O)
open class MineFieldView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : InteractiveView(context, attrs, defStyle), KoinComponent {

    private var labelSeparation = 0
    private var labelTextSize = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }

    private var labelTextColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    private var maxLabelWidth = 1
    private var labelHeight = 1
    private var gridThickness = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    private var gridColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    private var axisThickness = 0f
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    private var axisColor = 0
        set(value) {
            field = value
            initPaints()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    private var dataThickness = 0f
    private var dataColor = 0

    private val mLabelTextPaint = Paint()
    private val mGridPaint = Paint()
    private val mAxisPaint = Paint()
    private val mDataPaint = Paint()
    private val coveredPaint = Paint()
    private val revealedPaint = Paint()
    private val normalTextPaint = Paint()
    private val zoomedTextPaint = Paint()
    private val markPaint = Paint()
    private val revealPaint = Paint()
    private val windowPaint = Paint()

    private val window = RectF()

    private val vibrationEffect: VibrationEffect? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) VibrationEffect.createOneShot(
            250,
            DEFAULT_AMPLITUDE
        ) else null
    }
    private val vibrator: Vibrator? by lazy { context.getSystemService(Vibrator::class.java) }

    private val viewModel: GameViewModel by viewModel(get())

    init {
        maxViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)
        currentViewport = RectF(
            maxViewport.centerX() - maxViewport.width() / 4,
            maxViewport.centerY() - maxViewport.height() / 4,
            maxViewport.centerX() + maxViewport.width() / 4,
            maxViewport.centerY() + maxViewport.height() / 4
        )

        initPaints()
        viewModel.reset(COLUMNS, ROWS, MINES_MED)
    }

    override fun onClick(e: MotionEvent): Boolean {
        return if (viewModel.state == GameState.PLAY) {
            when {
                window.contains(e.x, e.y) -> {
                    viewModel.toggleMode()
                    true
                }
                contentRect.contains(e.x.roundToInt(), e.y.roundToInt()) -> {
                    val cell = viewModel.findNearest(e.x, e.y)
                    when (viewModel.mode) {
                        ClickMode.MARK -> viewModel.markCell(cell)
                        ClickMode.REVEAL -> viewModel.revealCell(cell)
                    }
                }
                else -> super.onClick(e)
            }
        } else false
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
        drawWindow(canvas)
    }

    override fun onLongClick(e: MotionEvent) {
        if (viewModel.state == GameState.PLAY) {
            if (contentRect.contains(e.x.roundToInt(), e.y.roundToInt())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(vibrationEffect!!)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(250L)
                }
                val cell = viewModel.findNearest(e.x, e.y)
                viewModel.markCell(cell)
            } else {
                super.onLongClick(e)
            }
        }
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

    /**
     * (Re)initializes [Paint] objects based on current attribute values.
     */
    private fun initPaints() {
        mLabelTextPaint.isAntiAlias = true
        mLabelTextPaint.textSize = labelTextSize
        mLabelTextPaint.color = labelTextColor
        labelHeight = Math.max(Math.abs(mLabelTextPaint.fontMetrics.top), 1f).toInt()
        maxLabelWidth = Math.max(mLabelTextPaint.measureText("0000"), 1f).toInt()

        window.set(30f, 30f, 130f, 120f)

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

        coveredPaint.color = 0xFF10E010.toInt()
        revealedPaint.color = Color.BLACK
        revealedPaint.style = Paint.Style.STROKE
        zoomedTextPaint.color = Color.BLUE
        zoomedTextPaint.textAlign = Paint.Align.CENTER
        normalTextPaint.color = Color.BLUE
        normalTextPaint.textAlign = Paint.Align.CENTER
        normalTextPaint.textSize = 18 * context.resources.displayMetrics.density

        revealPaint.color = Color.WHITE
        revealPaint.style = Paint.Style.FILL
        markPaint.color = Color.YELLOW
        markPaint.style = Paint.Style.FILL
        windowPaint.color = Color.BLACK
        windowPaint.style = Paint.Style.STROKE
        windowPaint.strokeWidth = 4f
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
        zoomedTextPaint.textSize = textSize
        revealPaint.strokeWidth = zoom * 2
        for (y in 0 until viewModel.rows) {
            val yc = getDrawY(y * ySpace + SHIFT)
            val yt = yc + textShift
            val xoffset = SHIFT + if (y % 2 == 0) 0f else xSpace2
            for (x in 0 until viewModel.columns) {
                val xc = getDrawX(x * xSpace + xoffset)
                val index = y * viewModel.columns + x
                val cell = viewModel.cells[index]
                cell.x = xc
                cell.y = yc
                if (viewModel.state != GameState.WON && !cell.isRevealed) {
                    canvas.drawCircle(xc, yc, radius, coveredPaint)
                } else {
                    canvas.drawCircle(xc, yc, radius, revealPaint)
                }
                val text = when {
                    cell.isMarked -> "\uD83D\uDEA9"
                    viewModel.state == GameState.LOST && cell.hasMine -> "💣"
                    !cell.isRevealed -> ""
                    cell.hasMine -> "💣"
                    cell.neighborMines == 0 -> ""
                    else -> cell.neighborMines.toString()
                }
                canvas.drawText(text, xc, yt, zoomedTextPaint)
            }
        }
    }

    private fun drawWindow(canvas: Canvas) {
        canvas.drawRoundRect(
            window,
            20f,
            20f,
            if (viewModel.mode == ClickMode.MARK) markPaint else revealPaint
        )
        canvas.drawRoundRect(window, 20f, 20f, windowPaint)
        val x = window.centerX()
        val y = window.top + window.height() * .8f
        canvas.drawText("\uD83D\uDEA9", x, y, normalTextPaint)
    }

    companion object {
        // Viewport extremes. See currentViewport for a discussion of the viewport.
        private const val AXIS_X_MIN = 0f
        private const val AXIS_X_MAX = 160f
        private const val AXIS_Y_MIN = 0f
        private const val AXIS_Y_MAX = 300f

        private const val COLUMNS = 13
        private const val ROWS = 30
        private val MINES_SQRT = Math.sqrt((COLUMNS * ROWS).toDouble())
        private val MINES_EASY = (MINES_SQRT * 2).roundToInt()
        private val MINES_MED = (MINES_SQRT * 3).roundToInt()
        private val MINES_HARD = (MINES_SQRT * 4).roundToInt()

        private const val CELL = (AXIS_Y_MAX - AXIS_Y_MIN) / ROWS
        private const val TEXT_SIZE = CELL * .8f
        private const val SHIFT = CELL
    }
}
