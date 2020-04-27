package com.fafadiatech.newscout.model

data class SuggestData(val value:String, val key:String)

data class SuggestBody(val result : ArrayList<SuggestData>)

data class SuggestResponse(var header: NewsStatus, var body: SuggestBody)