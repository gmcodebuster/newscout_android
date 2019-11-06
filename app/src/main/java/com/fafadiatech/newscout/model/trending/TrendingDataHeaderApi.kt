package com.fafadiatech.newscout.model.trending

import com.fafadiatech.newscout.model.NewsStatus

data class TrendingDataHeaderApi(var header: NewsStatus, var body: TrendingDataBodyAPI)