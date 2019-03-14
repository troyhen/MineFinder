package com.troy.mine.model.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.troy.mine.model.db.entity.Pref

@Dao
abstract class PrefDao : BaseDao<Pref> {

    @Query("select * from Pref where `key` = :key")
    abstract fun find(key: String): Pref?

    fun get(key: String) = find(key)?.value
    fun getFloat(key: String, default: Float = 0f): Float = get(key)?.toFloatOrNull() ?: default
    fun getInt(key: String, default: Int = 0): Int = get(key)?.toIntOrNull() ?: default
    fun getString(key: String, default: String = ""): String = get(key) ?: default
    fun put(key: String, value: Any) = save(Pref(key, value.toString()))
}