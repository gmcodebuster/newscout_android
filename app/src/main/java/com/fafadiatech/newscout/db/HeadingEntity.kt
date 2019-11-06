package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HeadingData")
class HeadingEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "name")
        var name: String
)