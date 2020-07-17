package com.fafadiatech.newscout.searchcomponent

import com.fafadiatech.newscout.model.ArticlesData

interface GNewsRepository {
    fun searchNews(searchQuery: String, pageSize: Int): Listing<ArticlesData>
}