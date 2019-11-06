package com.fafadiatech.newscout.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HashTagData(var id: Int, var name: String, var count: Int) : Parcelable

