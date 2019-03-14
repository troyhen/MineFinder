package com.troy.mine.model.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.troy.mine.model.db.entity.Cell

@Dao
interface CellDao : BaseDao<Cell> {
    @Query("select * from Cell order by `row`, `column`")
    fun findAll(): List<Cell>
}