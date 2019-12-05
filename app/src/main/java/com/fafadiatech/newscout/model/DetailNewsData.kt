package com.fafadiatech.newscout.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DetailNewsData(
        var article_id: Int,
        var title: String,
        var source: String,
        var category: String,
        var source_url: String,
        var cover_image: String,
        var description: String,
        var published_on: String,
        var like_status: Int,
        var bookmark_status: Int,
        var article_score: Float) : Parcelable
