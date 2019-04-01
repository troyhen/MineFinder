package com.troy.mine.game

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.AnyThread
import androidx.lifecycle.MutableLiveData
import com.troy.mine.model.db.MineDatabase
import com.troy.mine.model.db.entity.Cell
import com.troy.mine.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Manages the logical aspects of the game setup, play and ending
 */
class GameEngine(private val db: MineDatabase) {

    var cells = emptyList<Cell>()   // list of cells organized by rows, end-to-end
    var columns = 0 // number of columns
    var rows = 0    // number of rows
    var difficulty = Difficulty.EASY
    var fieldSize = FieldSize.SMALL
    var infoPanelOffset: PointF? = null // location of the info panel
    private var mode = ClickMode.REVEAL
        set(value) {
            field = value
            modeLive.postValue(value)
        }
    var modeLive = MutableLiveData<ClickMode>().apply { value = mode }
    var state = GameState.PLAY
    var viewport = RectF()  // visible area of the game, adjusted for pan and zoom

    val updateEvent = SingleLiveEvent<Void>()   // notify when the game needs to be redrawn

    init {
        load()
    }

    /**
     * Find the nearest cell to the screen coordinates
     */
    fun findNearest(x: Float, y: Float): Cell? {
        var shortest = Float.MAX_VALUE
        var cell: Cell? = null
        cells.forEach {
            val dx = it.x - x
            val dy = it.y - y
            val distance = dx * dx + dy * dy
            if (distance < shortest) {
                shortest = distance
                cell = it
            }
        }
        return cell
    }

    /**
     * Mark a cell with a flag
     */
    fun markCell(cell: Cell?): Boolean {
        cell ?: return false
        Timber.d("cell $cell")
        cell.isMarked = !cell.isMarked
        detectWin()
        redraw()
        return true
    }

    /**
     * Start a new game and save initial state values. Note, the game state is only partially saved at this point.
     */
    fun startGame(difficulty: Difficulty, fieldSize: FieldSize) {
        this.difficulty = difficulty
        this.fieldSize = fieldSize
        val mines = (Math.sqrt(fieldSize.columns.toDouble() * fieldSize.rows) * difficulty.scale).roundToInt()
        val columns = fieldSize.columns
        val rows = fieldSize.rows
        GlobalScope.launch {
            db.runInTransaction {
                db.cellDao.deleteAll()
                db.prefDao.put(COLUMNS, columns)
                db.prefDao.put(ROWS, rows)
                db.prefDao.put(DIFFICULTY, difficulty.name)
                db.prefDao.put(SIZE, fieldSize.name)
            }
        }
        mode = ClickMode.REVEAL
        state = GameState.PLAY
        this.columns = columns
        this.rows = rows
        val size = columns * rows
        cells = MutableList(size) { index ->
            val column = index % columns
            val row = index / columns
            Cell(column, row)
        }
        for (i in 1..mines) {
            var cell: Cell
            do {
                val index = Random.nextInt(size)
                cell = cells[index]
            } while (cell.hasMine)
            cell.hasMine = true
        }
        for (i in 0 until size) {
            val column = i % columns
            val row = i / columns
            val above = row - 1
            val below = row + 1
            val shift = 1 - row % 2
            val left = column - shift
            val right = left + 1
            val cell = cells[i]
            cell.neighborMines = countOne(left, above) + countOne(right, above) +
                    countOne(column - 1, row) + countOne(column + 1, row) +
                    countOne(left, below) + countOne(right, below)
        }
        val cx = columns * .5f
        val cy = rows * .5f
        val w = FieldSize.SMALL.columns.toFloat() / 2
        val h = FieldSize.SMALL.rows.toFloat() / 2
        viewport = RectF(cx - w, cy - h, cx + w, cy + h)
    }

    /**
     * Reveal the cell
     */
    fun revealCell(cell: Cell?): Boolean {
        cell ?: return false
        if (cell.isRevealed) {
            if (countNeighbors(cell) == cell.neighborMines) {
                revealNeighbors(cell)
            }
        } else {
            cell.isRevealed = true
            redraw()
            if (cell.isRevealed && cell.neighborMines == 0 && !cell.hasMine) {
                GlobalScope.launch { revealZeros(cell) }
            }
            if (cell.hasMine) state = GameState.LOST
        }
        detectWin()
        return true
    }

    /**
     * Save the current game state
     */
    @AnyThread
    fun save() = GlobalScope.launch {
        db.runInTransaction {
            for (cell in cells) {
                db.cellDao.save(cell)
            }
            db.prefDao.put(MODE, mode.name)
            db.prefDao.put(STATE, state.name)
            db.prefDao.put(VIEW, viewport)
            infoPanelOffset?.let { db.prefDao.put(INFO_PANEL, it) }
        }
    }

