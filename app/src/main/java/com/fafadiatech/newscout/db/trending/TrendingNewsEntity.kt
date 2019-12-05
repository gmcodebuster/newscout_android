package com.fafadiatech.newscout.db.trending

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "TrendingArticlesData", foreignKeys = [])
data class TrendingNewsEntity(
        @PrimaryKey
        @ColumnInfo(name = "article_id")
        var id: Int,
        @ColumnInfo(name = "category_id")
        var category_id: Int,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "source")
        var source: String,
        @ColumnInfo(name = "category")
        var category: String,
        @ColumnInfo(name = "source_url")
        var source_url: String,
        @ColumnInfo(name = "cover_image")
        var cover_image: String,
        @ColumnInfo(name = "description")
        var blurb: String = "",
        @ColumnInfo(name = "published_on")
        var published_on: String,
        @ColumnInfo(name = "hash_tags")
        var hash_tags: ArrayList<String>,
        @ColumnInfo(name = "article_score")
        var article_score: Float = 0.0f

) : Parcelable {

}