package com.troy.mine

import androidx.room.Room
import com.troy.mine.game.GameViewModel
import com.troy.mine.model.db.MineDatabase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(get(), MineDatabase::class.java, "mine.db")
            .addMigrations(*MineDatabase.migrations)
            .build()
    }
    viewModel { GameViewModel() }
}
