package com.fafadiatech.newscout.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticlesData(var category: String, var hash_tags: ArrayList<String>?, var published_on: String, var article_media: ArrayList<ArticleMediaData>?, var title: String, var source_url: String, var source: String, var cover_image: String, var category_id: Int, var id: Int, var blurb: String, var article_score: Float) : Parcelable {

    constructor() : this("", null, "", null, "", "", "", "", 0, 0, "", 0.0f)
}
