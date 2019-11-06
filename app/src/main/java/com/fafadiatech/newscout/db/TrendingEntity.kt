package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TrendingData")
data class TrendingEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "cluster_id")
        var cluster_id: Int,
        @ColumnInfo(name = "article_id")
        var article_id: Int,
        @ColumnInfo(name = "count")
        var count: Int
)