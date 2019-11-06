package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SubMenuData")
class SubMenuEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "heading_id")
        var heading_id: Int,
        @ColumnInfo(name = "name")
        var name: String
)