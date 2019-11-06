package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BookmarkData")
data class BookmarkEntity(
        @ColumnInfo(name = "id")
        var id: Int,
        @PrimaryKey
        @ColumnInfo(name = "article_id")
        var article: Int,
        @ColumnInfo(name = "status")
        var status: Int
)