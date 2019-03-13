package com.troy.mine.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    version = 1, entities = [
        /*  1 */ Pref::class
    ]
)
abstract class MineDatabase : RoomDatabase() {

    companion object {
        val migrations get(): Array<Migration> = emptyArray()
    }
}
