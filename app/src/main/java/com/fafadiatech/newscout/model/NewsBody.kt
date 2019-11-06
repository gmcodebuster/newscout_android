package com.fafadiatech.newscout.model

data class NewsBody(var count: Int, var current_page: Int, var total_pages: Int, var results: ArrayList<ArticlesData>, var next: String, var filters: ArticleFilterData, var previous: String)