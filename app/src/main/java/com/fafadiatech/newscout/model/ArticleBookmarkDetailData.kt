package com.fafadiatech.newscout.model

import com.fafadiatech.newscout.db.NewsEntity

data class ArticleBookmarkDetailData(var id: Int, var article: NewsEntity, var status: Int)