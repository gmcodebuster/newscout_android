package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "LikeData", indices = [Index(value = ["article_id"], unique = true)])
data class LikeEntity(
        @ColumnInfo(name = "id")
        var id: Int,
        @PrimaryKey
        @ColumnInfo(name = "article_id")
        var article: Int,
        @ColumnInfo(name = "is_like")
        var is_like: Int
)