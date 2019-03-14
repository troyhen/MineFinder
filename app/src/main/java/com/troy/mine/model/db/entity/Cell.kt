package com.troy.mine.model.db.entity

import androidx.room.Entity

@Entity(primaryKeys = ["column", "row"])
data class Cell(
    val column: Int,
    val row: Int,
    var hasMine: Boolean = false,
    var neighborMines: Int = 0,
    var isRevealed: Boolean = false,
    var isMarked: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
)