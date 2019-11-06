package com.fafadiatech.newscout.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CategorySelectedData")
class CategorySelectedEntity(
        @PrimaryKey
        @ColumnInfo(name = "selected_id")
        val selectedId: Int,
        @ColumnInfo(name = "category_selected_name")
        val categorySelectedName: String)