package com.fafadiatech.newscout.db.dailydigest

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "DDHashTagData", primaryKeys = ["article_id", "name"])
class DDHashTagEntity(
        @ColumnInfo(name = "article_id")
        var article_id: Int,
        @ColumnInfo(name = "name")
        var name: String)