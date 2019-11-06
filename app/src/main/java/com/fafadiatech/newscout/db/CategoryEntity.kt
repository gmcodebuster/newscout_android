package com.fafadiatech.newscout.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "CategoryData")
data class CategoryEntity(
        @PrimaryKey
        @ColumnInfo(name = "category_id")
        var id: Int,
        @ColumnInfo(name = "category_name")
        var name: String
) : Parcelable