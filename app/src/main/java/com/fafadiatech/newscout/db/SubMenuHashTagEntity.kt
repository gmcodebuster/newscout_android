package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "SubMenuHashTagData", primaryKeys = ["id", "submenu_id"])
class SubMenuHashTagEntity(
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "submenu_id")
        var submenu_id: Int,
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "count")
        var count: Int
)