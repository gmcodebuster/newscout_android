package com.fafadiatech.newscout.model

data class UserData(var id: Int, var passion: ArrayList<UserPassionData>, var first_name: String, var last_name: String, var token: String, var breaking_news: Boolean, var daily_edition: Boolean, var personalized: Boolean)