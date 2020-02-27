package com.fafadiatech.newscout.model

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DetailNewsData(
        var article_id: Int,
        @Ignore
        var category_id: Int,
        var title: String,
        var source: String,
        var category: String,
        var source_url: String,
        var cover_image: String,
        var description: String,
        var published_on: String,
        @Ignore
        var hashTags: ArrayList<String>,
        var like_status: Int,
        var bookmark_status: Int,
        var article_score: String) : INews, Parcelable{
    constructor() : this(0,0,"","","","","","","", arrayListOf<String>(),0,0,"0.0")

    constructor(article_id: Int,
                title: String,
                source: String,
                category: String,
                source_url: String,
                cover_image: String,
                description: String,
                published_on: String,
                like_status: Int,
                bookmark_status: Int,
                article_score: String) :
            this(article_id,
            0,
            title,
            source,
            category,
            source_url,
            cover_image,
            description,
            published_on,
            arrayListOf<String>(),
            like_status,
            bookmark_status,
            article_score)
}
