package com.fafadiatech.newscout.model.trending

import com.fafadiatech.newscout.db.trending.TrendingNewsEntity

data class TrendingAPIResultBody(var id: Int, var articles: ArrayList<TrendingNewsEntity>, var domain: String, var created_at: String, var modified_at: String, var active: Boolean, var score: Float)