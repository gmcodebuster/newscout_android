package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "HashTagData", primaryKeys = ["article_id", "name"])
class HashTagEntity(
        @ColumnInfo(name = "article_id")
        var article_id: Int,
        @ColumnInfo(name = "name")
        var name: String)

