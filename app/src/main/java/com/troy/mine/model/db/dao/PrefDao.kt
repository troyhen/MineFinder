package com.troy.mine.model.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.troy.mine.model.db.entity.Pref

@Dao
interface PrefDao : BaseDao<Pref> {

    @Query("select * from Pref where `key` = :key")
    fun find(key: String): Pref?
}