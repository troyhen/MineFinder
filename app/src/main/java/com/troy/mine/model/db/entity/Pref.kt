package com.troy.mine.model.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Pref(
    @PrimaryKey val key: String,
    val value: String
)
