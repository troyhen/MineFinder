package com.troy.mine.model.db.migrate

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrate2AddCell : Migration(1, 2) {
    private const val TABLE_NAME = "Cell"

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`column` INTEGER NOT NULL, `row` INTEGER NOT NULL, `hasMine` INTEGER NOT NULL, `neighborMines` INTEGER NOT NULL, `isRevealed` INTEGER NOT NULL, `isMarked` INTEGER NOT NULL, `x` REAL NOT NULL, `y` REAL NOT NULL, PRIMARY KEY(`column`, `row`))")
    }
}