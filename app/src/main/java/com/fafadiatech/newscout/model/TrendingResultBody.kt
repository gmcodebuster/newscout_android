package com.fafadiatech.newscout.model

data class TrendingResultBody(var id: Int, var articles: ArrayList<ArticlesData>, var created_at: String, var modified_at: String, var active: Boolean, var score: Int)