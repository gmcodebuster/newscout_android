package com.fafadiatech.newscout.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "RecommendedData")
data class RecommendedDataEntity(
        @PrimaryKey
        @ColumnInfo(name = "article_id")
        var id: Int,
        @ColumnInfo(name = "title")
        var title: String?,
        @ColumnInfo(name = "source")
        var source: String,
        @ColumnInfo(name = "category")
        var category: String,
        @ColumnInfo(name = "source_url")
        var source_url: String,
        @ColumnInfo(name = "cover_image")
        var cover_image: String?,
        @ColumnInfo(name = "description")
        var blurb: String?,
        @ColumnInfo(name = "published_on")
        var published_on: String

) : Parcelable