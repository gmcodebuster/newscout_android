package com.fafadiatech.newscout.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrendingNewsData(
        var cluster_id: Int,
        var article_id: Int,
        var category_id: Int,
        var title: String,
        var source: String,
        var category: String,
        var source_url: String,
        var cover_image: String,
        var description: String,
        var published_on: String,
        var count: Int
) : Parcelable