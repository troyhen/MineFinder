package com.troy.mine.model.db.dao

import android.graphics.PointF
import android.graphics.RectF
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

    fun getRectF(key: String, default: RectF? = null): RectF? {
        val left = get("${key}Left")?.toFloatOrNull() ?: return default
        val top = get("${key}Top")?.toFloatOrNull() ?: return default
        val right = get("${key}Right")?.toFloatOrNull() ?: return default
        val bottom = get("${key}Bottom")?.toFloatOrNull() ?: return default
        return RectF(left, top, right, bottom)
    }

    fun getPointF(key: String, default: PointF? = null): PointF? {
        val x = get("${key}X")?.toFloatOrNull() ?: return default
        val y = get("${key}Y")?.toFloatOrNull() ?: return default
        return PointF(x, y)
    }

    fun put(key: String, value: Any) {
        when (value) {
            is RectF -> {
                put("${key}Left", value.left)
                put("${key}Top", value.top)
                put("${key}Right", value.right)
                put("${key}Bottom", value.bottom)
            }
            is PointF -> {
                put("${key}X", value.x)
                put("${key}Y", value.y)
            }
            else -> save(Pref(key, value.toString()))
        }
    }
}