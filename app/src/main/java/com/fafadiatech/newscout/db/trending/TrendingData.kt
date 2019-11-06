package com.fafadiatech.newscout.db.trending

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "TrendingAPIData")
data class TrendingData(
        @PrimaryKey
        var id: Int,
        @Embedded
        var articles: ArrayList<TrendingNewsEntity>,
        @ColumnInfo(name = "domain")
        var domain: String,
        @ColumnInfo(name = "created_at")
        var created_at: String,
        @ColumnInfo(name = "modified_at")
        var modified_at: String? = "",
        @ColumnInfo(name = "active")
        var active: Boolean,
        @ColumnInfo(name = "score")
        var score: Float) {
}