package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchSuggestionData")
data class SearchSuggestionEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "query")
        var query: String?) {

    constructor() : this(0, "")
}