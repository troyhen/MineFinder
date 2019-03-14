package com.troy.mine.game

import androidx.lifecycle.ViewModel
import com.troy.mine.model.db.entity.Cell
import com.troy.mine.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.random.Random

class GameViewModel : ViewModel() {

    val redrawEvent = SingleLiveEvent<Void>()

    var state: GameState = GameState.PLAY
    var mode: ClickMode = ClickMode.REVEAL
    var cells = emptyList<Cell>()
    var columns = 0
    var rows = 0

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

    fun markCell(cell: Cell?): Boolean {
        cell ?: return false
        Timber.d("cell $cell")
        cell.isMarked = !cell.isMarked
        detectWin()
        invalidate()
        return true
    }

    fun reset(columns: Int, rows: Int, mines: Int) {
        mode = ClickMode.REVEAL
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
            cell.neighborMines = count(left, above) + count(right, above) +
                    count(column - 1, row) + count(column + 1, row) +
                    count(left, below) + count(right, below)
        }
    }

    fun revealCell(cell: Cell?): Boolean {
        cell ?: return false
        if (cell.isRevealed) {
            if (countFlags(cell) == cell.neighborMines) {
                revealNeighbors(cell)
            }
        } else {
            cell.isRevealed = true
            invalidate()
            if (cell.isRevealed && cell.neighborMines == 0 && !cell.hasMine) {
                GlobalScope.launch { revealZeros(cell) }
            }
            if (cell.hasMine) state = GameState.LOST
        }
        detectWin()
        return true
    }

    fun toggleMode() {
        mode = when (mode) {
            ClickMode.REVEAL -> ClickMode.MARK
            ClickMode.MARK -> ClickMode.REVEAL
        }
        invalidate()
    }

    private fun count(column: Int, row: Int): Int {
        val cell = getCell(column, row) ?: return 0
        return if (cell.hasMine) 1 else 0
    }

    private fun countFlags(cell: Cell): Int {
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

    private fun getCell(column: Int, row: Int): Cell? {
        if (column < 0 || column >= columns || row < 0 || row >= rows) return null
        val index = column + row * columns
        return cells[index]
    }

    private fun invalidate() = redrawEvent.postCall()

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
                withContext(Dispatchers.Main) { invalidate() }
            } while (first.neighborMines > 0)
        }
        detectWin()
    }
}