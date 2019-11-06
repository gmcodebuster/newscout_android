package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ArticleMediaData")
class ArticleMediaEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "created_at")
        var created_at: String?,
        @ColumnInfo(name = "modified_at")
        var modified_at: String?,
        @ColumnInfo(name = "category")
        var category: String?,
        @ColumnInfo(name = "url")
        var url: String?,
        @ColumnInfo(name = "video_url")
        var video_url: String?,
        @ColumnInfo(name = "article")
        var article: Int?

)