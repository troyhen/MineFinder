package com.troy.mine.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.troy.mine.model.db.dao.CellDao
import com.troy.mine.model.db.dao.PrefDao
import com.troy.mine.model.db.entity.Cell
import com.troy.mine.model.db.entity.Pref
import com.troy.mine.model.db.migrate.Migrate1AddPref
import com.troy.mine.model.db.migrate.Migrate2AddCell

@Database(
    version = 2, entities = [
        /*  1 */ Pref::class,
        /*  2 */ Cell::class
    ]
)
abstract class MineDatabase : RoomDatabase() {

    abstract val cellDao: CellDao
    abstract val prefDao: PrefDao

    companion object {
        val migrations
            get() = arrayOf(
                Migrate1AddPref,
                Migrate2AddCell
            )
    }
}
