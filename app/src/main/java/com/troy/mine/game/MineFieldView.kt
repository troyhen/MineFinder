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
import org.koin.core.KoinComponent
import org.koin.core.inject
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

    private val gameEngine: GameEngine by inject()
    private val vibrationEffect: VibrationEffect? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) VibrationEffect.createOneShot(
            250,
            DEFAULT_AMPLITUDE
        ) else null
    }
    private val vibrator: Vibrator? by lazy { context.getSystemService(Vibrator::class.java) }

    init {
        maxViewport = RectF(0f, 0f, gameEngine.columns + 4f, gameEngine.rows + 6f)
        currentViewport = RectF(gameEngine.viewport)

        initPaints()
    }

    override fun onClick(e: MotionEvent): Boolean {
        return if (gameEngine.state == GameState.PLAY) {
            when {
                contentRect.contains(e.x.roundToInt(), e.y.roundToInt()) -> {
                    val cell = gameEngine.findNearest(e.x, e.y)
                    when (gameEngine.modeLive.value) {
                        ClickMode.MARK -> gameEngine.markCell(cell)
                        else -> gameEngine.revealCell(cell)
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
    }

    override fun onLongClick(e: MotionEvent) {
        if (gameEngine.state == GameState.PLAY) {
            if (contentRect.contains(e.x.roundToInt(), e.y.roundToInt())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(vibrationEffect!!)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(250L)
                }
                val cell = gameEngine.findNearest(e.x, e.y)
                gameEngine.markCell(cell)
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
        val cellSize = maxViewport.height() / gameEngine.rows
        val baseTextSize = cellSize * .8f
        val zoom = maxViewport.height() / currentViewport.height()
        val ratio = contentRect.height() / maxViewport.height()
        val textSize = baseTextSize * zoom * ratio
        val textShift = textSize / 3
        val radius = cellSize * zoom * .5f * ratio
        val xSpace = cellSize * .75f
        val xSpace2 = xSpace / 2
        val ySpace = cellSize * .95f
        zoomedTextPaint.textSize = textSize
        revealPaint.strokeWidth = zoom * 2
        val bomb = context.getString(R.string.bomb)    //"💣"
        val flag = context.getString(R.string.flag) //"\uD83D\uDEA9"
        for (y in 0 until gameEngine.rows) {
            val yc = getDrawY(y * ySpace + cellSize)
            val yt = yc + textShift
            val xOffset = cellSize + if (y % 2 == 0) 0f else xSpace2
            for (x in 0 until gameEngine.columns) {
                val xc = getDrawX(x * xSpace + xOffset)
                val index = y * gameEngine.columns + x
                val cell = gameEngine.cells[index]
                cell.x = xc
                cell.y = yc
                if (gameEngine.state != GameState.WON && !cell.isRevealed) {
                    canvas.drawCircle(xc, yc, radius, coveredPaint)
                } else {
                    canvas.drawCircle(xc, yc, radius, revealPaint)
                }
                val text = when {
                    cell.isMarked -> flag
                    gameEngine.state == GameState.LOST && cell.hasMine -> bomb
                    !cell.isRevealed -> ""
                    cell.hasMine -> bomb
                    cell.neighborMines == 0 -> ""
                    else -> cell.neighborMines.toString()
                }
                canvas.drawText(text, xc, yt, zoomedTextPaint)
            }
        }
    }
}