    /**
     * Switch between click modes
     */
    fun toggleMode() {
        mode = when (mode) {
            ClickMode.REVEAL -> ClickMode.MARK
            ClickMode.MARK -> ClickMode.REVEAL
        }
        redraw()
    }

    /**
     * Count the number of immediately surrounding mines
     */
    private fun countNeighbors(cell: Cell): Int {
        val column = cell.column
        val row = cell.row
        val above = row - 1
        val below = row + 1
        val shift = 1 - row % 2
        val left = column - shift
        val right = left + 1
        var count = 0
        getCell(left, above)?.takeIf { it.isMarked }?.let { count++ }
        getCell(right, above)?.takeIf { it.isMarked }?.let { count++ }
        getCell(column - 1, row)?.takeIf { it.isMarked }?.let { count++ }
        getCell(column + 1, row)?.takeIf { it.isMarked }?.let { count++ }
        getCell(left, below)?.takeIf { it.isMarked }?.let { count++ }
        getCell(right, below)?.takeIf { it.isMarked }?.let { count++ }
        return count
    }

    /**
     * Return 1 if a mine is present at the coordinate or 0 if not
     */
    private fun countOne(column: Int, row: Int): Int {
        val cell = getCell(column, row) ?: return 0
        return if (cell.hasMine) 1 else 0
    }

    /**
     * Detect if the player has won the game
     */
    private fun detectWin() {
        var empty = 0
        var mines = 0
        var revealed = 0
        var unrevealed = 0
        cells.forEach { cell ->
            if (cell.hasMine) mines++ else empty++
            if (cell.isRevealed) revealed++
            else if (cell.hasMine) unrevealed++
        }
        if (unrevealed == mines && revealed == empty) {
            state = GameState.WON
        }
    }

    /**
     * Return the requested cell or null if coordinates are out of bounds
     */
    private fun getCell(column: Int, row: Int): Cell? {
        if (column < 0 || column >= columns || row < 0 || row >= rows) return null
        val index = column + row * columns
        return cells[index]
    }

    /**
     * Load the current game state from the database or call setup if not found
     */
    @AnyThread
    private fun load() = GlobalScope.launch {
        state = try {
            GameState.valueOf(db.prefDao.getString(STATE))
        } catch (e: Exception) {
            setup()
            GameState.PLAY
        }
        columns = db.prefDao.getInt(COLUMNS, 0)
        rows = db.prefDao.getInt(ROWS, 0)
        mode = try {
            ClickMode.valueOf(db.prefDao.getString(MODE))
        } catch (e: Exception) {
            ClickMode.REVEAL
        }
        cells = db.cellDao.findAll()
        viewport = db.prefDao.getRectF(VIEW) ?: viewport
        db.prefDao.getPointF(INFO_PANEL)?.let { infoPanelOffset = it }
    }

    /**
     * Request to redraw the game view
     */
    private fun redraw() = updateEvent.postCall()

    /**
     * Reveal each of the immediately neighboring cells
     */
    private fun revealNeighbors(cell: Cell) {
        val column = cell.column
        val row = cell.row
        val above = row - 1
        val below = row + 1
        val shift = 1 - row % 2
        val left = column - shift
        val right = left + 1
        getCell(left, above)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
        getCell(right, above)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
        getCell(column - 1, row)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
        getCell(column + 1, row)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
        getCell(left, below)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
        getCell(right, below)?.takeIf { !it.isRevealed && !it.isMarked }?.let { revealCell(it) }
    }

    /**
     * Reveal all of the neighboring empty cells, and 1 step into the non empty ones
     */
    private suspend fun revealZeros(cell: Cell) {
        var first = cell
        val list = mutableSetOf<Cell>()
        loop@ while (true) {
            val column = first.column
            val row = first.row
            val above = row - 1
            val below = row + 1
            val shift = 1 - row % 2
            val left = column - shift
            val right = left + 1
            getCell(left, above)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            getCell(right, above)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            getCell(column - 1, row)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            getCell(column + 1, row)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            getCell(left, below)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            getCell(right, below)?.takeIf { !it.isRevealed }?.let { list.add(it) }
            do {
                if (list.isEmpty()) break@loop
                first = list.first()
                list.remove(first)
                first.isRevealed = true
                // Dispatch on Main here to give the OS time to redraw, while we are still revealing cells
                withContext(Dispatchers.Main) { redraw() }
            } while (first.neighborMines > 0)
        }
        detectWin()
    }

    /**
     * Setup initial values
     */
    private fun setup() {
        difficulty = Difficulty.EASY
        fieldSize = FieldSize.SMALL
    }

    companion object {
        private const val COLUMNS = "columns"
        private const val ROWS = "rows"
        private const val DIFFICULTY = "difficulty"
        private const val MODE = "mode"
        private const val SIZE = "size"
        private const val STATE = "state"
        private const val VIEW = "view"
        private const val INFO_PANEL = "infoPanel"
    }
}