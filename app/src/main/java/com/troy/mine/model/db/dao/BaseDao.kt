package com.troy.mine.model.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase

interface BaseDao<in T> {
    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     */
    @Insert
    fun insert(obj: T)

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     */
    @Insert
    fun insert(vararg obj: T)

    /**
     * Save an object in the database.
     *
     * @param obj the object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(obj: T)

    /**
     * Save an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg obj: T)

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    fun update(vararg obj: T)

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    fun delete(obj: T)

    companion object {
        fun isTableEmpty(db: SupportSQLiteDatabase, table: String) = countRecords(db, table) == 0L

        fun countRecords(db: SupportSQLiteDatabase, table: String): Long {
            return db.query("select count(*) from `$table`").use { cursor ->
                if (cursor.moveToNext()) {
                    cursor.getLong(0)
                } else 0L
            }
        }
    }
}