package com.fafadiatech.newscout.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleMediaData(var id: Int, var created_at: String, var modified_at: String, var category: String, var url: String, var video_url: String, var article: Int) : Parcelable