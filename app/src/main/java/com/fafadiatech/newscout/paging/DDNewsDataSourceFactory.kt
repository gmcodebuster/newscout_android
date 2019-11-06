package com.fafadiatech.newscout.paging

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity

class DDNewsDataSourceFactory(context: Context, deviceId: String) : DataSource.Factory<Int, DailyDigestEntity>() {
    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, DailyDigestEntity>>()
    var deviceId: String
    var mContext: Context

    init {
        this.deviceId = deviceId
        mContext = context
    }

    override fun create(): DataSource<Int, DailyDigestEntity> {
        var newsDataSource = DDNewsItemDataSource(mContext, deviceId)
        itemLiveDataSource.postValue(newsDataSource)
        return newsDataSource
    }

    fun getNewsSorceData(): MutableLiveData<PageKeyedDataSource<Int, DailyDigestEntity>> {
        return itemLiveDataSource
    }
}